package jlibrtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.Iterator;

public class RTCPReceiverThread extends Thread {
	private RTPSession rtpSession = null;
	private RTCPSession rtcpSession = null;
	
	RTCPReceiverThread(RTCPSession rtcpSession, RTPSession rtpSession) {
		this.rtpSession = rtpSession;
		this.rtcpSession = rtcpSession;
		
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTCPReceiverThread created");
		} 

	}
	
	private Participant findParticipant(long ssrc, DatagramPacket packet) {
		Participant p = rtpSession.partDb.getParticipant(ssrc);
		if(p == null) {
			Enumeration<Participant> enu = rtpSession.partDb.getParticipants();
			while(enu.hasMoreElements()) {
				Participant tmp = (Participant) enu.nextElement();
				if(tmp.ssrc < 0 && 
						(tmp.rtcpAddress.getAddress().equals(packet.getAddress())
						|| tmp.rtpAddress.getAddress().equals(packet.getAddress()))) {
					
					// Best guess
					System.out.println("RTCPReceiverThread: Got an unexpected packet from SSRC:" 
							+ ssrc  + " @" + packet.getAddress().toString() + ", WAS able to match it." );
					
					tmp.ssrc = ssrc;
					return tmp;
				}
			}
			// Create an unknown sender
			System.out.println("RTCPReceiverThread: Got an unexpected packet from SSRC:" 
					+ ssrc  + " @" + packet.getAddress().toString() + ", was NOT able to match it." );
			p = new Participant((InetSocketAddress) null, (InetSocketAddress) packet.getSocketAddress(), ssrc);
			rtpSession.partDb.addParticipant(2,p);
		}
		return p;
	}
	
	private int parsePacket(DatagramPacket packet) {

		if(packet.getLength() % 4 != 0) {
			if(RTPSession.rtcpDebugLevel > 2) {
				System.out.println("RTCPReceiverThread.parsePacket got packet that had length " + packet.getLength());
			}
			return -1;
		} else {
			byte[] rawPkt = packet.getData();
			
			// Parse the received compound RTCP (?) packet
			CompRtcpPkt compPkt = new CompRtcpPkt(rawPkt, packet.getLength(), 
					(InetSocketAddress) packet.getSocketAddress(), rtpSession);
			
			if(this.rtpSession.debugAppIntf != null) {
				String intfStr; 
			
				if(rtpSession.mcSession) {
					intfStr = this.rtcpSession.rtcpMCSock.getLocalSocketAddress().toString();
				} else {
					intfStr = this.rtpSession.rtpSock.getLocalSocketAddress().toString();
				}
				
				if( compPkt.problem == 0) {
					String str = new String("Received compound RTCP packet of size " + packet.getLength() + 
							" from " + packet.getSocketAddress().toString() + " via " + intfStr
							+ " containing " + compPkt.rtcpPkts.size() + " packets" );
					
					this.rtpSession.debugAppIntf.debugPacketReceived(1, 
							(InetSocketAddress) packet.getSocketAddress(), str);
				} else {
					String str = new String("Received invalid RTCP packet of size " + packet.getLength() + 
							" from " + packet.getSocketAddress().toString() + " via " +  intfStr
							+ ": " + this.debugErrorString(compPkt.problem) );
					
					this.rtpSession.debugAppIntf.debugPacketReceived(-2, 
							(InetSocketAddress) packet.getSocketAddress(), str);
				}
			}

			//Loop over the information
			Iterator iter = compPkt.rtcpPkts.iterator();

			long curTime = System.currentTimeMillis();

			while(iter.hasNext()) {
				RtcpPkt aPkt = (RtcpPkt) iter.next();

				// Our own packets should already have been filtered out.
				if(aPkt.ssrc == rtpSession.ssrc) {
					System.out.println("RTCPReceiverThread() received RTCP packet" 
							+ " with conflicting SSRC from " + packet.getSocketAddress().toString());
					rtpSession.resolveSsrcConflict();
					return -1;
				}
				
				/**        Receiver Reports        **/
				if(	aPkt.getClass() == RtcpPktRR.class) {
					RtcpPktRR rrPkt = (RtcpPktRR) aPkt;

					Participant p = findParticipant(rrPkt.ssrc, packet);
					p.lastRtcpPkt = curTime;

					if(rtpSession.rtcpAppIntf != null) {
						rtpSession.rtcpAppIntf.RRPktReceived(rrPkt.ssrc, rrPkt.reporteeSsrc, 
								rrPkt.lossFraction, rrPkt.lostPktCount, rrPkt.extHighSeqRecv,
								rrPkt.interArvJitter, rrPkt.timeStampLSR, rrPkt.delaySR);
					}

					/**        Sender Reports        **/
				} else if(aPkt.getClass() == RtcpPktSR.class) {
					RtcpPktSR srPkt = (RtcpPktSR) aPkt;

					Participant p = findParticipant(srPkt.ssrc, packet);
					p.lastRtcpPkt = curTime;
					
					if(p != null) {

						if(p.ntpGradient < 0 && p.lastNtpTs1 > -1) {
							//Calculate gradient NTP vs RTP
							long newTime = StaticProcs.undoNtpMess(srPkt.ntpTs1, srPkt.ntpTs2);
							p.ntpGradient = ((double) (newTime - p.ntpOffset))/((double) srPkt.rtpTs - p.lastSRRtpTs);
							if(RTPSession.rtcpDebugLevel > 4) {
								System.out.println("RTCPReceiverThread calculated NTP vs RTP gradient: " + Double.toString(p.ntpGradient));
							}
						} else {
							// Calculate sum of ntpTs1 and ntpTs2 in milliseconds
							p.ntpOffset = StaticProcs.undoNtpMess(srPkt.ntpTs1, srPkt.ntpTs2);
							p.lastNtpTs1 = srPkt.ntpTs1;
							p.lastNtpTs2 = srPkt.ntpTs2;
							p.lastSRRtpTs = srPkt.rtpTs;
						}

						// For the next RR
						p.timeReceivedLSR = curTime;
						p.setTimeStampLSR(srPkt.ntpTs1,srPkt.ntpTs2);

					}


					if(rtpSession.rtcpAppIntf != null) {
						if(srPkt.rReports != null) {
							rtpSession.rtcpAppIntf.SRPktReceived(srPkt.ssrc, srPkt.ntpTs1, srPkt.ntpTs2, 
									srPkt.rtpTs, srPkt.sendersPktCount, srPkt.sendersPktCount,
									srPkt.rReports.reporteeSsrc, srPkt.rReports.lossFraction, srPkt.rReports.lostPktCount,
									srPkt.rReports.extHighSeqRecv, srPkt.rReports.interArvJitter, srPkt.rReports.timeStampLSR,
									srPkt.rReports.delaySR);
						} else {
							rtpSession.rtcpAppIntf.SRPktReceived(srPkt.ssrc, srPkt.ntpTs1, srPkt.ntpTs2, 
									srPkt.rtpTs, srPkt.sendersPktCount, srPkt.sendersPktCount,
									null, null, null,
									null, null, null,
									null);
						}
					}

					/**        Source Descriptions       **/
				} else if(aPkt.getClass() == RtcpPktSDES.class) {
					RtcpPktSDES sdesPkt = (RtcpPktSDES) aPkt;				

					// The the participant database is updated
					// when the SDES packet is reconstructed by CompRtcpPkt	
					if(rtpSession.rtcpAppIntf != null) {
						rtpSession.rtcpAppIntf.SDESPktReceived(sdesPkt.participants);
					}

					/**        Bye Packets       **/
				} else if(aPkt.getClass() == RtcpPktBYE.class) {
					RtcpPktBYE byePkt = (RtcpPktBYE) aPkt;

					long time = System.currentTimeMillis();
					Participant[] partArray = new Participant[byePkt.ssrcArray.length];

					for(int i=0; i<byePkt.ssrcArray.length; i++) {
						partArray[i] = rtpSession.partDb.getParticipant(byePkt.ssrcArray[i]);
						if(partArray[i] != null)
							partArray[i].timestampBYE = time;
					}

					if(rtpSession.rtcpAppIntf != null) {
						rtpSession.rtcpAppIntf.BYEPktReceived(partArray, new String(byePkt.reason));
					}
				}
			}
		}
		return 0;

	}
	
	private String debugErrorString(int errorCode) {
		String aStr = "";
		switch(errorCode) {
			case -1: aStr = "The first packet was not of type SR or RR."; break;
			case -2: aStr = "The padding bit was set for the first packet."; break;
			case -200: aStr = " Error parsing Sender Report packet."; break;
			case -201: aStr = " Error parsing Receiver Report packet."; break;
			case -202: aStr = " Error parsing SDES packet"; break;
			case -203: aStr = " Error parsing BYE packet."; break;
			case -204: aStr = " Error parsing Application specific packet."; break;
		default:
			aStr = "Unknown error code " + errorCode + ".";
		}
		
		return aStr;
	}

	public void run() {
		if(RTPSession.rtcpDebugLevel > 1) {
			if(rtpSession.mcSession) {
				System.out.println("-> RTCPReceiverThread.run() starting on MC " + rtcpSession.rtcpMCSock.getLocalPort() );
			} else {
				System.out.println("-> RTCPReceiverThread.run() starting on " + rtcpSession.rtcpSock.getLocalPort() );
			}
		}

		while(!rtpSession.endSession) {
			
			if(RTPSession.rtcpDebugLevel > 4) {
				if(rtpSession.mcSession) {
					System.out.println("-> RTCPReceiverThread.run() waiting for packet on MC " + rtcpSession.rtcpMCSock.getLocalPort() );
				} else {
					System.out.println("-> RTCPReceiverThread.run() waiting for packet on " + rtcpSession.rtcpSock.getLocalPort() );
				}
			}

			// Prepare a packet
			byte[] rawPkt = new byte[1500];
			DatagramPacket packet = new DatagramPacket(rawPkt, rawPkt.length);

			// Wait for it to arrive
			if(! rtpSession.mcSession) {
				//Unicast
				try {
					rtcpSession.rtcpSock.receive(packet);
				} catch (IOException e) {
					if(!rtpSession.endSession) {
						e.printStackTrace();
					} else {
						continue;
					}
				}
			} else {
				//Multicast
				try {
					rtcpSession.rtcpMCSock.receive(packet);
				} catch (IOException e) {
					if(!rtpSession.endSession) {
						e.printStackTrace();
					} else {
						continue;
					}
				}
			}
			
			// Check whether this is one of our own
			if( (rtpSession.mcSession && ! packet.getSocketAddress().equals(rtcpSession.rtcpMCSock) )
					|| ! packet.getSocketAddress().equals(rtcpSession.rtcpSock) ) {
				//System.out.println("Packet received from: " + packet.getSocketAddress().toString());
				parsePacket(packet);
				//rtpSession.partDb.debugPrint();
			}			
		}
		
		if(RTPSession.rtcpDebugLevel > 1) {
			System.out.println("<-> RTCPReceiverThread terminating");
		}
	}

}

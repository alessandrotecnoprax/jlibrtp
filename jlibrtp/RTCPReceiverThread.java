package jlibrtp;

import java.io.IOException;
import java.net.DatagramPacket;
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
	
	private void parsePacket(DatagramPacket packet, byte[] rawPkt) {
		// Parse the received compound RTCP (?) packet
		CompRtcpPkt compPkt = new CompRtcpPkt(rawPkt, packet.getLength(), packet.getAddress(), rtpSession.partDb);
		
		//Loop over the information
		Iterator iter = compPkt.rtcpPkts.iterator();
		
		while(iter.hasNext()) {
			RtcpPkt aPkt = (RtcpPkt) iter.next();
			
			// Our own packets should already have been filtered out.
			if(aPkt.ssrc == rtpSession.ssrc) {
				System.out.println("RTCPReceiverThread() received RTCP packet" 
						+ " with conflicting SSRC from " + packet.getSocketAddress().toString());
				rtpSession.resolveSsrcConflict();
				return;
			}
			
			
			
			if(	aPkt.getClass() == RtcpPktRR.class) {
				RtcpPktRR rrPkt = (RtcpPktRR) aPkt;
				
				if(rtpSession.rtcAppIntf != null) {
					rtpSession.rtcAppIntf.RRPktReceived(rrPkt.ssrc, rrPkt.reporteeSsrc, 
							rrPkt.lossFraction, rrPkt.lostPktCount, rrPkt.extHighSeqRecv,
							rrPkt.interArvJitter, rrPkt.timeStampLSR, rrPkt.delaySR);
				}

			} else if(aPkt.getClass() == RtcpPktSR.class) {
				RtcpPktSR srPkt = (RtcpPktSR) aPkt;
				
				Participant p = rtpSession.partDb.getParticipant(srPkt.ssrc);
				if(p != null) {
					if(p.lastNtpTs1 > -1) {
						//Calculate gradient NTP vs RTP

					} else {
						// Calculate sum of ntpTs1 and ntpTs2;
						//Recover the seconds
						p.ntpOffset = (srPkt.ntpTs1 - (70*365 + 17)*24*3600)*1000;	
						double tmp = (double) Integer.MAX_VALUE*2;
						//Recover the milliseconds
						tmp = (tmp * 1000.0) / (srPkt.ntpTs2);
						
						if(tmp > 1025) {
							System.out.println(" RTCPReceiverThread: There's a bug in the NTP code.");
						} else {
							p.ntpOffset += tmp;
						}
					}
					
					p.lastNtpTs1 = srPkt.ntpTs1;
					p.lastNtpTs2 = srPkt.ntpTs2;
				}
				
				
				if(rtpSession.rtcAppIntf != null) {
					rtpSession.rtcAppIntf.SRPktReceived(srPkt.ssrc, srPkt.ntpTs1, srPkt.ntpTs2, 
							srPkt.rtpTs, srPkt.sendersPktCount, srPkt.sendersPktCount );
				}

				
			} else if(aPkt.getClass() == RtcpPktSDES.class) {
				RtcpPktSDES sdesPkt = (RtcpPktSDES) aPkt;
				
				// The the participant database is updated
				// when the SDES packet is reconstructed by CompRtcpPkt
				
				if(rtpSession.rtcAppIntf != null) {
					rtpSession.rtcAppIntf.SDESPktReceived(sdesPkt.participants);
				}

				
			} else if(aPkt.getClass() == RtcpPktBYE.class) {
				
				RtcpPktBYE byePkt = (RtcpPktBYE) aPkt;
				
				long time = System.currentTimeMillis();
				Participant[] partArray = new Participant[byePkt.ssrcArray.length];
				
				for(int i=0; i<byePkt.ssrcArray.length; i++) {
					partArray[i] = rtpSession.partDb.getParticipant(byePkt.ssrcArray[i]);
					if(partArray[i] != null)
						partArray[i].timestampBYE = time;
				}
				
				if(rtpSession.rtcAppIntf != null) {
					rtpSession.rtcAppIntf.BYEPktReceived(partArray, new String(byePkt.reason));
				}
			}
		}
		
	}

	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			if(rtpSession.mcSession) {
				System.out.println("-> RTCPReceiverThread.run() starting on MC " + rtcpSession.rtcpMCSock.getLocalPort() );
			} else {
				System.out.println("-> RTCPReceiverThread.run() starting on " + rtcpSession.rtcpSock.getLocalPort() );
			}
		}

		while(!rtpSession.endSession) {
			
			if(RTPSession.rtpDebugLevel > 6) {
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
					e.printStackTrace();
				}
			} else {
				//Multicast
				try {
					rtcpSession.rtcpMCSock.receive(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Check whether this is one of our own
			if( (rtpSession.mcSession && ! packet.getSocketAddress().equals(rtcpSession.rtcpMCSock) )
					|| ! packet.getSocketAddress().equals(rtcpSession.rtcpSock) ) {
				parsePacket(packet, rawPkt);
			}			
		}
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTCPReceiverThread terminating");
		}
	}

}

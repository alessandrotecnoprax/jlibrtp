package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.*;

public class RTCPSenderThread extends Thread {
	private RTPSession rtpSession = null;
	private RTCPSession rtcpSession = null;
	
	// Whether we have sent byes for the last conflict
	private boolean byesSent = false;
	
	
	protected RTCPSenderThread(RTCPSession rtcpSession, RTPSession rtpSession) {
		this.rtpSession = rtpSession;
		this.rtcpSession = rtcpSession;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTCPSenderThread created");
		} 
	}
	
	protected void sendByes() {
		// Create the packet
		CompRtcpPkt compPkt = new CompRtcpPkt();
		
		//Need a SR for validation
		RtcpPktSR srPkt = new RtcpPktSR(this.rtpSession.ssrc, 
				this.rtpSession.sentPktCount, this.rtpSession.sentOctetCount);
		compPkt.addPacket(srPkt);
		
		//Add the actualy BYE Pkt
		long[] ssrcArray = {this.rtpSession.ssrc};
		byte[] reasonBytes = "SSRC collision".getBytes();
		RtcpPktBYE byePkt = new RtcpPktBYE( ssrcArray, reasonBytes);
		
		compPkt.addPacket(byePkt);
		
		// Send it off
		if(rtpSession.mcSession) {
			mcSendCompRtcpPkt(compPkt);
		} else {
			Enumeration<Participant> enu = rtpSession.partDb.getParticipants();
		
			while(enu.hasMoreElements()) {
				Participant part = (Participant) enu.nextElement();
				if(!part.unexpected)
					sendCompRtcpPkt(compPkt, part.rtcpAddress);
			}
		}
	}
	protected int mcSendCompRtcpPkt(CompRtcpPkt pkt) {
		byte[] pktBytes = pkt.encode();
		DatagramPacket packet;
		
		// Create datagram
		try {
			packet = new DatagramPacket(pktBytes,pktBytes.length);
		} catch (Exception e) {
			System.out.println("RCTPSenderThread.MCSendCompRtcpPkt() packet creation failed.");
			e.printStackTrace();
			return -1;
		}
		
		// Send packet
		if(RTPSession.rtcpDebugLevel > 5) {
			System.out.println("<-> RTCPSenderThread.SendCompRtcpPkt() multicast");
		}
		try {
			rtcpSession.rtcpMCSock.send(packet);
		} catch (Exception e) {
			System.out.println("RCTPSenderThread.MCSendCompRtcpPkt() multicast failed.");
			e.printStackTrace();
			return -1;
		}
		return packet.getLength();
	}
	
	protected int sendCompRtcpPkt(CompRtcpPkt pkt, InetSocketAddress receiver) {
		byte[] pktBytes = pkt.encode();
		DatagramPacket packet;
		
		//Create datagram
		try {
			//System.out.println("receiver: " + receiver);
			packet = new DatagramPacket(pktBytes,pktBytes.length,receiver);
		} catch (Exception e) {
			System.out.println("RCTPSenderThread.SendCompRtcpPkt() packet creation failed.");
			e.printStackTrace();
			return -1;
		}
		
		//Send packet
		if(RTPSession.rtcpDebugLevel > 5) {
			System.out.println("<-> RTCPSenderThread.SendCompRtcpPkt() unicast " + receiver);
		}
		try {
			rtcpSession.rtcpSock.send(packet);
		} catch (Exception e) {
			System.out.println("RTCPSenderThread.SendCompRtcpPkt() unicast failed.");
			e.printStackTrace();
			return -1;
		}
		return packet.getLength();
	}
	
	public void run() {
		if(RTPSession.rtcpDebugLevel > 1) {
			System.out.println("<-> RTCPSenderThread running");
		}
		
		// Give the application a chance to register some participants
		try { Thread.sleep(100); } 
		catch (Exception e) { System.out.println("RTCPSenderThread didn't get any initial rest."); }
		
		// Set up an iterator for the member list
		// TODO Change to rtcpReceivers
		Enumeration<Participant> enu = rtpSession.partDb.getParticipants();
		
		while(! rtpSession.endSession) {
			if(RTPSession.rtcpDebugLevel > 6) {
				System.out.println("<-> RTCPSenderThread sleeping for " +rtcpSession.nextDelay+" ms");
			}
			
			try { Thread.sleep(rtcpSession.nextDelay); } 
			catch (Exception e) { System.out.println("RTCPSenderThread Exception message:" + e.getMessage());}
			
			if(RTPSession.rtcpDebugLevel > 6) {
				System.out.println("<-> RTCPSenderThread waking up");
			}
			
			// Regenerate nextDelay, before anything happens.
			rtcpSession.calculateDelay();
			
			// We'll wait here until a conflict (if any) has been resolved,
			// so that the bye packets for our current SSRC can be sent.
			if(rtpSession.conflict) {
				if(! this.byesSent) {
					sendByes();
					this.byesSent = true;
				}
				continue;
			}
			this.byesSent = false;
			

			
			//Grab the next person
			Participant part = null;
			//try {
				// Get user stats
				if(! enu.hasMoreElements()) {
					enu = rtpSession.partDb.getParticipants();
				}
				part = enu.nextElement();
				
				//Find someone we actually want to communicate with
				while((part.rtcpAddress == null || part.unexpected) && enu.hasMoreElements()) {
					part = enu.nextElement();
				}
			//} catch (Exception e) { }
			
			if(part == null) {
				//Out of luck
				continue;
			}
			
			/*********** Figure out what we are going to send ***********/
			// Check whether this person has sent RTP packets since the last RR.
			boolean incRR = false;
			if(part.secondLastRtcpRRPkt > part.lastRtcpRRPkt) {
				incRR = true;
				part.secondLastRtcpRRPkt = part.lastRtcpRRPkt;
				part.lastRtcpRRPkt = System.currentTimeMillis();
			}
			
			// Are we sending packets? -> add SR
			boolean incSR = false;
			if(rtpSession.sentPktCount > 0) {
				incSR = true;
			}
			
			
			/*********** Actually create the packet ***********/
			// Create compound packet
			CompRtcpPkt compPkt = new CompRtcpPkt();
			
			//If we're sending packets we'll use a SR for header
			if(incSR) {
				RtcpPktSR srPkt = new RtcpPktSR(this.rtpSession.ssrc, 
						this.rtpSession.sentPktCount, this.rtpSession.sentOctetCount);
				compPkt.addPacket(srPkt);
			}
			
			//If we got anything from this participant since we sent the 2nd to last RtcpPkt
			if(incRR || !incSR) {
				Participant[] partArray = {part};
				
				if(part.receivedPkts < 1)
					partArray = null;
				
				RtcpPktRR rrPkt = new RtcpPktRR(partArray, rtpSession.ssrc);
				compPkt.addPacket(rrPkt);
			}
			
			// For now we'll stick the SDES on every time, and only for us
			RtcpPktSDES sdesPkt = new RtcpPktSDES(true, this.rtpSession, null);
			compPkt.addPacket(sdesPkt);
			
			/*********** Send the packet ***********/
			// Keep track of sent packet length for average;
			int datagramLength;
			if(rtpSession.mcSession) {
				datagramLength = this.mcSendCompRtcpPkt(compPkt);
			} else {
				//part.debugPrint();
				datagramLength = this.sendCompRtcpPkt(compPkt, part.rtcpAddress);
			}
			
			/*********** Administrative tasks ***********/			
			//Update average packet size
			if(datagramLength > 0) {
				rtcpSession.updateAvgPacket(datagramLength);
			}
		}

		// Be polite, say Bye to everone
		sendByes();
		
		if(RTPSession.rtcpDebugLevel > 0) {
			System.out.println("<-> RTCPSenderThread terminating");
		}
	}
}

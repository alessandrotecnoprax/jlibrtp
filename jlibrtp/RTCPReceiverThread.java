package jlibrtp;

import java.io.IOException;
import java.net.DatagramPacket;

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

			// Parse the received compound RTCP (?) packet
			CompRtcpPkt pkt = new CompRtcpPkt(rawPkt, packet.getLength(), packet.getAddress(), rtpSession.partDb);
			
			// Loop over the information

		}
	}

}

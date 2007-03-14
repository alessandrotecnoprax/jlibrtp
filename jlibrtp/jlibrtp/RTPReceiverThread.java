/**
 * Java RTP Library
 * Copyright (C) 2006 Arne Kepp
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jlibrtp;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * The RTP receiver thread waits on the designated UDP socket for new packets.
 * 
 * Once one arrives, it is parsed and tested. We also check the ip-address of the sender. 
 * If accepted, the packet is added onto the packet buffer of the participant.
 * 
 * A separate thread moves the packet from the packet buffer to the application.
 * 
 * @author Arne Kepp
 */
public class RTPReceiverThread extends Thread {
	RTPSession rtpSession = null;

	RTPReceiverThread(RTPSession session) {
		rtpSession = session;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTPReceiverThread created");
		} 
	}

	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			if(rtpSession.mcSession) {
				System.out.println("-> RTPReceiverThread.run() starting on MC " + rtpSession.rtpMCSock.getLocalPort() );
			} else {
				System.out.println("-> RTPReceiverThread.run() starting on " + rtpSession.rtpSock.getLocalPort() );
			}
		}

		while(!rtpSession.endSession) {
			if(RTPSession.rtpDebugLevel > 6) {
				if(rtpSession.mcSession) {
					System.out.println("-> RTPReceiverThread.run() waiting for MC packet on " + rtpSession.rtpMCSock.getLocalPort() );
				} else {
					System.out.println("-> RTPReceiverThread.run() waiting for packet on " + rtpSession.rtpSock.getLocalPort() );
				}
			}

			// Prepare a packet
			byte[] rawPkt = new byte[1500];
			DatagramPacket packet = new DatagramPacket(rawPkt, rawPkt.length);
			// Wait for it to arrive
			if(! rtpSession.mcSession) {
				//Unicast
				try {
					rtpSession.rtpSock.receive(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				//Multicast 
				try {
					rtpSession.rtpMCSock.receive(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Parse the received RTP (?) packet
			RtpPkt pkt = new RtpPkt(rawPkt, packet.getLength());

			// Check whether it was valid.
			if(pkt == null) {
				System.out.println("Received invalid RTP packet. Ignoring");
				continue;
			}

			if(RTPSession.rtpDebugLevel > 6) {
				System.out.println("-> RTPReceiverThread.run() received packet with sequence number " + pkt.getSeqNumber() );
				if(RTPSession.rtpDebugLevel > 10) {
					String str = new String(pkt.getPayload());
					System.out.println("-> RTPReceiverThread.run() payload is " + str );
				}
			}
 
			//Find the participant in the database based on SSRC
			Participant part = rtpSession.partDb.getParticipant(pkt.getSsrc());

			if(part == null) {
				System.out.println("RTPReceiverThread: Got an unexpected packet from " + pkt.getSsrc() + "@" + toString() );
				//Create an unknown sender
				part = new Participant(packet.getAddress(),pkt.getSsrc());
				part.unexpected = true;
				rtpSession.addParticipant(part);
			}

			// Do checks on whether the datagram came from the expected source for that SSRC.
			if(packet.getAddress().equals(part.rtpAddress.getAddress())) {
				PktBuffer pktBuffer = part.pktBuffer;

				if(pktBuffer != null) {
					//A buffer already exists, append to it
					pktBuffer.addPkt(pkt);
				} else {
					// Create a new packet/frame buffer
					pktBuffer = new PktBuffer(pkt);
					part.pktBuffer = pktBuffer;
				}
			} else {
				System.out.println("RTPReceiverThread: Got an unexpected packet from " + pkt.getSsrc() + "@" + toString()
						+ ", the sending ip-address was " + packet.getAddress().toString() + ", we expected from " + part.rtpAddress.toString());
			}

			// Statistics for receiver report.
			//part.receivedOctets += pkt.getPayloadLength();
			part.receivedOctets += packet.getLength();
			part.receivedPkts++;
			
			if( part.lastSeqNumber < pkt.getSeqNumber() || part.lastSeqNumber - part.lastSeqNumber < -100) {
				part.lastSeqNumber = pkt.getSeqNumber();
			}

			if(RTPSession.rtpDebugLevel > 15) {
				System.out.println("<-> RTPReceiverThread signalling pktBufDataReady");
			}
			
			// Signal the thread that pushes data to application
			rtpSession.pktBufLock.lock();
			try { rtpSession.pktBufDataReady.signalAll(); } finally {
				rtpSession.pktBufLock.unlock();
			}

		}
	}

}

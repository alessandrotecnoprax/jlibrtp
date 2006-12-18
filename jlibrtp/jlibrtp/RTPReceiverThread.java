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
//Added 06-12-18 from Vaishnav's tree
import java.util.Hashtable;

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
	
	//Added 06-12-18 from Vaishnav's tree
	Hashtable RTCPRecvRptTable = new Hashtable();
	
	RTPReceiverThread(RTPSession session) {
		rtpSession = session;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTPReceiverThread created");
		} 
	
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			 System.out.println("-> RTPReceiverThread.run() listening on " + rtpSession.rtpSock.getLocalPort() );
		}
		
		while(!rtpSession.endSession) {
	       if(RTPSession.rtpDebugLevel > 6) {
	    	   System.out.println("-> RTPReceiverThread.run() waiting for packet on " + rtpSession.rtpSock.getLocalPort() );
	       }
	       
	       // Prepare a packet
	       byte[] rawPkt = new byte[2000];
	       DatagramPacket packet = new DatagramPacket(rawPkt, rawPkt.length);
	       
	       if(! rtpSession.mcSession) {
	    	   //Unicast
	    	   // Wait for it to arrive
	    	   try {
	    		   rtpSession.rtpSock.receive(packet);
	    	   } catch (IOException e) {
	    		   e.printStackTrace();
	    	   }
	       } else {
	    	   //Multicast 
	    	   // Wait for it to arrive
	    	   try {
	    		   rtpSession.rtpMCSock.receive(packet);
	    	   } catch (IOException e) {
	    		   e.printStackTrace();
	    	   }
	       }
	       
	       // Make a minimum-size bytebyffer
	       byte[] slicedPkt = new byte[packet.getLength()];
	       System.arraycopy(rawPkt, 0, slicedPkt, 0, packet.getLength());
	       
	       // Parse the received RTP (?) packet
	       RtpPkt pkt = new RtpPkt(slicedPkt);
	       
	       // Check whether it was valid.
	       if(pkt == null) {
	    	   System.out.println("Received invalid packet. Ignoring");
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
	    	   part = new Participant(packet.getAddress(),packet.getPort(),pkt.getSsrc());
	    	   rtpSession.addParticipant(part);
	    	   part.lastRecvSeqNumber = pkt.getSeqNumber();
	       }
	       
	       	// Do checks on whether the datagram came from the expected source for that SSRC.
	      	if(packet.getAddress().equals(part.getInetAddress())) {
	    	   PktBuffer pktBuffer  = part.pktBuffer;
	       
	    	   //Temporarily we'll assume there is only one source
	    	   if(pktBuffer != null) {
	    		   //A buffer already exists, append to it
	    		   pktBuffer.addPkt(pkt);
	    	   } else {
	    		   // Create a new packet/frame buffer
	    		   pktBuffer = new PktBuffer(pkt,1);
	    		   part.pktBuffer = pktBuffer;
	    	   }
	       } else {
	    	   System.out.println("RTPReceiverThread: Got an unexpected packet from " + pkt.getSsrc() + "@" + toString()
	    			   + ", the sending ip-address was " + packet.getAddress().toString() + ", we expected from " + part.getInetAddress().toString());
	       }
	      
	      	// Statistics for receiver report.
	      	part.lostPktCount += pkt.getSeqNumber() - part.lastRecvSeqNumber;
	      	part.recvOctetCount += pkt.getPayloadLength();
	      	part.lastRecvSeqNumber = pkt.getSeqNumber();
	      	part.lastRecvTimeStamp = pkt.getTimeStamp();
	      	
//	    	Added 06-12-18 from Vaishnav's tree
	      	updateRRStatistics(pkt);
	      	
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
//	Added 06-12-18 from Vaishnav's tree
	void updateRRStatistics(RtpPkt pkt) {		
			long curr_ssrc = pkt.getSsrc();
	       //System.out.println("-->" + pkt.getSeqNumber() + " " + packet.getLength() + " " + pktCount++ +" " + part.pktBuffer.length);
	       
	       RTCPRRPkt rr = (RTCPRRPkt)RTCPRecvRptTable.get(new Long(curr_ssrc));
	       if(pkt.getSeqNumber() != (rr.getExtHighSeqNumRcvd()+1)) {
	    	   //rr.incPktLostCount();
	    	   ((RTCPRRPkt)RTCPRecvRptTable.get(new Long(curr_ssrc))).incPktLostCount();
	       }
	       //rr.setExtHighSeqNumRcvd(pkt.getSeqNumber());
	       ((RTCPRRPkt)RTCPRecvRptTable.get(new Long(curr_ssrc))).setExtHighSeqNumRcvd(pkt.getSeqNumber());
	       //System.out.println("The stats update for "+pkt.getSsrc());
	}

}

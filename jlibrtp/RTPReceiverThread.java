package jlibrtp;

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


import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Hashtable;


public class RTPReceiverThread extends Thread {
	RTPSession session = null;
	
	RTPReceiverThread(RTPSession session) {
		this.session = session;
		//udpSock = session.udpSock;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTPReceiverThread created");
		} 
	
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			 System.out.println("-> RTPReceiverThread.run() listening on " + session.rtpSock.getLocalPort() );
		}
		
		while(!session.endSession) {
	       byte[] rawPkt = new byte[1500];
	       DatagramPacket packet = new DatagramPacket(rawPkt, rawPkt.length);
	       
	       if(RTPSession.rtpDebugLevel > 6) {
	    	   System.out.println("-> RTPReceiverThread.run() waiting for packet on " + session.rtpSock.getLocalPort() );
	       }
	       
	       try {
	    	   session.rtpSock.receive(packet);
	       } catch (IOException e) {
	    	   e.printStackTrace();
	       }
	       byte[] slicedPkt = new byte[packet.getLength()];
	       System.arraycopy(rawPkt, 0, slicedPkt, 0, packet.getLength());
	       RtpPkt pkt = new RtpPkt(slicedPkt);
	       
	       if(pkt == null) {
	    	   System.out.println("Received invalid packet. Ignoring");
	    	   continue;
	       }
	       if(RTPSession.rtpDebugLevel > 6) {
	    	   System.out.println("-> RTPReceiverThread.run() received packet with sequence number " + pkt.getSeqNumber() );
	       }
	       
	       if(RTPSession.rtpDebugLevel > 10) {
	    	   String str = new String(pkt.getPayload());
	    	   System.out.println("-> RTPReceiverThread.run() payload is " + str );
	       }
	       
	       Participant part = session.partDb.getParticipant(pkt.getSsrc());
	       
	       if(part == null) {
	    	   System.out.println("RTPReceiverThread: Got an unexpected packet from " + pkt.getSsrc() + "@" + toString() );
	    	   part = new Participant(packet.getAddress(),packet.getPort(),pkt.getSsrc());
	    	   session.addParticipant(part);
	    	   part.lastRecvSeqNumber = pkt.getSeqNumber();
	       }
	       
	       // Do checks on whether the datagram came from the right source.
	      	if(true && part != null) {
	    	   PktBuffer pktBuffer  = part.pktBuffer;
	       
	    	   //Temporarily we'll assume there is only one source
	    	   if(pktBuffer != null) {
	    		   //A buffer already exists, append to it (sync)
	    		   pktBuffer.addPkt(pkt);
	    	   } else {
	    		   // Create a new packet/frame buffer
	    		   // Need to lookup the frame-size based on payloadType.
	    		   pktBuffer = new PktBuffer(pkt,1);
	    		   part.pktBuffer = pktBuffer;
	    	   }
	       }
	      
	      	// Statistics for receiver report.
	      	part.lostPktCount += pkt.getSeqNumber() - part.lastRecvSeqNumber;
	      	part.octetCount += pkt.getPayloadLength();
	      	part.lastRecvSeqNumber = pkt.getSeqNumber();
	      	part.lastRecvTimeStamp = pkt.getTimeStamp();

			if(RTPSession.rtpDebugLevel > 15) {
				System.out.println("<-> RTPReceiverThread signalling pktBufDataReady");
			}
			
	       // Signal the thread that pushes data to application
			session.pktBufLock.lock();
		    try { session.pktBufDataReady.signalAll(); } finally {
		       session.pktBufLock.unlock();
		    }
		 
		}
	}
}
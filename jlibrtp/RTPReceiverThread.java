package jlibrtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class RTPReceiverThread extends Thread {
	RTPSession session = null;
	long rcvdTimeStamp = -1;
	 
	RTPReceiverThread(RTPSession session) {
		this.session = session;
		//udpSock = session.udpSock;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTPReceiverThread created");
		} 
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			 System.out.println("-> RTPReceiverThread.run() listening on " + session.udpSock.getLocalPort() );
		}
		
		while(!session.endSession) {
	       byte[] rawPkt = new byte[2000];
	       DatagramPacket packet = new DatagramPacket(rawPkt, rawPkt.length);
	       
	       if(RTPSession.rtpDebugLevel > 1) {
	    	   System.out.println("-> RTPReceiverThread.run() waiting for packet on " + session.udpSock.getLocalPort() );
	       }
	       
	       try {
	    	   session.udpSock.receive(packet);
	       } catch (IOException e) {
	    	   e.printStackTrace();
	       }
	       
	       RtpPkt pkt = new RtpPkt(rawPkt);
		
	       if(RTPSession.rtpDebugLevel > 6) {
	    	   System.out.println("-> RTPReceiverThread.run() received packet with sequence number " + pkt.getSeqNumber() );
	       }
	       
	       if(RTPSession.rtpDebugLevel > 10) {
	    	   String str = new String(pkt.getPayload());
	    	   System.out.println("-> RTPReceiverThread.run() payload is " + str );
	       }
	       
	       Participant part = session.lookupSsrc(pkt.getSsrc());
	       
	       if(part == null) {
	    	   System.out.println("RTPReceiverThread: Got an unexpected packet from " + pkt.getSsrc() + "@" + toString() );
	    	   part = new Participant(packet.getAddress(),packet.getPort(),pkt.getSsrc());
	    	   session.addParticipant(part);
	       }
	       
	       // Do checks on whether the datagram came from the right source.
	       if(true && part != null) {
	    	   PktBuffer pktBuffer  = part.pktBuffer;
	       
	    	   /* Temporarily we'll assume there is only one source */
	    	   if(pktBuffer != null) {
	    		   /* A buffer already exists, append to it (sync) */
	    		   pktBuffer.addPkt(pkt);
	    	   } else {
	    		   /* Create a new packet/frame buffer */
	    		   // Need to lookup the frame-size based on payloadType.
	    		   pktBuffer = new PktBuffer(pkt,1);
	    		   part.pktBuffer = pktBuffer;
	    	   }
	       }
		}
	}
}

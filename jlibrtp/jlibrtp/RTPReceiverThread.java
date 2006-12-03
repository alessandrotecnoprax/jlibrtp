/**
 * The major flaw in this is that the PktBuffers should be connect to
 * other threads, this thread should be dealing with the pkt / frame queue.
 */
package jlibrtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class RTPReceiverThread extends Thread {
	RTPSession session = null;
	DatagramPacket packet = null;
	DatagramSocket socket4 = null;
	//Hashtable pktBuffer = new Hashtable();
	PktBuffer pktBuffer = null;
	int recvPort = 0;
	long rcvdTimeStamp = -1;
	 
	RTPReceiverThread(RTPSession session,int recvPort)
	{
		this.session = session;
		this.recvPort = recvPort;
		try {
			this.socket4 = new DatagramSocket(this.recvPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			 System.out.println("-> RTPReceiverThread.run() listening on " + socket4.getLocalPort() );
		}
		
		while(true) {
	       byte[] rawPkt = new byte[2000];
	       packet = new DatagramPacket(rawPkt, rawPkt.length);
	       
	       try {
	    	   socket4.receive(packet);
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
	       
	       /* Temporarily we'll assume there is only one source */
	       
	       if(pktBuffer != null) {
	    	   /* A buffer already exists, append to it */
	    	   pktBuffer.addPkt(pkt);
	       } else {
	    	   /* Create a new packet/frame buffer */
	    	   pktBuffer = new PktBuffer(pkt,1);
	       }
		}
	}
}

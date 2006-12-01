package jlibrtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

public class RTPReceiverThread extends Thread {
	RTPSession session = null;
	DatagramPacket packet = null;
	DatagramSocket socket4 = null;
	Hashtable pktBuffer = new Hashtable();
	int recvPort = 0;
	 long rcvdTimeStamp = -1;
	 
	RTPReceiverThread(RTPSession session,int recvPort)
	{
		this.session = session;
		this.session = session;
		this.recvPort = recvPort;
		try
		{
			
			this.socket4 = new DatagramSocket(this.recvPort);
		} 
		catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		int lastSeqNumRcvd = 0;
		
		while(this.session.isBYERcvd())
		{
	       byte[] rcvdByte = new byte[1036];
	       packet = new DatagramPacket(rcvdByte, 1036);
	       System.out.println("I am expecting on "+socket4.getLocalPort());
	       try
	       {
			socket4.receive(packet);
	       }
	       catch (IOException e)
	       {
			e.printStackTrace();
	       }
	    
	       RtpPkt pkt = new RtpPkt(rcvdByte);

	      String ss = new String(pkt.getPayload());
	      System.out.println("The data I received is "+pkt.getPayloadLength());
	       
	       
	       /* Only the basic implementation is beign done, where all the packets from one source are packed*/
	       
	       /* Here I am assuming that all packets arrive in-order*/
	       if(rcvdTimeStamp == -1)
	       {
	    	   rcvdTimeStamp = pkt.getTimeStamp();
	       }
	       
	       
	       
	       if(!(pktBuffer.containsKey(new Long(pkt.getTimeStamp()))))
	    	{
	    	   ByteBuffer tempBuf = ByteBuffer.allocate(100000);
	    	   pktBuffer.put(new Long(pkt.getTimeStamp()),tempBuf);
	    	   
	    	   ((ByteBuffer)(pktBuffer.get(new Long(pkt.getTimeStamp())))).put(rcvdByte); 
	    	}
	       else
	       {
	    	   ((ByteBuffer)(pktBuffer.get(new Long(pkt.getTimeStamp())))).put(rcvdByte);
	    	   
	    	  
				
	    	   if(rcvdTimeStamp != pkt.getTimeStamp())
	    	   {
	    		   /* When I receive a pkt with a new time stamp, then it is pushed to the session*/
	   	    	
		    	  
			    		Enumeration set = pktBuffer.elements();
			
						
							ByteBuffer buff = ByteBuffer.allocate(100000);
							while(set.hasMoreElements())
							{
									ByteBuffer p = (ByteBuffer)set.nextElement();
									buff.put(p.array());
							}
							
						session.addtoFrameBuffer(buff,pkt.getSsrc());
		    	     
	    	   }
					
	       }
	       
	       rcvdTimeStamp = pkt.getTimeStamp();
	       rcvdByte = null;
	       packet = null;
		}
	}
}

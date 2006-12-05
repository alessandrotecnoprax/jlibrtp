package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Enumeration;
import java.util.Hashtable;

public class RTCPRRSendThread implements Signalable
{
	RTCPSession rtcpSession = null;
	int rtcpPort = 0;

	RTCPRRSendThread(int rtcpPort,RTCPSession rtcpSession)
	{
		this.rtcpSession = rtcpSession;
		this.rtcpPort = rtcpPort;
		Timer t = new Timer(3000,this);
		t.startTimer();
	}
	
	public void signalTimeout() 
	{
		Hashtable rrRpt = rtcpSession.rtpSession.recvThrd.RTCPRecvRptTable;
		
		 Enumeration set = rtcpSession.rtpSession.participantTable.elements();
		 while(set.hasMoreElements()) 
		 {
			 Participant p = (Participant)set.nextElement();
			
			 System.out.println("The SSRC in RR Thread SSRC="+p.ssrc);
			 if(rrRpt.containsKey(new Long(p.ssrc)))
			 {
				 RTCPRRPkt rrPkt= (RTCPRRPkt) rrRpt.get(new Long(p.ssrc));
				 
				 byte[] rawRRPkt = rrPkt.encodeRRPkt(rtcpSession.rtpSession.ssrc);
				 System.out.println("The RR DAta is being sent to SSRC of the participant="+p.ssrc);	 
				 //////////////////////////////////////////////////////////////////////////
				 
					
					
						      int port = this.rtcpPort;

						      String group = "225.4.5.6";

						    
						      try
						      {
								      MulticastSocket s = new MulticastSocket();
		
								 
								      DatagramPacket pack = new DatagramPacket(rawRRPkt, rawRRPkt.length,
								      					 InetAddress.getByName(group), port);
	     
								      s.send(pack);
								      s.close();
						      }
						      catch(Exception e)
						      {
						    	  e.printStackTrace();
						      }
				 //////////////////////////////////////////////////////////////////////////
				 
			 }
			 
		 }
		 
		 
		Timer t = new Timer(30,this);
		t.startTimer();
	}

}

package jlibrtp;

/**
 * Java RTP Library
 * Copyright (C) 2006 Vaishnav Janardhan
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


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.util.Enumeration;
import java.util.Hashtable;

public class RTCPRRSendThread implements Signalable
{
	RTCPSession rtcpSession = null;
	RTPSession rtpSession = null;
	int rtcpPort = 0;

	RTCPRRSendThread(int rtcpPort,RTCPSession rtcpSession)
	{
		this.rtcpSession = rtcpSession;
		this.rtcpPort = rtcpPort;
		Timer t = new Timer(8000,this);
		t.startTimer();
	}
	
	public void signalTimeout() 
	{
		Hashtable rrRpt = rtcpSession.rtpSession.recvThrd.RTCPRecvRptTable;
		 System.out.println("I am Sending Signal timeout");
		 Enumeration set = rtcpSession.rtpSession.participantTable.elements();
		 while(set.hasMoreElements()) 
		 {
			 Participant p = (Participant)set.nextElement();
	
			 if(rrRpt.containsKey(new Long(p.ssrc)))
			 {
				 System.out.println("I am Sending out the RR packet");
				 //RTCPRRPkt rrPkt= (RTCPRRPkt) rrRpt.get(new Long(p.ssrc));
				 RTCPRRPkt rrPkt= (RTCPRRPkt) rtcpSession.rtpSession.recvThrd.RTCPRecvRptTable.get(new Long(p.ssrc));
		//	 RTCPRRPkt rrPkt= new RTCPRRPkt(p.ssrc);
				 rrPkt.setLSR(System.currentTimeMillis());
				 byte[] rawRRPkt = rrPkt.encodeRRPkt(rtcpSession.rtpSession.ssrc);
	//			 System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
		//		 System.out.println("The RR DAta is being sent to SSRC of the participant="+p.ssrc);	 
				 //////////////////////////////////////////////////////////////////////////
				 
					
					
						      int port = this.rtcpPort;

						      String group = "225.4.5.6";

						    
						      try
						      {
								      MulticastSocket s = new MulticastSocket();
		
								      
								      DatagramPacket pack = new DatagramPacket(rawRRPkt, rawRRPkt.length,
								      					 InetAddress.getByName(group), port);
	     
							//	      s.send(pack);
							//	      s.close();
								      if(this.rtpSession.mcSession == false)
								      {
								    	  this.rtpSession.rtcpSock.send(pack);
								      }
						      }
						      catch(Exception e)
						      {
						    	  e.printStackTrace();
						      }
				 //////////////////////////////////////////////////////////////////////////
				 
			 }
			 
		 }
		 
		 
		Timer t = new Timer(8000,this);
		t.startTimer();
	}

}
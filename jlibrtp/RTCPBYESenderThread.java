package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Enumeration;
import java.util.Hashtable;

public class RTCPBYESenderThread extends Thread implements Signalable 
{
	int rtcpPort = 0;
	RTCPSession rtcpSession = null;
	
	
	RTCPBYESenderThread(int rtcpPort,RTCPSession session)
	{
		this.rtcpPort = rtcpPort;
		this.rtcpSession = session;
		Timer t = new Timer(800,this);
	//	t.startTimer();
		
	}
	
		public void signalTimeout() 
		{
			 Hashtable participantTable = this.rtcpSession.getRTPSession().getParticipantDB();
			 int[] ssrcArray = new int[32];
			 int ssrcArrayCount = 0;
			 
					Enumeration set = participantTable.elements();

					while(set.hasMoreElements())
					{
						Participant p = (Participant)set.nextElement();
						ssrcArray[ssrcArrayCount++] = p.getSSRC();
						
					}
					RTCPByePkt byePkt = new RTCPByePkt(ssrcArrayCount,ssrcArray);
					byte[] sendByeBuf = byePkt.encodeBYEPkt();
					
						      int port = this.rtcpPort;

						      String group = "225.4.5.6";

						    
						      try
						      {
								      MulticastSocket s = new MulticastSocket();
		
								 
								      DatagramPacket pack = new DatagramPacket(sendByeBuf, sendByeBuf.length,
								      					 InetAddress.getByName(group), port);
	     
								      s.send(pack);
								      s.close();
						      }
						      catch(Exception e)
						      {
						    	  e.printStackTrace();
						      }

						      
								Timer t = new Timer(800,this);
								t.startTimer();
								
								if(rtcpSession.rtpSession.isBYERcvd)
								{
									t.stopTimer();
								}

				}
	
		void sendBYEMsg(RTCPByePkt byePkt)
		{
			byte[] sendByeBuf = byePkt.encodeBYEPkt();
			
		      int port = 6000;

		      String group = "225.4.5.6";

		    
		      try
		      {
				      MulticastSocket s = new MulticastSocket();

				 
				      DatagramPacket pack = new DatagramPacket(sendByeBuf, sendByeBuf.length,
				      					 InetAddress.getByName(group), port);
				    

				      s.send(pack);
				      s.close();
		      }
		      catch(Exception e)
		      {
		    	  e.printStackTrace();
		      }

		}
	
}

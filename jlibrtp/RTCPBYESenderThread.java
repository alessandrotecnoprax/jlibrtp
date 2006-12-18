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
package jlibrtp;/**
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
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * RTCP Bye Sender Thread
 * 
 * @author Vaishnav Janardhan
 */
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
//			 Hashtable participantTable = this.rtcpSession.rtpSession.participantTable;
//			 long[] ssrcArray = new long[32];
//			 int ssrcArrayCount = 0;
//			 
//					Enumeration set = participantTable.elements();
//
//					while(set.hasMoreElements())
//					{
//						Participant p = (Participant)set.nextElement();
//						ssrcArray[ssrcArrayCount++] = p.getSSRC();
//						
//					}
//					RTCPByePkt byePkt = new RTCPByePkt(ssrcArrayCount,ssrcArray);
//					byte[] sendByeBuf = byePkt.encodeBYEPkt();
//					
//						      int port = this.rtcpPort;
//
//						      String group = "225.4.5.6";
//
//						    
//						      try
//						      {
//								      MulticastSocket s = new MulticastSocket();
//		
//								 
//								      DatagramPacket pack = new DatagramPacket(sendByeBuf, sendByeBuf.length,
//								      					 InetAddress.getByName(group), port);
//								      System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvBYE");
//						//		      s.send(pack);
//								      s.close();
//						      }
//						      catch(Exception e)
//						      {
//						    	  e.printStackTrace();
//						      }
//
//						      
//								Timer t = new Timer(800,this);
//								t.startTimer();
//								
//								if(rtcpSession.rtpSession.isEnding())
//								{
//									t.stopTimer();
//								}

				}
	
		void sendBYEMsg(RTCPByePkt byePkt)
		{
//			byte[] sendByeBuf = byePkt.encodeBYEPkt();
//			
//		      int port = 6000;
//
//		      String group = "225.4.5.6";
//
//		    
//		      try
//		      {
//				      MulticastSocket s = new MulticastSocket();
//
//				 
//				      DatagramPacket pack = new DatagramPacket(sendByeBuf, sendByeBuf.length,
//				      					 InetAddress.getByName(group), port);
//				    
//
//				      s.send(pack);
//				      s.close();
//		      }
//		      catch(Exception e)
//		      {
//		    	  e.printStackTrace();
//		      }

		}
	
}

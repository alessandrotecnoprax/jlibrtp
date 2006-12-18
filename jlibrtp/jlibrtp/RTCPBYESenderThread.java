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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * RTCP Bye Sender Thread
 * This class is used to send out the BYE message to all the participant and close down the session
 * 
 * @author Vaishnav Janardhan
 */
public class RTCPBYESenderThread extends Thread  
{
	//int rtcpPort = 0;
	RTCPSession rtcpSession = null;
	
	
	RTCPBYESenderThread(RTCPSession session)
	{
		//
		this.rtcpSession = session;
		
	}
	
	/**
	 * This method will send the BYE Msg to all the participants and close down
	 * the session
	 * @param byePkt
	 */

		void sendBYEMsg(RTCPByePkt byePkt)
		{
			byte[] sendByeBuf = byePkt.encodeBYEPkt();
			
	    
		      try
		      {
		    		Enumeration set = this.rtcpSession.rtpSession.partDb.getReceivers();
		    		
		    		while(set.hasMoreElements()) {
		    			Participant p = (Participant)set.nextElement();

		    			if(p.isReceiver()) {				 
				      DatagramPacket pack = new DatagramPacket(sendByeBuf, sendByeBuf.length,
				      					 p.getInetAddress(),p.getRtcpDestPort());
				      
				      if(this.rtcpSession.rtpSession.mcSession == false)
				      {
				    	  this.rtcpSession.rtpSession.rtpSock.send(pack);
				      }
		    			}
		    		}

				
		      }
		      catch(Exception e)
		      {
		    	  e.printStackTrace();
		      }

		}
	
}

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
package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Enumeration;

/**
 * RTCP Sender Report Thread
 * This class will send the Sender Reports periodically to all of the 
 * participants in the participant database. The SR message packet is 
 * constructed through the RTCPSenderReport class.
 * 
 * @author Vaishnav Janardhan
 */
public class RTCPSRSendThread implements Signalable
{
	RTPSession rtpSession = null;

	RTCPSenderReport sendReport = null;
	
	/**
	 * Constructor for creation of the RTCPSRSendThread
	 * @param rtpSession
	 */
	RTCPSRSendThread(RTPSession rtpSession)
	{
		this.rtpSession = rtpSession;
	
		this.sendReport = new RTCPSenderReport(this.rtpSession.ssrc,this.rtpSession);
		Timer t = new Timer(8000,this);
		t.startTimer();
	}
	
	
	/**
	 * The callback method that will be invoked, when the timer fires.
	 * This method will send the SR message to all the participants in the 
	 * database.
	 */
	public void signalTimeout() 
	{
	      

	      //String group = "225.4.5.6";
  		Enumeration set = this.rtpSession.partDb.getReceivers();
		
		while(set.hasMoreElements()) {
			Participant p = (Participant)set.nextElement();

			if(p.isReceiver()) {
				byte[] rawSRPkt = this.sendReport.encodeSRPkt();
				try
				{
			      

			 
					DatagramPacket pack = new DatagramPacket(rawSRPkt, rawSRPkt.length,
							p.getInetAddress(), p.getRtcpDestPort());
						
			      //     s.send(pack);
			      //     s.close();
			      if(this.rtpSession.mcSession == false)
			      {
			    	  this.rtpSession.rtpSock.send(pack);
			      }
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
			Timer t = new Timer(8000,this);
			t.startTimer();
	

	}
}

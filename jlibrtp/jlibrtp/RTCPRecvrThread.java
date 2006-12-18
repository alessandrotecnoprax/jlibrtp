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
import java.net.DatagramSocket;
import java.net.MulticastSocket;

/**
 * RTCPRecvrThread is the reciever thread that will be waiting for the 
 * incoming RTCP packets. Once the packets are received, the header will
 * be decoded and the depending on the type of the packet, the corresponding
 * decode method would be invoked.
 * 
 * @author Vaishnav Janardhan
 *
 */
public class RTCPRecvrThread extends Thread   {
	RTCPSession session = null;

	MulticastSocket s = null;

	
	RTCPRecvrThread(RTCPSession session) {

		this.session = session;


	}
	
	public void run() {
		while(!this.session.rtpSession.endSession) {
			
			
			try
			{
				if(RTPSession.rtpDebugLevel > 1){
		//		System.out.println("INSIDE RTCPRecvThread 1 the port="+this.rtcpPort);
				}

				
	
				byte buf[] = new byte[1024];
				DatagramPacket pack = new DatagramPacket(buf, buf.length);
			//	s.receive(pack);
				session.rtpSession.rtcpSock.receive(pack);
				String ss = new String(buf);
				
				RTCPCommonHeader header = new RTCPCommonHeader(buf);
			
				if(header.getPktType() == 203) {
					
					this.session.rtpSession.endSession = true;
				}
				else if(header.getPktType() == 201)
				{System.out.println(" 201 ");
					System.out.println("The RR Packet received");
					RTCPRRPkt rrPkt = new RTCPRRPkt();
					rrPkt.decodeRRPkt(buf);
					
				}
				else if(header.getPktType() == 202)
				{
					System.out.println(" 202 ");
					RTCPSDESHeader sdesPkt = new RTCPSDESHeader(buf);
					sdesPkt.decode();
					//if(RTPSession.rtpDebugLevel > 1){
						System.out.println("The SDES pkt has been received");
					System.out.println("The CNAME rcvd is ="+sdesPkt.CNAME);
					System.out.println("The SSRC rcvd is ="+sdesPkt.ssrc);
					//}
				
			//		Participant P = (Participant) session.rtpSession.participantTable.get(new String(sdesPkt.CNAME));
					Participant P = (Participant) session.rtpSession.participantTable.get(new Long(sdesPkt.ssrc));
					if(P != null && P.ssrc !=  -1)
					{
						System.out.println("The Newly selected Participant CNAME="+sdesPkt.ssrc);
						//P.setSSRC(sdesPkt.ssrc);
					//	((Participant) session.rtpSession.participantTable.get(new String(sdesPkt.CNAME))).setSSRC(sdesPkt.ssrc);
						((Participant) session.rtpSession.participantTable.get(new Long(sdesPkt.ssrc))).setSSRC(sdesPkt.ssrc);
						((Participant) session.rtpSession.participantTable.get(new Long(sdesPkt.ssrc))).cname = sdesPkt.CNAME;
						
					}
					
					
					
				}
				else if(header.getPktType() == 200)
				{System.out.println(" 200 ");
					if(RTPSession.rtpDebugLevel > 1){
						System.out.println("The Sender Report Pkt received ");
					}
					RTCPSenderReport srPkt = new RTCPSenderReport();
					srPkt.decodeSRPkt(buf);
					
				}
				

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
}


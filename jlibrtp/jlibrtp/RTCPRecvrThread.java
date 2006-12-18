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

public class RTCPRecvrThread extends Thread   {
	RTCPSession session = null;
//	int rtcpPort = 0;
	MulticastSocket s = null;
	String group = "225.4.5.6";
	
	RTCPRecvrThread(RTCPSession session) {
//		this.rtcpPort = rtcpPort;
		this.session = session;
	//	int port = this.rtcpPort;

	}
	
	public void run() {
		while(!this.session.rtpSession.endSession) {
			
			
			try
			{
				if(RTPSession.rtpDebugLevel > 1){
		//		System.out.println("INSIDE RTCPRecvThread 1 the port="+this.rtcpPort);
				}
				s = new MulticastSocket(8000);
				
				s.joinGroup(InetAddress.getByName(group));	
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
				
				s.leaveGroup(InetAddress.getByName(group));
				s.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
}


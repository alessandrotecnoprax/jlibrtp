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

public class RTCPRecvrThread extends Thread   {
	RTCPSession rtcpSession = null;
	RTPSession rtpSession = null;
	
	RTCPRecvrThread(RTCPSession rtcpsess) {
		this.rtcpSession = rtcpsess;
		this.rtpSession = rtcpSession.rtpSession;
	}
	
	public void run() {
		while(!this.rtpSession.isEnding()) {
			if(RTPSession.rtpDebugLevel > 8){
				System.out.println("RTCPRecvrThread looping");
			}
			
			DatagramSocket s = rtcpSession.rtcpSock;
			
			byte buffer[] = new byte[1500];
			DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
			
			try {
				s.receive(pkt);
			} catch(Exception e) {
				System.out.println("RTCPRecvrThread " + e.getMessage());
			}
			byte[] rawPkt = new byte[pkt.getLength()];
			
			System.arraycopy(buffer, 0, rawPkt, 0, rawPkt.length);
			
			String ss = new String(rawPkt);
				
			RTCPCommonHeader header = new RTCPCommonHeader(rawPkt);
			
			int pktType = header.getPktType();
			if( pktType == 203) {
				this.rtpSession.endSession();
			} else if(pktType == 201) {
				System.out.println("The RR Packet received");
				RTCPRRPkt rrPkt = new RTCPRRPkt();
				rrPkt.decodeRRPkt(rawPkt);
			} else if(pktType == 202) {
				RTCPSDESHeader sdesPkt = new RTCPSDESHeader(rawPkt);
				sdesPkt.decode();
				if(RTPSession.rtpDebugLevel > 5){
					System.out.println("The SDES pkt has been received");
					System.out.println("The CNAME rcvd is ="+sdesPkt.CNAME);
					System.out.println("The SSRC rcvd is ="+sdesPkt.ssrc);
				}
				
				// Arne: I think this is supposed to update the participant table. 
				// 
				//Participant P = (Participant) rtpSession.participantTable.get(new Long(sdesPkt.ssrc));
				//if(P != null && P.ssrc !=  -1) {
				//		System.out.println("The Newly selected Participant CNAME="+sdesPkt.ssrc);
				//		((Participant) rtpSession.participantTable.get(new Long(sdesPkt.ssrc))).setSSRC(sdesPkt.ssrc);
				//		((Participant) rtpSession.participantTable.get(new Long(sdesPkt.ssrc))).cname = sdesPkt.CNAME;
				//}
			}
		}
	}
}

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

public class RTCPRRSendThread extends Thread {
	RTCPSession rtcpSession = null;
	RTPSession rtpSession = null;
	int rtcpPort = 0;

	RTCPRRSendThread(RTCPSession rtcpSession) {
		this.rtcpSession = rtcpSession;
		this.rtpSession = rtcpSession.rtpSession;
	}
	
	void Run() {
		while(! rtpSession.isEnding()) {
			//Hashtable rrRpt = rtcpSession.rtpSession.recvThrd.RTCPRecvRptTable;
			Enumeration set = rtpSession.partDb.getSenders();
		
			while(set.hasMoreElements()) {
				Participant p = (Participant)set.nextElement();
				
				// Arne: We need to create packet based on the history of packets,
				// but this info cannot be held by the receiving thread object?
				
				//if(rrRpt.containsKey(p.ssrc)) {
				//	RTCPRRPkt rrPkt= (RTCPRRPkt) rtpSession.recvThrd.RTCPRecvRptTable.get(p.ssrc);
				//	byte[] rawRRPkt = rrPkt.encodeRRPkt(rtcpSession.rtpSession.ssrc);
				//	rtcpSession.sendPkt(p, rawRRPkt);
				//}
			}
		}
		 
	}
}
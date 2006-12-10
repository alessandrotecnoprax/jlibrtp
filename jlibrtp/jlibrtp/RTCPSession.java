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


public class RTCPSession {
	int rtcpPort = 0;
	RTPSession rtpSession = null;
	
	RTCPBYESenderThread byeThread = null;
	RTCPRecvrThread recvThread = null;
	RTCPRRSendThread rrSendThread = null;
	RTCPSDESHeader sdesThread = null;
	
	RTCPSession(int rtcpPort, RTPSession rtpSession) {
		this.rtcpPort = rtcpPort;
		this.rtpSession = rtpSession;
		byeThread = new RTCPBYESenderThread(rtcpPort,this);
		this.recvThread = new RTCPRecvrThread(rtcpPort,this);
		
		this.rrSendThread = new RTCPRRSendThread(rtcpPort,this);
		this.sdesThread = new RTCPSDESHeader(rtcpPort,this);
		
		this.recvThread.start();	
	}
	
	RTPSession getRTPSession() {
		return this.rtpSession;
	}
	
	void requestBYE(int ssrc) {
		System.out.println("BYE Request rcvd");
		long [] ssrcArray = new long[1];
		ssrcArray[0] = ssrc;
		
		RTCPByePkt byePkt = new RTCPByePkt(1,ssrcArray);		
		byeThread.sendBYEMsg(byePkt);
	}
}

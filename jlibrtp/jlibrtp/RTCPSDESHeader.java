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

public class RTCPSDESHeader {
	int sdesid;
	int length;
	RTCPSession rtcpSession = null;
	RTCPCommonHeader commonHdr = null;
	byte[] rawSDESPkt = null;
	String CNAME = null;
	long ssrc = 0;
	
	// For received packet, to be decoded
	RTCPSDESHeader(byte[] buf) {
		this.rawSDESPkt = buf;
	}
	
	// For new packet, that we want to send.
	RTCPSDESHeader(int rtcpPort, RTCPSession session) {
		this.rtcpSession = session;
		CNAME = this.rtcpSession.rtpSession.CNAME;
		ssrc = this.rtcpSession.rtpSession.ssrc;
		
		rawSDESPkt = new byte[32+32+CNAME.length()+32];
		
		commonHdr =  new RTCPCommonHeader(2,0,1,202);
		
		
		this.commonHdr.pktType = 202;
		this.commonHdr.padding = 0;
		this.commonHdr.pktLen = 32+32+CNAME.length()+32;
	
	}
	
	
	void sendSDESPkt(Participant p) {
		 
		byte[] firstLine = commonHdr.writeFristLine();
		
		System.arraycopy(firstLine, 0, this.rawSDESPkt,0,32);
		System.out.println("The SSRC in SDES Msg is SSRC="+this.ssrc);
		byte[] reporteeSSRCArry = StaticProcs.longToBin(this.ssrc);
		System.arraycopy(reporteeSSRCArry, 0, this.rawSDESPkt, 32, 32);
		
		byte[] cnameLenArry = StaticProcs.intToBin(CNAME.length());
		System.arraycopy(cnameLenArry, 0, this.rawSDESPkt, 64,32);
		
		byte[] cnameArry = CNAME.getBytes();
		System.arraycopy(cnameArry, 0, this.rawSDESPkt, 96, cnameArry.length);
		
		rtcpSession.sendPkt(p, rawSDESPkt);
		
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("The SDES packet has been sent...");
		}
	}
	
	byte[] encodeSDES() {
		return this.rawSDESPkt;
	}
	
	void decode() {
		byte[] ssrcArry = new byte[32]; 
			System.arraycopy(this.rawSDESPkt, 32, ssrcArry, 0, 32);
			
	   this.ssrc = StaticProcs.longBin2Dec(ssrcArry);
	
	   byte[] cnameLenArry = new byte[32];
	   		System.arraycopy(this.rawSDESPkt, 64, cnameLenArry, 0, 32);
	   	int cnameLen = StaticProcs.intBin2Dec(cnameLenArry);
	   	
	   	byte[] cnameArry = new byte[cnameLen];
	   		System.arraycopy(this.rawSDESPkt, 96, cnameArry, 0, cnameLen);
	   		
	   	this.CNAME = new String(cnameArry);
		
	}
	
	long getSSRC() {
		return this.ssrc;
	}
	
	String getCNAME() {
		return this.CNAME;
	}
}

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

/**
 * RTCP Receiver Report Packet
 * 
 * @author Vaishnav Janardhan
 */
public class RTCPRRPkt
{
	long ssrc; 
	int fractionlost = 0;
	int packetslost = 0;
	int exthighseqnr=0;
	long jitter=0;
	long lsr=0;
	long dlsr=0;
	RTCPCommonHeader commonHdr = null;
	byte[] rawRRPkt = new byte[32*8];
	
	RTCPRRPkt()
	{
		
	}
	RTCPRRPkt(long ssrc)
	{
		this.ssrc = ssrc;
		commonHdr =  new RTCPCommonHeader(2,0,1,201);
		
	
		this.commonHdr.pktType = 201;
		this.commonHdr.padding = 0;
		this.commonHdr.pktLen = 192;
	}
	
	void setExtHighSeqNumRcvd(int seqNum)
	{
		this.exthighseqnr = seqNum;
	}
	int getExtHighSeqNumRcvd()
	{
		return this.exthighseqnr;
	}
	
	void setLSR(long lsr)
	{
		this.lsr = lsr;
	}
	
	long getLSR()
	{
		return this.lsr;
	}
	
	long getDLSR()
	{
		return (System.currentTimeMillis() - this.lsr);
	}
	
	void incPktLostCount()
	{
		this.packetslost +=1;
	}
	
	int getPktLostCount()
	{
		return this.packetslost;
	}
	
	byte[] encodeRRPkt(long reporteeSSRC)
	{
		 
		byte[] firstLine = commonHdr.writeFristLine();
		
		System.arraycopy(firstLine, 0, this.rawRRPkt,0,32);
		
		System.out.println("The Session SSRC="+this.ssrc+" The Participant SSRC="+reporteeSSRC);
		byte[] reporterSSRCArry = StaticProcs.longToBin(this.ssrc);
		System.arraycopy(reporterSSRCArry, 0, this.rawRRPkt, 32, 32);
		
		byte[] reporteeSSRCArry = StaticProcs.longToBin(reporteeSSRC);
		System.arraycopy(reporteeSSRCArry, 0, this.rawRRPkt, 64, 32);
		
		//// Left the calculation of the loss fraction. The pkt Lost is full 32 bits
		byte[] cumPktLostArry = StaticProcs.intToBin(this.getPktLostCount());
		System.arraycopy(cumPktLostArry, 0, this.rawRRPkt, 96, 32);
		
		//// Left out Interval Jitter for now
		byte[] lsrArry = StaticProcs.longToBin(this.getLSR());
		System.arraycopy(lsrArry, 0, this.rawRRPkt, 128, 32);
		
		// byte[] dlsrArry = longToBin(this.getDLSR());
		byte[] dlsrArry = StaticProcs.longToBin(32323232);
		System.arraycopy(dlsrArry, 0, this.rawRRPkt, 160, 32);
		
		return this.rawRRPkt;
	
		
	}
	
	void decodeRRPkt(byte[] rcvdPkt)
	{
		byte[] reporterSSRCArry = new byte[32]; 
			System.arraycopy(rcvdPkt,32,reporterSSRCArry, 0, 32);
		
		System.out.println("The Reported SSRC="+StaticProcs.longBin2Dec(reporterSSRCArry));
		
		byte[] reporteeSSRCArry = new byte[32];
		System.arraycopy(rcvdPkt,64,reporteeSSRCArry, 0, 32);
		System.out.println("The Reportee SSRC="+StaticProcs.longBin2Dec(reporteeSSRCArry));
		
		byte[] cumPktLostArry = new byte[32];
		System.arraycopy(rcvdPkt,96,cumPktLostArry, 0, 32);
		System.out.println("The Cumulative Packet lost="+StaticProcs.intBin2Dec(cumPktLostArry));
		
		byte[] lsrArry = new byte[32];
		System.arraycopy(rcvdPkt,128,lsrArry, 0, 32);
		System.out.println("The Last time Receiver Report sent="+StaticProcs.longBin2Dec(lsrArry));
	}
}

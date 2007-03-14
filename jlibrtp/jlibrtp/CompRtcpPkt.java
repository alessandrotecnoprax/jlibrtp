package jlibrtp;

import java.util.*;
import java.net.InetAddress;

public class CompRtcpPkt {
	protected LinkedList rtcpPkts = new LinkedList();
	
	protected CompRtcpPkt() {
		// Will have to add packets directly to rtcpPkts.
	}
	
	protected void addPacket(RtcpPkt aPkt) {
		rtcpPkts.add(aPkt);
	}
	
	protected CompRtcpPkt(byte[] rawPkt, int packetSize, InetAddress adr, ParticipantDatabase partDb) {
		// Chop it up
		int start = 0;
		
		 //  o  The payload type field of the first RTCP packet in a compound
		 //     packet must be equal to SR or RR.

		 //  o  The padding bit (P) should be zero for the first packet of a
		 //     compound RTCP packet because padding should only be applied, if it
		 //     is needed, to the last packet.

		 //  o  The length fields of the individual RTCP packets must add up to
		 //     the overall length of the compound RTCP packet as received.  This
		 //     is a fairly strong check.
		      
		while(start < (packetSize - 32)) {
			int length = StaticProcs.combineBytes(rawPkt[start + 2], rawPkt[start + 3]);
			byte[] tmpBuf = new byte[length];
			
			int pktType = (int) rawPkt[start + 1];
			if(pktType < 0) {
				pktType += 256;
			}
			
			System.arraycopy(rawPkt, start, tmpBuf, 0, length);
			if(pktType == 200)
				rtcpPkts.add(new RtcpPktSR(tmpBuf));
			if(pktType == 201 )
				rtcpPkts.add(new RtcpPktRR(tmpBuf, -1));
			if(pktType == 202)
				rtcpPkts.add(new RtcpPktSDES(tmpBuf, partDb));
			if(pktType == 203 )
				rtcpPkts.add(new RtcpPktBYE(tmpBuf));
			if(pktType == 204)
				rtcpPkts.add(new RtcpPktAPP(tmpBuf));
			start += length;
		}
	}
	
	protected byte[] encode() {
		// We have to do it every time since we have no control over the packets.
		ListIterator  iter = rtcpPkts.listIterator();
	
		// Encode the packets and find the total size
		int rawPktSize = 0;
		while(iter.hasNext()) {
			RtcpPkt aPkt = (RtcpPkt) iter.next();
			
			if(aPkt.packetType == 200) {
				System.out.println("hepp");
				RtcpPktSR pkt = (RtcpPktSR) aPkt;
				System.out.println("hepp 1");
				pkt.encode(null);
				System.out.println("hepp 2");
				iter.set(pkt);
			} else if(aPkt.packetType == 201 ) {
				RtcpPktRR pkt = (RtcpPktRR) aPkt;
				pkt.encode();
				iter.set(pkt);
			} else if(aPkt.packetType == 202) {
				RtcpPktSDES pkt = (RtcpPktSDES) aPkt;
				pkt.encode();
				iter.set(pkt);
			} else if(aPkt.packetType == 203) {
				RtcpPktBYE pkt = (RtcpPktBYE) aPkt;
				pkt.encode();
				iter.set(pkt);
			} else if(aPkt.packetType == 204) {
				RtcpPktAPP pkt = (RtcpPktAPP) aPkt;
				pkt.encode();
				iter.set(pkt);
			} else {
				System.out.println("oops");
			}
			
			rawPktSize += aPkt.rawPkt.length;
		} 
		
		if(rawPktSize > 1500) {
			System.out.println("CompRtcpPkt.encode() exceeds 1500 bytes, this is probably not going to work.");
		}
	
		byte[] rawPkt = new byte[rawPktSize];
		
		// Copy the data to the actual rawPkt
		int pos = 0;
		iter = rtcpPkts.listIterator();
		while(iter.hasNext()) {
			RtcpPkt aPkt = (RtcpPkt) iter.next();
			
			if(aPkt.rawPkt == null) {
				System.out.println("is null, packetType " + aPkt.packetType);
			} else {
				System.out.println("cool");
			}
			
			System.arraycopy(aPkt.rawPkt, 0, rawPkt, pos, aPkt.rawPkt.length);
			pos += aPkt.rawPkt.length;
		} 
		
		return rawPkt;
	}
}
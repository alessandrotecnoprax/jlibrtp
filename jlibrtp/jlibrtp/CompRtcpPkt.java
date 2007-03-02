package jlibrtp;

import java.util.*;
import java.net.InetAddress;

public class CompRtcpPkt {
	protected LinkedList<RtcpPkt> rtcpPkts = new LinkedList<RtcpPkt>();
	
	protected CompRtcpPkt() {
		// Will have to add packets directly to rtcpPkts.
	}
	
	protected void addPacket(RtcpPkt aPkt) {
		rtcpPkts.add(aPkt);
	}
	
	protected CompRtcpPkt(byte[] rawPkt, int packetSize, InetAddress adr, ParticipantDatabase partDb) {
		// Chop it up
		int start = 0;
		
		while(start < (packetSize - 32)) {
			int length = StaticProcs.combineBytes(rawPkt[start + 2], rawPkt[start + 3]);
			byte[] tmpBuf = new byte[length];
			int pktType = (int) rawPkt[start + 1];
			System.arraycopy(rawPkt, start, tmpBuf, 0, length);
			
			if(pktType == 200) {
				RtcpPktSR tmpPkt = new RtcpPktSR(tmpBuf);
				if(tmpPkt.check(adr, partDb)) {
					rtcpPkts.add(tmpPkt);
				}
			}
			if(pktType == 201) {
				RtcpPktRR tmpPkt = new RtcpPktRR(tmpBuf, -1);
				if(tmpPkt.check(adr, partDb)) {
					rtcpPkts.add(tmpPkt);
				}
			}
			if(pktType == 202) {
				RtcpPktSDES tmpPkt = new RtcpPktSDES(tmpBuf, partDb);
				if(tmpPkt.check(adr, partDb)) {
					rtcpPkts.add(tmpPkt);
				}
			}
			if(pktType == 203 ) {
				RtcpPktBYE tmpPkt = new RtcpPktBYE(tmpBuf);
				if(tmpPkt.check(adr, partDb)) {
					rtcpPkts.add(tmpPkt);
				}
			}
			if(pktType == 204) {
				RtcpPktAPP tmpPkt = new RtcpPktAPP(tmpBuf);
				if(tmpPkt.check(adr, partDb)) {
					rtcpPkts.add(tmpPkt);
				}
			}
			
			start += length;
		}
	}
	
	protected byte[] encode() {
		// We have to do it every time since we have no control over the packets.
		ListIterator<RtcpPkt>  iter = rtcpPkts.listIterator();
	
		// Encode the packets and find the total size
		int rawPktSize = 0;
		do {
			RtcpPkt aPkt = iter.next();
			aPkt.encode();
			rawPktSize += aPkt.getLength();
		} while(iter.hasNext());
		
		if(rawPktSize > 1500) {
			System.out.println("CompRtcpPkt.encode() exceeds 1500 bytes, this is probably not going to work.");
		}
	
		byte[] rawPkt = new byte[rawPktSize];
		
		// Copy the data to the actual rawPkt
		int pos = 0;
		iter = rtcpPkts.listIterator();
		do {
			RtcpPkt aPkt = iter.next();
			System.arraycopy(aPkt.rawPkt, 0, rawPkt, pos, (1 + 8*aPkt.getLength()));
			pos += aPkt.getLength();
		} while(iter.hasNext());
		
		return rawPkt;
	}
}

package jlibrtp;

import java.util.*;

public class CompRtcpPkt {
	protected LinkedList<RtcpPkt> rtcpPkts = new LinkedList<RtcpPkt>();
	
	protected CompRtcpPkt() {
		// Will have to add packets directly to rtcpPkts.
	}
	
	protected void addPacket(RtcpPkt aPkt) {
		rtcpPkts.add(aPkt);
	}
	
	protected CompRtcpPkt(byte[] rawPkt, ParticipantDatabase partDb) {
		// Chop it up
		int start = 0;
		
		while(start < (rawPkt.length-32)) {
			int length = StaticProcs.combineBytes(rawPkt[start + 2], rawPkt[start + 3]);
			byte[] tmpBuf = new byte[length];
			int pktType = (int) rawPkt[start + 1];
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
		ListIterator<RtcpPkt>  iter = rtcpPkts.listIterator();
	
		int rawPktSize = 0;
		do {
			RtcpPkt aPkt = iter.next();
			rawPktSize += aPkt.getLength();
		} while(iter.hasNext());
		byte[] rawPkt = new byte[rawPktSize];
		
		int pos = 0;
		iter = rtcpPkts.listIterator();
		do {
			RtcpPkt aPkt = iter.next();
			aPkt.encode();
			System.arraycopy(aPkt.rawPkt, 0, rawPkt, pos, (1 + 8*aPkt.getLength()));
			pos += aPkt.getLength();
		} while(iter.hasNext());
		
		return rawPkt;
	}
}

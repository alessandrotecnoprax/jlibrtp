package jlibrtp;

import java.util.*;
import java.net.InetSocketAddress;

public class CompRtcpPkt {
	protected LinkedList rtcpPkts = new LinkedList();
	
	protected CompRtcpPkt() {
		// Will have to add packets directly to rtcpPkts.
		if(RTPSession.rtpDebugLevel > 7) {
			System.out.println("<-> CompRtcpPkt()");
		}
	}
	
	protected void addPacket(RtcpPkt aPkt) {
		if(RTPSession.rtpDebugLevel > 11) {
			System.out.println("  <-> CompRtcpPkt.addPacket( "+ aPkt.getClass() + " )");
		}
		rtcpPkts.add(aPkt);
	}
	
	protected CompRtcpPkt(byte[] rawPkt, int packetSize, InetSocketAddress adr, ParticipantDatabase partDb) {
		if(RTPSession.rtpDebugLevel > 7) {
			System.out.println("-> CompRtcpPkt(" + rawPkt.getClass() + ", size " + packetSize + ", from " + adr.toString() + ", " + partDb.getClass() + ")");
		}
		//System.out.println("rawPkt.length:" + rawPkt.length + " packetSize:" + packetSize);
		
		// Chop it up
		int start = 0;

		while(start < packetSize) {

			int length = 4* (StaticProcs.bytesToUIntInt(rawPkt, start + 2) + 1);
			int pktType = (int) rawPkt[start + 1];
			
			if(pktType < 0) {
				pktType += 256;
			}

			//System.out.println("start: " + start + "   pktType: " + pktType + "  length:" + length );

			if(pktType == 200) {
				addPacket(new RtcpPktSR(rawPkt,start,length));
			} else if(pktType == 201 ) {
				addPacket(new RtcpPktRR(rawPkt,start, -1));
			} else if(pktType == 202) {
				addPacket(new RtcpPktSDES(rawPkt,start, adr, partDb));
			} else if(pktType == 203 ) {
				addPacket(new RtcpPktBYE(rawPkt,start));
			} else if(pktType == 204) {
				addPacket(new RtcpPktAPP(rawPkt,start));
			} else {
				System.out.println("CompRtcpPkt Ooops");
			}
			start += length;
			
			if(RTPSession.rtpDebugLevel > 12) {
				System.out.println("  parsing " + " pktType " + pktType + " length: " + length + " ");
			}
		}
		if(RTPSession.rtpDebugLevel > 7) {
			System.out.println("<- CompRtcpPkt(rawPkt....)");
		}
	}
	
	protected byte[] encode() {
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println(" <- CompRtcpPkt.encode()");
		}
		
		// We have to do it every time since we have no control over the packets.
		ListIterator  iter = rtcpPkts.listIterator();
	
		// Encode the packets and find the total size
		int rawPktSize = 0;
		
		while(iter.hasNext()) {
			RtcpPkt aPkt = (RtcpPkt) iter.next();
			if(aPkt.packetType == 200) {
				RtcpPktSR pkt = (RtcpPktSR) aPkt;
				pkt.encode(null);
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
				System.out.println("CompRtcpPkt aPkt.packetType:" + aPkt.packetType);
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
			}
			
			System.arraycopy(aPkt.rawPkt, 0, rawPkt, pos, aPkt.rawPkt.length);
			pos += aPkt.rawPkt.length;
		} 
		
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println(" -> CompRtcpPkt.encode()");
		}
		return rawPkt;
	}
}
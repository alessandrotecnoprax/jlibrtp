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

			int length = (StaticProcs.bytesToUIntInt(rawPkt, start + 2));
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
				System.out.println("CompRtcpPkt Ooops:" + pktType);
			}
			start += (4 + 4*length);
			
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
		
		ListIterator  iter = rtcpPkts.listIterator();

		byte[] rawPkt = new byte[1500];
		int index = 0;
		
		while(iter.hasNext()) {
			RtcpPkt aPkt = (RtcpPkt) iter.next();
			
			if(aPkt.packetType == 200) {
				RtcpPktSR pkt = (RtcpPktSR) aPkt;
				pkt.encode(null);
				System.arraycopy(pkt.rawPkt, 0, rawPkt, index, pkt.rawPkt.length);
				index += pkt.rawPkt.length;
			} else if(aPkt.packetType == 201 ) {
				RtcpPktRR pkt = (RtcpPktRR) aPkt;
				pkt.encode();
				System.arraycopy(pkt.rawPkt, 0, rawPkt, index, pkt.rawPkt.length);
				index += pkt.rawPkt.length;
			} else if(aPkt.packetType == 202) {
				RtcpPktSDES pkt = (RtcpPktSDES) aPkt;
				pkt.encode();
				System.arraycopy(pkt.rawPkt, 0, rawPkt, index, pkt.rawPkt.length);
				index += pkt.rawPkt.length;
			} else if(aPkt.packetType == 203) {
				RtcpPktBYE pkt = (RtcpPktBYE) aPkt;
				pkt.encode();
				System.arraycopy(pkt.rawPkt, 0, rawPkt, index, pkt.rawPkt.length);
				index += pkt.rawPkt.length;
			} else if(aPkt.packetType == 204) {
				RtcpPktAPP pkt = (RtcpPktAPP) aPkt;
				pkt.encode();
				System.arraycopy(pkt.rawPkt, 0, rawPkt, index, pkt.rawPkt.length);
				index += pkt.rawPkt.length;
			} else {
				System.out.println("CompRtcpPkt aPkt.packetType:" + aPkt.packetType);
			}
			//System.out.println(" packetType:" + aPkt.packetType + " length:" + aPkt.rawPkt.length + " index:" + index);
		} 
		
		byte[] output = new byte[index];
		
		System.arraycopy(rawPkt, 0, output, 0, index);

		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println(" -> CompRtcpPkt.encode()");
		}
		return output;
	}
}
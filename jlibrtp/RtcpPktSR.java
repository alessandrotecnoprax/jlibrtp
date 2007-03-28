package jlibrtp;

public class RtcpPktSR extends RtcpPkt {
	protected long ntpTs1 = -1; //32 bits
	protected long ntpTs2 = -1; //32 bits
	protected long rtpTs = -1; //32 bits
	protected long sendersPktCount = -1; //32 bits
	protected long sendersOctCount = -1; //32 bits
	protected RtcpPktRR rReports = null;
	
	protected RtcpPktSR(long ssrc, long pktCount, long octCount) {
		// Fetch all the right stuff from the database
		super.ssrc = ssrc;
		super.packetType = 200;
		sendersPktCount = pktCount;
		sendersOctCount = octCount;
	}
	
	protected RtcpPktSR(byte[] aRawPkt, int start, int length) {
		if(RTPSession.rtpDebugLevel > 9) {
				System.out.println("  -> RtcpPktSR(rawPkt)");
		}
		
		super.rawPkt = aRawPkt;

		if(!super.parseHeaders(start) || packetType != 200) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktSR.parseHeaders() etc. problem: "+ (!super.parseHeaders(start) ) + " " + packetType + " " + super.length);
			}
			this.problem = 1;
		} else {
			super.ssrc = StaticProcs.bytesToUIntLong(aRawPkt,4+start);
			if(length > 11)
				ntpTs1 = StaticProcs.bytesToUIntLong(aRawPkt,8+start);
			if(length > 15)
				ntpTs2 = StaticProcs.bytesToUIntLong(aRawPkt,12+start);
			if(length > 19)
				rtpTs = StaticProcs.bytesToUIntLong(aRawPkt,16+start);
			if(length > 23)
				sendersPktCount = StaticProcs.bytesToUIntLong(aRawPkt,20+start);
			if(length > 27)
				sendersOctCount = StaticProcs.bytesToUIntLong(aRawPkt,24+start);
			
			// RRs attached?
			if(itemCount > 0) {
				rReports = new RtcpPktRR(rawPkt,start,itemCount);
			}
		}
		
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println("  <- RtcpPktSR(rawPkt)");
		}
	}
	
	protected void encode(RtcpPktRR[] receptionReports) {		
		if(RTPSession.rtpDebugLevel > 9) {
			if(receptionReports != null) {
				System.out.println("  -> RtcpPktSR.encode() receptionReports.length: " + receptionReports.length );
			} else {
				System.out.println("  -> RtcpPktSR.encode() receptionReports: null");
			}
		}
		if(receptionReports != null) {
			super.itemCount = receptionReports.length;
			super.length = 6 + 6*receptionReports.length;
			// Loop over reception reports, figure out their combined size
			super.rawPkt = new byte[28 + 24*receptionReports.length];
			
			for(int i=0; i<receptionReports.length; i++) {
				byte[] recRep = receptionReports[i].encodeRR();
				System.arraycopy(recRep, 0, super.rawPkt, 28 + 24*i, recRep.length);				
			}
			
		} else {
			super.itemCount = 0;
			super.rawPkt = new byte[28];
			super.length = 6;
		}
		//Write the common header
		super.writeHeaders();
		
		// Convert to NTP and chop up
		long timeNow = System.currentTimeMillis();
		ntpTs1 = 2208988800L + (timeNow/1000);
		long ms = timeNow % 1000;
		double tmp = ((double)ms) / 1000.0;
		tmp = tmp * (double)4294967295L;
		ntpTs2 = (long) tmp;
		rtpTs = System.currentTimeMillis();
		
		//Write SR stuff
		byte[] someBytes;
		someBytes = StaticProcs.uIntLongToByteWord(super.ssrc);
		System.arraycopy(someBytes, 0, super.rawPkt, 4, 4);
		someBytes = StaticProcs.uIntLongToByteWord(ntpTs1);
		System.arraycopy(someBytes, 0, super.rawPkt, 8, 4);
		someBytes = StaticProcs.uIntLongToByteWord(ntpTs2);
		System.arraycopy(someBytes, 0, super.rawPkt, 12, 4);
		someBytes = StaticProcs.uIntLongToByteWord(rtpTs);
		System.arraycopy(someBytes, 0, super.rawPkt, 16, 4);
		someBytes = StaticProcs.uIntLongToByteWord(sendersPktCount);
		System.arraycopy(someBytes, 0, super.rawPkt, 20, 4);
		someBytes = StaticProcs.uIntLongToByteWord(sendersOctCount);
		System.arraycopy(someBytes, 0, super.rawPkt, 24, 4);
		
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println("  <- RtcpPktSR.encode() ntpTs1: "
					+ Long.toString(ntpTs1) + " ntpTs2: " + Long.toString(ntpTs2));
		}
	}

	public void debugPrint() {
		System.out.println("RtcpPktSR.debugPrint() ");
			System.out.println("  SSRC:"+super.ssrc +" ntpTs1:"+ntpTs1+" ntpTS2:"+ntpTs2+" rtpTS:"+rtpTs
					+" senderPktCount:"+sendersPktCount+" sendersOctetCount:"+sendersOctCount);
	}
}

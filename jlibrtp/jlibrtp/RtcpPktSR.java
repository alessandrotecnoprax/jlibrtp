package jlibrtp;

public class RtcpPktSR extends RtcpPkt {
	protected long ntpTS1 = -1; //32 bits
	protected long ntpTS2 = -1; //32 bits
	protected long rtpTS = -1; //32 bits
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
	
	protected RtcpPktSR(byte[] aRawPkt) {
		if(RTPSession.rtpDebugLevel > 9) {
				System.out.println("  -> RtcpPktSR(rawPkt)");
		}
		
		super.rawPkt = aRawPkt;

		if(!super.parseHeaders() || packetType != 200 || super.length < 28) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktSR.parseHeaders() etc. problem: "+ (!super.parseHeaders() ) + " " + packetType + " " + super.length);
			}
			this.problem = 1;
		} else {
			super.ssrc = StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
			ntpTS1 = StaticProcs.combineBytes(aRawPkt[8],aRawPkt[9],aRawPkt[10],aRawPkt[11]);
			ntpTS2 = StaticProcs.combineBytes(aRawPkt[12], aRawPkt[13],aRawPkt[14],aRawPkt[15]);
			rtpTS = StaticProcs.combineBytes(aRawPkt[16],aRawPkt[17],aRawPkt[18],aRawPkt[19]);
			sendersPktCount = StaticProcs.combineBytes(aRawPkt[20],aRawPkt[21],aRawPkt[22],aRawPkt[23]);
			sendersOctCount = StaticProcs.combineBytes(aRawPkt[24],aRawPkt[25],aRawPkt[26],aRawPkt[27]);
			
			// RRs attached?
			if(itemCount > 0) {
				rReports = new RtcpPktRR(rawPkt,itemCount);
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
		ntpTS1 = (70*365 + 17)*24*3600 + System.currentTimeMillis()/1000;
		ntpTS2 = System.currentTimeMillis() % 1000;
		rtpTS = System.currentTimeMillis();
		
		//Write SR stuff
		byte[] someBytes;
		someBytes = StaticProcs.longToByteWord(super.ssrc);
		System.arraycopy(someBytes, 0, super.rawPkt, 4, 4);
		someBytes = StaticProcs.longToByteWord(ntpTS1);
		System.arraycopy(someBytes, 0, super.rawPkt, 8, 4);
		someBytes = StaticProcs.longToByteWord(ntpTS2);
		System.arraycopy(someBytes, 0, super.rawPkt, 12, 4);
		someBytes = StaticProcs.longToByteWord(rtpTS);
		System.arraycopy(someBytes, 0, super.rawPkt, 16, 4);
		someBytes = StaticProcs.longToByteWord(sendersPktCount);
		System.arraycopy(someBytes, 0, super.rawPkt, 20, 4);
		someBytes = StaticProcs.longToByteWord(sendersOctCount);
		System.arraycopy(someBytes, 0, super.rawPkt, 24, 4);
		
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println("  <- RtcpPktSR.encode() longs!: ntpTS1: "+ ntpTS1 + " ntpTS2: " + ntpTS2);
		}
	}

	public void debugPrint() {
		System.out.println("RtcpPktSR.debugPrint() ");
			System.out.println("  SSRC:"+super.ssrc +" ntpTS1:"+ntpTS1+" ntpTS2:"+ntpTS2+" rtpTS:"+rtpTS
					+" senderPktCount:"+sendersPktCount+" sendersOctetCount:"+sendersOctCount);
	}
}

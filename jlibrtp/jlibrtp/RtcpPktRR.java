package jlibrtp;

public class RtcpPktRR extends RtcpPkt {
	protected Participant reportees[] = null;
	protected long[] reporteeSsrc = null;// -1; //32 bits
	protected int[] lossFraction = null;//-1; //8 bits
	protected int[] lostPktCount = null;//-1; //24 bits
	protected long[] extHighSeqRecv = null;//-1; //32 bits
	protected long[] interArvJitter = null;//-1; //32 bits
	protected long[] timeStampLSR = null;//-1; //32 bits
	protected long[] delaySR = null;//-1; //32 bits
	
	protected RtcpPktRR(Participant[] reportees, long ssrc) {
		// Fetch all the right stuff from the database
		this.reportees = reportees;
		super.ssrc = ssrc;
		super.packetType = 201;
	}

	// If rcount < 0 we assume we have to parse the entire packet
	// otherwise we'll just parse the body. 
	protected RtcpPktRR(byte[] aRawPkt, int rrCount) {
		rawPkt = aRawPkt;
		
		if(!super.parseHeaders() || packetType != 201 || super.length < 7) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktRR.parseHeaders() etc. problem: "+(!super.parseHeaders())+" "+packetType+" "+super.length);
			}
			this.problem = 1;
		}
		
		int base;
		if(rrCount > 1) {
			base = 28;
		} else {
			base = 8;
			rrCount = itemCount;
			ssrc = StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
		}
		
		reporteeSsrc = new long[rrCount];
		lossFraction = new int[rrCount];
		lostPktCount = new int[rrCount];
		extHighSeqRecv = new long[rrCount];
		interArvJitter = new long[rrCount];
		timeStampLSR = new long[rrCount];
		delaySR = new long[rrCount];
		
		for(int i=0; i<rrCount; i++ ) {
			int pos = base + i*24;
			reporteeSsrc[i] = StaticProcs.combineBytes(aRawPkt[pos],aRawPkt[pos + 1],aRawPkt[pos + 2],aRawPkt[pos + 3]);
			lossFraction[i] = (int) aRawPkt[pos + 4];
			lostPktCount[i] = (int) StaticProcs.combineBytes((byte) 0, aRawPkt[pos + 5],aRawPkt[pos + 6],aRawPkt[pos + 7]);
			extHighSeqRecv[i] = StaticProcs.combineBytes(aRawPkt[pos + 8],aRawPkt[pos + 9],aRawPkt[pos + 10],aRawPkt[pos + 11]);
			interArvJitter[i] = StaticProcs.combineBytes(aRawPkt[pos + 12],aRawPkt[pos + 13],aRawPkt[pos + 14],aRawPkt[pos + 15]);
			timeStampLSR[i] = StaticProcs.combineBytes(aRawPkt[pos + 16],aRawPkt[pos + 17],aRawPkt[pos + 18],aRawPkt[pos + 19]);
			delaySR[i] = StaticProcs.combineBytes(aRawPkt[pos + 20],aRawPkt[pos + 21],aRawPkt[pos + 22],aRawPkt[pos + 24]);
		}
	}
	// Makes a complete packet
	protected void encode() {
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println("  -> RtcpPktRR.encode()");
		}
		super.rawPkt = encodeRR();
		//Write the common header
		super.writeHeaders();
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println("  <- RtcpPktRR.encode()");
		}
	}
	
	// Makes only RR part of packet -> do not include our SSRC
	protected byte[] encodeRR() {
		if(RTPSession.rtpDebugLevel > 10) {
			System.out.println("   -> RtcpPktRR.encodeRR()");
		}
		//assuming we will always create complete reports:
		byte[] ret = new byte[24*reportees.length];
		
		//Write SR stuff
		for(int i = 0; i<reportees.length; i++) {
			int offset = 24*i;
			byte[] someBytes = StaticProcs.longToByteWord(reportees[i].ssrc);
			System.arraycopy(someBytes, 0, ret, offset, 4);
			
			//Cumulative number of packets lost
			someBytes = StaticProcs.longToByteWord(reportees[i].lastSeqNumber - (long) reportees[i].firstSeqNumber);
		
			//Calculate the loss fraction COMPLICATED, WAIT FOR NOW.
			//int lost = 0;
			//int expected = 0;
			someBytes[3] = (byte) 0;
		
			//Write Cumulative number of packets lost and loss fraction to packet:
			System.arraycopy(someBytes, 0, ret, 4 + offset, 4);
		
			// Extended highest sequence received
			someBytes = StaticProcs.longToByteWord(reportees[i].extHighSeqRecv);
			System.arraycopy(someBytes, 0, ret, 8 + offset, 4);
		
			// Interarrival jitter COMPLICATED, WAIT FOR NOW.
			someBytes = StaticProcs.longToByteWord(0);
			System.arraycopy(someBytes, 0, ret, 12 + offset, 4);
		
			// Timestamp last sender report received
			someBytes = StaticProcs.longToByteWord(reportees[i].timeStampLSR);
			System.arraycopy(someBytes, 0, ret, 16 + offset, 4);
		
			// Delay since last sender report received, in terms of 1/655536 s = 0.02 ms
			if(reportees[i].timeReceivedLSR > 0) {
				someBytes = StaticProcs.longToByteWord((System.currentTimeMillis() - reportees[i].timeStampLSR) / (1000*655536));
			} else {
				someBytes = StaticProcs.longToByteWord(0);
			}
			System.arraycopy(someBytes, 0, ret, 20 + offset, 4);
		}
		if(RTPSession.rtpDebugLevel > 10) {
			System.out.println("   <- RtcpPktRR.encodeRR()");
		}
		return ret;
	}
	public void debugPrint() {
		System.out.println("RtcpPktRR.debugPrint() ");
		if(reportees != null) {
			for(int i= 0; i<reportees.length; i++) {
				Participant part = reportees[i];
				System.out.println("     part.ssrc: " + part.ssrc + "  part.cname: " + part.cname);
			}
		} else {
			for(int i=0;i<reporteeSsrc.length; i++) {
				System.out.println("     reporteeSSRC: " + reporteeSsrc[i] + "  timeStampLSR: " + timeStampLSR[i]);
			}
		}

	}
}

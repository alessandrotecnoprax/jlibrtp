package jlibrtp;

public class RtcpPktRR extends RtcpPkt {
	protected Participant reportee = null;
	protected long reporterSsrc = -1;//-1; //32 bits
	protected long[] reporteeSsrc = null;// -1; //32 bits
	protected int[] lossFraction = null;//-1; //8 bits
	protected int[] lostPktCount = null;//-1; //24 bits
	protected long[] extHighSeqRecv = null;//-1; //32 bits
	protected long[] interArvJitter = null;//-1; //32 bits
	protected long[] timeStampLSR = null;//-1; //32 bits
	protected long[] delaySR = null;//-1; //32 bits
	
	protected RtcpPktRR(Participant reporteePart, long ssrc) {
		// Fetch all the right stuff from the database
		reportee = reporteePart;
		reporterSsrc = ssrc;
	}

	// If rcount < 0 we assume we have to parse the entire packet
	// otherwise we'll just parse the body. 
	protected RtcpPktRR(byte[] aRawPkt, int rrCount) {
		rawPkt = aRawPkt;
		
		if(rrCount < 0 && super.parseHeaders() != 0 || packetType != 201 || super.length > 7) {
			//Error...
			this.problem = 1;
		}
		
		int base;
		if(rrCount > 1) {
			base = 28;
		} else {
			base = 8;
			rrCount = itemCount;
			reporterSsrc = StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
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
		packetType = 200;
		
		//Write the common header
		writeHeaders();
		
		rawPkt = encodeRR();
	}
	
	// Makes only RR part of packet -> do not include our SSRC
	protected byte[] encodeRR() {
		//assuming we will always create complete reports:
		byte[] ret = new byte[24];
		
		//Write SR stuff
		
		byte[] someBytes = StaticProcs.longToByteWord(reportee.ssrc);
		System.arraycopy(someBytes, 0, ret, 0, 4);
		
		//Cumulative number of packets lost
		someBytes = StaticProcs.longToByteWord(reportee.lastSeqNumber - (long) reportee.firstSeqNumber);
		
		//Calculate the loss fraction COMPLICATED, WAIT FOR NOW.
		//int lost = 0;
		//int expected = 0;
		someBytes[4] = (byte) 0;
		
		//Write Cumulative number of packets lost and loss fraction to packet:
		System.arraycopy(someBytes, 0, ret, 4, 4);
		
		// Extended highest sequence received
		someBytes = StaticProcs.longToByteWord(reportee.extHighSeqRecv);
		System.arraycopy(someBytes, 0, ret, 8, 4);
		
		// Interarrival jitter COMPLICATED, WAIT FOR NOW.
		someBytes = StaticProcs.longToByteWord(0);
		System.arraycopy(someBytes, 0, ret, 12, 4);
		
		// Timestamp last sender report received
		someBytes = StaticProcs.longToByteWord(reportee.timeStampLSR);
		System.arraycopy(someBytes, 0, ret, 16, 4);
		
		// Delay since last sender report received, in terms of 1/655536 s = 0.02 ms
		if(reportee.timeReceivedLSR > 0) {
			someBytes = StaticProcs.longToByteWord((System.currentTimeMillis() - reportee.timeStampLSR) / (1000*655536));
		} else {
			someBytes = StaticProcs.longToByteWord(0);
		}
		System.arraycopy(someBytes, 0, rawPkt, 20, 4);
		
		return ret;
	}
}

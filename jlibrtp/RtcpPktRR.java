package jlibrtp;

public class RtcpPktRR extends RtcpPkt {
	protected Participant reportee = null;
	protected long reporterSsrc = -1; //32 bits
	protected long reporteeSsrc = -1; //32 bits
	protected int lossFraction = -1; //8 bits
	protected int lostPktCount = -1; //24 bits
	protected long extHighSeqRecv = -1; //32 bits
	protected long interArvJitter = -1; //32 bits
	protected long timeStampLSR = -1; //32 bits
	protected long delaySR = -1; //32 bits
	
	protected RtcpPktRR(Participant reporteePart, long ssrc) {
		// Fetch all the right stuff from the database
		reportee = reporteePart;
		reporterSsrc = ssrc;
	}

	protected RtcpPktRR(byte[] aRawPkt) {
		rawPkt = aRawPkt;

		if(super.parseHeaders() != 0 || packetType != 201 || super.length > 7) {
			//Error...
		} else {

			// We don't really use RC / Item Count
			if(super.length > 0)
				reporterSsrc = 		StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
			if(super.length > 1)
				reporteeSsrc = 		StaticProcs.combineBytes(aRawPkt[8],aRawPkt[9],aRawPkt[10],aRawPkt[11]);
			if(super.length > 2) {
				lossFraction = 	(int) aRawPkt[12];
				lostPktCount = 	(int) StaticProcs.combineBytes((byte) 0, aRawPkt[13],aRawPkt[14],aRawPkt[15]);
			}
			if(super.length > 3)
				extHighSeqRecv = 	StaticProcs.combineBytes(aRawPkt[16],aRawPkt[17],aRawPkt[18],aRawPkt[19]);
			if(super.length > 4)
				interArvJitter = 	StaticProcs.combineBytes(aRawPkt[20],aRawPkt[21],aRawPkt[22],aRawPkt[23]);
			if(super.length > 5)
				timeStampLSR = 		StaticProcs.combineBytes(aRawPkt[24],aRawPkt[25],aRawPkt[26],aRawPkt[27]);
			if(super.length > 6)
				delaySR = 			StaticProcs.combineBytes(aRawPkt[28],aRawPkt[29],aRawPkt[30],aRawPkt[31]);
		}
	}
	// Makes a complete packet
	protected byte[] encode() {
		return new byte[1];
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
		System.arraycopy(someBytes, 0, ret, 4, 4);
		
		//Calculate the loss fraction COMPLICATED, WAIT FOR NOW.
		int lost = 0;
		int expected = 0;
		someBytes[4] = (byte) 0;
		
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

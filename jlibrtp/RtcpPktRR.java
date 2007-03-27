package jlibrtp;

public class RtcpPktRR extends RtcpPkt {
	protected Participant[] reportees = null;
	protected long[] reporteeSsrc = null;// -1; //32 bits
	protected int[] lossFraction = null;//-1; //8 bits
	protected int[] lostPktCount = null;//-1; //24 bits
	protected long[] extHighSeqRecv = null;//-1; //32 bits
	protected long[] interArvJitter = null;//-1; //32 bits
	protected long[] timeStampLSR = null;//-1; //32 bits
	protected long[] delaySR = null;//-1; //32 bits
	
	protected RtcpPktRR(Participant[] reportees, long ssrc) {
		super.packetType = 201;
		// Fetch all the right stuff from the database
		super.ssrc = ssrc;
		this.reportees = reportees;


	}

	// If rcount < 0 we assume we have to parse the entire packet
	// otherwise we'll just parse the body. 
	protected RtcpPktRR(byte[] aRawPkt, int start, int rrCount) {
		super.rawPkt = aRawPkt;
		
		if(!super.parseHeaders(start) || packetType != 201 || super.length < 1) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktRR.parseHeaders() etc. problem: "+(!super.parseHeaders(start))+" "+packetType+" "+super.length);
			}
			this.problem = 1;
		}
		
		int base;
		if(rrCount > 1) {
			base = 28;
		} else {
			base = 8;
			rrCount = super.itemCount;
			super.ssrc = StaticProcs.bytesToUIntLong(aRawPkt, 4);
		}
		
		if(rrCount > 0) {
			reporteeSsrc = new long[rrCount];
			lossFraction = new int[rrCount];
			lostPktCount = new int[rrCount];
			extHighSeqRecv = new long[rrCount];
			interArvJitter = new long[rrCount];
			timeStampLSR = new long[rrCount];
			delaySR = new long[rrCount];

			for(int i=0; i<rrCount; i++ ) {
				int pos = base + i*24;
				reporteeSsrc[i] = StaticProcs.bytesToUIntLong(aRawPkt, pos);
				lossFraction[i] = (int) aRawPkt[pos + 4];
				aRawPkt[pos + 4] = (byte) 0;
				lostPktCount[i] = (int) StaticProcs.bytesToUIntLong(aRawPkt, pos + 4);
				extHighSeqRecv[i] = StaticProcs.bytesToUIntLong(aRawPkt, pos + 8);
				interArvJitter[i] = StaticProcs.bytesToUIntLong(aRawPkt, pos + 12);
				timeStampLSR[i] = StaticProcs.bytesToUIntLong(aRawPkt, pos + 16);
				delaySR[i] = StaticProcs.bytesToUIntLong(aRawPkt, pos + 20);
			}
		}
	}
	// Makes a complete packet
	protected void encode() {
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println("  -> RtcpPktRR.encode()");
		}
		
		//Gather up the actual receiver reports
		byte[] rRs = this.encodeRR();
		if(rRs != null) {
			super.rawPkt = new byte[rRs.length + 8];
			System.arraycopy(rRs, 0, super.rawPkt, 8, rRs.length);
		}
		
		//Set item count?
		super.itemCount = reportees.length;
			
		//Write the common header
		super.writeHeaders();
		
		//Add our SSRC (as sender)
		byte[] someBytes;
		someBytes = StaticProcs.uIntLongToByteWord(super.ssrc);
		System.arraycopy(someBytes, 0, super.rawPkt, 4, 4);
		
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
			byte[] someBytes = StaticProcs.uIntLongToByteWord(reportees[i].ssrc);
			System.arraycopy(someBytes, 0, ret, offset, 4);
			
			//Cumulative number of packets lost
			someBytes = StaticProcs.uIntLongToByteWord(reportees[i].getLostPktCount());
		
			someBytes[0] = (byte) reportees[i].getFractionLost();
		
			//Write Cumulative number of packets lost and loss fraction to packet:
			System.arraycopy(someBytes, 0, ret, 4 + offset, 4);
		
			// Extended highest sequence received
			someBytes = StaticProcs.uIntLongToByteWord(reportees[i].getExtHighSeqRecv());
			System.arraycopy(someBytes, 0, ret, 8 + offset, 4);
		
			// Interarrival jitter
			someBytes = StaticProcs.uIntLongToByteWord((long)reportees[i].interArrivalJitter);
			System.arraycopy(someBytes, 0, ret, 12 + offset, 4);
		
			// Timestamp last sender report received
			someBytes = StaticProcs.uIntLongToByteWord(reportees[i].timeStampLSR);
			System.arraycopy(someBytes, 0, ret, 16 + offset, 4);
		
			// Delay since last sender report received, in terms of 1/655536 s = 0.02 ms
			if(reportees[i].timeReceivedLSR > 0) {
				someBytes = StaticProcs.uIntLongToByteWord(reportees[i].delaySinceLastSR());
			} else {
				someBytes = StaticProcs.uIntLongToByteWord(0);
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

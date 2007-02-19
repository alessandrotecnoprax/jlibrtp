package jlibrtp;

public class RtcpPktSR extends RtcpPkt {
	protected long reporterSsrc = -1; //32 bits
	protected long ntpTS1 = -1; //32 bits
	protected long ntpTS2 = -1; //32 bits
	protected long rtpTS = -1; //32 bits
	protected long sendersPktCount = -1; //32 bits
	protected long sendersOctCount = -1; //32 bits
	
	protected RtcpPktSR(long ssrc, long pktCount, long octCount) {
		// Fetch all the right stuff from the database
		reporterSsrc = ssrc;
		sendersPktCount = pktCount;
		sendersOctCount = octCount;
	}
	
	protected RtcpPktSR(byte[] aRawPkt) {
		rawPkt = aRawPkt;

		if(super.parseHeaders() != 0 || packetType != 200 || super.length > 7) {
			//Error...
		} else {
			// We don't really use RC / Item Count
			if(super.length > 0)
				reporterSsrc = 		StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
			if(super.length > 2) {
				ntpTS1 = 	StaticProcs.combineBytes(aRawPkt[8],aRawPkt[9],aRawPkt[10],aRawPkt[11]);
				ntpTS2 = 	StaticProcs.combineBytes(aRawPkt[12], aRawPkt[13],aRawPkt[14],aRawPkt[15]);
			}
			if(super.length > 3)
				rtpTS= 	StaticProcs.combineBytes(aRawPkt[16],aRawPkt[17],aRawPkt[18],aRawPkt[19]);
			if(super.length > 4)
				sendersPktCount = 	StaticProcs.combineBytes(aRawPkt[20],aRawPkt[21],aRawPkt[22],aRawPkt[23]);
			if(super.length > 5)
				sendersOctCount = 	StaticProcs.combineBytes(aRawPkt[24],aRawPkt[25],aRawPkt[26],aRawPkt[27]);
		}
	}
	
	protected byte[] encode(RtcpPktRR[] receptionReports) {

		packetType = 200;
		
		if(receptionReports != null) {
			itemCount = receptionReports.length;
			length = 6 + 6*receptionReports.length;
			// Loop over reception reports, figure out their combined size
			rawPkt = new byte[28 + 24*receptionReports.length];
			
			for(int i=0; i<receptionReports.length; i++) {
				byte[] recRep = receptionReports[i].encodeRR();
				System.arraycopy(recRep, 0, rawPkt, 28 + 24*i, recRep.length);				
			}
		} else {
			itemCount = 0;
			rawPkt = new byte[28];
			length = 6;
		}
		//Write the common header
		writeHeaders();
		
		// Convert to NTP and chop up
		ntpTS1 = (70*365 + 17)*24*3600 + System.currentTimeMillis()/1000;
		ntpTS2 = System.currentTimeMillis() % 1000;
		rtpTS = System.currentTimeMillis();
		
		//Write SR stuff
		byte[] someBytes = StaticProcs.longToByteWord(reporterSsrc);
		System.arraycopy(someBytes, 0, rawPkt, 4, 4);
		someBytes = StaticProcs.longToByteWord(ntpTS1);
		System.arraycopy(someBytes, 0, rawPkt, 8, 4);
		someBytes = StaticProcs.longToByteWord(ntpTS2);
		System.arraycopy(someBytes, 0, rawPkt, 12, 4);
		someBytes = StaticProcs.longToByteWord(rtpTS);
		System.arraycopy(someBytes, 0, rawPkt, 16, 4);
		someBytes = StaticProcs.longToByteWord(sendersPktCount);
		System.arraycopy(someBytes, 0, rawPkt, 20, 4);
		someBytes = StaticProcs.longToByteWord(sendersOctCount);
		System.arraycopy(someBytes, 0, rawPkt, 24, 4);
		
		return rawPkt;
	}
}

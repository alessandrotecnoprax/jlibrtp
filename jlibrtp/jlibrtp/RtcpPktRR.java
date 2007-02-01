package jlibrtp;

public class RtcpPktRR extends RtcpPkt {
	
	protected long reporterSsrc = -1; //32 bits
	protected long reporteeSsrc = -1; //32 bits
	protected int lossFraction = -1; //
	protected int cumPacketsLost = -1;
	protected long extHighSeqRecv = -1;
	protected long interArvJitter = -1;
	protected long timeStampLSR = -1;
	protected long delaySR = -1;
	
	protected RtcpPktRR(reporteeSsrc) {
		// Fetch all the right stuff from the database
		
		
	}
	
	protected RtcpPktRR(byte[] aRawPkt) {
		rawPkt = aRawPkt;
		
		byte[] header = new byte[4];
		System.arraycopy(aRawPkt, 0, header, 0, 4);
		
		if(super.parseHeaders() != 0 || packetType != 201) {
			//Error...
		}
	
		reporterSsrc = 		StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
		reporteeSsrc = 		StaticProcs.combineBytes(aRawPkt[8],aRawPkt[9],aRawPkt[10],aRawPkt[11]);
		lossFraction = 		(int) aRawPkt[12];
		cumPacketsLost = 	(int) StaticProcs.combineBytes((byte) 0, aRawPkt[13],aRawPkt[14],aRawPkt[15]);
		extHighSeqRecv = 	StaticProcs.combineBytes(aRawPkt[16],aRawPkt[17],aRawPkt[18],aRawPkt[19]);
		interArvJitter = 	StaticProcs.combineBytes(aRawPkt[20],aRawPkt[21],aRawPkt[22],aRawPkt[23]);
		timeStampLSR = 		StaticProcs.combineBytes(aRawPkt[24],aRawPkt[25],aRawPkt[26],aRawPkt[27]);
		delaySR = 			StaticProcs.combineBytes(aRawPkt[28],aRawPkt[29],aRawPkt[30],aRawPkt[31]);
	}
	
	protected byte[] encode() {
		
	}
}

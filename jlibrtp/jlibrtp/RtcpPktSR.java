package jlibrtp;

public class RtcpPktSR extends RtcpPkt {
	protected long reporterSsrc = -1; //32 bits
	protected long ntpTS1 = -1; //32 bits
	protected long ntpTS2 = -1; //32 bits
	protected long ntpTS3 = -1; //32 bits
	protected long rtpTS = -1; //32 bits
	protected long sendersPktCount = -1; //32 bits
	protected long sendersOctCount = -1; //32 bits
	
	protected RtcpPktSR() {
		// Fetch all the right stuff from the database
		
		
	}
	
	protected RtcpPktSR(byte[] aRawPkt) {
		rawPkt = aRawPkt;

		//byte[] header = new byte[4];
		//System.arraycopy(aRawPkt, 0, header, 0, 4);

		if(super.parseHeaders() != 0 || packetType != 200 || super.length > 7) {
			//Error...
		} else {
			// We don't really use RC / Item Count
			if(super.length > 0)
				reporterSsrc = 		StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
			if(super.length > 3) {
				ntpTS1 = 	StaticProcs.combineBytes(aRawPkt[8],aRawPkt[9],aRawPkt[10],aRawPkt[11]);
				ntpTS2 = 	StaticProcs.combineBytes(aRawPkt[12], aRawPkt[13],aRawPkt[14],aRawPkt[15]);
				ntpTS3 = 	StaticProcs.combineBytes(aRawPkt[16],aRawPkt[17],aRawPkt[18],aRawPkt[19]);
			}
			if(super.length > 4)
				rtpTS= 	StaticProcs.combineBytes(aRawPkt[20],aRawPkt[21],aRawPkt[22],aRawPkt[23]);
			if(super.length > 5)
				sendersPktCount = 	StaticProcs.combineBytes(aRawPkt[24],aRawPkt[25],aRawPkt[26],aRawPkt[27]);
			if(super.length > 6)
				sendersOctCount = 	StaticProcs.combineBytes(aRawPkt[28],aRawPkt[29],aRawPkt[30],aRawPkt[31]);
		}
	}
	
	protected byte[] encode() {
		
		return new byte[1];
	}
}

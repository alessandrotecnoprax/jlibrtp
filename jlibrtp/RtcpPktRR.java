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
	
	
	protected RtcpPktRR(byte[] aRawPkt) {
		rawPkt = aRawPkt;
		
		byte[] header = new byte[4];
		System.arraycopy(aRawPkt, 0, header, 0, 4);
		
		if(super.parseHeaders() != 0 || packetType != 201) {
			//Error...
		}
		
		
		
		
	}
}

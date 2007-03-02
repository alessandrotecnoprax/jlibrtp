package jlibrtp;

public class RtcpPktAPP extends RtcpPkt {
	private long reporterSsrc = -1;
	private byte[] pktName = null;
	private byte[] pktData = null;
	
	protected RtcpPktAPP(byte[] aRawPkt) {
		reporterSsrc = 	StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
		
		if(super.parseHeaders() != 0 || packetType != 204) {
			//Error...
			this.problem = 1;
		} else {
			reporterSsrc = StaticProcs.combineBytes(aRawPkt[4],aRawPkt[5],aRawPkt[6],aRawPkt[7]);
			if(super.length > 11) {
				pktName = new byte[4];
				System.arraycopy(aRawPkt, 8, pktName, 0, 4);
			}
			if(super.length > 12) {
				pktData = new byte[super.length - 12];
				System.arraycopy(pktData, 12, pktData, 0, (super.length - 12));
			}
		}
	}
	
	protected void encode() {	
	
	}
}

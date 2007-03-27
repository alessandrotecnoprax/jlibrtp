package jlibrtp;

public class RtcpPktAPP extends RtcpPkt {
	private long reporterSsrc = -1;
	private byte[] pktName = null;
	private byte[] pktData = null;
	
	protected RtcpPktAPP(byte[] aRawPkt) {
		reporterSsrc = StaticProcs.bytesToUIntLong(aRawPkt,4);
		
		if(!super.parseHeaders() || packetType != 204) {
			//Error...
			this.problem = 1;
		} else {
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
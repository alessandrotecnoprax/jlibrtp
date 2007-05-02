package jlibrtp;

public class RtcpPktAPP extends RtcpPkt {
	protected byte[] pktName = null;
	protected byte[] pktData = null;

	//subtype = super.itemcount
	
	protected RtcpPktAPP(long ssrc, int subtype, byte[] pktName, byte[] pktData) {
		// Fetch all the right stuff from the database
		super.ssrc = ssrc;
		super.packetType = 204;
		super.itemCount = subtype;
		this.pktName = pktName;
		this.pktData = pktData;
	}
	
	
	protected RtcpPktAPP(byte[] aRawPkt, int start) {
		super.ssrc = StaticProcs.bytesToUIntLong(aRawPkt,4);
		
		if(!super.parseHeaders(start) || packetType != 204 ) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktAPP.parseHeaders() etc. problem");
			}
			super.problem = -204;
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
		super.rawPkt = new byte[12 + this.pktData.length];
		byte[] tmp = StaticProcs.uIntLongToByteWord(super.ssrc);
		System.arraycopy(tmp, 0, super.rawPkt, 4, 4);
		System.arraycopy(this.pktName, 0, super.rawPkt, 8, 4);
		System.arraycopy(this.pktData, 0, super.rawPkt, 12, this.pktData.length);
		writeHeaders();
	}
}

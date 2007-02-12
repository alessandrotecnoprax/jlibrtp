package jlibrtp;

public class RtcpPktBYE extends RtcpPkt {
	private long[] ssrcArray = null;//32xn bits, n<16
	byte[] reason = null;
	
	protected RtcpPktBYE() {
		// Fetch all the right stuff from the database
		
		
	}
	
	protected RtcpPktBYE(byte[] aRawPkt) {
		rawPkt = aRawPkt;

		//byte[] header = new byte[4];
		//System.arraycopy(aRawPkt, 0, header, 0, 4);

		if(super.parseHeaders() != 0 || packetType != 203) {
			//Error...
		} else {
			ssrcArray = new long[super.itemCount];
			
			for(int i=0; i<super.itemCount; i++) {
				ssrcArray[i] = StaticProcs.combineBytes(aRawPkt[i*4 + 4],aRawPkt[i*4 + 5],aRawPkt[i*4 + 6],aRawPkt[i*4 + 7]);
			}
			if(super.length > super.itemCount) {
				int reasonLength = (int) aRawPkt[super.itemCount*4];
				reason = new byte[reasonLength];
				System.arraycopy(aRawPkt, super.itemCount*4 + 1, reason, 0, reasonLength);
			}
		}
	}
	
	protected byte[] encode() {	
		return new byte[1];
	}
}
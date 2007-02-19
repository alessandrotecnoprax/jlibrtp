package jlibrtp;

public class RtcpPktBYE extends RtcpPkt {
	private long[] ssrcArray = null;//32xn bits, n<16
	byte[] reason = null;
	
	protected RtcpPktBYE(long[] ssrcs,byte[] aReason) {
		// Fetch all the right stuff from the database
		reason = aReason;
		ssrcArray = ssrcs;
		if(ssrcs.length < 1) {
			System.out.println("RtcpBYE.RtcpPktBYE(long[] ssrcs, byte[] aReason) requires at least one SSRC!");
		}
	}
	
	protected RtcpPktBYE(byte[] aRawPkt) {
		rawPkt = aRawPkt;

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
		packetType = 203;
		
		itemCount = ssrcArray.length;
		length = 4*ssrcArray.length;
		
		if(reason != null) {
			length += (reason.length + 1)/4;
			if((reason.length + 1) % 4 != 0) {
				length +=1;
			}
		}
		rawPkt = new byte[length*4 + 4];
		
		int i;
		byte[] someBytes;
		
		// SSRCs
		for(i=0; i<ssrcArray.length; i++ ) {
			someBytes = StaticProcs.longToByteWord(ssrcArray[i]);
			System.arraycopy(someBytes, 0, rawPkt, 4 + 4*i, 4);			
		}
		
		// Reason for leaving
		if(reason != null) {
			rawPkt[8+4*i] = (byte) reason.length;
		}
		
		System.arraycopy(reason, 0, rawPkt, 9+4*i, reason.length);			
		
		return rawPkt;
	}
}
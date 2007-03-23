package jlibrtp;

public class RtcpPktBYE extends RtcpPkt {
	protected long[] ssrcArray = null;//32xn bits, n<16
	protected byte[] reason = null;
	
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

		if(!super.parseHeaders() || packetType != 203) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktBYE.parseHeaders() etc. problem");
			}
			this.problem = 1;
		} else {
			ssrcArray = new long[super.itemCount];
			
			for(int i=0; i<super.itemCount; i++) {
				ssrcArray[i] = StaticProcs.combineBytes(aRawPkt[i*4 + 4],aRawPkt[i*4 + 5],aRawPkt[i*4 + 6],aRawPkt[i*4 + 7]);
			}
			if(super.length > (super.itemCount + 1)) {
				int reasonLength = (int) aRawPkt[4 + super.itemCount*4];
				//System.out.println("super.itemCount:"+super.itemCount+" reasonLength:"+reasonLength+" start:"+(super.itemCount*4 + 4 + 1));
				reason = new byte[reasonLength];
				System.arraycopy(aRawPkt, super.itemCount*4 + 4 + 1, reason, 0, reasonLength);
				//System.out.println("test:" + new String(reason));
			}
		}
	}
	
	protected void encode() {	
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
			//System.out.println("Writing to:"+(4+4*ssrcArray.length)+ " reason.length:"+reason.length );
			rawPkt[(4 + 4*ssrcArray.length)] = (byte) reason.length;
			System.arraycopy(reason, 0, rawPkt, 4+4*i +1, reason.length);		
		}
		
		super.writeHeaders();
	}
	
	public void debugPrint() {
		System.out.println("RtcpPktBYE.debugPrint() ");
		if(ssrcArray != null) {
			for(int i= 0; i<ssrcArray.length; i++) {
				long anSsrc = ssrcArray[i];
				System.out.println("     ssrc: " + anSsrc);
			}
		}
		if(reason != null) {
			System.out.println("     Reason: " + new String(reason));
		}
	}
}
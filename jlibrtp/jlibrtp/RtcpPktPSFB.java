package jlibrtp;

//import java.util.Vector;

public class RtcpPktPSFB extends RtcpPkt {
	private long ssrcPacketSender = -1;
	private long ssrcMediaSource = -1;
	
	// Slice Loss Indication (SLI)
	protected int[] sliFirst;
	protected int[] sliNumber;
	protected int[] sliPictureId;
	
	// Picture loss indication
	
	// Reference Picture Selection Indication (RPSI)
	protected int rpsiPadding = -1;
	protected int rpsiPayloadType = -1;
	protected byte[] rpsiBitString;
	
	// Application Layer Feedback Messages
	protected byte[] alfBitString;
	
	protected RtcpPktPSFB(long ssrcPacketSender, long ssrcMediaSource, int FMT) {
		super.packetType = 206; //PSFB
		super.itemCount = FMT; 
	}
	
	protected RtcpPktPSFB(byte[] aRawPkt, int start) {		
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  -> RtcpPktPSFB(byte[], int start)");
		}
		
		rawPkt = aRawPkt;

		if(! super.parseHeaders(start) || packetType != 206 || super.length < 2 ) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktRTPFB.parseHeaders() etc. problem");
			}
			super.problem = -206;
		} else {
			//FMT = super.itemCount;
			ssrcPacketSender = StaticProcs.bytesToUIntLong(aRawPkt,4+start);
			ssrcMediaSource = StaticProcs.bytesToUIntLong(aRawPkt,8+start);
			
			switch(super.itemCount) {
				case 1: // Picture Loss Indication 
					decPictureLossIndic();
				break; 
				case 2: // Slice Loss Indication
					decSliceLossIndic(aRawPkt, start + 12); 
				break;
				case 3: // Reference Picture Selection Indication 
					decRefPictureSelIndic(aRawPkt, start + 12); 
				break;
				case 15: // Application Layer Feedback Messages
					decAppLayerFB(aRawPkt, start + 12); 
				break;
				default: 
					System.out.println("!!!! RtcpPktPSFB(byte[], int start) unexpected FMT " + super.itemCount);
			}
		}
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  <- RtcpPktPSFB()");
		}
	}
	
	private void decPictureLossIndic() {
		// What do we do now?
	}
	
	private void decSliceLossIndic(byte[] aRawPkt, int start) {	
		// 13 bit off-boundary numbers? That's rather cruel
		//    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		//   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		//   |            First        |        Number           | PictureID |
		//   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
				
		int count = super.length - 2;
		
		sliFirst = new int[count];
		sliNumber = new int[count];
		sliPictureId = new int[count];
		
		// Loop over the FCI lines
		for(int i=0; i < count; i++) {
			sliFirst[i] = StaticProcs.bytesToUIntInt(aRawPkt, start) >> 3;
			sliNumber[i] = (int) (StaticProcs.bytesToUIntInt(aRawPkt, start) & 0x0007FFC0) >> 6;
			sliPictureId[i] = (StaticProcs.bytesToUIntInt(aRawPkt, start + 2) & 0x003F);
			start += 4;
		}
		
	}
	private void decRefPictureSelIndic(byte[] aRawPkt, int start) {	
		//  0                   1                   2                   3
		//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		// |      PB       |0| Payload Type|    Native RPSI bit string     |
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		// |   defined per codec          ...                | Padding (0) |
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		
		//Minimize the padding
		int paddingBits = aRawPkt[start];
		int paddingBytes = paddingBits/8;
		rpsiPadding = paddingBits - 8*paddingBytes;
		
		if(paddingBits > 32) {
			System.out.println("!!!! RtcpPktPSFB.decRefPictureSelcIndic paddingBits: " 
					+ paddingBits);
		}
		
		rpsiPayloadType = (int) rawPkt[start];
		if(rpsiPayloadType < 0) {
			System.out.println("!!!! RtcpPktPSFB.decRefPictureSelcIndic 8th bit not zero: " 
					+ rpsiPayloadType);
		}
		
		//
		int stringLength = (super.length - 2)*4 - 2 - paddingBytes;
		rpsiBitString = new byte[stringLength];
		System.arraycopy(aRawPkt, start + 2, rpsiBitString, 0, stringLength);
		
	}
	
	private void decAppLayerFB(byte[] aRawPkt, int start) {	
		//Application Message (FCI): variable length
		int stringLength = (super.length - 2)*4;
		
		alfBitString = new byte[stringLength];
		
		System.arraycopy(aRawPkt, start, alfBitString, 0, stringLength);
		//Accumulate and send to application
	}
	
	protected void encode() {
		//super.rawPkt = new byte[12 + this.PID.length*4];
		byte[] someBytes = StaticProcs.uIntLongToByteWord(this.ssrcPacketSender);
		System.arraycopy(someBytes, 0, super.rawPkt, 4, 4);
		someBytes = StaticProcs.uIntLongToByteWord(this.ssrcMediaSource);
		System.arraycopy(someBytes, 0, super.rawPkt, 8, 4);
		
		writeHeaders();
	}
	
	protected int getFMT() {
		return this.itemCount;
	}
	
	public void debugPrint() {
		System.out.println("->RtcpPktPSFB.debugPrint() ");
		
		System.out.println("<-RtcpPktPSFB.debugPrint() ");
	}
}

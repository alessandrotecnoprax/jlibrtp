package jlibrtp;

/**
 * RTCP packets for Payload-Specific Feedback Messages 
 * 
 * @author Arne Kepp
 */
public class RtcpPktPSFB extends RtcpPkt {
	protected boolean notRelevant = false;
	private RTPSession rtpSession;
	protected long ssrcPacketSender = -1;
	protected long ssrcMediaSource = -1;
	
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
	
	/**
	 * Generic constructor, then call make<something>
	 * 
	 * @param ssrcPacketSender
	 * @param ssrcMediaSource
	 */
	protected RtcpPktPSFB(long ssrcPacketSender, long ssrcMediaSource) {
		this.ssrcPacketSender = ssrcPacketSender;
		this.ssrcMediaSource = ssrcMediaSource;
		super.packetType = 206; //PSFB
	}
	
	/**
	 * Make this packet a Picture loss indication
	 */
	protected void makePictureLossIndication() {
		super.itemCount = 1; //FMT
	}
	
	/**
	 * Make this packet a Slice Loss Indication
	 * 
	 * @param sliFirst macroblock (MB) address of the first lost macroblock
	 * @param sliNumber number of lost macroblocks
	 * @param sliPictureId six least significant bits of the codec-specific identifier
	 */
	protected void makeSliceLossIndication(int[] sliFirst, int[] sliNumber, int[] sliPictureId) {
		super.itemCount = 2; //FMT
		this.sliFirst = sliFirst;
		this.sliNumber = sliNumber;
		this.sliPictureId = sliPictureId;
	}
	
	/**
	 * Make this packet a Reference Picture Selection Indication
	 * 
	 * @param bitPadding number of padded bits at end of bitString
	 * @param payloadType RTP payload type for codec
	 * @param bitString RPSI information as natively defined by the video codec
	 */
	protected void makeRefPictureSelIndic(int bitPadding, int payloadType, byte[] bitString) {
		super.itemCount = 3; //FMT
		this.rpsiPadding = bitPadding;
		this.rpsiPayloadType = payloadType;
		this.rpsiBitString = bitString;
	}
	
	/** 
	 * Make this packet a Reference Picture Selection Indication
	 * 
	 * @param bitString the original application message
	 */
	protected void makeAppLayerFeedback(byte[] bitString) {
		super.itemCount = 15; //FMT
		this.alfBitString = bitString; 
	}
	
	/**
	 * Constructor that parses a raw packet to retrieve information
	 * 
	 * @param aRawPkt the raw packet to be parsed
	 * @param start the start of the packet, in bytes
	 * @param rtpSession the session on which the callback interface resides
	 */
	protected RtcpPktPSFB(byte[] aRawPkt, int start, RTPSession rtpSession) {		
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  -> RtcpPktPSFB(byte[], int start)");
		}
		this.rtpSession = rtpSession;
		
		rawPkt = aRawPkt;

		if(! super.parseHeaders(start) || packetType != 206 || super.length < 2 ) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktRTPFB.parseHeaders() etc. problem");
			}
			super.problem = -206;
		} else {
			//FMT = super.itemCount;
			ssrcMediaSource = StaticProcs.bytesToUIntLong(aRawPkt,8+start);
			
			if(ssrcMediaSource == rtpSession.ssrc) {
				ssrcPacketSender = StaticProcs.bytesToUIntLong(aRawPkt,4+start);
				
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
			} else {
				this.notRelevant = true;
			}
		}
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  <- RtcpPktPSFB()");
		}
	}
	
	/**
	 * Decode Picture Loss indication
	 *
	 */
	private void decPictureLossIndic() {
		if(this.rtpSession.rtcpAVPFIntf != null) {
			this.rtpSession.rtcpAVPFIntf.PSFBPktPictureLossReceived(
					this.ssrcPacketSender);
		}
	}
	
	/**
	 * Decode Slice Loss Indication
	 * 
	 * @param aRawPkt
	 * @param start
	 */
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
		
		if(this.rtpSession.rtcpAVPFIntf != null) {
			this.rtpSession.rtcpAVPFIntf.PSFBPktSliceLossIndic(
					ssrcPacketSender,
					sliFirst, sliNumber, sliPictureId);
		}
	}
	
	/**
	 * Decode Reference Picture Selection Indication
	 * 
	 * @param aRawPkt
	 * @param start
	 */
	private void decRefPictureSelIndic(byte[] aRawPkt, int start) {	
		//  0                   1                   2                   3
		//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		// |      PB       |0| Payload Type|    Native RPSI bit string     |
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		// |   defined per codec          ...                | Padding (0) |
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		
		rpsiPadding = aRawPkt[start];
		
		if(rpsiPadding  > 32) {
			System.out.println("!!!! RtcpPktPSFB.decRefPictureSelcIndic paddingBits: " 
					+ rpsiPadding);
		}
		
		rpsiPayloadType = (int) rawPkt[start];
		if(rpsiPayloadType < 0) {
			System.out.println("!!!! RtcpPktPSFB.decRefPictureSelcIndic 8th bit not zero: " 
					+ rpsiPayloadType);
		}
		
		rpsiBitString = new byte[(super.length - 2)*4 - 2];
		System.arraycopy(aRawPkt, start + 2, rpsiBitString, 0, rpsiBitString.length);
		
		if(this.rtpSession.rtcpAVPFIntf != null) {
			this.rtpSession.rtcpAVPFIntf.PSFBPktRefPictureSelIndic(
					ssrcPacketSender,
					rpsiPayloadType, rpsiBitString, rpsiPadding);
		}
		
	}
	
	/**
	 * Decode Application specific feedback message
	 * 
	 * @param aRawPkt
	 * @param start
	 */
	private void decAppLayerFB(byte[] aRawPkt, int start) {	
		//Application Message (FCI): variable length
		int stringLength = (super.length - 2)*4;
		
		alfBitString = new byte[stringLength];
		
		System.arraycopy(aRawPkt, start, alfBitString, 0, stringLength);

		if(this.rtpSession.rtcpAVPFIntf != null) {
			this.rtpSession.rtcpAVPFIntf.PSFBPktAppLayerFBReceived(
					ssrcPacketSender, alfBitString);
		}
	}
	

	
	/**
	 * Encode a Slice Loss Indication
	 */
	private void encSliceLossIndic() {
		byte[] firstBytes;
		byte[] numbBytes;
		byte[] picBytes;
		
		int offset = 8;
		// Loop over the FCI lines
		for(int i=0; i < sliFirst.length; i++) {
			offset = 8 + 8*i;
			firstBytes = StaticProcs.uIntLongToByteWord(sliFirst[i] << 3);
			numbBytes = StaticProcs.uIntLongToByteWord(sliNumber[i] << 2);
			picBytes = StaticProcs.uIntIntToByteWord(sliPictureId[i]);
			
			super.rawPkt[offset] = firstBytes[2];
			super.rawPkt[offset+1] = (byte) (firstBytes[3] | numbBytes[2]);
			super.rawPkt[offset+2] = numbBytes[3];
			super.rawPkt[offset+3] = (byte) (numbBytes[3] | picBytes[1]);
		}
	}
	
	/**
	 * Encode a Reference Picture Selection Indication
	 *
	 */
	private void encRefPictureSelIndic() {	
		byte[] someBytes;
		someBytes = StaticProcs.uIntIntToByteWord(rpsiPadding);
		super.rawPkt[8] = someBytes[1];
		someBytes = StaticProcs.uIntIntToByteWord(rpsiPayloadType);
		super.rawPkt[9] = someBytes[1];
		
		System.arraycopy(rpsiBitString, 0, super.rawPkt, 10, rpsiBitString.length);
	}
	
	
	/**
	 * Encode Application Layer Feedback
	 *
	 */
	private void encAppLayerFB() {	
		//Application Message (FCI): variable length
		System.arraycopy(alfBitString, 0, super.rawPkt, 8, alfBitString.length);
	}
	
	/** 
	 * Get the FMT (Feedback Message Type)
	 * @return value stored in .itemcount, same field
	 */
	protected int getFMT() {
		return this.itemCount;
	}
	
	/**
	 * Encode the packet into a byte[], saved in .rawPkt
	 * 
	 * CompRtcpPkt will call this automatically
	 */
	protected void encode() {
		switch(super.itemCount) {
		case 1: // Picture Loss Indication 
			//Nothing to do really
			super.rawPkt = new byte[24];
			break; 
		case 2: // Slice Loss Indication
			super.rawPkt = new byte[24 + 4*this.sliFirst.length];
			encSliceLossIndic(); 
			break;
		case 3: // Reference Picture Selection Indication
			super.rawPkt = new byte[24 + 2 + this.rpsiBitString.length/4];
			encRefPictureSelIndic();
			break;
		case 15: // Application Layer Feedback Messages
			super.rawPkt = new byte[24 + this.alfBitString.length/4];
			encAppLayerFB();
			break;
		}
		
		byte[] someBytes = StaticProcs.uIntLongToByteWord(this.ssrcPacketSender);
		System.arraycopy(someBytes, 0, super.rawPkt, 4, 4);
		someBytes = StaticProcs.uIntLongToByteWord(this.ssrcMediaSource);
		System.arraycopy(someBytes, 0, super.rawPkt, 8, 4);
		
		writeHeaders();
	}

	/**
	 * Debug purposes only
	 */
	public void debugPrint() {
		System.out.println("->RtcpPktPSFB.debugPrint() ");
		
		String str;
		switch(super.itemCount) {
		case 1: // Picture Loss Indication 
			System.out.println("  FMT: Picture Loss Indication");
			break; 
		case 2: // Slice Loss Indication
			if(sliFirst != null) {
				str = "sliFirst[].length: " + sliFirst.length;
			} else {
				str = "sliFirst[] is null";
			}
			System.out.println("  FMT: Slice Loss Indication, " + str);
			break;
		case 3: // Reference Picture Selection Indication
			if(rpsiBitString != null) {
				str = "rpsiBitString[].length: " + rpsiBitString.length;
			} else {
				str = "rpsiBitString[] is null";
			}
			System.out.println("  FMT: Reference Picture Selection Indication, " 
					+ str + " payloadType: " + this.rpsiPayloadType);
			break;
		case 15: // Application Layer Feedback Messages
			if(alfBitString != null) {
				str = "alfBitString[].length: " + alfBitString.length;
			} else {
				str = "alfBitString[] is null";
			}
			System.out.println("  FMT: Application Layer Feedback Messages, " + str);
			break;
		}

		System.out.println("<-RtcpPktPSFB.debugPrint() ");
	}
}
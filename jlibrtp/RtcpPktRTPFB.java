package jlibrtp;

/**
 * RTCP packets for RTP Feedback Messages 
 * 
 * In line with RFC 4585, this packet currently only supports NACKs
 * 
 * @author Arne Kepp
 */
public class RtcpPktRTPFB extends RtcpPkt {
	public boolean notRelevant = false;
	protected long ssrcPacketSender = -1;
	protected long ssrcMediaSource = -1;
	protected int PID[];
	protected int BLP[];
	
	/**
	 * Constructor for RTP Feedback Message
	 * 
	 * @param ssrcPacketSender SSRC of sender, taken from RTPSession
	 * @param ssrcMediaSource SSRC of recipient of this message
	 * @param FMT the Feedback Message Subtype
	 * @param PID RTP sequence numbers of lost packets
	 * @param BLP bitmask of following lost packets, shared index with PID 
	 */
	protected RtcpPktRTPFB(long ssrcPacketSender, long ssrcMediaSource, int FMT, int[] PID, int[] BLP) {
		super.packetType = 205; //RTPFB
		super.itemCount = FMT; 
		this.PID = PID;
		this.BLP = BLP;
	}
	
	/**
	 * Constructor that parses a raw packet to retrieve information
	 * 
	 * @param aRawPkt the raw packet to be parsed
	 * @param start the start of the packet, in bytes
	 * @param rtpSession the session on which the callback interface resides
	 */
	protected RtcpPktRTPFB(byte[] aRawPkt, int start, RTPSession rtpSession) {		
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  -> RtcpPktRTPFB(byte[], int start)");
		}
		
		rawPkt = aRawPkt;

		if(! super.parseHeaders(start) || packetType != 205 || super.length < 2) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktRTPFB.parseHeaders() etc. problem");
			}
			super.problem = -205;
		} else {
			//FMT = super.itemCount;
			
			ssrcMediaSource = StaticProcs.bytesToUIntLong(aRawPkt,8+start);
			
			if(ssrcMediaSource == rtpSession.ssrc) {
				ssrcPacketSender = StaticProcs.bytesToUIntLong(aRawPkt,4+start);
				int loopStop = super.length - 2;
				PID = new int[loopStop];
				BLP = new int[loopStop];
				int curStart = 12;

				// Loop over Feedback Control Information (FCI) fields
				for(int i=0; i< loopStop; i++) {
					PID[i] = StaticProcs.bytesToUIntInt(aRawPkt, curStart);
					BLP[i] = StaticProcs.bytesToUIntInt(aRawPkt, curStart + 2);
					curStart += 4;
				}

				rtpSession.rtcpAVPFIntf.RTPFBPktReceived(
						ssrcPacketSender, super.itemCount, PID, BLP);
			}
		}
		

		
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  <- RtcpPktRTPFB()");
		}
	}
	
	/**
	 * Encode the packet into a byte[], saved in .rawPkt
	 * 
	 * CompRtcpPkt will call this automatically
	 */
	protected void encode() {
		super.rawPkt = new byte[12 + this.PID.length*4];
		
		byte[] someBytes = StaticProcs.uIntLongToByteWord(this.ssrcPacketSender);
		System.arraycopy(someBytes, 0, super.rawPkt, 4, 4);
		someBytes = StaticProcs.uIntLongToByteWord(this.ssrcMediaSource);
		System.arraycopy(someBytes, 0, super.rawPkt, 8, 4);
		
		// Loop over Feedback Control Information (FCI) fields
		int curStart = 12;
		for(int i=0; i < this.PID.length; i++ ) {
			someBytes = StaticProcs.uIntIntToByteWord(PID[i]);
			super.rawPkt[curStart++] = someBytes[0];
			super.rawPkt[curStart++] = someBytes[1];
			someBytes = StaticProcs.uIntIntToByteWord(BLP[i]);
			super.rawPkt[curStart++] = someBytes[0];
			super.rawPkt[curStart++] = someBytes[1];
		}
		writeHeaders();
	}
	
	/** 
	 * Get the FMT (Feedback Message Type)
	 * @return value stored in .itemcount, same field
	 */
	protected int getFMT() {
		return this.itemCount;
	}
	
	/**
	 * Debug purposes only
	 */
	public void debugPrint() {
		System.out.println("->RtcpPktRTPFB.debugPrint() ");
		System.out.println("  ssrcPacketSender: " + ssrcPacketSender + "  ssrcMediaSource: " + ssrcMediaSource);
		
		if(this.PID != null && this.PID.length < 1) {
			System.out.println("  No Feedback Control Information (FCI) fields");
		}
		
		for(int i=0; i < this.PID.length; i++ ) {
			System.out.println("  FCI -> PID: " + PID[i] + "  BLP: " + BLP[i]);
		}
		System.out.println("<-RtcpPktRTPFB.debugPrint() ");
	}
}

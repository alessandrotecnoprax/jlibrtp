package jlibrtp;

/**
 * Application specific RTCP packets
 * 
 * @author Arne Kepp
 */
public class RtcpPktAPP extends RtcpPkt {
	/** Name of packet, 4 bytes ASCII */
	protected byte[] pktName = null;
	/** Data of packet */
	protected byte[] pktData = null;	
	
	/**
	 * Constructor for a new Application RTCP packet
	 * 
	 * @param ssrc the SSRC of the sender, presumably taken from RTPSession
	 * @param subtype the subtype of packet, application specific
	 * @param pktName byte[4] representing ASCII name of packet
	 * @param pktData the byte[4x] data that represents the message itself
	 */
	protected RtcpPktAPP(long ssrc, int subtype, byte[] pktName, byte[] pktData) {
		// Fetch all the right stuff from the database
		super.ssrc = ssrc;
		super.packetType = 204;
		super.itemCount = subtype;
		this.pktName = pktName;
		this.pktData = pktData;
	}
	
	/**
	 * Constructor that parses a received Application RTCP packet
	 * 
	 * @param aRawPkt the raw packet containing the date
	 * @param start where in the raw packet this packet starts
	 */
	protected RtcpPktAPP(byte[] aRawPkt, int start) {
		super.ssrc = StaticProcs.bytesToUIntLong(aRawPkt,4);
		super.rawPkt = aRawPkt;
		
		if(!super.parseHeaders(start) || packetType != 204 ) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktAPP.parseHeaders() etc. problem");
			}
			super.problem = -204;
		} else {
			//System.out.println("super.length:  " + super.length);
			if(super.length > 1) {
				pktName = new byte[4];
				System.arraycopy(aRawPkt, 8, pktName, 0, 4);
			}
			if(super.length > 2) {
				pktData = new byte[(super.length+1)*4 - 12];
				System.arraycopy(aRawPkt, 12, pktData, 0, pktData.length);
			}
		}
	}
	
	/**
	 * Encode the packet into a byte[], saved in .rawPkt
	 * 
	 * CompRtcpPkt will call this automatically
	 */
	protected void encode() {	
		super.rawPkt = new byte[12 + this.pktData.length];
		byte[] tmp = StaticProcs.uIntLongToByteWord(super.ssrc);
		System.arraycopy(tmp, 0, super.rawPkt, 4, 4);
		System.arraycopy(this.pktName, 0, super.rawPkt, 8, 4);
		System.arraycopy(this.pktData, 0, super.rawPkt, 12, this.pktData.length);
		writeHeaders();
		//System.out.println("ENCODE: " + super.length + " " + rawPkt.length + " " + pktData.length);
	}
}

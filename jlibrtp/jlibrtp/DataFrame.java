package jlibrtp;
/**
 * Data structure to hold a complete frame. It also contains most
 * of the data from the individual packets that make it up.
 * 
 * Everything is public, to make it easy to pick the structure apart
 * further down the line.
 * 
 * @author Arne Kepp
 */

public class DataFrame {
	public long timeStamp;
	public long SSRC;
	public long[] CSRCs;
	public int payloadType;
	public int dataLength;
	public byte[] data;
	
	/**
	 * The usual way to construct a frame is by giving it a PktBufNode,
	 * which contains links to all the other pkts that make it up.
	 */
	public DataFrame(PktBufNode aBufNode, int noPkts) {
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("-> DataFrame(PktBufNode, noPkts = " + noPkts +")");
		}
		RtpPkt aPkt = aBufNode.pkt;
		
		// All this data should be shared, so we just get it from the 
		timeStamp = aPkt.getTimeStamp();
		SSRC = aPkt.getSsrc();
		CSRCs = aPkt.getCsrcArray();
		
		// Make data the right length
		int payloadLength = aPkt.getPayloadLength();
		data = new byte[aPkt.getPayloadLength() * noPkts];
		
		// Concatenate the data of the packets
		for(int i=0; i< noPkts; i++) {
			aPkt = aBufNode.pkt;
			// This is somewhat silly, because getPayload results in an array copy itself.
			System.arraycopy(aPkt.getPayload(), 0, data, i*payloadLength, payloadLength);
			// Get next node
			aBufNode = aBufNode.nextFrameNode;
		}
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("<- DataFrame(PktBufNode, noPkt), data length: " + data.length);
		}
	}
}

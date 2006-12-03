package jlibrtp;

/**
 * This is a four-directional data structures used for
 * the frame buffer, i.e. buffer for pkts that need
 * to be assimilated into complete frames.
 * 
 * All the actual work is done by PktBuffer.
 * 
 * @author Arne Kepp
 *
 */
public class PktBufNode {
	// These are used to sort within the list of frames
	// Looking from the back, next means older!
	public PktBufNode nextFrameQueueNode = null;
	public PktBufNode prevFrameQueueNode = null;
	
	// These are used to sort packets for a single frame.
	public PktBufNode nextFrameNode = null;
	//public PktBufNode prevFrameNode = null;
	
	// Bookkeeping stuff
	int pktCount;
	public long timeStamp;
	public int seqNum;
	
	// Actual payload
	public RtpPkt pkt = null;
	
	public PktBufNode(RtpPkt aPkt) {
		pkt = aPkt;
		timeStamp = aPkt.getTimeStamp();
		seqNum = aPkt.getSeqNumber();
		pktCount = 1;
	}
}

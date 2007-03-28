/**
 * Java RTP Library
 * Copyright (C) 2006 Arne Kepp
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
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
	private long rtpTimestamp;
	private long timestamp = -1;
	private long SSRC;
	private long[] CSRCs;
	private int payloadType;
	private boolean marked; 
	//private int dataLength;
	private byte[] data;
	
	/**
	 * The usual way to construct a frame is by giving it a PktBufNode,
	 * which contains links to all the other pkts that make it up.
	 */
	protected DataFrame(PktBufNode aBufNode, Participant p, int noPkts) {
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("-> DataFrame(PktBufNode, noPkts = " + noPkts +")");
		}
		RtpPkt aPkt = aBufNode.pkt;
		
		this.marked = aPkt.isMarked();
		
		// All this data should be shared, so we just get it from the first one
		this.rtpTimestamp = aPkt.getTimeStamp();
		SSRC = aPkt.getSsrc();
		CSRCs = aPkt.getCsrcArray();
		
		// Check whether we can compute an NTPish timestamp? Requires two SR reports 
		if(p.ntpGradient > 0) {
			//System.out.print(Long.toString(p.ntpOffset)+" " 
			timestamp =  p.ntpOffset + (long) (p.ntpGradient*(double)(this.rtpTimestamp-p.lastSRRtpTs));
		}
		
		// Make data the right length
		int payloadLength = aPkt.getPayloadLength();
		data = new byte[aPkt.getPayloadLength() * noPkts];
		
		// Concatenate the data of the packets
		for(int i=0; i< noPkts; i++) {
			aPkt = aBufNode.pkt;
			System.arraycopy(aPkt.getPayload(), 0, data, i*payloadLength, payloadLength);
			// Get next node
			aBufNode = aBufNode.nextFrameNode;
		}
		
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("<- DataFrame(PktBufNode, noPkt), data length: " + data.length);
		}
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public long getRTPTimestamp() {
		return this.rtpTimestamp;
	}
	
	public int getPayloadType() {
		return this.payloadType;
	}
	
	public boolean firstPacketMarked() {
		return this.marked;
	}
	
	public long getSSRC() {
		return this.SSRC;
	}
	
	public long[] getCSRCs() {
		return this.CSRCs;
	}
}

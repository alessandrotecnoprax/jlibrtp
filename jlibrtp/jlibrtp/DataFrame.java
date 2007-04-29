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
	private boolean[] marks;
	private boolean anyMarked = false;
	private boolean isComplete = false;
	//private int dataLength;
	private byte[][] data;
	private int[] seqNum;
	private int totalLength = 0;
	protected int lastSeqNum;
	protected int firstSeqNum;
	protected int noPkts;
	/**
	 * The usual way to construct a frame is by giving it a PktBufNode,
	 * which contains links to all the other pkts that make it up.
	 */
	protected DataFrame(PktBufNode aBufNode, Participant p, int noPkts) {
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("-> DataFrame(PktBufNode, noPkts = " + noPkts +")");
		}
		this.noPkts = noPkts;
		RtpPkt aPkt = aBufNode.pkt;
		int pktCount = aBufNode.pktCount;
		firstSeqNum = aBufNode.pktCount;
		
		// All this data should be shared, so we just get it from the first one
		this.rtpTimestamp = aBufNode.timeStamp;
		SSRC = aPkt.getSsrc();
		CSRCs = aPkt.getCsrcArray();
		
		// Check whether we can compute an NTPish timestamp? Requires two SR reports 
		if(p.ntpGradient > 0) {
			//System.out.print(Long.toString(p.ntpOffset)+" " 
			timestamp =  p.ntpOffset + (long) (p.ntpGradient*(double)(this.rtpTimestamp-p.lastSRRtpTs));
		}
		
		// Make data the right length
		int payloadLength = aPkt.getPayloadLength();
		System.out.println("aBufNode.pktCount " + aBufNode.pktCount);
		data = new byte[aBufNode.pktCount][payloadLength];
		seqNum = new int[aBufNode.pktCount];
		marks = new boolean[aBufNode.pktCount];
		
		// Concatenate the data of the packets
		int i;
		for(i=0; i< pktCount; i++) {
			aPkt = aBufNode.pkt;
			byte[] temp = aPkt.getPayload();
			totalLength += temp.length;
			if(temp.length == payloadLength) {
				data[i] = temp;
			} else if(temp.length < payloadLength){
				System.arraycopy(temp, 0, data[i], 0, temp.length);
			} else {
				System.out.println("DataFrame() received node structure with increasing packet payload size.");
			}
			//System.out.println("i " + i + " seqNum[i] " + seqNum[i] + " aBufNode"  + aBufNode);
			seqNum[i] = aBufNode.seqNum;
			marks[i] = aBufNode.pkt.isMarked();
			if(marks[i])
				anyMarked = true;
			
			// Get next node
			aBufNode = aBufNode.nextFrameNode;
		}
		
		lastSeqNum = seqNum[i - 1];
		
		if(firstSeqNum - lastSeqNum == pktCount && pktCount == noPkts) {
			isComplete = true;
		}
		
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("<- DataFrame(PktBufNode, noPkt), data length: " + data.length);
		}
	}
	
	public byte[][] getData() {
		return this.data;
	}
	
	public byte[] getConcatenatedData() {
		if(this.noPkts < 2) {
			byte[] ret = new byte[this.totalLength];
			int pos = 0;
		
			for(int i=0; i<data.length; i++) {
				int length = data[i].length;
				
				// Last packet may be shorter
				if(pos + length > totalLength) 
					length = totalLength - pos;
				
				System.arraycopy(data[i], 0, ret, pos, length);
				pos += data[i].length;
			}
			return ret;
		} else {
			return data[0];
		}
	}
	
	public long timestamp() {
		return this.timestamp;
	}
	
	public long rtpTimestamp() {
		return this.rtpTimestamp;
	}
	
	public int payloadType() {
		return this.payloadType;
	}

	public int[] sequenceNumbers() {
		return seqNum;
	}
	
	public boolean[] marks() {
		return this.marks;
	}
	
	public boolean marked() {
		return this.anyMarked;
	}
	
	public long ssrc() {
		return this.SSRC;
	}
	
	public long[] csrcs() {
		return this.CSRCs;
	}
	
	public boolean complete() {
		return this.isComplete;
	}
}

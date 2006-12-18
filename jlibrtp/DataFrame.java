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
			System.arraycopy(aPkt.getPayload(), 0, data, i*payloadLength, payloadLength);
			// Get next node
			aBufNode = aBufNode.nextFrameNode;
		}
		
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("<- DataFrame(PktBufNode, noPkt), data length: " + data.length);
		}
	}
}

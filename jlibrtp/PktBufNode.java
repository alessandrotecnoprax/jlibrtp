package jlibrtp;
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
	protected PktBufNode nextFrameQueueNode = null;
	protected PktBufNode prevFrameQueueNode = null;
	
	// These are used to sort packets for a single frame.
	protected PktBufNode nextFrameNode = null;
	//public PktBufNode prevFrameNode = null;
	
	// Bookkeeping stuff
	protected int pktCount;
	
	// Cached information from packet
	protected long timeStamp;
	protected int seqNum;
	
	// Actual payload
	protected RtpPkt pkt = null;
	
	/**
	 * Create a new packet buffer node based on a packet
	 * @param aPkt the packet
	 */
	protected PktBufNode(RtpPkt aPkt) {
		pkt = aPkt;
		timeStamp = aPkt.getTimeStamp();
		seqNum = aPkt.getSeqNumber();
		pktCount = 1;
	}
}

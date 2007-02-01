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
 * A PktBuffer stores packets either for buffering purposes,
 * or because they need to be assimilated to create a complete frame.
 * 
 * It also drops duplicate packets.
 * 
 * Note that newest is the most recently received, i.e. highest timeStamp
 * Next means new to old (from recently received to previously received)
 * 
 *  All this stuff needs to be adjusted for rollovers!!!
 * 
 * 
 * @author Arne Kepp
 */
public class PktBuffer {
	//Used to identify the buffer
	long SSRC;

	//Bookkeeping
	int length = 0;
	//int compLen = 0;
	PktBufNode oldest = null;
	PktBufNode newest = null;

	/** 
	 * Creates a new PktBuffer, a linked list of PktBufNode
	 * 
	 * @param aPkt First packet 
	 * @param completionLength How many pkts make up a complete frame, depends on paylod type.
	 */
	public PktBuffer(RtpPkt aPkt) {
		SSRC = aPkt.getSsrc();
		PktBufNode newNode = new PktBufNode(aPkt);
		oldest = newNode;
		newest = newNode;
		length = 1;
	}

	/**
	 * Adds a packet, this happens in constant time if they arrive in order.
	 * Optimized for the case where each pkt is a complete frame.
	 * @param aPkt the packet to be added to the buffer.
	 * @return integer, negative if operation failed (see code)
	 */
	protected synchronized int addPkt(RtpPkt aPkt) {
		if(aPkt == null) {
			System.out.println("! PktBuffer.addPkt(aPkt) aPkt was null");
			return -5;
		}

		if(RTPSession.rtpDebugLevel > 7) {
			System.out.println("-> PktBuffer.addPkt() , length:" + length + " , timeStamp of Pkt: " + aPkt.getTimeStamp());
		}

		long timeStamp = aPkt.getTimeStamp();
		PktBufNode newNode = new PktBufNode(aPkt);
		if(aPkt.getSsrc() != SSRC) {
			System.out.println("PktBuffer.addPkt() SSRCs don't match!");
		}

		if(length != 0) {
			// The packetbuffer is not empty.
			if(newNode.timeStamp > newest.timeStamp) {
				// Yippi, it came in order
				newNode.nextFrameQueueNode = newest;
				newest.prevFrameQueueNode = newNode;
				newest = newNode;
				length++;
			} else {
				//There are packets, we need to order this one right.
				if(oldest.timeStamp > timeStamp) { //|| oldest.seqNum > aPkt.getSeqNumber()) {
					// We got this too late, can't put it in order anymore.
					if(RTPSession.rtpDebugLevel > 2) {
						System.out.println("PktBuffer.addPkt Dropped a packet due to lag! " + timeStamp + " vs "+ oldest.timeStamp);
					}
					return -1;
				}

				//Need to do some real work, find out where it belongs (linear search from the back).
				PktBufNode tmpNode = newest;
				while(tmpNode.timeStamp > timeStamp) {
					tmpNode = tmpNode.nextFrameQueueNode;
				}

				// Update the length of this buffer
				length++;
				
				// Insert into buffer
				newNode.nextFrameQueueNode = tmpNode;
				newNode.prevFrameQueueNode = tmpNode.prevFrameQueueNode;

				// Update the node behind
				if(newNode.prevFrameQueueNode != null) {
					newNode.prevFrameQueueNode.nextFrameQueueNode = newNode;
				}
				tmpNode.prevFrameQueueNode = newNode;

				if(timeStamp > newest.timeStamp) {
					newest = newNode; 
				}

				// Doesn't happen.
				//if(timeStamp > oldest.timeStamp) {
				//	oldest = newNode;
				//}
			}
		} else {
			// The buffer was empty, this packet is the one and only.
			newest = newNode;
			oldest = newNode;
			length = 1;
		}


		if(RTPSession.rtpDebugLevel > 7) {
			if(RTPSession.rtpDebugLevel > 10) {
				this.debugPrint();
			}
			System.out.println("<- PktBuffer.addPkt() , length:" + length);
		}
		return 0;
	}

	/** 
	 * Checks the oldest frame, if there is one, sees whether it is complete.
	 * @return Returns null if there are no complete frames available.
	 */
	protected synchronized DataFrame popOldestFrame() {
		if(RTPSession.rtpDebugLevel > 7) {
			System.out.println("-> PktBuffer.popOldestFrame()");
		}
		if(RTPSession.rtpDebugLevel > 10) {
			this.debugPrint();
		}
		PktBufNode tmpNode = oldest;

		if(oldest != null) {
			// Pop it off, null all references.
			if(length == 1) {
				//There's only one frame
				newest = null;
				oldest = null;
			} else {
				//There are more frames
				oldest = oldest.prevFrameQueueNode;
				oldest.nextFrameQueueNode = null;
			}

			length--;

			//if(tmpNode.pktCount == compLen) {
			if(RTPSession.rtpDebugLevel > 7) {
				System.out.println("<- PktBuffer.popOldestFrame() returns frame");
			}
			return new DataFrame(tmpNode, 1);
			//}
		} else {
			// If we get here we have little to show for.
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println("<- PktBuffer.popOldestFrame() returns null");
			}
			return null;
		}
	}

	/** 
	 * Returns the length of the packetbuffer.
	 * @return number of frames (complete or not) in packetbuffer.
	 */
	protected int getLength() {
		return length;
	}

	/** 
	 * Prints out the packet buffer, oldest node first (on top).
	 */
	public void debugPrint() {
		System.out.println("PktBuffer.debugPrint() : length " + length + " SSRC " + SSRC);
		PktBufNode tmpNode = oldest;
		int i = 0;
		while(tmpNode != null) {
			//String str = tmpNode.timeStamp.toString();
			System.out.println("   " + i + " timeStamp: " + tmpNode.timeStamp + " pktCount:" + tmpNode.pktCount );
			i++;
			tmpNode = tmpNode.prevFrameQueueNode;
		}
	}
}

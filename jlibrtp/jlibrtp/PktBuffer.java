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
 * @author Arne Kepp
 */
public class PktBuffer {
	// Just to get maxBufferSize
	RTPSession rtpSession;
	
	//Used to identify the buffer
	long SSRC;
	Participant p;
	
	//Bookkeeping
	int length = 0;

	PktBufNode oldest = null;
	PktBufNode newest = null;
	
	int lastSeqNumber = -1;
	long lastTimestamp = -1;

	/** 
	 * Creates a new PktBuffer, a linked list of PktBufNode
	 * 
	 * @param aPkt First packet 
	 * @param completionLength How many pkts make up a complete frame, depends on paylod type.
	 */
	public PktBuffer(RTPSession rtpSession, Participant p, RtpPkt aPkt) {
		this.rtpSession = rtpSession;
		this.p = p;
		SSRC = aPkt.getSsrc();
		PktBufNode newNode = new PktBufNode(aPkt);
		oldest = newNode;
		newest = newNode;
		//lastSeqNumber = aPkt.getSeqNumber();
		//lastTimestamp = aPkt.getTimeStamp();
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

		if(length == 0) {
			// The buffer was empty, this packet is the one and only.
			newest = newNode;
			oldest = newNode;
			length = 1;
		} else {
			// The packetbuffer is not empty.
			if(newNode.timeStamp > newest.timeStamp || newNode.seqNum > newest.seqNum) {
				// Packet came in order
				newNode.nextFrameQueueNode = newest;
				newest.prevFrameQueueNode = newNode;
				newest = newNode;
				length++;
			} else {
				//There are packets, we need to order this one right.
				
				if(! pktOnTime(aPkt) ) {
					// We got this too late, can't put it in order anymore.
					if(RTPSession.rtpDebugLevel > 2) {
						System.out.println("PktBuffer.addPkt Dropped a packet due to lag! " +  timeStamp + " " 
								+ aPkt.getSeqNumber() + " vs "+ oldest.timeStamp + " " + oldest.seqNum);
					}
					return -1;
				}

				//Need to do some real work, find out where it belongs (linear search from the back).
				PktBufNode tmpNode = newest;
				while(tmpNode.timeStamp > timeStamp) {
					tmpNode = tmpNode.nextFrameQueueNode;
				}
				
				// Check that it's not a duplicate
				if(tmpNode.timeStamp == timeStamp && aPkt.getSeqNumber() == tmpNode.seqNum) {
					if(RTPSession.rtpDebugLevel > 2) {
						System.out.println("PktBuffer.addPkt Dropped a duplicate packet! " +  timeStamp + " " + aPkt.getSeqNumber() );
					}
					return -1;
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
			}
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
		PktBufNode retNode = oldest;
		
		/**
		 * Three scenarios:
		 * 1) There are no packets available
		 * 2) The first packet is vailable and in order
		 * 3) The first packet is not the next on in the sequence
		 * 		a) We have exceeded the wait buffer
		 * 		b) We wait
		 */

		// Pop it off, null all references.
		if( retNode != null && (retNode.seqNum == this.lastSeqNumber + 1 || retNode.seqNum == 0 
					|| this.length > this.rtpSession.maxReorderBuffer || this.lastSeqNumber < 0)) {
			if(1 == length) {
				//There's only one frame
				newest = null;
				oldest = null;
			} else {
				//There are more frames
				oldest = oldest.prevFrameQueueNode;
				oldest.nextFrameQueueNode = null;
			}

			// Update counters
			length--;
			this.lastSeqNumber = retNode.seqNum;
			this.lastTimestamp = retNode.timeStamp;
				
			//if(tmpNode.pktCount == compLen) {
			if(RTPSession.rtpDebugLevel > 7) {
				System.out.println("<- PktBuffer.popOldestFrame() returns frame");
			}
			
			return new DataFrame(retNode, this.p,1);
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
	 * Checks whether a packet is too late, i.e. the next packet has already been returned.
	 * @param aPkt
	 * @return
	 */
	protected boolean pktOnTime(RtpPkt aPkt) {
		if(this.lastSeqNumber == -1) {
			// First packet
			return true;
		} else {
			// Check whether we can sort it in
			int seqNum = aPkt.getSeqNumber();
			long timeStamp = aPkt.getTimeStamp();
			
			if(seqNum >= this.lastSeqNumber) {
				if(this.lastSeqNumber < 3 && timeStamp < this.lastTimestamp ) {
					return false;
				}
			} else {
				if(seqNum > 3 || timeStamp < this.lastTimestamp) {
					return false;
				}
			}
		}
		return true;
	}

	/** 
	 * Prints out the packet buffer, oldest node first (on top).
	 */
	public void debugPrint() {
		System.out.println("PktBuffer.debugPrint() : length "+length+" SSRC "+SSRC+" lastSeqNum:"+lastSeqNumber);
		PktBufNode tmpNode = oldest;
		int i = 0;
		while(tmpNode != null) {
			//String str = tmpNode.timeStamp.toString();
			System.out.println("   " + i + " seqNum:"+tmpNode.seqNum+" timeStamp: " + tmpNode.timeStamp + " pktCount:" + tmpNode.pktCount );
			i++;
			tmpNode = tmpNode.prevFrameQueueNode;
		}
	}
}

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
	int compLen = 0;
	PktBufNode oldest = null;
	PktBufNode newest = null;
	String lastOp = null;
	
	/** 
	 * Creates a new PktBuffer, a linked list of PktBufNode
	 * 
	 * @param aPkt First packet 
	 * @param completionLength How many pkts make up a complete frame, depends on paylod type.
	 */
	public PktBuffer(RtpPkt aPkt, int completionLength) {
		compLen = completionLength;
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
			if(newNode.timeStamp > newest.timeStamp && compLen == 1) {
				// Yippi, small frames, and they're coming in order
				newNode.nextFrameQueueNode = newest;
				newest.prevFrameQueueNode = newNode;
				newest = newNode;
				length++;
				lastOp = "simpleAdd";
			} else {
				//There are packets, we need to order this one right.
				
				if(oldest.timeStamp > timeStamp || oldest.seqNum > aPkt.getSeqNumber()) {
					// We got this too late, can't put it in order anymore.
					if(RTPSession.rtpDebugLevel > 2) {
						System.out.println("PktBuffer.addPkt Dropped a packet due to lag! " + timeStamp + " vs "+ oldest.timeStamp);
					}
					lastOp = "late packet";
					return -1;
				}
				
				//Need to do some real work, find out where it belongs (lin search from back).
				PktBufNode tmpNode = newest;
				while(tmpNode.timeStamp > timeStamp) {
					tmpNode = tmpNode.nextFrameQueueNode;
				}
				
				//----------------------- START same frame --------------------------------
				if(tmpNode.timeStamp == timeStamp) {
					//Packet has same timestamp, presumably belongs to frame. Need to order within frame.
					if(RTPSession.rtpDebugLevel > 8) {
						System.out.println("Found pkt with existing timeStamp: " + timeStamp);
					}
					lastOp = "dupe timestamp";
					
					// Node has same timeStamp, assume pkt belongs to frame
					int seqNumber = aPkt.getSeqNumber();
					
					if(tmpNode.seqNum < seqNumber) {
						// this is not the first packet in the frame
						tmpNode.pktCount++;
						
						// Find the right spot
						while( tmpNode.nextFrameNode != null && tmpNode.nextFrameNode.seqNum < seqNumber) {
							tmpNode = tmpNode.nextFrameNode;
						}
						
						// Check whether packet is duplicate
						if(tmpNode.nextFrameNode != null && tmpNode.nextFrameNode.seqNum == seqNumber) {
							if(RTPSession.rtpDebugLevel > 2) {
								System.out.println("PktBuffer.addPkt Dropped a duplicate packet!");
							}
							return -2;
						}
						
						newNode.nextFrameNode = tmpNode.nextFrameNode;
						tmpNode.nextFrameNode = newNode;
					
					} else {
						// newNode has the lowest sequence number
						newNode.nextFrameNode = tmpNode;
						newNode.pktCount = tmpNode.pktCount + 1;
						
						//Update the queue
						if(tmpNode.nextFrameQueueNode != null) {
							tmpNode.nextFrameQueueNode.prevFrameQueueNode = newNode;
							newNode.nextFrameQueueNode = tmpNode.nextFrameQueueNode;
							tmpNode.nextFrameQueueNode = null;
						}
						if(tmpNode.prevFrameQueueNode != null) {
							tmpNode.prevFrameQueueNode.nextFrameQueueNode = newNode;
							newNode.prevFrameQueueNode = tmpNode.prevFrameQueueNode;
							tmpNode.prevFrameQueueNode = null;
						}
						if(newest.timeStamp == timeStamp) {
							newest = newNode;
						}
					}
					//-----------------------------  END same frame ---------------------------------
				} else {
					// Update the length of this buffer
					length++;
					lastOp = "sorted in somewhere";
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
			
			if(tmpNode.pktCount == compLen) {
				if(RTPSession.rtpDebugLevel > 7) {
					System.out.println("<- PktBuffer.popOldestFrame() returns frame");
				}
				return new DataFrame(tmpNode, compLen);
			}
		}
		// If we get here we have little to show for.
		if(RTPSession.rtpDebugLevel > 2) {
			System.out.println("<- PktBuffer.popOldestFrame() returns null");
		}
		return null;
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

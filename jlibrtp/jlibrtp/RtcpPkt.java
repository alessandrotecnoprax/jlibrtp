/**
 * Java RTP Library
 * Copyright (C) 2007 Arne Kepp
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
 * Common RTCP packet headers.
 *
 * @author Arne Kepp
 */
public class RtcpPkt {
	protected boolean rawPktCurrent = false;
	protected int version = 2; 		//2 bits
	protected int padding; 			//1 bit
	protected int itemCount;	 		//5 bits
	protected int packetType;			//8 bits
	protected int length;				//16 bits
	
	// Contains the actual data (eventually)
	protected byte[] rawPkt = null;
	
	protected int getLength() {
		return length;
	}
	
	protected int parseHeaders() {
		version = ((rawPkt[0] & 0xC0) >>> 6);
		padding = ((rawPkt[0] & 0x20) >>> 5);
		itemCount = (rawPkt[0] & 0x1F);
		packetType = (int) rawPkt[1];
		length = StaticProcs.combineBytes(rawPkt[2], rawPkt[3]);
		
		if(version == 2 && packetType < 205 && packetType > 199 && length < 65536) {
			return 0;
		} else {
			return -1;
		}
	}
	protected void writeHeaders() {
		byte aByte = 0;
		aByte |=(version << 6);
		aByte |=(padding << 5);
		aByte |=(itemCount);
		rawPkt[0] = aByte;
		aByte = 0;
		aByte |= packetType;
		rawPkt[1] = aByte;
		byte[] someBytes = StaticProcs.intToByteWord(length);
		rawPkt[2] = someBytes[2];
		rawPkt[3] = someBytes[3];
	}
}

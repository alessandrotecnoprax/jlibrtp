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

import java.net.InetAddress;
/** 
 * Common RTCP packet headers.
 *
 * @author Arne Kepp
 */
public class RtcpPkt {
	//protected boolean rawPktCurrent = false;
	protected int problem = 0;
	protected int version = 2; 		//2 bits
	protected int padding = 0; 		//1 bit
	protected int itemCount = 0;	 //5 bits
	protected int packetType = -1;	//8 bits
	protected int length = -1;		//16 bits
	protected long ssrc = -1;
	
	// Contains the actual data (eventually)
	protected byte[] rawPkt = null;
	
	// These are only used for feedback messages
	protected long time = -1;			// Timestamp to decide whether it is worth sending
	protected boolean received = false;	// Whether packet was received
	
	
	/**
	 * Parses the common header of an RTCP packet
	 * 
	 * @param start where in this.rawPkt the headers start
	 * @return true if parsing succeeded and header cheks 
	 */
	protected boolean parseHeaders(int start) {
		version = ((rawPkt[start+0] & 0xC0) >>> 6);
		padding = ((rawPkt[start+0] & 0x20) >>> 5);
		itemCount = (rawPkt[start+0] & 0x1F);
		packetType = (int) rawPkt[start+1];
		if(packetType < 0) {
			packetType += 256;
		}
		length = StaticProcs.bytesToUIntInt(rawPkt, start+2);
		
		if(RTPSession.rtpDebugLevel > 9) {
			System.out.println(" <-> RtcpPkt.parseHeaders() version:"+version+" padding:"+padding+" itemCount:"+itemCount
					+" packetType:"+packetType+" length:"+length);
		}
		
		if(packetType < 207 && packetType > 199) 
			System.out.println("RtcpPkt.parseHeaders problem discovered, packetType " + packetType);
		
		if(version == 2 && length < 65536) {
			return true;
		} else {
			System.out.println("RtcpPkt.parseHeaders() failed header checks, check size and version");
			this.problem = -1;
			return false;
		}
	}
	/**
	 * Writes the common header of RTCP packets. 
	 * The values should be filled in when the packet is initiliazed and this function
	 * called at the very end of .encode()
	 */
	protected void writeHeaders() {
		byte aByte = 0;
		aByte |=(version << 6);
		aByte |=(padding << 5);
		aByte |=(itemCount);
		rawPkt[0] = aByte;
		aByte = 0;
		aByte |= packetType;
		rawPkt[1] = aByte;
		if(rawPkt.length % 4 != 0)
			System.out.println("!!!! RtcpPkt.writeHeaders() rawPkt was not a multiple of 32 bits / 4 octets!");
		byte[] someBytes = StaticProcs.uIntIntToByteWord((rawPkt.length / 4) - 1);
		rawPkt[2] = someBytes[0];
		rawPkt[3] = someBytes[1];
	}
	
	/**
	 * This is just a dummy to make Eclipse complain less.
	 */
	protected void encode() {
		System.out.println("RtcpPkt.encode() should never be invoked!! " + this.packetType);
	}
	
	/**
	 * Check whether this packet came from the source we expected.
	 * 
	 * Not currently used!
	 * 
	 * @param adr address that packet came from
	 * @param partDb the participant database for the session
	 * @return true if this packet came from the expected source
	 */
	protected boolean check(InetAddress adr, ParticipantDatabase partDb) {
		//Multicast -> We have to be naive
		if (partDb.rtpSession.mcSession && adr.equals(partDb.rtpSession.mcGroup))
			return true;
		
		//See whether this participant is known
		Participant part = partDb.getParticipant(this.ssrc);
		if(part != null && part.rtcpAddress.getAddress().equals(adr))
			return true;
		
		//If not, we should look for someone without SSRC with his ip-address?
		return false;
	}
}

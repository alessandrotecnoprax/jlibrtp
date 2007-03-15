/**
 * Java RTP Library
 * Copyright (C) 2006 Arne Kepp / Vaishnav Janardhan
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

import java.net.InetSocketAddress;
import java.net.InetAddress;

/**
 * A participant represents a peer in an RTPSession. Based on the information stored on 
 * these objects, packets are processed and statistics generated for RTCP.
 */
public class Participant {
	protected boolean isSender = false;
	protected boolean isReceiver = false;
	protected boolean unexpected = false;
	protected InetSocketAddress rtpAddress = null; 	
	protected InetSocketAddress rtcpAddress = null; 
	
	// SDES Items
	protected long ssrc = -1;
	protected String cname = null;
	protected String name = null;
	protected String email = null;
	protected String phone = null;
	protected String loc = null;
	protected String tool = null;
	protected String note = null;
	protected String priv = null;
	
	// Receiver Report Items
	protected int firstSeqNumber = -1;
	protected int lastSeqNumber = -1;
	protected int recvPktCount = 0;
	//protected int lostPktCount = 0;
	protected long extHighSeqRecv = -1;
	protected long prevDelay = -1;
	protected long curJitter = -1;
	protected long prevJitter = -1;
	protected long timeStampLSR = -1;		//The timestamp of the last SR
	protected long timeReceivedLSR = -1; 	//The time when we actually got it
	
	// Sender Report Items
	protected long reportedPkts = -1;
	protected long reportedOctets = -1;
	protected long reportedPktsOffset = -1;
	protected long reportedOctetsOffset = -1;
	protected long receivedPkts = -1;
	protected long receivedOctets = -1;
	protected long ntpRelativeOffset = 0; //Offset between our clock and his
	protected long ntpRtpOffset = 0;	//Offset between his RTP timestamps and his NTP
	
	// BYE Items
	protected long timestampBYE = -1;	// The user said BYE at this time
	
	//Store the packets received from this participant
	protected PktBuffer pktBuffer = null;


	/**
	 * Create a basic participant. If this is a <b>unicast</b> session you must provide network address (ipv4 or ipv6) and ports for RTP and RTCP, 
	 * as well as a cname for this contact. These things should be negotiated through SIP or a similar protocol.
	 * 
	 * jlibrtp will listen for RTCP packets to obtain a matching SSRC for this participant, based on cname.
	 * @param networkAddress string representation of network address (ipv4 or ipv6). Use "127.0.0.1" for multicast session.
	 * @param rtpPort port on which peer expects RTP packets. Use 0 if this is a sender-only, or this is a multicast session.
	 * @param rtcpPort port on which peer expects RTCP packets. Use 0 if this is a sender-only, or this is a multicast session.
	 */
	public Participant(String networkAddress, int rtpPort, int rtcpPort) {
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("Creating new participant: " + networkAddress);
		}
		
		// RTP
		if(rtpPort > 0) {
			try {
				rtpAddress = new InetSocketAddress(networkAddress, rtpPort);
			} catch (Exception e) {
				System.out.println("Couldn't resolve " + networkAddress);
			}
			isReceiver = true;
		}
		
		// RTCP 
		if(rtcpPort > 0) {
			try {
				rtcpAddress = new InetSocketAddress(networkAddress, rtcpPort);
			} catch (Exception e) {
				System.out.println("Couldn't resolve " + networkAddress);
			}
		}
		
		//By default this is a sender
		isSender = true;
	}
	
	// We got a packet, but we don't know this person yet.
	protected Participant(InetAddress adr, long SSRC) {
		rtpAddress = new InetSocketAddress(adr,0);
		ssrc = SSRC;
		unexpected = true;
	}
	
	// Surprise through SDES
	protected Participant(long SSRC) {
		ssrc = SSRC;
		unexpected = true;
	}
	
	/**
	 * Toggle whether this participant is expected to send packets to us or not.
	 * 
	 * @param doesSend true if we expect packets from this participant.
	 */
	public void isSender(boolean doesSend) {
		isSender = doesSend;
	}

	/**
	 * Check whether we expect packets from this participant or not.
	 * 
	 * @return true if we expect packets from this source.
	 */
	public boolean isSender() {
		return isSender;
	}
	
	/**
	 * Toggle whether this participant should receive packets from us or not.
	 * 
	 * @param doesReceive true if we should send packets to this participant.
	 */
	public void isReceiver(boolean doesReceive) {
		isReceiver = doesReceive;
	}

	/**
	 * Check whether this participant expects packets from us or not.
	 * 
	 * @return true if the receiver expects packets from us.
	 */
	public boolean isReceiver() {
		return isReceiver;
	}
	
	/**
	 * RTP port we expect peer to listen on.
	 * 
	 * @return the UDP port number
	 */
	//public int getRtpDestPort() {
	//	return rtpDestPort;
	//}
	
	/**
	 * RTCP port we expect peer to listen on.
	 * 
	 * @return the UDP port number
	 */
	//public int getRtcpDestPort() {
	//	return rtcpDestPort;
	//}
	
	/**
	 * RTP Address registered with this participant.
	 * 
	 * @return address of participant
	 */
	InetSocketAddress getRtpSocketAddress() {
		return rtpAddress;
	}
	
	
	/**
	 * RTCP Address registered with this participant.
	 * 
	 * @return address of participant
	 */
	InetSocketAddress getRtcpSocketAddress() {
		return rtcpAddress;
	}
	
	/**
	 * CNAME registered for this participant.
	 * 
	 * @return the cname
	 */
	public String getCNAME() {
		return cname;
	}
	
	/**
	 * SSRC for participant, determined through RTCP SDES
	 * 
	 * @return SSRC (32 bit unsigned integer as long)
	 */
	public long getSSRC() {
		return this.ssrc;
	}
}
/**
 * Java RTP Library
 * Copyright (C) 2006 Vaishnav Janardhan / Arne Kepp
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
 * A participant represents a peer in an RTPSession. Based on the information stored on these objects, packets are processed and statistics generated for RTCP.
 */
public class Participant {
	private int rtpDestPort = 0;
	private int rtcpDestPort = 0;
	public boolean isSender = true;
	public boolean isReceiver = true;
	private InetAddress address = null;
	long ssrc = -1;
	public String cname = null;
	public int	lastRecvSeqNumber = 0;
	public int	lostPktCount = 0;
	public int	recvOctetCount = 0;
	public int  recvPktCount = 0;
	public int	sendOctetCount = 0;
	public int  sendPktCount = 0;
	long lastRecvTimeStamp;
	
	//Store the packets received from this participant
	public PktBuffer pktBuffer = null;


	/**
	 * Create a basic participant. If this is a <b>unicast</b> session you must provide network address (ipv4 or ipv6) and ports for RTP and RTCP, 
	 * as well as a cname for this contact. These things should be negotiated through SIP or a similar protocol.
	 * 
	 * If this is a <b>multicast</b> session you should set the ports and networkAddress to 0, 0, 127.0.0.1 (they will be ignored). 
	 * 
	 * jlibrtp will listen for RTCP packets to obtain a matching SSRC for this participant, based on cname.
	 * @param networkAddress string representation of network address (ipv4 or ipv6). Use "127.0.0.1" for multicast session.
	 * @param rtpPort port on which peer expects RTP packets. Use 0 if this is a sender-only, or this is a multicast session.
	 * @param rtcpPort port on which peer expects RTCP packets. Use 0 if this is a sender-only, or this is a multicast session.
	 * @param CNAME the cname of the peer, we expect this to be included in his RTCP SDES messages.
	 */
	public Participant(String networkAddress, int rtpPort, int rtcpPort, String CNAME) {
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("New participant created: " + CNAME + "@" + networkAddress);
		}
		
		try {
			address = InetAddress.getByName(networkAddress);
		} catch (Exception e) {
			System.out.println("Couldn't resolve " + networkAddress);
		}
		cname = CNAME;
		rtpDestPort = rtpPort;
		rtcpDestPort = rtcpPort;
	}
	
	// Incomplete insert, we got a packet, but we don't know this person yet.
	public Participant(InetAddress adr, int rtpPort, long SSRC) {
		address = adr;
		rtpDestPort = rtpPort;
		ssrc = SSRC;
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
	 * Check whether this participant expects packets from us or not.
	 * 
	 * @return true if the receiver expects packets from us.
	 */
	int getRtpDestPort() {
		return rtpDestPort;
	}
	
	/**
	 * Check whether this participant expects packets from us or not.
	 * 
	 * @return true if the receiver expects packets from us.
	 */
	int getRtcpDestPort() {
		return rtcpDestPort;
	}
	
	/**
	 * Address registered with this participant.
	 * 
	 * @return address of participant
	 */
	InetAddress getInetAddress() {
		return address;
	}
	
	/**
	 * Address registered with this participant.
	 * 
	 * @return address of participant
	 */
	public String getCNAME() {
		return cname;
	}
	
	/**
	 * SSRC for participant, determined through RTCP SDES
	 * 
	 * @return SSRC (32 bit unsigned integer)
	 */
	public long getSSRC() {
		return this.ssrc;
	}
	
	/**
	 * SSRC for participant, determined through RTCP SDES
	 * 
	 * @return SSRC (32 bit unsigned integer)
	 */
	public int setSSRC(long anSSRC) {
		if(ssrc < 0) {
			return -1;
		} else {
			ssrc = anSSRC;
			return 0;
		}
	}

	// Check ParticipantDatabase.java if you change this!
	public long simpleHash() {
		if(ssrc > 0) {
			return ssrc;
		} else {
			// This is a bit pricey
			return cname.hashCode();
		}
	}
	
}

package jlibrtp;
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


import java.net.InetAddress;

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
	public int	octetCount = 0;
	long lastRecvTimeStamp;
	
	
	//Store the packets received from this participant
	public PktBuffer pktBuffer = null;

	// Known contact, but we don't know their ssrc yet.
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
	
	public void isSender(boolean doesSend) {
		isSender = doesSend;
	}

	public boolean isSender() {
		return isSender;
	}
	
	public void isReceiver(boolean doesReceive) {
		isReceiver = doesReceive;
	}

	public boolean isReceiver() {
		return isReceiver;
	}
	
	int getRtpDestPort() {
		return rtpDestPort;
	}
	
	int getRtcpDestPort() {
		return rtcpDestPort;
	}
	
	InetAddress getInetAddress() {
		return address;
	}
	
	public String getCNAME() {
		return cname;
	}
	
	
	public long getSSRC() {
		return this.ssrc;
	}
	
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

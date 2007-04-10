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
	protected boolean unexpected = false;
	protected InetSocketAddress rtpAddress = null; 	
	protected InetSocketAddress rtcpAddress = null;
	//These are used for matchin SSRC packets without owners
	protected InetSocketAddress rtpReceivedFromAddress = null;
	protected InetSocketAddress rtcpReceivedFromAddress = null;
	
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
	protected int receivedSinceLastSR = 0;
	protected int lastSRRseqNumber = -1;
	protected long seqRollOverCount = 0;
	
	protected double interArrivalJitter = -1.0;
	protected long lastRtpTimestamp = -1;
	
	protected long timeStampLSR = -1;		//Middle 32 bits of the NTP timestamp in the last SR
	protected long timeReceivedLSR = -1; 	//The time when we actually got it
	
	// Sender Report Items
	//protected long reportedPkts = -1;
	//protected long reportedOctets = -1;
	//protected long reportedPktsOffset = -1;
	//protected long reportedOctetsOffset = -1;
	protected long receivedPkts = -1;
	protected long receivedOctets = -1;
	
	protected double ntpGradient = -1; // How many ms for each RTP time unit
	protected long ntpOffset = -1;	// NTP offset in ms, compared to our system time
	
	protected long lastNtpTs1 = -1; //32 bits
	protected long lastNtpTs2 = -1; //32 bits
	protected long lastSRRtpTs = -1; //32 bits
	
	// BYE Items
	protected long timestampBYE = -1;	// The user said BYE at this time
	
	//Store the packets received from this participant
	protected PktBuffer pktBuffer = null;

	//To check whether this participant has sent anything recently
	protected long lastRtpPkt = -1; //Time of last RTP packet
	protected long lastRtcpPkt = -1; //Time of last RTCP packet
	protected long addedByApp = -1; //Time the participant was added by application
	protected long lastRtcpRRPkt = -1; //Timestamp of last time we sent this person an RR packet
	protected long secondLastRtcpRRPkt = -1; //Timestamp of 2nd to last time we sent this person an RR Packet
	
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
			//isReceiver = true;
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
		//isSender = true;
	}
	
	// We got a packet, but we don't know this person yet.
	protected Participant(InetSocketAddress rtpAdr, InetSocketAddress rtcpAdr, long SSRC) {
		rtpReceivedFromAddress = rtpAdr;
		rtcpReceivedFromAddress = rtcpAdr;
		ssrc = SSRC;
		unexpected = true;
	}
	
	// Dummy constructor to ease testing
	protected Participant() {
		System.out.println("Don't use the Participan(void) Constructor!");
	}
	
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
	 * InetSocketAddress this participant has used to
	 * send us RTP packets.
	 * 
	 * @return address of participant
	 */
	InetSocketAddress getRtpReceivedFromAddress() {
		return rtpAddress;
	}

	
	
	/**
	 * InetSocketAddress this participant has used to
	 * send us RTCP packets.
	 * 
	 * @return address of participant
	 */
	InetSocketAddress getRtcpReceivedFromAddress() {
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
	 * NAME registered for this participant.
	 * 
	 * @return the name
	 */
	public String getNAME() {
		return name;
	}
	
	/**
	 * EMAIL registered for this participant.
	 * 
	 * @return the email address
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * PHONE registered for this participant.
	 * 
	 * @return the phone number
	 */
	public String getPhone() {
		return phone;
	}
	
	/**
	 * LOCATION registered for this participant.
	 * 
	 * @return the location
	 */
	public String getLocation() {
		return loc;
	}
	
	/**
	 * NOTE registered for this participant.
	 * 
	 * @return the note
	 */
	public String getNote() {
		return note;
	}
	
	/**
	 * PRIVATE something registered for this participant.
	 * 
	 * @return the private-string
	 */
	public String getPriv() {
		return priv;
	}
	
	/**
	 * TOOL something registered for this participant.
	 * 
	 * @return the tool
	 */
	public String getTool() {
		return tool;
	}
		
	/**
	 * SSRC for participant, determined through RTCP SDES
	 * 
	 * @return SSRC (32 bit unsigned integer as long)
	 */
	public long getSSRC() {
		return this.ssrc;
	}
	
	protected void updateRRStats(int packetLength, RtpPkt pkt) {
		int curSeqNum = pkt.getSeqNumber();
		
		if(firstSeqNumber < 0) {
			firstSeqNumber = curSeqNum;
		}
		
		receivedOctets += packetLength;
		receivedSinceLastSR++;
		receivedPkts++;
		
		long curTime =  System.currentTimeMillis();
		
		if( this.lastSeqNumber < curSeqNum ) {
			//In-line packet, best thing you could hope for
			this.lastSeqNumber = curSeqNum;
						
		} else if(this.lastSeqNumber - this.lastSeqNumber < -100) {
			//Sequence counter rolled over
			this.lastSeqNumber = curSeqNum;
			seqRollOverCount++;
			
		} else {
			//This was probably a duplicate or a late arrival.
		}
		
		// Calculate jitter
		if(this.lastRtpPkt > 0) {
			
			long D = (pkt.getTimeStamp() - curTime) - (this.lastRtpTimestamp - this.lastRtpPkt);
			if(D < 0)
				D = (-1)*D;
			
			this.interArrivalJitter += ((double)D - this.interArrivalJitter) / 16.0;
		}

		lastRtpPkt = curTime;
	}
	
	protected long getExtHighSeqRecv() {
		return ((10^16)*seqRollOverCount + lastSeqNumber);
	}
	
	protected int getFractionLost() {
		int denominator = (lastSeqNumber - lastSRRseqNumber);
		if(denominator < 0)
			denominator = (10^16) + denominator;

		int fraction = 256*receivedSinceLastSR;
		if(denominator > 0) {
			fraction = (fraction / denominator);
		} else {
			fraction = 0;
		}
		
		//Clear counters 
		receivedSinceLastSR = 0;
		lastSRRseqNumber = lastSeqNumber;
		
		return fraction;
	}
	
	protected long getLostPktCount() {
		long lost = (this.getExtHighSeqRecv() - this.firstSeqNumber) - receivedPkts;
		
		if(lost < 0)
			lost = 0;
		return lost;
	}
	
	protected double getInterArrivalJitter() {
		return this.interArrivalJitter;
	}
	
	protected void setTimeStampLSR(long ntp1, long ntp2) {
		// Use what we've got
		byte[] high = StaticProcs.uIntLongToByteWord(ntp1);
		byte[] low = StaticProcs.uIntLongToByteWord(ntp2);
		low[3] = low[1];
		low[2] = low[0];
		low[1] = high[3];
		low[0] = high[2];
		
		this.timeStampLSR = StaticProcs.bytesToUIntLong(low, 0);
	}
	
	protected long delaySinceLastSR() {
		if(this.timeReceivedLSR < 1) 
			return 0;
			
		long delay = System.currentTimeMillis() - this.timeReceivedLSR;
		
		//Convert ms into 1/65536s = 1/65.536ms
		return (long) ((double)delay / 65.536);
	}
	
	public void debugPrint() {
		System.out.print(" Participant.debugPrint() SSRC:"+this.ssrc+" CNAME:"+this.cname);
		if(this.rtpAddress != null)
			System.out.print(" RTP:"+this.rtpAddress.toString());
		if(this.rtcpAddress != null)
			System.out.print(" RTCP:"+this.rtcpAddress.toString());
		System.out.println("");
		
		System.out.println("                          Packets received:"+this.receivedPkts);
	}
}

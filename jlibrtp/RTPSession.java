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

import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.*;
import java.util.Random;

/**
 * The RTPSession object is the core of jlibrtp. One should be instanciated for every communication channel, i.e. if you send voice and video, you should create one for each.
 * 
 * The instance holds a participant database, as well as other information about the session. When the application registers with the session, the necessary threads for receiving and processing RTP packets are spawned.
 * 
 * RTP Packets are sent synchronously, all other operations are asynchronous.
 */
public class RTPSession {
	 final static public int rtpDebugLevel = 1;
	 Hashtable participantTable = new Hashtable();
	 
	 // Is this a multicast session?
	 boolean mcSession = false;
	 // Network stuff
	 protected DatagramSocket rtpSock = null;
	 protected DatagramSocket rtcpSock = null;
	 protected MulticastSocket rtpMCSock = null;
	 protected MulticastSocket rtcpMCSock = null;
	 
	 // Internal state
	 protected int payloadType = 0;
	 protected long ssrc;
	 protected long lastTimestamp = 0;
	 protected int seqNum = 0;
	 protected String CNAME = "";
	 int sentPktCount = 0;
	 int sentOctetCount = 0;
	 
	 protected ParticipantDatabase partDb = new ParticipantDatabase(); 
	 // Handle to application
	 protected RTPAppIntf appIntf = null;
	 // Threads etc.
	 protected RTCPSession rtcpSession = null;
	 protected RTPReceiverThread recvThrd = null;
	 protected AppCallerThread appCallerThrd = null;

	 
	 // Locks
	 final public Lock partDbLock = new ReentrantLock();
	 final public Lock pktBufLock = new ReentrantLock();
	 final public Condition pktBufDataReady = pktBufLock.newCondition();
	 
	 // Enough is enough, set to true when you want to quit.
	 protected boolean endSession = false;
	 // Only one registered application, please
	 protected boolean registered = false;

	 /**
	  * Returns an instance of a <b>unicast</b> RTP session. 
	  * Following this you should register your application.
	  * 
	  * @param	rtpPort a free port for RTP communication
	  * @param	rctpPort a free port for RTCP communication
	  * @param	aCNAME the character string that identifies you, example username@hostname.
	  * @return	an instance of RTPSession
	  */
	 public RTPSession(int rtpPort, int rtcpPort, String aCNAME) throws Exception {
		 rtpSock = new DatagramSocket(rtpPort);
		 rtcpSock = new DatagramSocket(rtcpPort);
		 CNAME = aCNAME;
		 this.rtcpSession = new RTCPSession(rtcpPort,this);

	 }
	 
	 /**
	  * Returns an instance of a <b>multicast</b> RTP session. 
	  * Following this you should register your application.
	  * 
	  * @param	rtpSock a multicast socket for RTP communication
	  * @param	rctpSock a multicast socket for RTCP communication
	  * @param	aCNAME the character string that identifies you, example username@hostname.
	  * @return	an instance of RTPSession
	  */
	 public RTPSession(MulticastSocket rtpSock, MulticastSocket rtcpSock, String aCNAME) throws Exception {
		 MulticastSocket rtpMCSock =rtpSock;
		 MulticastSocket rtcpMCSock = rtcpSock;
		 CNAME = aCNAME;
		 mcSession = true;
	 }
	 
	 /**
	  * Registers an application (RTPAppIntf) with the RTP session.
	  * The session will call receiveData() on the supplied instance whenever data has been received.
	  * 
	  * Following this you should set the payload type and add participants to the session.
	  * 
	  * @param	rtpApp an object that implements the RTPAppIntf-interface 
	  * @return	-1 if this RTPSession-instance already has an application registered.
	  */
	 public int RTPSessionRegister(RTPAppIntf rtpApp) {
		if(registered) {
			System.out.println("RTPSessionRegister(): Can\'t register another application!");
			return -1;
		} else {
			registered = true;
			if(RTPSession.rtpDebugLevel > 0) {
				System.out.println("-> RTPSessionRegister");
			}  
			this.appIntf = rtpApp;
			recvThrd = new RTPReceiverThread(this);
			appCallerThrd = new AppCallerThread(this, rtpApp);
			recvThrd.start();
		 	appCallerThrd.start();
		 	
		 	// Set an SSRC
		 	Random r = new Random();
		 	this.ssrc = r.nextInt();
			if(this.ssrc < 0) {
				 this.ssrc = this.ssrc * -1;
			}
		 	return 0;
		}
	}
	
	 /**
	  * Send data to all participants registered as receivers, using the current timeStamp and
	  * payload type.
	  * 
	  * @param buf A buffer of bytes, should not bed padded and less than 1500 bytes on most networks.	
	  * @return	-1 if there was a problem sending the data, 0 otherwise.
	  */
	 public int sendData(byte[] buf) {
		if(RTPSession.rtpDebugLevel > 5) {
				System.out.println("-> RTPSession.sendData(byte[])");
		}  
		
		if(buf.length > 1500) {
			System.out.println("RTPSession.sendData() called with buffer exceeding 1500 bytes.");
		}
		
		RtpPkt pkt = new RtpPkt(System.currentTimeMillis(),this.ssrc,getNextSeqNum(),this.payloadType,buf);
		
		byte[] pktBytes = pkt.encode();
		Enumeration set = partDb.getReceivers();
			
		while(set.hasMoreElements()) {
			Participant p = (Participant)set.nextElement();

			if(p.isReceiver()) {
				if(RTPSession.rtpDebugLevel > 8) {
					System.out.println("RTPSenderThread.sendData() Participant: " + p.getCNAME() + "@" +  p.getInetAddress() + ":" + p.getRtpDestPort());
				}
				try {	
					DatagramPacket packet = new DatagramPacket(pktBytes,pktBytes.length,p.getInetAddress(),p.getRtpDestPort());
					rtpSock.send(packet);
					this.sentPktCount++;
					this.sentOctetCount++;
				} catch (Exception e) {
					System.out.println("RTPSession.sendData() - Possibly lost socket.");
					e.printStackTrace();
					return -1;
				}
			}
	
		}
		
		if(RTPSession.rtpDebugLevel > 3) {
				System.out.println("<- RTPSession.sendData(byte[])");
		}  
			
		 return 0;
	 }
	
	 /**
	  * Add a participant object to the participant database.
	  * 
	  * If packets have already been received from this user, we will try to update the automatically inserted participant with the information provided here.
	  *
	  * @param p A participant.
	  */
	public void addParticipant(Participant p) {
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTPSession.addParticipant("+p.cname+ ","+p.ssrc+")");
		}		
		
		Participant tmp = null;
		if(p.ssrc > 0) {
			tmp = partDb.getParticipant(ssrc);
			if(tmp != null) {
				tmp.ssrc = p.ssrc;
				tmp.isReceiver = p.isReceiver;
				tmp.isSender = p.isSender;
				
				partDb.updateParticipant(tmp);
			}
		}
		
		// 
		Participant tmp2 = null;
		if(p.cname != null) {
			tmp2 = partDb.getParticipant(p.cname);
			if(tmp2 != null) {
				tmp.cname = p.cname;
				tmp.isReceiver = p.isReceiver;
				tmp.isSender = p.isSender;
				
				partDb.updateParticipant(tmp2);
			}
		}
		
		// This is actually a new one.
		if(tmp == null && tmp2 == null) {
			partDb.addParticipant(p);
			
			// Send messages with SDES.
		}
	}
	
	 /**
	  * Remove a participant from the database. All buffered packets will be destroyed.
	  *
	  * @param p A participant.
	  */
	 void removeParticipant(Participant p) {
		partDb.removeParticipant(p);
	 }
	 
	 /**
	  * Remove a participant from the database. All buffered packets will be destroyed.
	  *
	  * @param cname The cname of the participant.
	  */
	 public void removeParticipant(String cname) {
		Participant p = partDb.getParticipant(cname);
		partDb.removeParticipant(p);
	 }
	 
	 /**
	  * End the RTP Session. This will halt all threads and send bye-messages to other participants.
	  * 
	  * RTCP related threads may require several seconds to wake up and terminate.
	  */
	void endSession() {
		this.endSession = true;
	}
	
	 /**
	  * Check whether this session is ending.
	  * 
	  * @return true if session and associated threads are terminating.
	  */
	boolean isEnding() {
		return this.endSession;
	}

	/**
	 * Update your CNAME, used for outgoing RTCP packets.
	 * 
	 * @param cname a string, e.g. username@hostname. Must be unique for session.
	 */
	public void setCNAME(String cname) {
		this.CNAME = cname;
	}
	
	/**
	 * Change the RTP port of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a unicast session to begin with.
	 * 
	 * @param rtpPort integer for new port number, check it is free first.
	 */
	public int updateRTPSock(int rtpPort) throws Exception {
		if(mcSession = false) {
			 rtpSock = new DatagramSocket(rtpPort);
			 return 0;
		} else {
			System.out.println("Can't switch from multicast to unicast.");
			return -1;
		}
	}
	
	/**
	 * Change the RTCP port of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a unicast session to begin with.
	 * 
	 * @param rtcpPort integer for new port number, check it is free first.
	 */
	public int updateRTCPSock(int rtcpPort) throws Exception {
		if(mcSession = false) {
			 rtpSock = new DatagramSocket(rtcpPort);
			 return 0;
		} else {
			System.out.println("Can't switch from multicast to unicast.");
			return -1;
		}
	}
	
	/**
	 * Change the RTP multicast socket of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a multicast session to begin with.
	 * 
	 * @param rtpSock the new multicast socket for RTP communication.
	 */
	public int updateRTPSock(MulticastSocket rtpSock) {
		if(mcSession = true) {
			 rtpMCSock = rtpSock;
			 return 0;
		} else {
			System.out.println("Can't switch from unicast to multicast.");
			return -1;
		}
	}
	
	/**
	 * Change the RTCP multicast socket of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a multicast session to begin with.
	 * 
	 * @param rtcpSock the new multicast socket for RTCP communication.
	 */
	public int updateRTCPSock(MulticastSocket rtcpSock) {
		if(mcSession = true) {
			 rtcpMCSock = rtcpSock;
			 return 0;
		} else {
			System.out.println("Can't switch from unicast to multicast.");
			return -1;
		}
	}
	
	/**
	 * Update the payload type used for the session. It is represented as a 7 bit integer, whose meaning must be negotiated elsewhere (see IETF RFCs <a href="http://www.ietf.org/rfc/rfc3550.txt">3550</a> and <a href="http://www.ietf.org/rfc/rfc3550.txt">3551</a>)
	 * 
	 * @param payloadT an integer representing the payload type of any subsequent packets that are sent.
	 */
	public int setPayloadType(int payloadT) {
		if(payloadT > 128 || payloadT < 0) {
			return -1;
		} else {
			this.payloadType = payloadT;
			return this.payloadType;
		}
	}
	
	/**
	 * Get the payload type that is currently used for outgoing RTP packets.
	 * 
	 * @return payload type as integer
	 */
	public int getPayloadType() {
		return this.payloadType;
	}
	
	/**
	 * Change the RTCP multicast socket of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a multicast session to begin with.
	 * 
	 * @param rtcpSock the new multicast socket for RTCP communication.
	 */
	private int getNextSeqNum() {
		seqNum++;
		// 16 bit number
		if(seqNum > (2^16)) { 
			seqNum = 0;
		}
		return seqNum;
	}
}

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
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.locks.*;
import java.util.Random;

/**
 * The RTPSession object is the core of jlibrtp. One should be instanciated for every communication channel, i.e. if you send voice and video, you should create one for each.
 * 
 * The instance holds a participant database, as well as other information about the session. When the application registers with the session, the necessary threads for receiving and processing RTP packets are spawned.
 * 
 * RTP Packets are sent synchronously, all other operations are asynchronous.
 * 
 * @author Arne Kepp
 */
public class RTPSession {
	 /**
	  * The debug level is final to avoid compilation of if-statements.</br>
	  * 0 provides no debugging information, 20 provides everything </br>
	  * Debug output is written to System.out</br>
	  */
	 final static public int rtpDebugLevel = 15;
	 
	 // Network stuff
	 protected DatagramSocket rtpSock = null;
	 protected MulticastSocket rtpMCSock = null;
	 protected InetAddress mcGroup = null;
	 
	 // Internal state
	 protected boolean mcSession = false; // Multicast session?
	 protected int payloadType = 0;
	 protected long ssrc;
	 protected long lastTimestamp = 0;
	 protected int seqNum = 0;
	 protected int sentPktCount = 0;
	 protected int sentOctetCount = 0;
	 
	 //Save the random seed
	 protected Random random = null;
	 
	 //Session bandwidth
	 protected int bandwidth = 72000;
	 
	 //By default we do not return packets from strangers.
	 protected boolean naiveReception = false;
	 
	 //Maximum number of packets used for reordering
	 protected int maxReorderBuffer = 5;
	 
	 // List of participants
	 protected ParticipantDatabase partDb = new ParticipantDatabase(this); 
	 // Handles to application
	 protected RTPAppIntf appIntf = null;
	 protected RTCPAppIntf rtcAppIntf = null;
	 // Threads etc.
	 protected RTCPSession rtcpSession = null;
	 protected RTPReceiverThread recvThrd = null;
	 protected AppCallerThread appCallerThrd = null;
	 
	 // Locks
	 final protected Lock pktBufLock = new ReentrantLock();
	 final protected Condition pktBufDataReady = pktBufLock.newCondition();
	 
	 //final protected Lock conflictResLock = new ReentrantLock();
	 //final protected Condition conflictResolved = conflictResLock.newCondition();
	 
	 // Enough is enough, set to true when you want to quit.
	 protected boolean endSession = false;
	 // Only one registered application, please
	 protected boolean registered = false;
	 // We're busy resolving a conflict, please try again later
	 protected boolean conflict = false;
	 protected int conflictCount = 0;

	 // SDES stuff
	 protected String cname;
	 public String name = null;
	 public String email = null;
	 public String phone = null;
	 public String loc = null;
	 public String tool = null;
	 public String note = null;
	 public String priv = null;
	 
	 
	 /**
	  * Returns an instance of a <b>unicast</b> RTP session. 
	  * Following this you should register your application.
	  * 
	  * @param	rtpSocket UDP socket to receive RTP communication on
	  * @param	rtcpSocket UDP socket to receive RTCP communication on, null if none.
	  */
	 public RTPSession(DatagramSocket rtpSocket, DatagramSocket rtcpSocket) {
		 mcSession = false;
		 rtpSock = rtpSocket;
		 this.generateSsrc();
		 generateCNAME();
		 this.rtcpSession = new RTCPSession(this,rtcpSocket);
		 System.out.println("mcSession: " + this.mcSession);
	 }
	 
	 /**
	  * Returns an instance of a <b>multicast</b> RTP session. 
	  * Following this you should register your application.
	  * 
	  * @param	rtpSock a multicast socket to receive RTP communication on
	  * @param	rtcpSock a multicast socket to receive RTP communication on
	  * @param	multicastGroup the multicast group that we want to communicate with.
	  */
	 public RTPSession(MulticastSocket rtpSock, MulticastSocket rtcpSock, InetAddress multicastGroup) throws Exception {
		 mcSession = true;
		 rtpMCSock =rtpSock;
		 mcGroup = multicastGroup;
		 rtpMCSock.joinGroup(mcGroup);
		 rtcpSock.joinGroup(mcGroup);
		 this.generateSsrc();
		 generateCNAME();
		 this.rtcpSession = new RTCPSession(this,rtcpSock,mcGroup);
	 }
	 
	 /**
	  * Registers an application (RTPAppIntf) with the RTP session.
	  * The session will call receiveData() on the supplied instance whenever data has been received.
	  * 
	  * Following this you should set the payload type and add participants to the session.
	  * 
	  * @param	rtpApp an object that implements the RTPAppIntf-interface
	  * @param	rtcpApp an object that implements the RTCPAppIntf-interface (optional)
	  * @return	-1 if this RTPSession-instance already has an application registered.
	  */
	 public int RTPSessionRegister(RTPAppIntf rtpApp, RTCPAppIntf rtcpApp) {
		if(registered) {
			System.out.println("RTPSessionRegister(): Can\'t register another application!");
			return -1;
		} else {
			registered = true;
			generateSeqNum();
			if(RTPSession.rtpDebugLevel > 0) {
				System.out.println("-> RTPSessionRegister");
			}  
			this.appIntf = rtpApp;
			this.rtcAppIntf = rtcpApp;
			recvThrd = new RTPReceiverThread(this);
			appCallerThrd = new AppCallerThread(this, rtpApp);
			recvThrd.start();
		 	appCallerThrd.start();
		 	rtcpSession.start();
			System.out.println("mcSession: " + this.mcSession);
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
			System.out.println("RTPSession.sendData() called with buffer exceeding 1500 bytes ("+buf.length+")");
		}
		
		// Create a new RTP Packet
		RtpPkt pkt = new RtpPkt(System.currentTimeMillis(),this.ssrc,getNextSeqNum(),this.payloadType,buf);
		
		// Creates a raw packet
		byte[] pktBytes = pkt.encode();
		
		// Loop over recipients
		Iterator iter = partDb.getRtpReceivers();
		
		// Pre-flight check, are resolving an SSRC conflict?
		if(this.conflict) {
			System.out.println("RTPSession.sendData() called while trying to ");
			return -1;
		}

		while(iter.hasNext()) {
			InetSocketAddress receiver = (InetSocketAddress) iter.next();
			DatagramPacket packet = null;
			
			if(RTPSession.rtpDebugLevel > 15) {
				System.out.println("   Sending to " + receiver.toString());
			}
			
			try {
				packet = new DatagramPacket(pktBytes,pktBytes.length,receiver);
			} catch (Exception e) {
				System.out.println("RTPSession.sendData() packet creation failed.");
				e.printStackTrace();
				return -1;
			}
			
			//Actually send the packet
			if(!this.mcSession) {
				try {
					rtpSock.send(packet);
				} catch (Exception e) {
					System.out.println("RTPSession.sendData() unicast failed.");
					e.printStackTrace();
					return -1;
				}
			} else {
				try {
					rtpMCSock.send(packet);
				} catch (Exception e) {
					System.out.println("RTPSession.sendData() multicast failed.");
					e.printStackTrace();
					return -1;
				}
			}
		}
		
		//Update our stats
		this.sentPktCount++;
		this.sentOctetCount++;
		
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
	public int addParticipant(Participant p) {
		//if(RTPSession.rtpDebugLevel > 1) {
		//	System.out.println("<-> RTPSession.addParticipant("+p.cname+ ","+p.ssrc+")");
		//}		
		
		if(partDb.all.contains(p)) {
			return -1;
		}
		
		//Check whether this participant already is in the database
		Enumeration enu = this.partDb.getParticipants();
		
		while(enu.hasMoreElements()) {
			Participant tmp = (Participant) enu.nextElement();
			
			if(tmp.rtpAddress == null && tmp.rtpReceivedFromAddress.equals(p.rtpAddress)) {
				tmp.rtpAddress = p.rtpAddress;
				tmp.rtcpAddress = p.rtcpAddress;
				partDb.updateParticipant(tmp);
				
				return 1;
			}
		}
		
		partDb.addParticipant(p);
		return 0;
	}
	
	 /**
	  * Remove a participant from the database. All buffered packets will be destroyed.
	  *
	  * @param p A participant.
	  */
	 public void removeParticipant(Participant p) {
		partDb.removeParticipant(p);
	 }
	 
	 /**
	  * End the RTP Session. This will halt all threads and send bye-messages to other participants.
	  * 
	  * RTCP related threads may require several seconds to wake up and terminate.
	  */
	public void endSession() {
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
	 * Overrides CNAME, used for outgoing RTCP packets.
	 * 
	 * @param cname a string, e.g. username@hostname. Must be unique for session.
	 */
	public void setCNAME(String cname) {
		this.cname = cname;
	}
	
	/**
	 * Overrides CNAME, used for outgoing RTCP packets.
	 * 
	 * @param cname a string, e.g. username@hostname. Must be unique for session.
	 */
	public String getCNAME() {
		return this.cname;
	}
	
	private void generateCNAME() {
		cname = System.getProperty ("user.name") + "@";
		if(this.mcSession) {
			cname += this.rtpMCSock.getLocalAddress().toString();
		} else {
			cname += this.rtpSock.getLocalAddress().toString();
		}
	}
	/**
	 * Change the RTP port of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a unicast session to begin with.
	 * 
	 * @param rtpPort integer for new port number, check it is free first.
	 */
	//public int updateRTPSock(int rtpPort) throws Exception {
	//	if(!mcSession) {
	//		 rtpSock = new DatagramSocket(rtpPort);
	//		 return 0;
	//	} else {
	//		System.out.println("Can't switch from multicast to unicast.");
	//		return -1;
	//	}
	//}
	
	/**
	 * Change the RTCP port of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a unicast session to begin with.
	 * 
	 * @param rtcpPort integer for new port number, check it is free first.
	 */
	//public int updateRTCPSock(int rtcpPort) throws Exception {
	//	if(mcSession) {
	//		 rtpSock = new DatagramSocket(rtcpPort);
	//		 return 0;
	//	} else {
	//		System.out.println("Can't switch from multicast to unicast.");
	//		return -1;
	//	}
	//}
	
	/**
	 * Change the RTP multicast socket of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a multicast session to begin with.
	 * 
	 * @param rtpSock the new multicast socket for RTP communication.
	 */
	//public int updateRTPSock(MulticastSocket rtpSock) {
	//	if(mcSession) {
	//		 rtpMCSock = rtpSock;
	//		 return 0;
	//	} else {
	//		System.out.println("Can't switch from unicast to multicast.");
	//		return -1;
	//	}
	//}
	
	/**
	 * Change the RTCP multicast socket of the session. 
	 * Peers must be notified through SIP or other signalling protocol.
	 * Only valid if this is a multicast session to begin with.
	 * 
	 * @param rtcpSock the new multicast socket for RTCP communication.
	 */
	//public int updateRTCPSock(MulticastSocket rtcpSock) {
	//	if(mcSession) {
	//		 rtcpMCSock = rtcpSock;
	//		 return 0;
	//	} else {
	//		System.out.println("Can't switch from unicast to multicast.");
	//		return -1;
	//	}
	//}
	
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
	 * Should packets from unknown participants be returned to the application? This can be dangerous.
	 * 
	 * @param doAccept packets from participants not added by the application.
	 */
	public void setNaivePktReception(boolean doAccept) {
		naiveReception = doAccept;
	}
	
	/**
	 * Are packets from unknown participants returned to the application?
	 * 
	 * @return whether we accept packets from participants not added by the application.
	 */
	public boolean naivePktReception() {
		return naiveReception;
	}
	
	public void setMaxReorderBuffer(int numberOfPackets) {
		if(numberOfPackets >= 0) {
			this.maxReorderBuffer = numberOfPackets;
		} else {
			this.maxReorderBuffer = -1;
		}
	}
	
	/**
	 * The maximum number of milliseconds that should pass before the callback
	 * interface is called with receiveData().
	 * 
	 * @return the number of milliseconds that can pass, a negative number means infinite.
	 */
	public int getMaxReorderBuffer() {
		return this.maxReorderBuffer;
	}
	
	private int getNextSeqNum() {
		seqNum++;
		// 16 bit number
		if(seqNum > 65536) { 
			seqNum = 0;
		}
		return seqNum;
	}
	
	private void createRandom() {
		this.random = new Random(System.currentTimeMillis() + Thread.currentThread().getId() 
				- Thread.currentThread().hashCode());
	}
	
	private void generateSeqNum() {
		if(this.random == null)
			createRandom();
		
		seqNum = this.random.nextInt();
		if(seqNum < 0)
			seqNum = -seqNum;
		while(seqNum > 65535) {
			seqNum = seqNum / 10;
		}
	}
	
	private void generateSsrc() {
		if(this.random == null)
			createRandom();
		
		// Set an SSRC
		this.ssrc = this.random.nextInt();
		if(this.ssrc < 0) {
			this.ssrc = this.ssrc * -1;
		}	
	}
	
	protected void resolveSsrcConflict() {
		System.out.println("!!!!!!! Beginning SSRC conflict resolution !!!!!!!!!");
		this.conflictCount++;
		
		if(this.conflictCount < 5) {
			//Don't send any more regular packets out until we have this sorted out.
			this.conflict = true;
		
			//Send byes
			rtcpSession.sendByes();
		
			//Calculate the next delay
			rtcpSession.calculateDelay();
			
			//Generate a new Ssrc for ourselves
			generateSsrc();
			
			//Get the SDES packets out faster
			rtcpSession.initial = true;
			
			this.conflict = false;
			System.out.println("SSRC conflict resolution complete");
			
		} else {
			System.out.println("Too many conflicts. There is probably a loop in the network.");
			this.endSession = true;
		}
	}
}

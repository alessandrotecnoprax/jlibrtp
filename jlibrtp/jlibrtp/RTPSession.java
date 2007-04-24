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
import java.util.Iterator;
import java.util.concurrent.locks.*;
import java.util.Random;

/**
 * The RTPSession object is the core of jlibrtp. 
 * 
 * One should be instantiated for every communication channel, i.e. if you send voice and video, you should create one for each.
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
	 final static public int rtpDebugLevel = 0;
	 final static public int rtcpDebugLevel = 0;
	 
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
	 
	 //Session bandwidth in BYTES per second
	 protected int bandwidth = 8000;
	 
	 //By default we do not return packets from strangers.
	 protected boolean naiveReception = false;
	 
	 //Maximum number of packets used for reordering
	 protected int maxReorderBuffer = 5;
	 
	 // List of participants
	 protected ParticipantDatabase partDb = new ParticipantDatabase(this); 
	 // Handles to application
	 protected RTPAppIntf appIntf = null;
	 protected RTCPAppIntf rtcAppIntf = null;
	 protected DebugAppIntf debugAppIntf = null;
	 
	 // Threads etc.
	 protected RTCPSession rtcpSession = null;
	 protected RTPReceiverThread recvThrd = null;
	 protected AppCallerThread appCallerThrd = null;
	 
	 // RFC4585 mode?
	 protected boolean modeRFC4585 = false; 
	 
	 // Locks
	 final protected Lock pktBufLock = new ReentrantLock();
	 final protected Condition pktBufDataReady = pktBufLock.newCondition();
	 
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
		 this.generateCNAME();
		 this.rtcpSession = new RTCPSession(this,rtcpSocket);
		 
		 // The sockets are not always imediately available?
		 try { Thread.sleep(1); } catch (InterruptedException e) { System.out.println("RTPSession sleep failed"); }
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
		 this.generateCNAME();
		 this.rtcpSession = new RTCPSession(this,rtcpSock,mcGroup);
		 
		 // The sockets are not always imediately available?
		 try { Thread.sleep(1); } catch (InterruptedException e) { System.out.println("RTPSession sleep failed"); }
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
	 public int RTPSessionRegister(RTPAppIntf rtpApp, RTCPAppIntf rtcpApp, DebugAppIntf debugApp) {
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
			this.debugAppIntf = debugApp;
			
			recvThrd = new RTPReceiverThread(this);
			appCallerThrd = new AppCallerThread(this, rtpApp);
			recvThrd.start();
		 	appCallerThrd.start();
		 	rtcpSession.start();
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
		 return this.sendData(buf, null, false);
	 }
	 /**
	  * Send data to all participants registered as receivers, using the current timeStamp and
	  * payload type.
	  * 
	  * @param buf A buffer of bytes, should not bed padded and less than 1500 bytes on most networks.
	  * @param csrcArray an array with the SSRCs of contributing sources
	  * @return	-1 if there was a problem sending the data, 0 otherwise.
	  */
	 public int sendData(byte[] buf, long[] csrcArray, boolean marker) {
		if(RTPSession.rtpDebugLevel > 5) {
				System.out.println("-> RTPSession.sendData(byte[])");
		}
		
		if(buf.length > 1500) {
			System.out.println("RTPSession.sendData() called with buffer exceeding 1500 bytes ("+buf.length+")");
		}
		
		// Create a new RTP Packet
		RtpPkt pkt = new RtpPkt(System.currentTimeMillis(),this.ssrc,getNextSeqNum(),this.payloadType,buf);
		
		if(csrcArray != null)
			pkt.setCsrcs(csrcArray);
		
		pkt.setMarked(marker);
		
		// Creates a raw packet
		byte[] pktBytes = pkt.encode();
		
		//System.out.println(Integer.toString(StaticProcs.bytesToUIntInt(pktBytes, 2)));
		
		// Pre-flight check, are resolving an SSRC conflict?
		if(this.conflict) {
			System.out.println("RTPSession.sendData() called while trying to resolve conflict.");
			return -1;
		}
		
		
		if(this.mcSession) {
			DatagramPacket packet = null;
			
			
			try {
				packet = new DatagramPacket(pktBytes,pktBytes.length,this.mcGroup,this.rtpMCSock.getPort());
			} catch (Exception e) {
				System.out.println("RTPSession.sendData() packet creation failed.");
				e.printStackTrace();
				return -1;
			}
			
			try {
				rtpMCSock.send(packet);
				//Debug
				if(this.debugAppIntf != null) {
					this.debugAppIntf.debugPacketSent(1, (InetSocketAddress) packet.getSocketAddress(), 
							new String("Sent multicast RTP packet of size " + packet.getLength() + 
									" to " + packet.getSocketAddress().toString() + " via " 
									+ rtpMCSock.getLocalSocketAddress().toString()));
				}
			} catch (Exception e) {
				System.out.println("RTPSession.sendData() multicast failed.");
				e.printStackTrace();
				return -1;
			}		
			
		} else {
			// Loop over recipients
			Iterator<Participant> iter = partDb.getUnicastReceivers();
			while(iter.hasNext()) {			
				InetSocketAddress receiver = iter.next().rtpAddress;
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
				try {
					rtpSock.send(packet);
					//Debug
					if(this.debugAppIntf != null) {
						this.debugAppIntf.debugPacketSent(0, (InetSocketAddress) packet.getSocketAddress(), 
								new String("Sent unicast RTP packet of size " + packet.getLength() + 
										" to " + packet.getSocketAddress().toString() + " via " 
										+ rtpSock.getLocalSocketAddress().toString()));
					}
				} catch (Exception e) {
					System.out.println("RTPSession.sendData() unicast failed.");
					e.printStackTrace();
					return -1;
				}
			}
		}
		
		//Update our stats
		this.sentPktCount++;
		this.sentOctetCount++;
		
		if(RTPSession.rtpDebugLevel > 5) {
				System.out.println("<- RTPSession.sendData(byte[]) " + pkt.getSeqNumber());
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
		//For now we make all participants added this way persistent
		return this.partDb.addParticipant(0, p);
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
		
		// No more RTP packets, please
		if(this.mcSession) {
			this.rtpMCSock.close();
		} else {
			this.rtpSock.close();
		}
		
		// Signal the thread that pushes data to application
		this.pktBufLock.lock();
		try { this.pktBufDataReady.signalAll(); } finally {
			this.pktBufLock.unlock();
		}
		// Interrupt what may be sleeping
		this.rtcpSession.senderThrd.interrupt();
		
		// Give things a chance to cool down.
		try { Thread.sleep(50); } catch (Exception e){ };
		
		this.appCallerThrd.interrupt();

		// Give things a chance to cool down.
		try { Thread.sleep(50); } catch (Exception e){ };
		
		if(this.rtcpSession != null) {		
			// No more RTP packets, please
			if(this.mcSession) {
				this.rtcpSession.rtcpMCSock.close();
			} else {
				this.rtcpSession.rtcpSock.close();
			}
		}
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
	
	public long getSsrc() {
		return this.ssrc;
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
	public int updateRTPSock(DatagramSocket newSocket) {
		if(!mcSession) {
			 rtpSock = newSocket;
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
	public int updateRTCPSock(DatagramSocket newSocket) {
		if(!mcSession) {
			this.rtcpSession.rtcpSock = newSocket;
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
	public int updateRTPSock(MulticastSocket newSock) {
		if(mcSession) {
			 this.rtpMCSock = newSock;
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
	public int updateRTCPSock(MulticastSocket newSock) {
		if(mcSession) {
			this.rtcpSession.rtcpMCSock = newSock;
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
	
	/**
	 * Set the number of RTP packets that should be buffered when a packet is
	 * missing or received out of order. Setting this number high increases
	 * the chance of correctly reordering packets, but increases latency when
	 * a packet is dropped by the network.
	 * 
	 * Packets that arrive in order are not affected, they are passed straight
	 * to the application.
	 * 
	 * The maximum delay is numberofPackets * packet rate , where the packet rate
	 * depends on the codec and profile used by the sender.
	 * 
	 * A negative value disables the buffering, out of order packets will simply be dropped.
	 * 
	 * @param numberOfPackets number of packets that can accumulate before the first is returned
	 */
	public void setMaxReorderBuffer(int numberOfPackets) {
		if(numberOfPackets >= 0) {
			this.maxReorderBuffer = numberOfPackets;
		} else {
			this.maxReorderBuffer = -1;
		}
	}
	
	/**
	 * The number of RTP packets that should be buffered when a packet is
	 * missing or received out of order. A high number  increases the chance 
	 * of correctly reordering packets, but increases latency when a packet is 
	 * dropped by the network.
	 * 
	 * A negative value disables the buffering, out of order packets will simply be dropped.
	 * 
	 * @return the maximum number of packets that can accumulate before the first is returned
	 */
	public int getMaxReorderBuffer() {
		return this.maxReorderBuffer;
	}
	
	/**
	 * Set whether the stack should operate in RFC 4585 mode.
	 * 
	 * NOT FULLY IMPLEMENTED!! 
	 * 
	 * @param set Whether to operate in this mode or not
	 */
	public void setRFC4585Mode(boolean set) {
		this.modeRFC4585 = set;
	}
	
	/**
	 * 
	 * Whether the stack should operate in RFC 4585 mode.
	 * 
	 * NOT FULLY IMPLEMENTED!! 
	 * 	
	 * @return whether the stack operates accordint to RFC 4585, otherwise RFC 3550
	 */
	public boolean getRFC4585Mode() {
		return this.modeRFC4585;
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
			this.endSession();
		}
	}
}

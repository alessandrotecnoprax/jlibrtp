package jlibrtp;
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


import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.util.Enumeration;
import java.util.concurrent.locks.*;
import java.util.Random;

public class RTPSession {
	 final static public int rtpDebugLevel = 1;
	 // This thing ought to be indexed by SSRC, since that is what we'll be getting constantly.
	 //Hashtable participantTable = new Hashtable();
	 
	 ParticipantDatabase partDb = new ParticipantDatabase();
	 
	 // Unique ID for hash, only incremented.
	 int nextPartId = 0;
	 String CNAME = "";
	 DatagramSocket rtpSock = null;
	 DatagramSocket rtcpSock = null;
	 MulticastSocket mcsocket = null;
	 long ssrc;
	 long lastTimestamp = 0;
	 int seqNum = 0;
	 
	 RTPAppIntf appIntf = null;
	
	 RTCPSession rtcpSession = null;
	 RTPReceiverThread recvThrd = null;
	 AppCallerThread appCallerThrd = null;

	 // Locks the participant database. May not be useful?
	 final public Lock partDbLock = new ReentrantLock();
	 
	 // Locks all the packet buffers.
	 final public Lock pktBufLock = new ReentrantLock();
	 final public Condition pktBufDataReady = pktBufLock.newCondition();
	 
	 // Enough is enough, set to true when you want to quit.
	 boolean endSession = false;
	 
	 // Only one registered application, please
	 boolean registered = false;
	 
	 /**
	  * Constructors
	  */
	 public RTPSession(int rtpPort, int rtcpPort, String aCNAME) throws Exception {
		 rtpSock = new DatagramSocket(rtpPort);
		 rtcpSock = new DatagramSocket(rtcpPort);
		 CNAME = aCNAME;
	 }
	 
	 // Add separate constructor for multicast
	 
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
	 
	 public int sendData(byte[] buf) {
		if(RTPSession.rtpDebugLevel > 5) {
				System.out.println("-> RTPSession.sendData(byte[])");
		}  
		
		// All this has to be changed to get from the participant table.
		RtpPkt pkt = new RtpPkt(System.currentTimeMillis(),ssrc,getNextSeqNum(),getPayLoadType(8),buf);
		
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
				} catch (Exception e) {
					// TODO Auto-generated catch block
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
	
	 void removeParticipant(Participant p) {
		partDb.removeParticipant(p);
	}
	 
	 public void startRTCPSession() {
		 rtcpSession = new RTCPSession(this);
	 }

	void endSession() {
		this.endSession = true;
	}
	
	boolean isEnding() {
		return this.endSession;
	}

	public void setCNAME(String cname) {
		this.CNAME = cname;
	}
	
	int getNextSeqNum() {
		seqNum++;
		if(seqNum < 0) { 
			seqNum = 0;
		}
		return seqNum;
	}
	
	/* A mapping of the payload type to the payload number has to be provided*/
	int getPayLoadType(int payLoad) {
		int payLd = 0;
		return payLd;
	}
}

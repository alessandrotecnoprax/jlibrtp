package jlibrtp;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.*;
import java.util.Random;

public class RTPSession {
	 final static public int rtpDebugLevel = 3;
	 // This thing ought to be indexed by SSRC, since that is what we'll be getting constantly.
	 Hashtable participantTable = new Hashtable();
	 
	 // Unique ID for hash, only incremented.
	 int nextPartId = 0;
	 String CNAME = "";
	 DatagramSocket udpSock = null;
	 long ssrc;
	 long timeStamp = 0;
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
	  * Constructor
	  */
	 public RTPSession(int aPort, String aCNAME) throws Exception {
		 udpSock = new DatagramSocket(aPort);
		 CNAME = aCNAME;
		 // SSRC can't be based on CNAME since CNAME is too likely to be reused
		 Random r = new Random(System.currentTimeMillis());
		 ssrc = r.nextLong();
	 }
	 
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
		 	
		 	return 0;
		}
	}
	 
	 public int sendData(byte[] buf) {
		if(RTPSession.rtpDebugLevel > 5) {
				System.out.println("-> RTPSession.sendData(byte[])");
		}  
		
		// All this has to be changed to get from the participant table.
		RtpPkt pkt = new RtpPkt(getTimeStamp(),ssrc,getNextSeqNum(),getPayLoadType(0),buf);
		
		byte[] pktBytes = pkt.encode();
		Enumeration set = participantTable.elements();
			
		while(set.hasMoreElements()) {
			Participant p = (Participant)set.nextElement();

			if(p.isReceiver()) {
				if(RTPSession.rtpDebugLevel > 8) {
					System.out.println("RTPSenderThread.sendData() Participant: " + p.getCNAME() + "@" +  p.getInetAddress() + ":" + p.getDestPort());
				}
				try {	
					DatagramPacket packet = new DatagramPacket(pktBytes,pktBytes.length,p.getInetAddress(),p.getDestPort());
					udpSock.send(packet);
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
		participantTable.put(nextPartId++, p);
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTPSession.addParticipant( " + p.getInetAddress().toString() + ")");
		}
	}
	
	 void removeParticipant(Participant p) {
		participantTable.remove(p);
	}
	 public Participant lookupSsrc(long ssrc) {
		 Enumeration set = participantTable.elements();
		 while(set.hasMoreElements()) {
			 Participant p = (Participant)set.nextElement();
			 if(p.ssrc == ssrc) {
				 return p;
			 }
		 }
		 return null;
	 }
	 
	 public void startRTCPSession(int rtcpPort){
		 rtcpSession = new RTCPSession(rtcpPort,this);
	 }
	 public void requestBYE(String CNAME) {
		 if(rtcpSession == null) {
			 System.out.println("The RTCPSession is null");
			 return;
		 }
		 rtcpSession.requestBYE(CNAME.hashCode()); 
	 }
	 
	 // Who wants this? Should provide functions to query.
	 //Hashtable getParticipantDB()  {
	 //	 return participantDB;
	 //}

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
		if(seqNum < 10000) {
			seqNum++;
			return seqNum;
		} else {
			seqNum = 1;
			return seqNum;
		}
	}
	
	long getTimeStamp() {
		if(timeStamp < 10000) {
			timeStamp++;
			return timeStamp;
		} else {
			timeStamp = 1;
			return timeStamp;
		}
	}
	
	/* A mapping of the payload type to the payload number has to be provided*/
	int getPayLoadType(int payLoad) {
		int payLd = 0;
		return payLd;
	}
}

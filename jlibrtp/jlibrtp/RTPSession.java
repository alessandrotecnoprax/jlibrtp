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

public class RTPSession {
	 final static public int rtpDebugLevel = 10;
	 
	 Hashtable participantTable = new Hashtable();
	 // Unique ID for hash, only incremented.
	 int nextPartId = 0;
	 
	 String CNAME = "";
	 DatagramSocket udpSock = null;
	 long timeStamp = 0;
	 int seqNum = 0;
	 
	 RTPAppIntf appIntf = null;
	
	 RTCPSession rtcpSession = null;
	 RTPReceiverThread recvThrd = null;
	 AppCallerThread appCallerThrd = null;

	 // Locks the participant database. May not be useful?
	 final public Lock partDbLock = new ReentrantLock();
	 
	 // Locks all the packet buffers.
	 final public Lock condLock = new ReentrantLock();
	 final public Condition dataReady = condLock.newCondition();
	 
	 // Enough is enough, set to true when you want to quit.
	 boolean endSession = false;
	 
	 /**
	  * Constructor
	  */
	 public RTPSession(int aPort, String aCNAME) {
		 try {
			 udpSock = new DatagramSocket(aPort);
		 } catch (Exception e) {
			 System.out.println("RTPSession failed to obtain port: " + aPort);
		 }
		 endSession = true;
		 CNAME = aCNAME;
	 }
	 
	 public void RTPSessionRegister(RTPAppIntf rtpApp) {
		 //recvThrd = new RTPReceiverThread(this);
		 this.appIntf = rtpApp;
		 
		 //recvThrd.start();
	 }
	 
	 public int sendData(byte[] buf) {
		if(RTPSession.rtpDebugLevel > 3) {
				System.out.println("-> RTPSession.sendData(byte[])");
		}  
		
		// All this has to be changed to get from the participant table.
		RtpPkt pkt = new RtpPkt(getTimeStamp(),getSSRCNum(),getNextSeqNum(),getPayLoadType(0),buf);
		
		byte[] data = pkt.encode();
		Enumeration set = participantTable.elements();
			
		while(set.hasMoreElements()) {
			Participant p = (Participant)set.nextElement();
			
			if(p.isReceiver()) {
				try {
					if(RTPSession.rtpDebugLevel > 4) {
						System.out.println("RTPSenderThread: pkt.encode().length  ="+data.length + " The port="+p.getdestPort());
					}
					
					DatagramPacket packet = new DatagramPacket(data,data.length , InetAddress.getByName(p.networkAdr), p.getdestPort());
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
	}
	
	 void removeParticipant(Participant p) {
		participantTable.remove(p);
	}
	 public void startRTCPSession(int rtcpPort)
	 {
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
	
	long getSSRCNum() {
		return this.CNAME.hashCode();
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

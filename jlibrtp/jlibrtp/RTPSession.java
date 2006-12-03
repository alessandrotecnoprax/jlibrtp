package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

public class RTPSession implements RTPSessionIntf, Signalable {
	 final static public int rtpDebugLevel = 10;
	 LinkedList sendQueue = new LinkedList();
	 Hashtable participantDB = new Hashtable();
	 int participantCount=0;
	 int seqNum = 0;
	 String CNAME = "";
	 int timeStamp = 0;
	 RTPAppIntf appIntf = null;
	 Timer t = null;
	 boolean isBYERcvd = false;
	 RTCPSession rtcpSession = null;
	 RTPReceiverThread recvThrd = null;
	 
	 public RTPSession() {
		 
	 }
	 
	 public void RTPSessionRegister(String CNAME,int recvPort,RTPAppIntf rtpApp) {
		 recvThrd = new RTPReceiverThread(this,recvPort);
		 this.appIntf = rtpApp;
		 
		 recvThrd.start();
		 
		 Timer t = new Timer(20,this);
		 t.startTimer();
		 
	 }
	 
	 public int sendData(byte[] buf) {
		if(RTPSession.rtpDebugLevel > 3) {
				System.out.println("-> RTPSession.sendData(byte[])");
		}  

		RtpPkt pkt = new RtpPkt(getTimeStamp(),getSSRCNum(),getNextSeqNum(),getPayLoadType(0),buf);
		Hashtable participantTable = getParticipantDB();
		
		Enumeration set = participantTable.elements();
			
		while(set.hasMoreElements()) {
			Participant p = (Participant)set.nextElement();
			
			// This must be wrong if(p.isSender()) {
				try {
					if(RTPSession.rtpDebugLevel > 4) {
						System.out.println("RTPSenderThread: pkt.encode().length  ="+pkt.encode().length + " The port="+p.getdestPort());
					}
					
					DatagramPacket packet = new DatagramPacket(pkt.encode(),pkt.encode().length , InetAddress.getByName(p.sendingHost), p.getdestPort());
					p.getSocket().send(packet);
				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			//}
		}
		
		if(RTPSession.rtpDebugLevel > 3) {
				System.out.println("<- RTPSession.sendData(byte[])");
		}  
			
		 return 0;
	 }
	 
	 void addSendFrame(RtpPkt pkt)
	{
		sendQueue.add(pkt);
	}
	
	 RtpPkt getFrameToSend()
	{
		 if(!sendQueue.isEmpty())
		 {
			 //System.out.println("Inside tosend");
			 return (RtpPkt)sendQueue.removeLast();
		 }
		 return null;
	}
	
	
	public void addParticipant(Participant p)
	{
		participantDB.put(new Integer(participantCount++), p);
	}
	
	 void removeParticipant(Participant p)
	{
		participantDB.remove(p);
		
		
	}
	 public void startRTCPSession(int rtcpPort)
	 {
		 rtcpSession = new RTCPSession(rtcpPort,this);
	 }
	 public void requestBYE(String CNAME)
	 {
		 if(rtcpSession == null)
		 {
			 System.out.println("The RTCPSession is null");
			 return;
		 }
		 
		 rtcpSession.requestBYE(CNAME.hashCode());
		 
	 }
	 
	 Hashtable getParticipantDB()
	 {
		 return participantDB;
	 }
	
/*	public void addtoFrameBuffer(RtpPkt pkt,int frameNumber)
	{
		if(!frameBuffer.containsKey(pkt.getTimeStamp()))
		{
			frameBuffer.put((new Long(pkt.getTimeStamp())), new HashMap());
			((HashMap) frameBuffer.get(new Long(pkt.getTimeStamp()))).put(frameNumber,pkt);
		}
		else
		{
			((HashMap) frameBuffer.get(new Long(pkt.getTimeStamp()))).put(frameNumber,pkt);
			
		}
		
	}
*/	
	public void addtoFrameBuffer(ByteBuffer buf,long ssrc) {
	
		//frameBuffer.put((new Long(ssrc)), buf);

		appIntf.receiveData(buf.array());
	}
	//TODO
	void setBYERcvd(boolean istrue)
	{
		this.isBYERcvd = istrue;
	}
	
	boolean isBYERcvd()
	{
		return this.isBYERcvd;
	}
	public static void main(String args[])
	{
	//	RTPSession session = new RTPSession("ABCD");
	//	session.RTPSessionRegister("ABCD");
	}

	public void setCNAME(String cname) {
		// TODO Auto-generated method stub
		
		this.CNAME = cname;
	}
	
	int getNextSeqNum()
	{
		if(seqNum < 10000)
		{
			seqNum++;
			return seqNum;
		}
		else
		{
			seqNum = 1;
			return seqNum;
		}
	}
	
	int getSSRCNum()
	{
		return this.CNAME.hashCode();
	}
	
	int getTimeStamp()
	{
		if(timeStamp < 10000)
		{
			timeStamp++;
			return timeStamp;
		}
		else
		{
			timeStamp = 1;
			return timeStamp;
		}
	}
	
	/* A mapping of the payload type to the payload number has to be provided*/
	int getPayLoadType(int payLoad)
	{
		int payLd = 0;
		return payLd;
	}
	
	public void signalTimeout()
	{
		/* In this method periodically the data will be sent over to the application*/
		if(recvThrd != null && recvThrd.pktBuffer != null && recvThrd.pktBuffer.frameIsReady()) {
			DataFrame aFrame = recvThrd.pktBuffer.popOldestFrame();
			
			if(aFrame != null) {
				appIntf.receiveData(aFrame.data);
			}
		}
		
		t = new Timer(50,this);
	 	t.startTimer();
	       
	       
	      if(isBYERcvd())
	      {
	    	  t.stopTimer();
	      }
	}
}

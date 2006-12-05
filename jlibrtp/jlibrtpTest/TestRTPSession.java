package jlibrtpTest;
import jlibrtp.*;

public class TestRTPSession implements RTPAppIntf {
	public RTPSession rtpSession = null;
	String CNAME = "test";
	int recvPort = 4545;
	
	TestRTPSession() {	
		try {
			rtpSession = new RTPSession(recvPort, CNAME);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port: " + recvPort);
		}
		if(rtpSession != null) {
			rtpSession.RTPSessionRegister(this);
		} else {
			System.out.println("Couldn't register");
		}
		
		//rtpSession.RTPSessionRegister("ABCD",4448,this);	
		Participant p = new Participant("127.0.0.1",4545,"someone");
		p.setSSRC(CNAME.hashCode());
		//p.setIsSender();
		rtpSession.addParticipant(p);		
		rtpSession.startRTCPSession(6000);
		rtpSession.requestBYE("ABCD");
	
	}
	
	
	public void receiveData(byte[] buff, String Cname, long time) {
	String s = new String(buff);
		System.out.println("The Data has been received: "+s+ " , thank you " + Cname);
	}
	
	public static void main(String[] args) {
		TestRTPSession test = new TestRTPSession();
		try { Thread.currentThread().sleep(100); } catch (Exception e) {  };
		String str = "abcd";
		test.rtpSession.sendData(str.getBytes());
		str = "efgh";
		try { Thread.currentThread().sleep(100); } catch (Exception e) {  };
		test.rtpSession.sendData(str.getBytes());
	}




}

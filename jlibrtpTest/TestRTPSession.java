package jlibrtpTest;
import jlibrtp.*;

public class TestRTPSession implements RTPAppIntf
{

	RTPSessionIntf rtpSession = null;
	
	TestRTPSession()
	{
		rtpSession = new RTPSession();
		
		
		rtpSession.RTPSessionRegister("ABCD",4445,this);	
		Participant p = new Participant("127.0.0.1",4445,"CNAME1");
		p.setIsSender();
		rtpSession.addParticipant(p);
		rtpSession.sendData((new String("ABCD")).getBytes());
		
		rtpSession.startRTCPSession(6000);
	//	rtpSession.requestBYE("ABCD");
	
	}
	
	
	public void receiveData(byte[] buff) {
	String s = new String(buff);
		System.out.println("The Data has been recieved = "+s.length());
		
	}
	
	public static void main(String[] args) {
		
		TestRTPSession test = new TestRTPSession();
	}




}

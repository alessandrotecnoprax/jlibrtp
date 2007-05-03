package jlibrtpTest;
import java.net.DatagramSocket;

import jlibrtp.*;

public class TestRTPSession implements RTPAppIntf {
	public RTPSession rtpSession = null;
	
	TestRTPSession() {
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;
		
		try {
			rtpSocket = new DatagramSocket(6002);
			rtcpSocket = new DatagramSocket(6003);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}
		
		
		rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		
		rtpSession.RTPSessionRegister(this,null,null);
		
		
		Participant p = new Participant("127.0.0.1", 6004, 6005);
		
		rtpSession.addParticipant(p);
	}
	
	
	public void receiveData(DataFrame frame, Participant p) {
		String s = new String(frame.getConcatenatedData());
		System.out.println("The Data has been received: "+s+" , thank you "
				+p.getCNAME()+"("+p.getSSRC()+")");
	}
	
	public void userEvent(int type, Participant[] participant) {
		//Do nothing
	}
	
	public int frameSize(int payloadType) {
		return 1;
	}
	
	public static void main(String[] args) {
		TestRTPSession test = new TestRTPSession();
		//try { Thread.currentThread().sleep(10000); } catch (Exception e) {  };
		long teststart = System.currentTimeMillis();
		String str = "abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd ";
		byte[] data = str.getBytes();
		System.out.println(data.length);
		
		int i=0;
		while(i<100000) {
				test.rtpSession.sendData(data);
				//try { Thread.currentThread().sleep(500); } catch (Exception e) {  };
				i++;
		}

		long testend = System.currentTimeMillis();
		//String str = "efgh";

		//test.rtpSession.sendData(str.getBytes());
		
		System.out.println("" + (testend - teststart));
	}
}

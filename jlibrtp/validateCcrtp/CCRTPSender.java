package validateCcrtp;

import java.net.DatagramSocket;
import jlibrtp.*;

/**
 * Sends packet to the rtplisten demo program in ccrtp 1.5.x
 * 
 * Listen on port 6004, unless you modify this program.
 * 
 * @author Arne Kepp
 *
 */

public class CCRTPSender implements RTPAppIntf {
	RTPSession rtpSession = null;
	
	public CCRTPSender() {
		// Do nothing;
	}
	
	public void receiveData(byte[] data, String cname, long time) {
		System.out.println("Got data: " + new String(data));
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CCRTPSender me = new CCRTPSender();
		
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;
		
		try {
			rtpSocket = new DatagramSocket(6003);
			rtcpSocket = new DatagramSocket(6013);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}
		
		me.rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		me.rtpSession.setNaivePktReception(true);
		me.rtpSession.RTPSessionRegister(me,null);
		
		Participant p = new Participant("127.0.0.1", 6004, 6005);		
		me.rtpSession.addParticipant(p);
		
		me.rtpSession.setPayloadType(100);
		
		for(int i=0; i<10; i++) {
			String str = "Test number " + i;
			me.rtpSession.sendData(str.getBytes());
		}
	}

}

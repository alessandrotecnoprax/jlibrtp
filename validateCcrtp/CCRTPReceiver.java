package validateCcrtp;

import java.net.DatagramSocket;
import jlibrtp.*;

/**
 * Receives and prints packets sent by the rtpsend demo program in ccrtp 1.5.x
 * 
 * Send them to port 6003, unless you modify this program.
 * 
 * @author Arne Kepp
 *
 */

public class CCRTPReceiver implements RTPAppIntf {
	RTPSession rtpSession = null;
	
	public CCRTPReceiver() {
		// Do nothing;
	}
	
	public void receiveData(byte[] data, String cname, long time) {
		System.out.println("Got data: " + new String(data));
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CCRTPReceiver me = new CCRTPReceiver();
		
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
	}

}

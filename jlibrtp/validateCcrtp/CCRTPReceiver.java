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
	
	public void receiveData(DataFrame frame, Participant p) {
		System.out.println("Got data: " + new String(frame.getConcatenatedData()));
	}
	
	public void userEvent(int type, Participant[] participant) {
		//Do nothing
	}
	
	public int frameSize(int payloadType) {
		return 1;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CCRTPReceiver me = new CCRTPReceiver();
		
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;
		
		try {
			rtpSocket = new DatagramSocket(16384);
			rtcpSocket = new DatagramSocket(16385);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}
		
		me.rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		me.rtpSession.naivePktReception(true);
		me.rtpSession.RTPSessionRegister(me,null,null);
		
		Participant p = new Participant("127.0.0.1",16386,16387);		
		me.rtpSession.addParticipant(p);
	}

}

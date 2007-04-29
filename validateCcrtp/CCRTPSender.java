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
		CCRTPSender me = new CCRTPSender();
		
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
		
		Participant p = new Participant("127.0.0.1", 16386, 16387);		
		me.rtpSession.addParticipant(p);
		
		//me.rtpSession.setPayloadType(0);
		
		for(int i=0; i<10; i++) {
			String str = "Test number " + i;
			me.rtpSession.sendData(str.getBytes());
		}
	}

}

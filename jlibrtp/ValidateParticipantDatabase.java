package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class ValidateParticipantDatabase {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;
		try {
			rtpSocket = new DatagramSocket(6002);
			rtcpSocket = new DatagramSocket(6003);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}
		RTPSession rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		
		ParticipantDatabase partDb = new ParticipantDatabase(rtpSession);
		
		Participant part0 = new Participant("127.0.0.1", 4545, 4555);
		Participant part1 = new Participant("127.0.0.1", 4546, 4556);
		Participant part2 = new Participant("127.0.0.1", 4547, 4556);
		
		partDb.addParticipant(0,part0);
		partDb.addParticipant(0,part1);
		partDb.addParticipant(0,part2);
		
		partDb.debugPrint();
		
		System.out.println("********************* Removing Participant 1 (4546) ***********************");
		partDb.removeParticipant(part1);
		partDb.debugPrint();

		
		InetAddress inetAdr = null;
		try { inetAdr = InetAddress.getByName("127.0.0.1"); } catch (Exception e) { };
		
		//Participant part3 = partDb.getParticipant(inetAdr);
		//part3.ssrc = 12345678;
		System.out.println("********************* Updating Participant 3 (4546) ***********************");
		//partDb.updateParticipant(part3);
		
		partDb.debugPrint();
	}

}

package jlibrtp;


//import java.net.DatagramSocket;
//import java.net.InetAddress;
import java.util.*;

public class ValidateRtcpPkt {
	public static void main(String[] args) {
		ParticipantDatabase partDb = new ParticipantDatabase(null);
		//InetAddress test = InetAddress.getByName("127.0.0.1");
		Participant part1 = new Participant("127.0.0.1",-1, -1);
		Participant part2 = new Participant("127.0.0.2",-1, -1);
		
		part1.cname = "test3";
		part2.cname = "test2";
		part1.loc = "1231231231";
		part2.loc = "Asker";
		part1.phone = "+452 1231231";
		part2.phone = "aasdasda.asdasdas";
		
		partDb.addParticipant(part1);
		partDb.addParticipant(part2);
		
		Participant[] partArray = new Participant[2];
		partArray[0] = part1;
		partArray[1] = part2;
		
		RtcpPktSR srpkt = new RtcpPktSR(123456789,12,21);
		RtcpPktRR rrpkt = new RtcpPktRR(partArray,123456789);
		
		CompRtcpPkt compkt = new CompRtcpPkt();
		compkt.addPacket(srpkt);
		compkt.addPacket(rrpkt);
		compkt.addPacket(rrpkt);
		
		byte[] test = compkt.encode();
		
		CompRtcpPkt decomppkt = new CompRtcpPkt(test,test.length,null,partDb);
		ListIterator iter = decomppkt.rtcpPkts.listIterator();
		
		while(iter.hasNext()) {
			Object aPkt = iter.next();
			if(	aPkt.getClass() == RtcpPktRR.class) {
				RtcpPktRR pkt = (RtcpPktRR) aPkt;
				pkt.debugPrint();
			} else if(aPkt.getClass() == RtcpPktSR.class) {
				RtcpPktSR pkt = (RtcpPktSR) aPkt;
				pkt.debugPrint();
			}
		} 
		
		
	}
}

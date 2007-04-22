package jlibrtp;

import java.util.*;
import java.net.*;

public class ValidateRtcpPkt {
	
	public static void main(String[] args) {
		DatagramSocket rtpSock = null;
		DatagramSocket rtcpSock = null;
		
		try {
			rtpSock = new DatagramSocket(1233);
			rtcpSock = new DatagramSocket(1234);
		} catch (Exception e) {
			//do nothing
		}
		RTPSession rtpSession = new RTPSession(rtpSock, rtcpSock);
		
		System.out.println("************************** SSRC: " + rtpSession.ssrc + " **************************");
		ParticipantDatabase partDb = new ParticipantDatabase(rtpSession);
		//InetAddress test = InetAddress.getByName("127.0.0.1");
		Participant part1 = new Participant("127.0.0.1",12, 34);
		Participant part2 = new Participant("127.0.0.2",56, 78);
		
		part1.ssrc = 123;
		part2.ssrc = 345;
		
		InetSocketAddress testadr = null;
		
		try {
			testadr = InetSocketAddress.createUnresolved("localhost", 12371);
		} catch (Exception e) {
			// Do nothing
		}
		
		part1.cname = "test3";
		part2.cname = "test2";
		part1.loc = "1231231231";
		part2.loc = "Asker";
		part1.phone = "+452 1231231";
		part2.phone = "aasdasda.asdasdas";
		
		partDb.addParticipant(0,part1);
		partDb.addParticipant(0,part2);
		
		Participant[] partArray = new Participant[1];
		partArray[0] = part1;
		//partArray[1] = part2;

		RtcpPktRR rrpkt = new RtcpPktRR(partArray,123456789);
		RtcpPktSR srpkt = new RtcpPktSR(rtpSession.ssrc,12,21,rrpkt);
		//RtcpPktSR srpkt2 = new RtcpPktSR(rtpSession.ssrc,12,21,null);
		//rrpkt = new RtcpPktRR(partArray,1234512311);
		
		//srpkt.debugPrint();
		//rrpkt.debugPrint();
		
		CompRtcpPkt compkt = new CompRtcpPkt();
		compkt.addPacket(srpkt);
		//compkt.addPacket(rrpkt);
		//compkt.addPacket(rrpkt);
		
		byte[] test = compkt.encode();
		//System.out.print(StaticProcs.bitsOfBytes(test));
		System.out.println("****************************** DONE ENCODING *******************************");
		CompRtcpPkt decomppkt = new CompRtcpPkt(test,test.length,testadr,partDb);
		System.out.println("****************************** DONE DECODING *******************************");
		//System.out.println("Problem code:" + decomppkt.problem);
		
		ListIterator iter = decomppkt.rtcpPkts.listIterator();
		int i = 0;
		
		while(iter.hasNext()) {
			System.out.println(" i:" + i + " ");
			i++;
			
			Object aPkt = iter.next();
			if(	aPkt.getClass() == RtcpPktRR.class) {
				RtcpPktRR pkt = (RtcpPktRR) aPkt;
				//pkt.debugPrint();
			} else if(aPkt.getClass() == RtcpPktSR.class) {
				RtcpPktSR pkt = (RtcpPktSR) aPkt;
				//pkt.debugPrint();
			}
		} 

		System.out.println("****************************** BYE *******************************");
		long[] tempArray = {rtpSession.ssrc};
		byte[] tempReason = "tas".getBytes();
		RtcpPktBYE byepkt = new RtcpPktBYE(tempArray,tempReason);
		//byepkt.debugPrint();
		byepkt.encode();
		byte[] rawpktbye = byepkt.rawPkt;
		
		RtcpPktBYE byepkt2 = new RtcpPktBYE(rawpktbye,0);
		byepkt2 .debugPrint();
		
		System.out.println("****************************** SDES *******************************");
		RtcpPktSDES sdespkt = new RtcpPktSDES(true,rtpSession,null);
		rtpSession.cname = "cname123@localhost";
		rtpSession.loc = "right here";
		sdespkt.encode();
		byte[] rawpktsdes = sdespkt.rawPkt;
		RtcpPktSDES decsdespkt = new RtcpPktSDES(rawpktsdes, 0, (InetSocketAddress) rtpSock.getLocalSocketAddress() , partDb);
		//decsdespkt.debugPrint();
		//partDb.debugPrint();
		

	}
}

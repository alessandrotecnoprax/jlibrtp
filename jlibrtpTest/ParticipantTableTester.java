package jlibrtpTest;

import java.util.Enumeration;

import jlibrtp.*;

public class ParticipantTableTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RTPSession session = null;
		try {
			session = new RTPSession(4523, "test");
		} catch(Exception e) {
			//
		}
		
		Participant p = new Participant("127.0.0.1", 6666, "test2");
		
		//session.addParticipant(p);
		//session.participantTable.put(new String(p.cname), p);
		
		Participant p2 = null;
		boolean temp = true;
		// Make sure we can get it
		//Enumeration set = session.participantTable.elements();
		 //while(set.hasMoreElements() && temp == true) {
		//	 p2 = (Participant)set.nextElement();
		//	 if(p2.getCNAME().equalsIgnoreCase("test2")) {
		//		 System.out.println("yappa");
		//		 temp = false;
		//	 }
		 //}
		 
		 // Now set ssrc to something:
		 //int something = p2.setSSRC(5555);
		 
		//set = session.participantTable.elements();
		//	while(set.hasMoreElements()) {
		//		 p2 = (Participant)set.nextElement();
		//		 if(p2.getSSRC() == 5555) {
		//			 System.out.println("yappa");
		//		 } else {
		//			 System.out.println("nooo: " + something + " " + p2.getSSRC());
		//		 }
		//	 }
	}
}

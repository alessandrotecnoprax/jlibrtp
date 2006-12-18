/**
 * Java RTP Library
 * Copyright (C) 2006 Arne Kepp
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jlibrtp;


/**
 * Validates the PktBuffer and associated classes.
 * 
 * @author Arne Kepp
 *
 */
public class ValidatePktBuffer {

	/**
	 * Instantiates a buffer, creates some packets, adds them and sorts them.
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str1 = "ab";
		String str2 = "cd";
		String str3 = "ef";
		String str4 = "gh";
		String str5 = "ij";
		String str6 = "kl";
		//String str7 = "mn";
		
		long syncSource1 = 1;
		long timeStamp1 = 1;
		long timeStamp2 = 2;
		long timeStamp3 = 3;
		long timeStamp4 = 4;
		int seqNumber1 = 1;
		//int seqNumber2 = 1;
		RtpPkt pkt1 = new RtpPkt(timeStamp1, syncSource1, seqNumber1++, 0, str1.getBytes());
		RtpPkt pkt2 = new RtpPkt(timeStamp1, syncSource1, seqNumber1++, 0, str2.getBytes());
		RtpPkt pkt3 = new RtpPkt(timeStamp2, syncSource1, seqNumber1++, 0, str3.getBytes());
		RtpPkt pkt4 = new RtpPkt(timeStamp2, syncSource1, seqNumber1++, 0, str4.getBytes());
		RtpPkt pkt5 = new RtpPkt(timeStamp3, syncSource1, seqNumber1++, 0, str5.getBytes());
		RtpPkt pkt6 = new RtpPkt(timeStamp3, syncSource1, seqNumber1++, 0, str6.getBytes());
		RtpPkt pkt7 = new RtpPkt(timeStamp4, syncSource1, seqNumber1++, 0, str1.getBytes());
		RtpPkt pkt8 = new RtpPkt(timeStamp4, syncSource1, seqNumber1++, 0, str2.getBytes());
		
		PktBuffer pktBuf = new PktBuffer(pkt1,2);
		pktBuf.addPkt(pkt3);
		pktBuf.addPkt(pkt2);
		DataFrame aFrame = pktBuf.popOldestFrame();
		String outStr = new String(aFrame.data);
		System.out.println("1 Data from first frame: " + outStr + ", should be abcd");
		pktBuf.addPkt(pkt4);
		pktBuf.addPkt(pkt8);
		pktBuf.addPkt(pkt7);
		pktBuf.addPkt(pkt6);
		pktBuf.addPkt(pkt5);
		// Pop second frame
		aFrame = pktBuf.popOldestFrame();
		outStr = new String(aFrame.data);
		System.out.println("2 Data from second frame: " + outStr + ", should be efgh");
		// Pop third frame
		aFrame = pktBuf.popOldestFrame();
		outStr = new String(aFrame.data);
		System.out.println("3 Data from third frame: " + outStr + ", should be ijkl");
		System.out.println("4 pktBuf.getLength is " + pktBuf.getLength() + ", should be 1");
		
		aFrame = pktBuf.popOldestFrame();
		outStr = new String(aFrame.data);
		System.out.println("5 Data from fourth frame: " + outStr + ", should be abcd");
	}

}

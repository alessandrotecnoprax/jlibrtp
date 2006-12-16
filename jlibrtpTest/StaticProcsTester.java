package jlibrtpTest;

import jlibrtp.*;

public class StaticProcsTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long one = 100;
		long two = 100000;
		long three = 9999000;
		
		//byte[] oneb = StaticProcs.longToByteWord(one);
		byte[] twob = StaticProcs.longToByteWord(two);
		//byte[] threeb = StaticProcs.longToByteWord(three);
		
		for(int i = 0; i< 4; i++) {
			StaticProcs.printBits(twob[i]);
		}
		//one = StaticProcs.combineBytes(oneb[0], oneb[1], oneb[2], oneb[3]);
		two = StaticProcs.combineBytes(twob[0], twob[1], twob[2], twob[3]);
		//three = StaticProcs.combineBytes(threeb[0], threeb[1], threeb[2], threeb[3]);
		
		System.out.println(" " + one + " " + two + " " + three);
		
		twob = StaticProcs.longToByteWord(two);
		for(int i = 0; i< 4; i++) {
			StaticProcs.printBits(twob[i]);
		}
	}

}

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
 * Validates the StaticProcs.
 * 
 * @author Arne Kepp
 *
 */
public class ValidateStaticProcs {

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
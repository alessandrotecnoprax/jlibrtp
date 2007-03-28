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
 * Generic functions for converting between unsigned integers and byte[]s.
 *
 * @author Arne Kepp
 */
public class StaticProcs {

	/** 
	 * Converts an integer into an array of bytes. 
	 * Primarily used for 16 bit unsigned integers, ignore the first two octets.
	 * 
	 * @param i a 16 bit unsigned integer in an int
	 * @return byte[2] representing the integer as unsigned, most significant bit first. 
	 * @author Arne Kepp
	 */
	public static byte[] uIntIntToByteWord(int i) {		
		byte[] byteWord = new byte[2];
		byteWord[0] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[1] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	/** 
	 * Converts an unsigned 32 bit integer, stored in a long, into an array of bytes.
	 * 
	 * @param j a long
	 * @return byte[4] representing the unsigned integer, most significant bit first. 
	 * @author Arne Kepp
	 */
	public static byte[] uIntLongToByteWord(long j) {
		int i = (int) j;
		byte[] byteWord = new byte[4];
		byteWord[0] = (byte) ((i >>> 24) & 0x000000FF);
		byteWord[1] = (byte) ((i >> 16) & 0x000000FF);
		byteWord[2] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[3] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	/** 
	 * Combines two bytes (most significant bit first) into a 16 bit unsigned integer.
	 * 
	 * @param index of most significant byte
	 * @return int with the 16 bit unsigned integer
	 * @author Arne Kepp
	 */
	public static int bytesToUIntInt(byte[] bytes, int index) {
		int accum = 0;
		int i = 1;
		for (int shiftBy = 0; shiftBy < 16; shiftBy += 8 ) {
			accum |= ( (long)( bytes[index + i] & 0xff ) ) << shiftBy;
			i--;
		}
		return accum;
	}
	
	/** 
	 * Combines four bytes (most significant bit first) into a 32 bit unsigned integer.
	 * 
	 * @param bytes
	 * @param index of most significant byte
	 * @return long with the 32 bit unsigned integer
	 * @author Arne Kepp
	 */
	public static long bytesToUIntLong(byte[] bytes, int index) {
		long accum = 0;
		int i = 3;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( bytes[index + i] & 0xff ) ) << shiftBy;
			i--;
		}
		return accum;
	}
	
	public static long undoNtpMess(long ntpTs1, long ntpTs2) {		
		long timeVal = (ntpTs1 - 2208988800L)*1000;
			
		double tmp = (1000.0*(double)ntpTs2)/((double)4294967295L);
		long ms = (long) tmp;
		//System.out.println(" timeVal: " +Long.toString(timeVal)+ " ms " + Long.toString(ms));
		timeVal += ms;
		
		return timeVal;
	}
	
	/** 
	 * Get the bits of a byte
	 * 
	 * @param aByte the byte you wish to convert
	 * @return a String of 1's and 0's
	 * @author Arne Kepp
	 */
	public static String bitsOfByte(byte aByte) {
		int temp;
		String out = "";
		for(int i=7; i>=0; i--) {
			temp = (aByte >>> i);
			temp &= 0x0001;
			out += (""+temp);
		}
		return out;
	}
	
	/** 
	 * Get the hex representation of a byte
	 * 
	 * @param aByte the byte you wish to convert
	 * @return a String of two chars 0-1,A-F
	 * @author Arne Kepp
	 */
	public static String hexOfByte(byte aByte) {
		String out = "";

		for(int i=0; i<2; i++) {
			int temp = (int) aByte;
			if(temp < 0) {
				temp +=256;
			}
			if(i == 0) {
				temp = temp/16;
			} else {
				temp = temp%16;
			}
			
			if( temp > 9) {
				switch(temp) {
				case 10: out += "A"; break;
				case 11: out += "B"; break;
				case 12: out += "C"; break;
				case 13: out += "D"; break;
				case 14: out += "E"; break;
				case 15: out += "F"; break;
				}
			} else {
				out += ""+temp;
			}
		}
		return out;
	}
	
	
	/** 
	 * Print the bits of a byte to standard out. For debugging.
	 * 
	 * @param aByte the byte you wish to print out.
	 * @author Arne Kepp
	 */
	public static void printBits(byte aByte) {
		int temp;
		for(int i=7; i>=0; i--) {
			temp = (aByte >>> i);
			temp &= 0x0001;
			System.out.print(""+temp);
		}
		System.out.println();
	}
}
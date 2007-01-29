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
	 * @param i an integer
	 * @return byte[4] representing the integer as unsigned, most significant bit first. 
	 * @author Arne Kepp
	 */
	public static byte[] intToByteWord(int i) {
		byte[] byteWord = new byte[4];
		byteWord[0] = (byte) ((i >>> 24) & 0x000000FF);
		byteWord[1] = (byte) ((i >> 16) & 0x000000FF);
		byteWord[2] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[3] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	/** 
	 * Converts an unsigned 32 bit integer, stored in a long, into an array of bytes.
	 * 
	 * @param j a long
	 * @return byte[4] representing the unsigned integer, most significant bit first. 
	 * @author Arne Kepp
	 */
	public static byte[] longToByteWord(long j) {
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
	 * @param highOrder most significant byte
	 * @param lowOrder least significant byte
	 * @return int with the 16 bit unsigned integer
	 * @author Arne Kepp
	 */
	public static int combineBytes(byte highOrder, byte lowOrder) {
		int temp = highOrder;
		temp = (temp << 8);
		temp |= lowOrder;
		return temp;
	}
	
	/** 
	 * Combines four bytes (most significant bit first) into a 32 bit unsigned integer.
	 * 
	 * @param highOrder most significant byte
	 * @param highMidOrder 2nd most significant byte
	 * @param lowMidOrder 3rd most significant byte
	 * @param lowOrder least significant byte
	 * @return long with the 32 bit unsigned integer
	 * @author Arne Kepp
	 */
	public static long combineBytes(byte highOrder, byte highMidOrder, byte lowMidOrder, byte lowOrder) {		
		byte[] arr = new byte[4];
		arr[0] = lowOrder;
		arr[1] = lowMidOrder;
		arr[2] = highMidOrder;
		arr[3] = highOrder;
		
		long accum = 0;
		int i = 0;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( arr[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return accum;
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
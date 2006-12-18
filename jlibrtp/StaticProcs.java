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
	
	//------------------ Need to move code from RTCP-files here and clean up -------------
	
	/** 
	 * Converts 32 bit uint (in long) to binary
	 * 
	 * @param srcPort 32 bit integer (in long)
	 * @return byte[32] with one bit in each byte
	 * @author Vaishnav Janardhan
	 */
	public static byte[] longToBin(long srcPort)
	{
		byte srcPortByte[] = new byte[32];
		for(int i=0;i<32;i++)
			srcPortByte[i]=0;

	
	
	
	    long N = Long.parseLong((new Long(srcPort)).toString());
	    int ccount=0;
	    byte bd[]= new byte[32];
	    // find largest power of two that is less than or equal to N
	    int v = 1;
	    while (v <= N/2)
	        v = v * 2;
	
	    // cast out powers of 2 
	    while (v > 0) {
	       if (N < v)
	       {
	    	//   System.out.print(0);
	    	   bd[ccount++]=(byte) 0;
	       }
	       else
	       {
	    	//   System.out.print(1); 
	    	   N = N - v;
	    	   bd[ccount++]=(byte) 1;
	       }
	       v = v / 2;
	    }
	   /* for(int i=0;i<ccount;i++)
	    	System.out.print(bd[i]);
	    */

		
		if(ccount<32)
		{

			for(int i=(32-ccount),cc=0;i<32;i++)
			{
			//	srcPortByte[i]=bd[i-1];
				srcPortByte[i]=bd[cc++];
			}
		}
		else
		{
			for(int i=0;i<32;i++)
			{
				srcPortByte[i] = bd[i];
			}
		}
		
	    return srcPortByte;
	
	}
	
	/** 
	 * Converts byte[32] to 32 bit uint (in long)
	 * 
	 * @param bin with one bit in each byte
	 * @return 32 bit integer (in long)
	 * @author Vaishnav Janardhan
	 */
	static long longBin2Dec(byte[] bin)   
	{
	  int  b, k, m, n;
	  int  len;
	  long sum = 0;
	 
	  len = bin.length - 1;
	  for(k = 0; k <= len; k++) 
	  {
	    //n = (bin[k] - '0'); // char to numeric value
		  n = (bin[k] ); // char to numeric value
	    if ((n > 1) || (n < 0)) 
	    {
	      System.out.println("\n\n ERROR! BINARY has only 1 and 0!\n");
	      return (0);
	    }
	    for(b = 1, m = len; m > k; m--) 
	    {
	      // 1 2 4 8 16 32 64 ... place-values, reversed here
	      b *= 2;
	    }
	    // sum it up
	    sum = sum + n * b;
	    //printf("%d*%d + ",n,b);  // uncomment to show the way this works
	  }
	  return(sum);
	}

	/** 
	 * Converts 32 bit int (in int) to binary
	 * 
	 * @param srcPort 32 bit integer (in int)
	 * @return byte[32] with one bit in each byte
	 * @author Vaishnav Janardhan
	 */
	public static byte[] intToBin(int srcPort)
	{
		byte srcPortByte[] = new byte[32];
		for(int i=0;i<32;i++)
			srcPortByte[i]=0;

	
	
	
	    int N = Integer.parseInt((new Integer(srcPort)).toString());
	    int ccount=0;
	    byte bd[]= new byte[32];
	    // find largest power of two that is less than or equal to N
	    int v = 1;
	    while (v <= N/2)
	        v = v * 2;
	
	    // cast out powers of 2 
	    while (v > 0) {
	       if (N < v)
	       {
	    	//   System.out.print(0);
	    	   bd[ccount++]=(byte) 0;
	       }
	       else
	       {
	    	//   System.out.print(1); 
	    	   N = N - v;
	    	   bd[ccount++]=(byte) 1;
	       }
	       v = v / 2;
	    }
	    for(int i=0;i<ccount;i++)
	    	System.out.print(bd[i]);
	    

		
		if(ccount<32)
		{

			for(int i=(32-ccount),cc=0;i<32;i++)
			{
			//	srcPortByte[i]=bd[i-1];
				srcPortByte[i]=bd[cc++];
			}
		}
		else
		{
			for(int i=0;i<32;i++)
			{
				srcPortByte[i] = bd[i];
			}
		}
		
	    return srcPortByte;
	
	}
	
	/** 
	 * Converts byte[32] to 32 bit int (in int)
	 * 
	 * @param bin with one bit in each byte
	 * @return 32 bit integer
	 * @author Vaishnav Janardhan
	 */
	static int intBin2Dec(byte[] bin)   
	{
	  int  b, k, m, n;
	  int  len;
	  int sum = 0;
	 
	  len = bin.length - 1;
	  for(k = 0; k <= len; k++) 
	  {
	    //n = (bin[k] - '0'); // char to numeric value
		  n = (bin[k] ); // char to numeric value
	    if ((n > 1) || (n < 0)) 
	    {
	      System.out.println("\n\n ERROR! BINARY has only 1 and 0!\n");
	      return (0);
	    }
	    for(b = 1, m = len; m > k; m--) 
	    {
	      // 1 2 4 8 16 32 64 ... place-values, reversed here
	      b *= 2;
	    }
	    // sum it up
	    sum = sum + n * b;
	    //printf("%d*%d + ",n,b);  // uncomment to show the way this works
	  }
	  return(sum);
	}
}
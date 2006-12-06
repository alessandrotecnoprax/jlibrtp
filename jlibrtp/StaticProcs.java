package jlibrtp;
/**
 * These functions are ridiculously expensive, better way?
 * 
 * Thought: Only return array for the specific character, combine arrays once outside of these functions.
 */

public class StaticProcs {
	public static byte[] intToByteWord(int i) {
		byte[] byteWord = new byte[4];
		byteWord[0] = (byte) ((i >>> 24) & 0x000000FF);
		byteWord[1] = (byte) ((i >> 16) & 0x000000FF);
		byteWord[2] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[3] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	public static byte[] longToByteWord(long j) {
		int i = (int) j;
		byte[] byteWord = new byte[4];
		byteWord[0] = (byte) ((i >>> 24) & 0x000000FF);
		byteWord[1] = (byte) ((i >> 16) & 0x000000FF);
		byteWord[2] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[3] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	public static int combineBytes(byte highOrder, byte lowOrder) {
		int temp = highOrder;
		temp = (temp << 8);
		temp |= lowOrder;
		return temp;
	}
	
	public static long combineBytes(byte highOrder, byte highMidOrder, byte lowMidOrder, byte lowOrder) {		
		byte[] arr = new byte[4];
		arr[0] = lowOrder;
		arr[1] = lowMidOrder;
		arr[2] = highMidOrder;
		arr[3] = highOrder;
		int start = 0;
		
		// From http://www.captain.at/howto-java-convert-binary-data.php
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			cnt++;
		}
		long accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return accum;
		
		/** This can have issues.
		System.out.println("test");
		long temp = highOrder;
		System.out.println(temp);
		temp = (temp << 8);
		System.out.println(temp);
		temp |= highMidOrder;
		System.out.println(temp);
		temp = (temp << 8);
		System.out.println(temp);
		temp |= lowMidOrder;
		System.out.println(temp);
		temp = (temp << 8);
		System.out.println(temp);
		temp |= lowOrder;
		System.out.println(temp);
		return temp;
		**/
		}

		
	public static long combineChars(char highOrder, char lowOrder) {
		long temp = highOrder;
		temp = (temp << 16);
		temp |= lowOrder;
		return temp;
	}
	public static void printBits(byte aByte) {
		int temp;
		for(int i=7; i>=0; i--) {
			temp = (aByte >>> i);
			temp &= 0x0001;
			System.out.print(""+temp);

		}
		System.out.println();
	}
	public static void printBits(char aChar) {
		int temp;
		for(int i=15; i>=0; i--) {
			temp = (aChar >>> i);
			temp &= 0x00000001;
			System.out.print(""+temp);

		}
		System.out.println();
	}
}
package jlibrtp;
/**
 * These functions are ridiculously expensive, better way?
 * 
 * Thought: Only return array for the specific character, combine arrays once outside of these functions.
 */

public class StaticProcs {
	static byte[] intToByteWord(int i) {
		byte[] byteWord = new byte[4];
		byteWord[0] = (byte) ((i >>> 24) & 0x000000FF);
		byteWord[1] = (byte) ((i >> 16) & 0x000000FF);
		byteWord[2] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[3] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	static byte[] longToByteWord(long j) {
		int i = (int) j;
		byte[] byteWord = new byte[4];
		byteWord[0] = (byte) ((i >>> 24) & 0x000000FF);
		byteWord[1] = (byte) ((i >> 16) & 0x000000FF);
		byteWord[2] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[3] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	static int combineBytes(byte highOrder, byte lowOrder) {
		int temp = highOrder;
		temp = (temp << 8);
		temp |= lowOrder;
		return temp;
	}
	
	static long combineBytes(byte highOrder, byte highMidOrder, byte lowMidOrder, byte lowOrder) {
		long temp = highOrder;
		temp = (temp << 8);
		temp |= highMidOrder;
		temp = (temp << 8);
		temp |= lowMidOrder;
		temp = (temp << 8);
		temp |= lowOrder;
		return temp;
	}
	static long combineChars(char highOrder, char lowOrder) {
		long temp = highOrder;
		temp = (temp << 16);
		temp |= lowOrder;
		return temp;
	}
	static void printBits(char aChar) {
		int temp;
		for(int i=15; i>=0; i--) {
			temp = (aChar >>> i);
			temp &= 0x00000001;
			System.out.print(""+temp);

		}
		System.out.println();
	}
}
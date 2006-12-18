package jlibrtp;
/**
 * Java RTP Library
 * Copyright (C) 2006 Vaishnav Janardhan
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



import java.sql.Timestamp;
/**
 * RTCP Receiver Report Packet. This class will periodically
 * send the Receiver Reports to all the participants in the
 * participant database. The RR message will include the statistics
 * collected since the last RR packet was sent to all of the recivers.
 * The statistics are updated at the RTPReceiverThread.
 *  
 * @author Vaishnav Janardhan
 */

public class RTCPRRPkt
{
	long ssrc; 
	int fractionlost = 0;
	int packetslost = 0;
	int exthighseqnr=0;
	long jitter=0;
	long lsr=0;
	long dlsr=System.currentTimeMillis();
	RTCPCommonHeader commonHdr = null;
	byte[] rawRRPkt = new byte[32*8];
	
	RTCPRRPkt()
	{
		
	}
	/**
	 * Constructor for the construction of RRPkt
	 * @param ssrc
	 */
	RTCPRRPkt(long ssrc)
	{
		this.ssrc = ssrc;
		commonHdr =  new RTCPCommonHeader(2,0,1,201);
		
	
		this.commonHdr.pktType = 201;
		this.commonHdr.padding = 0;
		this.commonHdr.pktLen = 192;
	}
	
	void setExtHighSeqNumRcvd(int seqNum)
	{
		this.exthighseqnr = seqNum;
	}
	int getExtHighSeqNumRcvd()
	{
		return this.exthighseqnr;
	}
	
	void setLSR(long lsr)
	{
		this.lsr = lsr;
	}
	
	long getLSR()
	{
		return this.lsr;
	}
	
	long getDLSR()
	{
		return (System.currentTimeMillis() - this.lsr);
	}
	
	void incPktLostCount()
	{
		this.packetslost +=1;
	}
	
	int getPktLostCount()
	{
		return this.packetslost;
	}
	/**
	 * Function to encode the RR packet with all the statistics
	 * for the SSRC of the receiver to be reported.
	 * @param reporteeSSRC
	 * @return
	 */
	byte[] encodeRRPkt(long reporteeSSRC)
	{
		 
		byte[] firstLine = commonHdr.writeFristLine();
		
		System.arraycopy(firstLine, 0, this.rawRRPkt,0,32);
		
//		System.out.println("The Session SSRC="+this.ssrc+" The Participant SSRC="+reporteeSSRC);
		byte[] reporterSSRCArry = longToBin(this.ssrc);
		System.arraycopy(reporterSSRCArry, 0, this.rawRRPkt, 32, 32);
		
		byte[] reporteeSSRCArry = longToBin(reporteeSSRC);
		System.arraycopy(reporteeSSRCArry, 0, this.rawRRPkt, 64, 32);
		
		//// Left the calculation of the loss fraction. The pkt Lost is full 32 bits
		byte[] cumPktLostArry = intToBin(this.getPktLostCount());
		System.arraycopy(cumPktLostArry, 0, this.rawRRPkt, 96, 32);
		this.packetslost = 0;
		
		//// Left out Interval Jitter for now
		byte[] lsrArry = longToBin2(this.getLSR());
		System.arraycopy(lsrArry, 0, this.rawRRPkt, 128, 32);
		
//		byte[] dlsrArry = longToBin(this.getDLSR());
	//	byte[] dlsrArry = longToBin(32323232);
	//	byte[] dlsrArry = longToBin(this.getDLSR());
		byte[] dlsrArry = longToBinDLS(System.currentTimeMillis());
		System.arraycopy(dlsrArry, 0, this.rawRRPkt, 160, 32);
		
		return this.rawRRPkt;
	
		
	}
	/**
	 * The function to decode the received RR packet and display the statistics
	 * @param rcvdPkt
	 */
	void decodeRRPkt(byte[] rcvdPkt)
	{
		byte[] reporterSSRCArry = new byte[32]; 
			System.arraycopy(rcvdPkt,32,reporterSSRCArry, 0, 32);
		
		System.out.println("The Reported SSRC="+longBin2Dec(reporterSSRCArry));
		
		byte[] reporteeSSRCArry = new byte[32];
		System.arraycopy(rcvdPkt,64,reporteeSSRCArry, 0, 32);
		System.out.println("The Reportee SSRC="+longBin2Dec(reporteeSSRCArry));
		
		byte[] cumPktLostArry = new byte[32];
		System.arraycopy(rcvdPkt,96,cumPktLostArry, 0, 32);
		System.out.println("The Cumulative Packet lost="+intBin2Dec(cumPktLostArry));
		
		byte[] lsrArry = new byte[32];
		System.arraycopy(rcvdPkt,128,lsrArry, 0, 32);
	//System.out.println("The Last time Receiver Report sent="+longBin2Dec(lsrArry));
		System.out.println("The Last time Receiver Report sent="+binLongToDec(lsrArry));
		
		byte[] dlsrArry = new byte[32];
		System.arraycopy(rcvdPkt,160,dlsrArry, 0, 32);
	//System.out.println("The Last time Receiver Report sent="+longBin2Dec(lsrArry));
		//System.out.println("The Delay since last Receiver Report sent="+longBin2Dec(lsrArry));
		System.out.println("The Delay since last Receiver Report sent="+(new String(dlsrArry)));
	}
	
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
	public static byte[] longToBin2(long srcPort)
	{
		Timestamp t = new Timestamp(srcPort);
		byte[] bd = t.toString().getBytes();
		
		int ccount = bd.length;
		byte[] srcPortByte = new byte[32];
		
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
		
/*		for(int i=0;i<ccount;i++)
		{
			srcPortByte[i]=bd[i];
		}
		for(int i=ccount-1;i<32;i++)
		{
			srcPortByte[i]=(byte)0;
		}*/
		return srcPortByte;
	}
	static String binLongToDec(byte [] bin)
	{
		int x=0,ii=0;
		while(bin[x]==(byte)0)
		{
			ii++;
			x++;
		}
		byte[] bx = new byte[32-ii];

		for(int i=0;i<(32-ii);i++)
		{
			bx[i]=bin[x++];

		}
		String ss = new String(bx);
		return ss;
			
	}
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
	
	public static byte[] longToBinDLS(long srcPort)
	{
		Long ll = new Long(srcPort);
		byte[] bd = ll.toString().getBytes();
		int ccount = bd.length;
		byte[] srcPortByte = new byte[32];
		
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
		
/*		for(int i=0;i<ccount;i++)
		{
			srcPortByte[i]=bd[i];
		}
		for(int i=ccount-1;i<32;i++)
		{
			srcPortByte[i]=(byte)0;
		}*/
		return srcPortByte;
	}
}

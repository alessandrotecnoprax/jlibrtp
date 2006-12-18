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
package jlibrtp;


import java.sql.Timestamp;

/**
 * RTCP Sender Reports
 * 
 * @author Vaishnav Janardhan
 */
public class RTCPSenderReport
{
	
	long ntptime;
	long rtptimestamp;
	int packetcount;
	int octetcount;
	long ssrc;
	RTCPCommonHeader commonHdr = null;
	byte[] rawRRPkt = new byte[32*8];
	RTPSession rtpSession = null;
	
	RTCPSenderReport()
	{
		
	}
	RTCPSenderReport(long ssrc,RTPSession rtpSession)
	{
		this.ssrc = ssrc;
		ntptime = 0;
		packetcount = 0;
		octetcount = 0;
		rtptimestamp = System.currentTimeMillis();
		commonHdr =  new RTCPCommonHeader(2,0,1,200);
		
		this.rtpSession = rtpSession;
		this.commonHdr.pktType = 200;
		this.commonHdr.padding = 0;
		this.commonHdr.pktLen = 192;
		
	}
	
	byte[] encodeSRPkt()
	{
		byte[] firstLine = commonHdr.writeFristLine();
		
		System.arraycopy(firstLine, 0, this.rawRRPkt,0,32);
		
//		System.out.println("The Session SSRC="+this.ssrc+" The Participant SSRC="+reporteeSSRC);
		byte[] reporterSSRCArry = longToBin(this.ssrc);
		System.arraycopy(reporterSSRCArry, 0, this.rawRRPkt, 32, 32);
		
		byte[] ntpTimeArry = longToBin2(System.currentTimeMillis());
		System.arraycopy(ntpTimeArry, 0, this.rawRRPkt, 64, 32);
		
		byte[] rtpTimeArry = longToBinDLS(System.currentTimeMillis()- this.rtptimestamp);
		System.arraycopy(rtpTimeArry, 0, this.rawRRPkt, 96, 32);
		this.rtptimestamp = System.currentTimeMillis();
		
		byte[] sendPktCount = intToBin(this.rtpSession.sentPktCount);
		System.arraycopy(sendPktCount, 0, this.rawRRPkt, 128, 32);
		this.rtpSession.sentPktCount = 0;
		
		byte[] sendOctCount = intToBin(this.rtpSession.sentOctetCount);
		System.arraycopy(sendOctCount, 0, this.rawRRPkt, 160, 32);
		this.rtpSession.sentOctetCount = 0;
		
		
		return this.rawRRPkt;
		
	}
	
	void decodeSRPkt(byte[] rcvdPkt)
	{
		byte[] reporterSSRCArry = new byte[32]; 
			System.arraycopy(rcvdPkt,32,reporterSSRCArry, 0, 32);
		
		System.out.println("The Reported SSRC="+longBin2Dec(reporterSSRCArry));
		

		
		byte[] ntpTimeArry = new byte[32];
		System.arraycopy(rcvdPkt,64,ntpTimeArry, 0, 32);
		System.out.println("The NTP Time Stamp="+binLongToDec(ntpTimeArry));
		
		byte[] rtpTimeArry = new byte[32];
		System.arraycopy(rcvdPkt,96,rtpTimeArry, 0, 32);

		System.out.println("The RTP time stamp when Sender Report was sent="+(new String(rtpTimeArry)));

		
		byte[] sendPktCount = new byte[32];
		System.arraycopy(rcvdPkt,128,sendPktCount, 0, 32);
		System.out.println("The sent packet count="+intBin2Dec(sendPktCount));
		
		byte[] sendOctCount = new byte[32];
		System.arraycopy(rcvdPkt,160,sendOctCount, 0, 32);
		System.out.println("The sent Octet count="+intBin2Dec(sendOctCount));
		
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
	
	static String binLongToDec(byte [] bin)
	{
		int x=0,ii=0;
		while(bin[x]==(byte)0)
		{
			ii++;
			x++;
		}
		byte[] bx = new byte[32-ii];
//		System.out.println("VVVVVVV="+ii);
		for(int i=0;i<(32-ii);i++)
		{
			bx[i]=bin[x++];
	//		System.out.print(" "+bx[i]);
		}
		String ss = new String(bx);
		return ss;
			
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
}

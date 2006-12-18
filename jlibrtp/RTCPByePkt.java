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

/**
 * RTCP Bye Packet
 * 
 * @author Vaishnav Janardhan
 */
public class RTCPByePkt 
{
	RTCPCommonHeader commonHdr = null;
	byte[] rawBYEPkt = null;
	long[] ssrcArray = new long[32];
	
	RTCPByePkt (byte[] byePkt,RTCPCommonHeader header)
	{
		this.commonHdr = header;
		byte[] pktData = new byte[header.iCount * 32];
		
		System.arraycopy(byePkt,32, pktData,0,header.iCount * 32);
		
		decodeBYEPkt(pktData);
	}
	
	RTCPByePkt(int ssrcCount, long[] ssrcArray)
	{
		commonHdr =  new RTCPCommonHeader(2,0,ssrcCount,203);
		this.commonHdr.iCount = ssrcCount;
		for(int i=0;i<ssrcCount;i++)
		{
			this.ssrcArray[i] = ssrcArray[i];
		}
		this.commonHdr.pktType = 203;
		this.commonHdr.padding = 0;
		this.commonHdr.pktLen = (this.commonHdr.iCount+1) * 32;
		
	
		
		rawBYEPkt = new byte[(this.commonHdr.iCount+1) * 32];
		
	}
	
	int getSSRCCount()
	{

		return commonHdr.iCount;
	}
	
	long getSSRC(int index)
	{
		return ssrcArray[index];
	}
	void setSSRCCount(int count)
	{
		commonHdr.iCount = count;
	}
	
	void setSSRCValue(int ssrcValue,int index)
	{
		ssrcArray[index] = ssrcValue;
	}
	
	RTCPCommonHeader getCommonHdr()
	{
		return commonHdr;
	}
	
	void decodeBYEPkt(byte[] data)
	{
		for(int i=0;i<this.commonHdr.iCount;i++)
		{
			byte[] ssrcData = new byte[32];
			System.arraycopy(data,(i+1)*32, ssrcData,0, 32);
			ssrcArray[i] = bin2dec(ssrcData);
		}
		
	}
	
	byte[] encodeBYEPkt()
	{
		byte[] firstLine = commonHdr.writeFristLine();
		System.arraycopy(firstLine, 0, this.rawBYEPkt,0,32);
		
		for(int i=0;i<this.commonHdr.iCount;i++)
		{
			// Only commented out to avoid compile warning
			//System.arraycopy(ssrcNumHeader(ssrcArray[i]),0, this.rawBYEPkt,((i+1)*32), 32);
		}
		
		return this.rawBYEPkt;
		
	}

	
	
	
	public  byte[] ssrcNumHeader(int ssrcNumber)
	{
		byte srcPortByte[] = new byte[32];
		for(int i=0;i<32;i++)
			srcPortByte[i]=0;
	
	    int N = Integer.parseInt((new Integer(ssrcNumber)).toString());
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
	    for(int i=0;i<ccount;i++){
	    //	System.out.print(bd[i]);
	    }
	 //   System.out.println("The ccount="+ccount);
		
		if(ccount<32)
		{
	//		System.out.println("Coming here");
			for(int i=(32-ccount),cc=0;i<32;i++)
			{
			//	srcPortByte[i]=bd[i-1];
				srcPortByte[i]=bd[cc++];
			}
		}
		
	    return srcPortByte;
	
	}
	int bin2dec(byte[] bin)   
	{
	  int  b, k, m, n;
	  int  len, sum = 0;
	 
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
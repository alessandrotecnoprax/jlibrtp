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


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RTCPSDESHeader implements Signalable
{
	int sdesid;
	int length;
	RTCPSession rtcpSession = null;
	RTCPCommonHeader commonHdr = null;
	byte[] rawSDESPkt = null;
	String CNAME = null;
	long ssrc = 0;
	int rtcpPort = 0;
	RTCPSDESHeader(byte[] buf)
	{
		this.rawSDESPkt = buf;
	}
	
	RTCPSDESHeader(int rtcpPort,RTCPSession session)
	{
		this.rtcpSession = session;
		CNAME = this.rtcpSession.rtpSession.CNAME;
		ssrc = this.rtcpSession.rtpSession.ssrc;
		this.rtcpPort = rtcpPort;
		
		rawSDESPkt = new byte[32+32+CNAME.length()+32];
		
		commonHdr =  new RTCPCommonHeader(2,0,1,202);
		
		
		this.commonHdr.pktType = 202;
		this.commonHdr.padding = 0;
		this.commonHdr.pktLen = 32+32+CNAME.length()+32;
	
		sendSDESPkt();
		
		Timer t = new Timer(10000,this);
		t.startTimer();
	
	}
	
	
	void sendSDESPkt()
	{
		 
		byte[] firstLine = commonHdr.writeFristLine();
		
		System.arraycopy(firstLine, 0, this.rawSDESPkt,0,32);
		System.out.println("The SSRC in SDES Msg is SSRC="+this.ssrc);
		byte[] reporteeSSRCArry = longToBin(this.ssrc);
		System.arraycopy(reporteeSSRCArry, 0, this.rawSDESPkt, 32, 32);
		
		byte[] cnameLenArry = RTCPRRPkt.intToBin(CNAME.length());
		System.arraycopy(cnameLenArry, 0, this.rawSDESPkt, 64,32);
		
		byte[] cnameArry = CNAME.getBytes();
		System.arraycopy(cnameArry, 0, this.rawSDESPkt, 96, cnameArry.length);
		
		

		int port = this.rtcpPort;

	      String group = "225.4.5.6";

	    
	      try
	      {
			      MulticastSocket s = new MulticastSocket();

			 
			      DatagramPacket pack = new DatagramPacket(this.rawSDESPkt, this.rawSDESPkt.length,
			      					 InetAddress.getByName(group), port);

			      //s.send(pack);
			      if(RTPSession.rtpDebugLevel > 1) {
			      System.out.println("The SDES packet has been sent out port"+port);
			      }
			      //s.close();
			      if(this.rtcpSession.rtpSession.mcSession == false)
			      {
			    	  this.rtcpSession.rtpSession.rtcpSock.send(pack);
			      }
	      }
	      catch(Exception e)
	      {
	    	  e.printStackTrace();
	      }
	
		
	}
	
	byte[] encodeSDES()
	{
		return this.rawSDESPkt;
	}
	
	void decode()
	{
		byte[] ssrcArry = new byte[32]; 
			System.arraycopy(this.rawSDESPkt, 32, ssrcArry, 0, 32);
			
	   this.ssrc = longBin2Dec(ssrcArry);
	
	   byte[] cnameLenArry = new byte[32];
	   		System.arraycopy(this.rawSDESPkt, 64, cnameLenArry, 0, 32);
	   	int cnameLen = RTCPRRPkt.intBin2Dec(cnameLenArry);
	   	
	   	byte[] cnameArry = new byte[cnameLen];
	   		System.arraycopy(this.rawSDESPkt, 96, cnameArry, 0, cnameLen);
	   		
	   	this.CNAME = new String(cnameArry);
		
	}
	
	long getSSRC()
	{
		return this.ssrc;
	}
	
	String getCNAME()
	{
		return this.CNAME;
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

	public void signalTimeout() {

		sendSDESPkt();
		Timer t = new Timer(10000,this);
		t.startTimer();

		
	}

}

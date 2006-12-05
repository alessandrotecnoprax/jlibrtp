package jlibrtp;

public class RTCPRRPkt
{
	long ssrc; 
	int fractionlost = 0;
	int packetslost = 0;
	int exthighseqnr=0;
	long jitter=0;
	long lsr=0;
	long dlsr=0;
	RTCPCommonHeader commonHdr = null;
	byte[] rawRRPkt = new byte[32*8];
	
	RTCPRRPkt()
	{
		
	}
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
	
	byte[] encodeRRPkt(long reporterSSRC)
	{
		byte[] firstLine = commonHdr.writeFristLine();
		
		System.arraycopy(firstLine, 0, this.rawRRPkt,0,32);
		
		
		byte[] reporterSSRCArry = longToBin(reporterSSRC);
		System.arraycopy(reporterSSRCArry, 0, this.rawRRPkt, 32, 32);
		
		byte[] reporteeSSRCArry = longToBin(this.ssrc);
		System.arraycopy(reporteeSSRCArry, 0, this.rawRRPkt, 64, 32);
		
		//// Left the calculation of the loss fraction. The pkt Lost is full 32 bits
		byte[] cumPktLostArry = intToBin(this.getPktLostCount());
		System.arraycopy(cumPktLostArry, 0, this.rawRRPkt, 96, 32);
		
		//// Left out Interval Jitter for now
		byte[] lsrArry = longToBin(this.getLSR());
		System.arraycopy(lsrArry, 0, this.rawRRPkt, 128, 32);
		
		byte[] dlsrArry = longToBin(this.getDLSR());
		System.arraycopy(dlsrArry, 0, this.rawRRPkt, 160, 32);
		
		return this.rawRRPkt;
	
		
	}
	
	void decodeRRPkt(byte[] rcvdPkt)
	{
		byte[] reporterSSRCArry = new byte[32]; 
			System.arraycopy(rcvdPkt,32,reporterSSRCArry, 0, 32);
		System.out.println("The Reported SSRC="+longBin2Dec(reporterSSRCArry));
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
}
package jlibrtp;

public class RTCPCommonHeader
{
	int version = 2;
	int padding = 1;
	int iCount = 0;
	
	int pktType = 0;
	int pktLen = 0;
	
	RTCPCommonHeader(byte[] rtcpPktbuf)
	{
		byte[] icountbuf = new byte[5];
		System.arraycopy(rtcpPktbuf,3, icountbuf,0,5);
		this.iCount = bin2dec(icountbuf);
		
		byte[] pktypebuf = new byte[8];
		System.arraycopy(rtcpPktbuf,8, pktypebuf,0,8);
		this.pktType = bin2dec(pktypebuf);
	//	System.out.println("The data decoded in construct ="+pktypebuf[0]+" "+pktypebuf[1]+" "+pktypebuf[2]+" "+pktypebuf[3]+pktypebuf[4]+" "+pktypebuf[5]+" "+pktypebuf[6]+" "+pktypebuf[7]);
		
		byte[] pkLen = new byte[16];
		System.arraycopy(rtcpPktbuf,16, pkLen,0,16);
		this.pktLen = bin2dec(pkLen);
		
		this.version = 2;
		
		if(rtcpPktbuf[2] == (byte)1)
		{
			this.padding = 1;
		}
		else
		{
			this.padding = 0;
		}
		
		
	}
	
	RTCPCommonHeader(int version,int padding,int iCount,int pktType)
	{
		this.version = version;
		this.padding = padding;
		this.iCount = iCount;
		this.pktType = pktType;
	}
	
	
	int getVersion()
	{
		return this.version;
	}
	
	int getPaddingBit()
	{
		return this.padding;
	}
	
	int getICount()
	{
		return this.iCount;
	}
	
	int getPktType()
	{
		return this.pktType;
	}
	
	int getPktLen()
	{
		return this.pktLen;
	}
	
	byte[] writeFristLine()
	{
		byte[] firstLine = new byte[32];
		
		firstLine[0] = (byte)1;
		firstLine[1] = (byte)0;
		firstLine[2] = (byte)padding;
		
		byte[] iCByte = iCountHeader(iCount);
		
		firstLine[3] = iCByte[0];
		firstLine[4] = iCByte[1];
		firstLine[5] = iCByte[2];
		firstLine[6] = iCByte[3];
		firstLine[7] = iCByte[4];
		
	//	this.pktType = 203;
		byte[] PkT = pkType(this.pktType);
				
		for(int i=0;i<8;i++)
		{
			firstLine[8+i] = PkT[i];
		}
		
		if(pktLen == 0)
		{
			return null;
		}
	
		byte[] len = lenHeader(pktLen);
		
		for(int i=0;i<16;i++)
		{
			firstLine[16+i] = len[i];
		}
		  
		return firstLine;
	
	}
	
	
	public byte[] lenHeader(int length)
	{
		byte lenByte[] = new byte[16];
		for(int i=0;i<16;i++)
			lenByte[i]=0;

		
	    int N = Integer.parseInt((new Integer(length)).toString());
	    int ccount=0;
	    byte bd[]= new byte[16];
	    // find largest power of two that is less than or equal to N
	    int v = 1;
	    while (v <= N/2)
	        v = v * 2;
	
	    // cast out powers of 2 
	    while (v > 0) {
	       if (N < v)
	       {

	    	   bd[ccount++]=(byte) 0;
	       }
	       else
	       {
 
	    	   N = N - v;
	    	   bd[ccount++]=(byte) 1;
	       }
	       v = v / 2;
	    }
		
		if(ccount<16)
		{

			for(int i=(16-ccount),cc=0;i<16;i++)
			{

				lenByte[i]=bd[cc++];
			}
		}
		else
		{
			for(int i=0;i<16;i++)
			{
				lenByte[i] = bd[i];
			}
		}
		
	    return lenByte;
	
	}
	
	public byte[] iCountHeader(int srcPort)
	{
		byte srcPortByte[] = new byte[5];
		for(int i=0;i<5;i++)
			srcPortByte[i]=0;

	
	
	
	    int N = Integer.parseInt((new Integer(srcPort)).toString());
	    int ccount=0;
	    byte bd[]= new byte[5];
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

		
		if(ccount<5)
		{

			for(int i=(5-ccount),cc=0;i<5;i++)
			{
			//	srcPortByte[i]=bd[i-1];
				srcPortByte[i]=bd[cc++];
			}
		}
		else
		{
			for(int i=0;i<5;i++)
			{
				srcPortByte[i]=bd[i];
			}
		}
		
	    return srcPortByte;
	
	}
	
	public  byte[] pkType(int srcPort)
	{
	//	System.out.println("The Pkt Type ="+srcPort);
//		srcPort = 20;
		byte srcPortByte[] = new byte[8];
		for(int i=0;i<8;i++)
			srcPortByte[i]=0;

	
	
	
	    int N = Integer.parseInt((new Integer(srcPort)).toString());
	    int ccount=0;
	    byte bd[]= new byte[8];
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

		
		if(ccount<8)
		{

			for(int i=(8-ccount),cc=0;i<8;i++)
			{
			//	srcPortByte[i]=bd[i-1];
				srcPortByte[i]=bd[cc++];
			}
		}
		else
		{
			for(int i=0;i<8;i++)
			{
				srcPortByte[i]=bd[i];
			}
		}
		if(RTPSession.rtpDebugLevel > 1) {
		System.out.println("The data  PkT sent ="+srcPortByte[0]+" "+srcPortByte[1]+" "+srcPortByte[2]+" "+srcPortByte[3]+srcPortByte[4]+" "+srcPortByte[5]+" "+srcPortByte[6]+" "+srcPortByte[7]);
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


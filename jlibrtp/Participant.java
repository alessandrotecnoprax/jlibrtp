package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Participant {
	
	String sendingHost = new String("127.0.0.1");
	private int destPort = 32000;
	
	private boolean isSender = true;
	private InetAddress address = null;
	int ssrc = -1;
	/* Here all the participant releated data will be loaded and one socket will be created for every user*/
	
	
	private DatagramSocket socket;
	
	public Participant(String sendingHost,int port,String CNAME)
	{
		this.sendingHost = sendingHost;
		this.ssrc = CNAME.hashCode();
		this.destPort = port;
		try {
			socket = new DatagramSocket();
			address = InetAddress.getByName(sendingHost);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	DatagramSocket getSocket()
	{
		return socket;
	}
	
	public void setIsSender()
	{
		isSender = true;
	}

	public boolean isSender()
	{
		return isSender;
	}
	
	int getdestPort()
	{
		return destPort;
	}
	
	int getSSRC()
	{
		return this.ssrc;
	}
}

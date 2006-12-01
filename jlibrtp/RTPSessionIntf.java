package jlibrtp;

public interface RTPSessionIntf 
{
	public void setCNAME(String CNAME);
	
	public void RTPSessionRegister(String CNAME,int recvPort,RTPAppIntf rtpApp);
	
	void sendData(byte[] buf);
	
	public void addParticipant(Participant p);
	
	public void requestBYE(String CNAME);
	
	public void startRTCPSession(int rtcpPort);
	
}

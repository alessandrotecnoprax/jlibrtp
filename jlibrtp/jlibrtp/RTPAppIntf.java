package jlibrtp;

public interface RTPAppIntf {
	
	public void receiveData(byte[] buff, String cName, long time);
	
}

package senderDemo;

import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.lang.String;
import jlibrtp.*;

public class SenderDemo implements RTPAppIntf  {
	RTPSession rtpSession = null;
	static int pktCount = 0;
	private String filename;
	private final int EXTERNAL_BUFFER_SIZE = 320; // 1 kbyte
	
	public SenderDemo(String CNAME,int recvPort)  {
		try {
			rtpSession = new RTPSession(recvPort, CNAME);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port: " + recvPort);
		}
		if(rtpSession != null) {
			rtpSession.RTPSessionRegister(this);
		} else {
			System.out.println("Couldn't register");
		}
		//public Participant(String sendingHost,int port,String CNAME)
		Participant p = new Participant("127.0.0.1", 4545, "Receiver");
		rtpSession.addParticipant(p);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Setup");
		SenderDemo aDemo = new SenderDemo("Sender",4546);
		//aDemo.filename = "/usr/share/sounds/login.wav";
		aDemo.filename = "/home/ak/test.wav";
		aDemo.run();
		System.out.println("pktCount: " + pktCount);
	}
	
	public void receiveData(byte[] dummy1, String dummy2, long dummy3) {
		// We don't expect any data.
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("-> Run()");
		} 
		File soundFile = new File(filename);
		if (!soundFile.exists()) {
			System.err.println("Wave file not found: " + filename);
			return;
		}

		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		//AudioFormat format = audioInputStream.getFormat();
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 8000.0), 16, 1, 2, ((float) 8000.0) ,false);
		System.out.println(format.toString());

		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
		long start = System.currentTimeMillis();
		try {
			while (nBytesRead != -1) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0) {
					rtpSession.sendData(abData);
					//System.out.println("pktCount:" + pktCount + " length:"  + abData.length + " hash:" + abData[0] + abData[2] + " nbytes: " + nBytesRead);
					try { Thread.sleep(13);} catch(Exception e) {}
					pktCount++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Time: " + (System.currentTimeMillis() - start)/1000 + " s");
		//rtpSession.endSession();
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<- Run()");
		} 
	}

}

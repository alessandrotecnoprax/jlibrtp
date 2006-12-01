package senderDemo;

import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;
import jlibrtp.RTPSessionIntf;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import jlibrtp.*;

public class SenderDemo extends Thread implements RTPAppIntf  {
	RTPSessionIntf rtpSession = null;
	
	private String filename;
	private final int EXTERNAL_BUFFER_SIZE = 1024; // 1 kbyte
	
	public SenderDemo(String CNAME,int recvPort,String recvAddr)  {
		rtpSession = new RTPSession();
		rtpSession.RTPSessionRegister(CNAME,recvPort,this);	
		//public Participant(String sendingHost,int port,String CNAME)
		Participant p = new Participant(recvAddr,recvPort, CNAME);
		p.setIsSender();
		rtpSession.addParticipant(p);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Setup");
		SenderDemo aDemo = new SenderDemo("Test",45455,"127.0.0.1");
		aDemo.filename = "/usr/share/sounds/login.wav";
		aDemo.run();
	}
	
	public void receiveData(byte[] dummy) {
		
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
		AudioFormat format = new AudioFormat(encoding,((float) 44100.0), 16, 2, 4, ((float) 44100.0) ,false);
		System.out.println(format.toString());

		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

		try {
			while (nBytesRead != -1) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0)
					// This is where we send the buffer off to the recipient.
					rtpSession.sendData(abData);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<- Run()");
		} 
	}

}

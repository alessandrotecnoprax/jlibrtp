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

//public class SenderDemo implements RTPAppIntf  {
//	RTPSession rtpSession = null;
//	static int pktCount = 0;
//	private String filename;
//	private final int EXTERNAL_BUFFER_SIZE = 1024; // 1 kbyte
//	
//	public SenderDemo()  {
//		rtpSession = new RTPSession(4545,"myNameIS");
//		rtpSession.RTPSessionRegister(this);
//		
//		//public Participant(String sendingHost,int port,String CNAME)
//		Participant p = new Participant("127.0.0.1", 4546, "part1");
//		rtpSession.addParticipant(p);
//	}
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		System.out.println("Setup");
//		SenderDemo aDemo = new SenderDemo();
//		aDemo.filename = "/usr/share/sounds/login.wav";
//		aDemo.run();
//		System.out.println("pktCount: " + pktCount);
//	}
//	
//	public void receiveData(byte[] dummy) {
//		
//	}
//	
//	public void run() {
//		if(RTPSession.rtpDebugLevel > 1) {
//			System.out.println("-> Run()");
//		} 
//		File soundFile = new File(filename);
//		if (!soundFile.exists()) {
//			System.err.println("Wave file not found: " + filename);
//			return;
//		}
//
//		AudioInputStream audioInputStream = null;
//		try {
//			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
//		} catch (UnsupportedAudioFileException e1) {
//			e1.printStackTrace();
//			return;
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			return;
//		}
//
//		//AudioFormat format = audioInputStream.getFormat();
//		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
//		AudioFormat format = new AudioFormat(encoding,((float) 44100.0), 16, 2, 4, ((float) 44100.0) ,false);
//		System.out.println(format.toString());
//
//		int nBytesRead = 0;
//		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
//
//		try {
//			while (nBytesRead != -1) {
//				nBytesRead = audioInputStream.read(abData, 0, abData.length);
//				if (nBytesRead >= 0) {
//					// This is where we send the buffer off to the recipient.
//					rtpSession.sendData(abData);
//					// String aTest = "test";
//					//rtpSession.sendData(aTest.getBytes());
//					pktCount++;
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//		
//		if(RTPSession.rtpDebugLevel > 1) {
//			System.out.println("<- Run()");
//		} 
//	}
//
//}
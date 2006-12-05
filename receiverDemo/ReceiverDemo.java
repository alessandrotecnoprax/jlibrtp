package receiverDemo;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import jlibrtp.*;


public class ReceiverDemo implements RTPAppIntf {
	//test
	RTPSession rtpSession = null;
	private Position curPosition;
	private final int EXTERNAL_BUFFER_SIZE = 1024; // 1 Kbyte
	byte[] abData = null;
	int nBytesRead = 0;
	static int pktCount = 0;
	SourceDataLine auline;
	
	enum Position {
		LEFT, RIGHT, NORMAL
	};

	public void receiveData(byte[] data, String cname, long time) {
		String str = new String(data);
		System.out.println("Received data! + + pktCount : " + pktCount);
		pktCount++;
	}
	
	public ReceiverDemo(String CNAME,int recvPort)  {
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
		//Participant p = new Participant(recvAddr,recvPort, CNAME);
		//p.setIsSender();
		//rtpSession.addParticipant(p);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Setup");
		ReceiverDemo aDemo = new ReceiverDemo("Test",4545);
		aDemo.doStuff();
		System.out.println("Done");
	}
	
	public void doStuff() {
		System.out.println("-> ReceiverDemo.doStuff()");
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 44100.0), 16, 2, 4, ((float) 44100.0) ,false);
		System.out.println(format.toString());
		auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		
		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (auline.isControlSupported(FloatControl.Type.PAN)) {
			FloatControl pan = (FloatControl) auline
					.getControl(FloatControl.Type.PAN);
			if (this.curPosition == Position.RIGHT)
				pan.setValue(1.0f);
			else if (this.curPosition == Position.LEFT)
				pan.setValue(-1.0f);
		}
		
		auline.start();
		try {
			while (nBytesRead != -1) {
				//System.out.println("n");
				if(nBytesRead > 0) {
					auline.write(abData, 0, nBytesRead);
					nBytesRead = 0;
				}
				try {
					Thread.currentThread().sleep(1000);
				} catch(Exception e) {
					System.out.println("ah. disaster.");
				}
			}
		} finally {
			auline.drain();
			auline.close();
		}
	}
}

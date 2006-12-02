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


public class ReceiverDemo extends Thread implements RTPAppIntf {
	RTPSessionIntf rtpSession = null;
	private Position curPosition;
	private final int EXTERNAL_BUFFER_SIZE = 1024; // 1 Kbyte
	byte[] abData = null;
	int nBytesRead = 0;

	
	enum Position {
		LEFT, RIGHT, NORMAL
	};

	public void receiveData(byte[] data) {
		abData = data;
		nBytesRead += data.length;
		//System.out.println("Data!");
		System.out.print("-");
	}
	
	public ReceiverDemo(String CNAME,int recvPort,String recvAddr)  {
		rtpSession = new RTPSession();
		rtpSession.RTPSessionRegister(CNAME,recvPort,this);	
		//public Participant(String sendingHost,int port,String CNAME)
		//Participant p = new Participant(recvAddr,recvPort, CNAME);
		//p.setIsSender();
		//rtpSession.addParticipant(p);
		this.start();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Setup");
		ReceiverDemo aDemo = new ReceiverDemo("Test",4545,"127.0.0.1");
		while(aDemo.nBytesRead == 0) {
			//Do nothing
		}
		aDemo.run();
	}
	
	public void run() {
		System.out.println("-> Run()");
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 44100.0), 16, 2, 4, ((float) 44100.0) ,false);
		System.out.println(format.toString());
		SourceDataLine auline = null;
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
			if (curPosition == Position.RIGHT)
				pan.setValue(1.0f);
			else if (curPosition == Position.LEFT)
				pan.setValue(-1.0f);
		} 

		auline.start();
		Thread thread = new Thread();
		try {
			while (nBytesRead != -1) {
				// This is where we fill abData with data;
				// nBytesRead = audioInputStream.read(abData, 0, abData.length);
				
				if (nBytesRead > 0) {
					System.out.print(".");
					auline.write(abData, 0, nBytesRead);
				} else {
					try {
						//System.out.println("preReceiverSleep");
						Thread.sleep(20);
						//System.out.println("postReceiverSleep");
					} catch (InterruptedException e) {
						System.out.println("Timer thread was interrupted");
					}
				}
			}
		} finally {
			auline.drain();
			auline.close();
		}
		System.out.println("<- Run()");
	}

}

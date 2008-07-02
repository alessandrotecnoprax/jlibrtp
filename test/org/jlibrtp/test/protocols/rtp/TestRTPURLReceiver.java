package org.jlibrtp.test.protocols.rtp;

import java.net.URL;
import java.io.InputStream;
import java.net.URLConnection;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Line;
import javax.sound.sampled.AudioInputStream;

/**
 * <p>Title: jlibrtp</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: VoiceInteraction</p>
 *
 * @author Renato Cassaca
 * @version 1.0
 */
public class TestRTPURLReceiver {

    static {
        registerProtocolHandlers();
    }

    private static void registerProtocolHandlers() {
        //Register protocol handler
        String javaPropName = "java.protocol.handler.pkgs";

        //Start value
        System.out.println(javaPropName + " = " + System.getProperty(javaPropName));

        //Vou actualizar a propriedade que define o meu protocol handler (URL)
        String packageName = "org.jlibrtp.protocols";
        System.setProperty(javaPropName, packageName);

        //Value after update
        System.out.println(javaPropName + " = " + System.getProperty(javaPropName));
    }


    public TestRTPURLReceiver() {
        super();
    }

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.FINEST);
        TestRTPURLReceiver testrtpurlreceiver = new TestRTPURLReceiver();
        testrtpurlreceiver.doIt();
    }

    /**
     * doIt
     */
    private void doIt() {
        try {
            // This block configure the logger with handler and formatter
            FileHandler fh = new FileHandler("Receiver.log", false);
            Logger logger = Logger.getLogger("org.jlibrtp");
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);


            AudioFormat receiveFormat = new AudioFormat(AudioFormat.Encoding.ULAW,
                                        8000,
                                        8,
                                        1,
                                        1,
                                        8000,
                                        false);

            AudioFormat playFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    8000,
                    16,
                    1,
                    2,
                    8000,
                    false);


            Info playbackLineInfo = new Info(SourceDataLine.class, playFormat, AudioSystem.NOT_SPECIFIED);
            SourceDataLine sourceLine = (SourceDataLine)AudioSystem.getLine(playbackLineInfo);
            System.out.println("Playing to: "+playbackLineInfo);
            sourceLine.open();
            sourceLine.start();


            URL recvURL = new URL("rtp://localhost:30000/audio?rate=8000&keepAlive=false");
            URLConnection recvC = recvURL.openConnection();
            recvC.connect();
            InputStream rtpIS = recvC.getInputStream();

            byte[] buffer = new byte[1024];
            int br;
            OutputStream os = new FileOutputStream("rtp_received.raw");

            AudioInputStream receiveStream = new AudioInputStream(rtpIS, receiveFormat, AudioSystem.NOT_SPECIFIED);
            AudioInputStream convStream = AudioSystem.getAudioInputStream(playFormat, receiveStream);

            while ((br = convStream.read(buffer)) != -1) {
                os.write(buffer, 0, br);
                sourceLine.write(buffer, 0, br);
            }

            os.close();
            rtpIS.close();
            convStream.close();
            receiveStream.close();

            System.out.println("Finished Receiver");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

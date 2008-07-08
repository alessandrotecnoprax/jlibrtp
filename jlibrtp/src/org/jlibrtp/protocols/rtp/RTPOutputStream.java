package org.jlibrtp.protocols.rtp;

import org.jlibrtp.RTPSession;
import java.io.OutputStream;
import java.io.IOException;
import com.Ostermiller.util.CircularByteBuffer;

/**
 * <p>Title: RTPOutputStream </p>
 *
 * <p>Description: Output stream that sends the audio in "real time"</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: www.VoiceInteraction.pt</p>
 *
 * @author Renato Cassaca
 * @version 1.0
 */
public class RTPOutputStream extends OutputStream {

    //RTPSession
    private final RTPSession rtpSession;

    //Number of RTP packets that should be sent per second
    private final long packetSize;

    //The time in seconds of each frame contents
    private final double packetDuration;

    //Buffer that will store bytes to send
    private final CircularByteBuffer circularByteBuffer;

    //The next packet timestamp
    private long pktTmestamp;

    //Buffer that hols temporary read data
    private final byte[] buffer;

    /**
     * Constructor
     * Given a RTPSession builds an OutputStream to it
     *
     * @param rtpSession RTPSession
     * @param bytesPerSecond long
     * @param packetsPerSecond int
     */
    public RTPOutputStream(RTPSession rtpSession, long bytesPerSecond,
                           int packetsPerSecond) {
        this.rtpSession = rtpSession;

        circularByteBuffer = new CircularByteBuffer( -1, false);
        packetSize = bytesPerSecond / packetsPerSecond;
        packetDuration = 1000f * ((double) packetSize / (double) bytesPerSecond);
        pktTmestamp = -1;
        buffer = new byte[(int) packetSize];
    }

    public void write(int b) throws IOException {
        circularByteBuffer.getOutputStream().write(b);

        flush();
    }

    public void write(byte b[], int off, int len) throws IOException {
        circularByteBuffer.getOutputStream().write(b, off, len);

        flush();
    }

    public void flush() throws IOException {
        while (circularByteBuffer.getInputStream().available() >= packetSize) {
            sendData();
        }
        pktTmestamp = -1;
    }

    public void close() throws IOException {
        System.err.println("RTPOutputStream.close() called");
        circularByteBuffer.getOutputStream().close();
        circularByteBuffer.getInputStream().close();
        rtpSession.endSession();
        System.err.println("RTPOutputStream.close() done! (rtpEndSession)");
        pktTmestamp = -1;
    }

    /**
     * Send data to RTP session
     *
     * @todo Should reset timestamp if current time has long passed
     */
    private void sendData() {

        //Initialize timestamp
        if (pktTmestamp < 0) {
            pktTmestamp = (long) (System.nanoTime() * 1E-6);
        }

        //Fill buffer to send
        int bytesRead = 0;
        try {
            bytesRead = circularByteBuffer.getInputStream().read(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (bytesRead != packetSize) {
            if (bytesRead < 0) {
                //ENDED??
                System.err.println("bytesRead != packetSize... @ RTPOutputStream");
            }
        }

        //Send data
        long[][] ret = null;
        byte[][] pkt = {buffer};
        ret = rtpSession.sendData(pkt, null, null, pktTmestamp, null);

        //Try to keep send rate as "real time" as possible...
        long sleepTime = pktTmestamp - (long) (System.nanoTime() * 1E-6);
        while (sleepTime > 0) {
            try {
                Thread.sleep(0, 999999);
                sleepTime--;
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
            }
        }

        //Update timestamp
        pktTmestamp += packetDuration;
    }

}
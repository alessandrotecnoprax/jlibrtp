package org.jlibrtp.protocols.rtp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import org.jlibrtp.RTPSession;
import com.Ostermiller.util.CircularByteBuffer;

/**
 * <p>Title: jlibrtp</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007-2008</p>
 *
 * <p>Company: VoiceInteraction</p>
 *
 * @author Renato Cassaca
 * @version 1.0
 */
public class OutputStreamRTPSender extends OutputStream implements Runnable {

    private static final int numBuffersBeforeSend = 3;

    private int bufferSize = 1024;

    private final CircularByteBuffer circularByteBuffer = new CircularByteBuffer(-1);

    private final long bytesPerSecond;

    private long rtpTimeStamp = 0;

    private Thread thread = null;
    private boolean started = false;
    private boolean stopRequested = false;

    private boolean flushing;
    private final UUID uuid = UUID.randomUUID();

    private final RTPSession rtpSession;

    public OutputStreamRTPSender(RTPSession rtpSession, long bytesPerSecond) throws
            IOException {
        super();

        this.bytesPerSecond = bytesPerSecond;
        this.rtpSession = rtpSession;

        flushing = false;

        //Start the thread
        /** @todo late start the thread @  write ? */
        start();
    }

    public OutputStreamRTPSender(RTPSession rtpSession, long bytesPerSecond,
                                 int bufSize) throws IOException {
        super();

        this.bytesPerSecond = bytesPerSecond;
        this.rtpSession = rtpSession;
        bufferSize = bufSize;

        flushing = false;

        //Start the thread
        /** @todo late start the thread @  write ? */
        start();
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular, an
     *   <code>IOException</code> may be thrown if the output stream has
     *   been closed.
     * @todo Implement this java.io.OutputStream method
     */
    public void write(int b) throws IOException {
        if (started == false)
            throw new IOException("Sender is closed");

        circularByteBuffer.getOutputStream().write(b);

        long dataTime = (long) Math.ceil(1000 / bytesPerSecond);
        if (dataTime > 0) {
            try {
                Thread.currentThread().sleep(dataTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (started == false)
            throw new IOException("Sender is closed");

        int bw = len - off;
        circularByteBuffer.getOutputStream().write(b, off, len);
    }


    public void flush() throws IOException {
        flushing = true;
        while (circularByteBuffer.getInputStream().available() > 0) {
            try {
                Thread.currentThread().sleep(5);
            } catch (InterruptedException ex) {
            }
        }
        flushing = false;
    }

    public void close() throws IOException {
        if (started == false)
            return;
        else {
            stopRequested = true;
            while (started == true) {
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        }
    }


    public void start() {
        thread = new Thread(this, "RTPDataSender_" + uuid);
        started = true;
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public void stop() {
        //System.out.println("RTPDataSender.stop() "+uuid);
        started = false;
    }


    public void run() {
        //This way, packages are always multiples of 1 ms
        int correctedBufferSize = bufferSize -
                                  bufferSize % ((int) bytesPerSecond / 1000);
        int br = -1;
        int available;
        int numberBuffers;

        while ((started) && (stopRequested == false)) {
            try {
                //Calculo o numero de buffers
                available = circularByteBuffer.getInputStream().available();
                if (available < 1) {
                    try {
                        Thread.currentThread().sleep(100);
                    } catch (InterruptedException ex1) {
                    }
                    continue ;
                }
                numberBuffers = (int) Math.floor(available /
                                                 correctedBufferSize);
                if ((numberBuffers < 1) && (flushing == false)) {
                    try {
                        Thread.currentThread().sleep(50);
                    } catch (InterruptedException ex1) {
                    }
                    continue ;
                }

                //Aloco os buffers prontos para enviar
                byte[][] buffers;
                if (numberBuffers > 1) {
                    buffers = new byte[numberBuffers][correctedBufferSize];

                    //Vou preencher os buffers
                    for (int i = 0; i < numberBuffers; i++) {
                        try {
                            br = circularByteBuffer.getInputStream().read(buffers[i]);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            br = -1;
                            break;
                        }
                    }

                    if (br == -1) {
                        break;
                    }
                } else {
                    if (available < correctedBufferSize)
                        buffers = new byte[1][available];
                    else
                        buffers = new byte[1][correctedBufferSize];
                    try {
                        br = circularByteBuffer.getInputStream().read(buffers[0]);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        br = -1;
                        break;
                    }

                    if (br == -1) {
                        break;
                    }
                }

                //Send data
                long[][] ret = null;
                ret = rtpSession.sendData(buffers, null, null, -1, null);
                if (ret != null) {
                    rtpTimeStamp = ret[0][0];
                } else {
                    System.err.println("Problem sending RTP packet...");
                    break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //Ends RTPSession
        rtpSession.endSession();
        started = false;

        try {
            circularByteBuffer.getOutputStream().close();
            circularByteBuffer.getInputStream().close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}

package org.jlibrtp.protocols.rtp;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.io.OutputStream;
import java.io.FileInputStream;

/**
 * <p>Title: TestRTPProtocol</p>
 *
 * <p>Description:  Handler to protocol rtp://</p>
 *
 * <p>Copyright: Copyright (c) 2007-2008</p>
 *
 * <p>Company: L2F INESC-ID</p>
 *
 * @author Renato Cassaca
 * @version 1.0
 */
public class TestRTPProtocol {

    static {
        registerProtocolHandlers();
    }

    private static void registerProtocolHandlers() {
        //Register protocol handler
        String javaPropName = "java.protocol.handler.pkgs";

        //Start value
        System.out.println(javaPropName + " = " +
                           System.getProperty(javaPropName));

        //Vou actualizar a propriedade que define o meu protocol handler (URL)
        String packageName = "org.jlibrtp.protocols";
        System.setProperty(javaPropName, packageName);

        //Value after update
        System.out.println(javaPropName + " = " +
                           System.getProperty(javaPropName));
    }


    public static void main_Trash(String[] args) {

        try {
            URL sendURL = new URL("rtp://127.0.0.1:21000/audio");
            URLConnection sendC = sendURL.openConnection();

            Thread.sleep(2000);

            URL recvURL = new URL("rtp://127.0.0.1:21000/audio");
            URLConnection recvC = recvURL.openConnection();

            Thread.sleep(2000);

            sendC.connect();
            InputStream rtpIS = sendC.getInputStream();

            recvC.connect();
            OutputStream rtpOS = recvC.getOutputStream();

            InputStream is = new FileInputStream("capture.raw");
            OutputStream os = new FileOutputStream("resent.raw");

            byte[] buffer = new byte[655];
            int br;
            byte[] rtpBuffer = new byte[128];
            while ((br = is.read(buffer)) != -1) {
                rtpOS.write(buffer, 0, br);

                br = rtpIS.read(rtpBuffer);
                if (br == -1) {
                    System.out.println("END OF RTP DATA");
                    break;
                } else {
                    os.write(rtpBuffer, 0, br);
                }
            }

            //Consume the remaining RTP data
            while ((br = rtpIS.read(rtpBuffer)) != -1) {
                os.write(rtpBuffer, 0, br);
            }

            System.out.println("Finished");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {

        //Make a URL describing input
        URL rtpURL = null;
        try {
            rtpURL = new URL("rtp://127.0.0.1:16384/audio?pport=12334");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return;
        }

        //Get connection to URL
        URLConnection c = null;
        try {
            c = rtpURL.openConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //Starts the connecion
        try {
            c.connect();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //Get the input stream from the URL
        InputStream in = null;
        try {
            in = c.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //Open file to write captured audio
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("capture.raw");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }

        //Write to a file
        try {
            int br;
            byte[] buffer = new byte[1024];
            while ((br = in.read(buffer)) != -1) {
                fos.write(buffer, 0, br);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}

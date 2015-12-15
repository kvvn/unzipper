package org.roda.unzipper;

/**
 * Created by kvvn on 5/7/15.
 *
 * Used for "in memory" unzip from php script
 * Receives a base64 encoded zip content and returns a json where the file names are keys
 * and values are base64 encoded file contents
 *
 */

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONObject;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Unzipper {

    private static int port=4444, maxConnections=0;

    public static void main(String[] args){
        int i=0;

        try{
            ServerSocket listener = new ServerSocket(port);
            Socket server;

            while((i++ < maxConnections) || (maxConnections == 0)){
                //doComms connection;

                server = listener.accept();
                doComms conn_c= new doComms(server);
                Thread t = new Thread(conn_c);
                t.start();
            }
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }
}
class doComms implements Runnable {
    private Socket server;
    private String line;
    doComms(Socket server) {
        this.server=server;
    }

    public void run () {
        try {

            BufferedReader br =	new BufferedReader(new InputStreamReader(server.getInputStream()));
            BufferedWriter bw =	new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
            Map<String, String> res;
            while((line = br.readLine()) != null) {
                //input=input + line;
                System.out.println(line);

                try {
                    res = this.unzip(line);
                } catch (IllegalArgumentException iae) {
                    res = this.unzip(line, "CP1251");
                }
                    JSONObject answer = new JSONObject(res);
                System.out.println(answer.toString());
                bw.write(answer.toString() + "\n");
                bw.flush();
            }
            bw.close();
            br.close();
            server.close();
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }
    private Map<String, String> unzip (String content) throws IOException, IllegalArgumentException {
        Map<String, String> res = new HashMap<String, String>();

        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        byte[] decodedBytes = decoder.decodeBuffer(content);


        String file_content = "";

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decodedBytes));
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            int needed = (int)entry.getSize();
            int read = 0;
            file_content = "";
            byte[] bytesIn = new byte[needed];
            System.out.println(entry.getName());
            while (needed > 0) {
                int pos = zis.read(bytesIn, read, needed);
                if (pos == -1) {
                    throw new IOException("Unexpected end of stream after " + pos + " bytes for entry " + entry.getName());
                }
                read += pos;
                needed -= pos;
            }
            file_content = encoder.encode(bytesIn);

            res.put(entry.getName(), file_content);
            zis.closeEntry();
        }
        zis.close();
        return res;
    }

    private Map<String, String> unzip (String content, String encoding) throws IOException, IllegalArgumentException {
        Map<String, String> res = new HashMap<String, String>();

        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        byte[] decodedBytes = decoder.decodeBuffer(content);


        String file_content = "";

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decodedBytes), Charset.forName(encoding));
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            int needed = (int)entry.getSize();
            int read = 0;
            file_content = "";
            byte[] bytesIn = new byte[needed];
            System.out.println(entry.getName());
            while (needed > 0) {
                int pos = zis.read(bytesIn, read, needed);
                if (pos == -1) {
                    throw new IOException("Unexpected end of stream after " + pos + " bytes for entry " + entry.getName());
                }
                read += pos;
                needed -= pos;
            }
            file_content = encoder.encode(bytesIn);

            res.put(entry.getName(), file_content);
            zis.closeEntry();
        }
        zis.close();
        return res;
    }
}


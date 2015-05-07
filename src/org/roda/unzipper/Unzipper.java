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
import java.util.Arrays;
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

            while((line = br.readLine()) != null) {
                //input=input + line;
                System.out.println(line);

                Map<String, String> res = this.unzip(line);
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
    private Map<String, String> unzip (String content) throws IOException {
        Map<String, String> res = new HashMap<String, String>();

        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        byte[] decodedBytes = decoder.decodeBuffer(content);

        int read = 0;
        String file_content = "";
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decodedBytes));
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            file_content = "";
            byte[] bytesIn = new byte[8192];
            System.out.println(entry.getName());
            while ((read = zis.read(bytesIn)) != -1) {
                file_content = encoder.encode(Arrays.copyOfRange(bytesIn, 0, read));
            }
            res.put(entry.getName(), file_content);
            zis.closeEntry();
        }
        zis.close();
        return res;
    }
}


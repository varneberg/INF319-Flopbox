package client;

import java.io.*;
import java.net.*;
public class Client implements Runnable{
    String name;
    String password;
    Thread t;
    String message;
    int port;
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static String storagePath = "src/main/resources/clientStorage/";


    public Client(int port, String name, String password) {

        this.port = port;
        this.name = name;
        this.password = password;
    }

    @Override
    public void run(){
        try{
            Socket s = new Socket("localhost",port);
            dataInputStream = new DataInputStream(s.getInputStream());
            dataOutputStream = new DataOutputStream(s.getOutputStream());

            sendFile("send1.txt");
            sendFile("send2.txt");

            dataInputStream.close();
            dataInputStream.close();
            s.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void sendFile(String filename) throws Exception{
        String fullPath = storagePath + filename;


        int bytes = 0;
        File file = new File(fullPath);
        String path = file.getAbsolutePath();

        FileInputStream fileInputStream = new FileInputStream(fullPath);

        // send file size
        dataOutputStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0,bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }


    public String getMessage() {
        return message;
    }

    public String getPassword() { return password; }

    public String getName() {
        return name;
    }

}

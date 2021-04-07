package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable{
    String name;
    String password;
    Thread t;
    String message;
    int port;
    private Socket s;
    private static DataOutputStream dataOutput = null;
    private static DataInputStream dataInput = null;
    private static String storagePath = "src/main/resources/clientStorage/";


    public Client(int port, String name, String password) {

        this.port = port;
        this.name = name;
        this.password = password;
    }
    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run(){
        try{
            s = new Socket("localhost",port);
            dataInput = new DataInputStream(s.getInputStream());
            dataOutput = new DataOutputStream(s.getOutputStream());

            //Send credentials to server
            String username = askUsername();
            String password = askPassword();
            sendServer(username, s);
            sendServer(password, s);

            /*
            sendFile("send1.txt");
            sendFile("send2.txt");
            */
            dataInput.close();
            dataInput.close();

            //sendCredentials();
            //s.close();
            System.out.println("Connection to server closed");

        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void sendServer(String message, Socket socket) throws IOException {
        dataOutput = new DataOutputStream(socket.getOutputStream());
        dataOutput.writeBytes(message + '\n');
        dataOutput.flush();
    }

    private String askUsername(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Username: ");
        String uname = sc.nextLine();
        return uname;
    }

    private String askPassword(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Password: ");
        String password = sc.nextLine();
        return password;
    }


    private void sendFile(String filename) throws Exception{
        String fullPath = storagePath + filename;


        int bytes = 0;
        File file = new File(fullPath);
        String path = file.getAbsolutePath();

        FileInputStream fileInputStream = new FileInputStream(fullPath);

        // send file size
        dataOutput.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutput.write(buffer,0,bytes);
            dataOutput.flush();
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

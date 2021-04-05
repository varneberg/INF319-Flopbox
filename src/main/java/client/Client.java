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
            boolean auth = false;
            while(true) {
                sendCredentials();
                String authmsg = receiveServer(s);
                System.out.println(authmsg);
                if(authmsg.equals("You are now logged in as ")){
                    break;
                }
                else{
                    continue;
                }
                //s.close();
            }
            System.out.println("Connection to server closed");

        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void sendServer(String message, Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        out.writeByte(1);
        out.writeUTF(message);
        out.writeByte(-1);
        out.flush();
        /*dataOutput = new DataOutputStream(socket.getOutputStream());
        dataOutput.writeBytes(message + '\n');
        dataOutput.flush();
         */
    }

    private String receiveServer(Socket socket) throws IOException {
        DataInputStream inp = new DataInputStream(socket.getInputStream());
        byte msgStream = inp.readByte();
        return inp.readUTF();
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

    private void sendCredentials() throws IOException {
        String uname = askUsername();
        String passwd = askPassword();
        String creds = uname +"\n"+ passwd;
        sendServer(creds, s);
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

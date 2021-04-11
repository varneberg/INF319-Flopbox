package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client implements Runnable{

    String name;
    Thread t;
    int port;
    private Socket s = null;
    private static DataOutputStream dataOutput = null;
    private static DataInputStream dataInput = null;
    private static String storagePath = "src/main/resources/clientStorage/";
    //BufferedReader serverInput = null;
    //PrintWriter clientOutput = null;


    public Client(int port){
        this.port = port;
    }

    public Client(String address, int port) {
        try{
            this.port = port;
            s = new Socket(address, port);


        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void run(){
        try{
            //s = new Socket("localhost",port);
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter clientOutput = new PrintWriter(s.getOutputStream(), true);

            boolean auth = false;
            while (true) {
                // Update to gui credentials
                String uname = "tes123";
                String password = "test";
                String creds = uname + "\t" + password;
                //

                clientOutput.println(creds);
                String serverMessage = serverInput.readLine();
                System.out.println("[Server]: " + serverMessage);
                if (serverMessage.equals(1)){
                    break;
                }
                serverInput.read();

            }
            /*
            while(true) {
                sendCredentials();
                String authmsg = receiveServer();
                if(authmsg.equals("1")){
                    System.out.println("[Server]: Valid credentials");
                    break;
                } if(authmsg.equals("-1")){
                    System.out.println("[Server]: Incorrect password");
                } if(authmsg.equals("0")){
                    System.out.println("[Server]: No user was found with given name");
                } else { continue; }
            }
             */

            //s.close();
            //closeConnection();

        }catch(Exception e){
            System.out.println(e);
        }
    }


    public void connect(){
    try{

        s = new Socket("localhost", port);

    } catch (IOException e){
        System.out.println(e.getMessage());
    }
    }


    // Send data to server
    public void sendServer(String message) {
        try{
            PrintWriter clientOutput = new PrintWriter(s.getOutputStream(), true);
            clientOutput.println(message);

        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Receive data from server
    public String receiveServer(BufferedReader input) throws IOException {
        input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return input.readLine();
    }


    public void sendAuthentication(String username, String password) throws IOException {
        String credentials = username + "\t" + password;
        //sendServer(credentials);
    }

    // Receive names for files stored on server
    public String[] receiveFileNames() {
        return null;
    }

    // Closes current connection to server
    private void closeConnection() throws IOException{
        //sendServer("exit()");
        s.close();
    }


    private void sendCredentials() throws IOException {
        Scanner sc = new Scanner(System.in);

        String uname = sc.nextLine();
        String passwd= sc.nextLine();

        String creds = uname +"\n"+ passwd;
        //sendServer(creds);
    }

    public boolean authenticateClient(String name, String password) {
        return false;
    }

    public boolean registerClient(String name, String password) { return false; }

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

    public Socket getSocket() {
        return s;
    }

    public void setSocket(Socket s) {
        this.s = s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

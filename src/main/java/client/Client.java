package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client implements Runnable{
    Thread t;
    int port;
    Socket s;
    private static DataOutputStream dataOutput = null;
    private static DataInputStream dataInput = null;
    private static String storagePath = "src/main/resources/clientStorage/";
    BufferedReader serverInput;
    PrintWriter clientOutput;


    public Client(int port, Socket socket) {
        this.port = port;
        this.s = socket;
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

    // Send data to server
    public void sendServer(String message) throws IOException {
        clientOutput = new PrintWriter(s.getOutputStream(), true);
        clientOutput.println(message);
    }

    // Receive data from server
    public String receiveServer() throws IOException {
        serverInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return serverInput.readLine();
    }


    public void sendAuthentication(String username, String password) throws IOException {
        String credentials = username + "\t" + password;
        sendServer(credentials);
    }

    // Receive names for files stored on server
    public String[] receiveFileNames() {
        return null;
    }

    // Closes current connection to server
    private void closeConnection() throws IOException{
        sendServer("exit()");
        s.close();
    }


    private void sendCredentials() throws IOException {
        Scanner sc = new Scanner(System.in);

        String uname = sc.nextLine();
        String passwd= sc.nextLine();

        String creds = uname +"\n"+ passwd;
        sendServer(creds);
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

}

package client;

import message.Message;

import java.io.*;
import java.net.Socket;


public class Client {
    //public class Client implements Runnable{


    String uuid;
    Thread t;
    int port;
    Socket s;
    private String name;
    private static DataOutputStream dataOutput = null;
    private static DataInputStream dataInput = null;
    private static String storagePath = "src/main/resources/clientStorage/";
    BufferedReader clientInput = null;
    PrintWriter clientOutput = null;


    public Client(int port) {
        this.port = port;
    }

    public Client(String address, int port) {
        try{
            this.port = port;
            s = new Socket(address, port);
            this.uuid = uuid;
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    /*
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

            //s.close();
            //closeConnection();

        }catch(Exception e){
            System.out.println(e);
        }
    }

    */

    public void connect(){
        try{
            s = new Socket("localhost", port);

        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }


    // Send data to server
    public void sendServer(String requestType, String content) {
        try{
            String uuid = getUuid();
            clientOutput = new PrintWriter(s.getOutputStream(), true);
            String message = s.getLocalAddress() + ":" + uuid + ":" + requestType + ":" + content;
            clientOutput.println(message);


        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Receive data from server
    public String receiveServer() {
        try{
            clientInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
            return clientInput.readLine();
        } catch (IOException e){
            return e.getMessage();
        }
    }

    public void sendMessage(String requestType, String contents){
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());
            clientOutput = new PrintWriter(s.getOutputStream(),true);
            Message message = new Message(s.getInetAddress().toString(), getUuid(),requestType, contents);
            clientOutput.println(message.createMessage());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createUser(String username, String password){
        //sendServer("CREATEUSER()");
        String credentials = username + "/" + password;
        //sendServer("CREATEUSER()",credentials);
        sendMessage("CREATEUSER()", credentials);
    }

    public void sendAuthentication(String username, String password) {
        //sendServer("LOGIN()", );
        String credentials = username + "/" + password;
        sendMessage("LOGIN()", credentials);
        //sendServer("LOGIN()", credentials);
    }


    // Receive names for files stored on server
    public void receiveFileNames() throws IOException, ClassNotFoundException {
        String input = receiveServer();
        System.out.println("[Server -> Client]: " + input);
    }

    // Closes current connection to server
    private void closeConnection() throws IOException{
        sendServer("EXIT()", null);
        s.close();
    }

    public boolean authenticateClient(String username, String password){
        if(getUuid().equals(null)){
            return false;
        }
        else{
            return true;
        }
    }

    public String attemptLogin(String username, String password){
        sendAuthentication(username, password);
        String input = receiveServer();
        if(input.length() > 2) {
            //System.out.println(input);
            setUuid(input);
        }

        return input;
    }


    public boolean registerClient(String name, String password) {
        return false;
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

    public String getName(){
        return this.name;
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

    public boolean registerClient(String text, String text1) {
        return false;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}

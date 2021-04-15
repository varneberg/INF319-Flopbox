package server;

import message.clientMessage;
import message.serverMessage;
import storage.ClientStorage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Server extends Thread {

    private ServerSocket ss;
    private int port;
    private boolean running = false;

    public Server(int port){
        this.port = port;
    }


    /*
    public boolean clientExists(String name){
        for(int i=0;i<clients.size();i++){
            Client current = clients.get(i);
            if(current.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public Client login(String name, String password){
        for(Client current: clients){
            if(current.getName().equals(name) && current.getPassword().equals(password)){
                return current;
            }
        }
        return null;
    }
     */

    public void startServer(){
        try {
            ss = new ServerSocket(port);
            this.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopServer(){
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("[Server]: Listening for a connection...");
        while (running) {
            try {
                //Call accept() to receive the next connection
                Socket socket = ss.accept();
                // Pass the socket to the request handler thread for processing
                RequestHandler requestHandler = new RequestHandler(socket);
                requestHandler.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


class RequestHandler extends Thread{
    private Socket s;
    private static DataOutputStream dataOutput = null;
    private static DataInputStream dataInput = null;
    private ClientStorage cs = new ClientStorage();
    private static ArrayList<String> authClients = new ArrayList<String>();
    private String currClientUUID = null;
    private String currClientAddress = null;

    BufferedReader serverInput;
    PrintWriter serverOutput;



    RequestHandler(Socket socket){
        this.s = socket;
    }

    @Override
    public void run(){
        try {
            System.out.println("[Server]: Received a connection\n");
            while(true) {
                //String input = receiveClient();
                clientMessage clientMsg = receiveMessage();
                String requestType = clientMsg.getRequestType();
                String contents = clientMsg.getMessageContents();
                if(requestType.equals("EXIT()")){closeConnection();}
                else if(requestType.equals("CREATEUSER()")){
                    createNewClient(contents);}
                else if(requestType.equals("LOGIN()")){
                    if(validateClient(contents)==1){
                        System.out.println("[Server]: Client Authenticated");
                    }

                }
            }


        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public clientMessage receiveMessage(){
        clientMessage msg = null;
        try {
            serverInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
            msg = new clientMessage();
            msg.receiveMessage(serverInput.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    /*
   public String receiveClient(){
       try{
           serverInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
           String[] clientMessage = serverInput.readLine().split(":");
           currClientAddress = clientMessage[0];
           currClientUUID = clientMessage[1];
           String requestType = clientMessage[2];
           String contents = clientMessage[3];
           return contents;
       }catch (IOException e){
           return e.getMessage();
       }
   }

     */


   public void sendMessage(String requestType, String requestStatus, String contents){
        try {
            serverOutput = new PrintWriter(s.getOutputStream(), true);
            serverMessage msg = new serverMessage(s.getInetAddress().toString(), requestType,requestStatus, contents); // Change to server message
            serverOutput.println(msg.createMessage());
        }catch (IOException e){
            e.printStackTrace();
        }

   }

   public void sendClient(String message){
       try{
           serverOutput = new PrintWriter(s.getOutputStream(), true);
           serverOutput.println(message);
       } catch (IOException e){
           System.out.println(e.getMessage());
       }
    }

    private String genUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private void createNewClient(String input){
        //clientMessage input = receiveMessage();
        //String[] creds = input.getMessageContents().split("|");
        String[] creds = input.split("/");
        String uname = creds[0];
        String passwd = creds[1];
        try {
            if(cs.clientExists(uname)){
                //sendClient("-1");
                sendMessage("CREATEUSER()", "-2", "Client with username "+ uname + " already exists");
            }else{
                cs.addClient(uname, passwd);
                sendMessage("CREATEUSER()", "2", "User "+ uname + " successfully added");
                //sendClient("1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

       //cs.addClient();
    }

    private String getAvailableFileNames(String clientName) throws IOException {
        String path = "./src/main/resources/clientDirs/"+clientName+"/";
        String[] files = cs.listClientFiles(path);
        String output = "";
        for (int i = 0; i < files.length; i++) {
            output += files[i] + "|";
        }
        return output;
    }

    public void sendFileNames(String username){
        try{
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            //out.writeObject(files);
            //out.close();

        } catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Closes current connection to client
    private void closeConnection() throws IOException{
        System.out.println("[Server]: Connection to client closed");
        s.close();
    }


    private int validateClient(String input){
        /*
        Status codes:
        -2: Client with username exists
        -1: Username not found
        0 : Password incorrect
        1 : Validated client
         */

        //String input = receiveClient();
        String[] creds = input.split("/");
        String username = creds[0];
        String password = creds[1];
        try {
            if (cs.clientExists(username)) {
                if(cs.verifyPassword(password)){ // Client is authenticated
                    //sendClient(genUUID());
                    sendMessage("LOGIN()", "1", genUUID());
                    return 1;
                }
                else{ // Password is wrong
                    //sendClient("0");
                    sendMessage("LOGIN()", "0", "Incorrect password");

                    return 0;
                }
            }
            else { // No user was found with given name
                sendClient("-1");
                sendMessage("LOGIN()", "-1", "No user was found");
                return -1;
            }

        } catch (SQLException e){ System.out.println(e.getMessage()); }
        return 0;
    }


    private void startFileHandler(){
        FileHandler handler = new FileHandler();
        //String input = receiveClient();

    }

    private void receiveFile(String fileName) throws Exception{
        File file = new File(fileName);
        if (file.createNewFile()) {
            System.out.println("File created: " + file.getName());
        } else {
            System.out.println("File already exists.");
        }
        String path = file.getAbsolutePath();
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(path);

        long size = dataInput.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInput.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();

    }


}

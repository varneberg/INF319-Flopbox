package server;

import client.Client;
import storage.ClientStorage;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.ArrayList;

public class Server extends Thread{

    private ServerSocket ss;
    private int port;
    private boolean running = false;
    private static ArrayList<Client> clients = new ArrayList<Client>();

    public Server(int port){
        this.port = port;
    }

    public void addClient(Client client){
        clients.add(client);
    }

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
        while (running) {
            try {
                System.out.println("Listening for a connection...");
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
    private static DataOutputStream dataout= null;
    private static DataInputStream dataIn = null;
    private ClientStorage cs = new ClientStorage();


    RequestHandler(Socket socket){
        this.s = socket;
    }

    @Override
    public void run(){ // What the server does when a client connects
        String clientIn = "";
        try{
            System.out.println("[Server]: Received a connection\n");
            /*
            receiveFile("src/main/resources/serverStorage/recived1.txt");
            receiveFile("src/main/resources/serverStorage/recived2.txt");
            */
            //dataIn = new DataInputStream(s.getInputStream());
            //dataout = new DataOutputStream(s.getOutputStream());

            // Authenticate Client
            String uname = "";
            String passwd = "";
            String authmsg = "";
            while(true) {
                String creds = receiveClient(s);
                String[] credsArr = creds.split("\n");
                uname = credsArr[0];
                passwd = credsArr[1];
                if (checkClient(uname)) {
                    if (cs.verifyPassword(passwd)) {
                        authmsg = "[Server]: You are now logged in as: " + uname;
                        sendClient(authmsg, s);
                        break;
                    } else {
                        sendClient("[Server]: Incorrect password", s);
                        continue;
                    }
                } else if (!checkClient(uname)) {
                    authmsg = "[Server]: No user with username: " + uname + " , was found\n";
                    sendClient(authmsg, s);
                    continue;
                }
            }

            sendAvailableFiles(uname);

            // Close connection
            s.close();
            System.out.println( "Connection to client closed" );
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

   private String receiveClient(Socket socket) throws IOException {
        //dataIn = new DataInputStream(socket.getInputStream());
        DataInputStream inp = new DataInputStream(socket.getInputStream());
        byte msgStream = inp.readByte();
        return inp.readUTF();
        //BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //String clientString = fromClient.readLine();
        //return clientString;
   }

    private void sendClient(String message, Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        out.writeByte(1);
        out.writeUTF(message);
        out.writeByte(-1);
        out.flush();
    }

    // Check if provided username and password is correct
    private boolean authenticateClient(){
        return true;
    }

    private void sendAvailableFiles(String clientName){
        String[] files = cs.listClientFiles(clientName);
        for(String file : files){
            System.out.println(file);
        }

    }

    // Closes current connection to client
    private void closeConnection(Socket socket) throws IOException{
       DataOutputStream out = new DataOutputStream(s.getOutputStream());
       out.close();
    }

    // From username, check is corresponding entry exists in database
    private boolean checkClient(String uname) throws SQLException {
        boolean exists = cs.clientExists(uname);
        if (exists){
            return true;
        } else{
            return false;
        }
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

        long size = dataIn.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataIn.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();

    }

}

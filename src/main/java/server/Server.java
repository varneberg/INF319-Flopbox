package server;

import client.Client;
import storage.ClientStorage;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Server extends Thread {

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

    private PrintWriter serverOutput;


    RequestHandler(Socket socket){
        this.s = socket;
    }

    @Override
    public void run(){ // What the server does when a client connects
        try{
            System.out.println("[Server]: Received a connection\n");
            String username = "";

            while (true) {
                BufferedReader clientInput = new BufferedReader(new InputStreamReader((s.getInputStream())));
                serverOutput = new PrintWriter(s.getOutputStream(), true);
                String[] creds = clientInput.readLine().split("\t");
                username = creds[0];
                String password = creds[1];
                boolean uath = authenticateClient(serverOutput, username, password);
                if (uath) {
                    break;
                }
            }
            serverOutput.flush();
            String files = getAvailableFileNames(username);
            serverOutput.printf(files);



            closeConnection();

        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

   private String receiveClient() throws IOException {
        //dataIn = new DataInputStream(socket.getInputStream());
        //DataInputStream inp = new DataInputStream(socket.getInputStream());
        dataInput = new DataInputStream(s.getInputStream());
        byte msgStream = dataInput.readByte();
        String inp = dataInput.readUTF();
        return inp;
        //BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //String clientString = fromClient.readLine();
        //return clientString;
   }

   private void nsendClient(String message){

   }

   private String nreceiveClient(BufferedReader input) throws IOException {
       input = new BufferedReader(new InputStreamReader((s.getInputStream())));
       return "0";
   }

   private void sendClient(String message) {
        try {
            //DataOutputStream dataout = new DataOutputStream(s.getOutputStream());
            dataOutput = new DataOutputStream(s.getOutputStream());
            dataOutput.writeByte(1);
            dataOutput.writeUTF(message);
            dataOutput.writeByte(-1);
            dataOutput.flush();
        } catch(IOException e){
            System.out.println(e.getStackTrace());
        }
    }


    // Check if provided username and password is correct
    private boolean authenticateClient(PrintWriter output, String uname, String passwd) throws SQLException {
        if (checkClient(uname)) {
            if (cs.verifyPassword(passwd)) {
                //sendClient("1");
                output.println("1");
                return true;
            } else {
                //sendClient("-1");
                output.println("1");
                return false;
            }
        } else {
            //sendClient("0");
            output.println("0");
            return false;
            //return 0;
        }
    }

    private String getAvailableFileNames(String clientName){
        String[] files = cs.listClientFiles(clientName);
        String msg = "";
        for(String file : files){
            //System.out.println(file);
            msg += file + "\n";
        }
        return msg;
    }

    // Closes current connection to client
    private void closeConnection() throws IOException{
        System.out.println("[Server]: Connection to client closed");
        s.close();
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

        long size = dataInput.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInput.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();

    }

}

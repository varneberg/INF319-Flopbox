package server;

import message.clientMessage;
import message.serverMessage;
import org.apache.commons.io.IOUtils;
import storage.ClientStorage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Server extends Thread {

    private ServerSocket ss;
    final int port;
    private boolean running = false;

    public Server(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            ss = new ServerSocket(port);
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
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


class RequestHandler extends Thread {
    private Socket s;
    //private DataOutputStream dataOutput;
    //private DataInputStream dataInput;
    InputStream dataInput = null;
    OutputStream dataOutput = null;
    private ClientStorage cs = new ClientStorage();
    private static ArrayList<String> authClients = new ArrayList<String>();
    private String currClientUUID = null;
    private String currClientAddress = null;
    private String clientName;

    private String sep = ";;";
    private BufferedReader serverInput;
    private PrintWriter serverOutput;


    RequestHandler(Socket socket) {
        this.s = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Server]: Received a connection\n");
            while (true) {
                clientMessage clientMsg = receiveMessage();
                FileHandler handler = new FileHandler();
                String requestType = clientMsg.getRequestType();
                String contents = clientMsg.getMessageContents();
                String clientUUID = clientMsg.getUuid();
                String storagePath;
                String filename;
                switch (requestType) {
                    case "EXIT()":
                        closeConnection();

                    case "CREATEUSER()":
                        createNewClient(contents);
                        break;

                    case "LOGIN()":
                        validateClient(contents);
                        break;

                    case "LIST()":
                        if (clientUUID.equals("null")) {
                            sendMessage("LIST()", "0", "Unauthorized action");
                            break;
                        }
                        //startFileHandler(getClientName(), contents);
                        String clientFiles = handler.listFiles(clientMsg.getMessageContents());
                        sendMessage("LIST()", "1", clientFiles);
                        break;

                    case "GET()":
                        if (clientUUID.equals("null")) {
                            sendMessage("GET()", "0", "Unauthorized action");
                            break;
                        }
                        storagePath = handler.getStoragePath();
                        filename = contents;
                        //System.out.println(storagePath + filename);
                        sendFile(storagePath+filename);
                        //String storagePath = handler.getStoragePath();
                        break;

                    case "PUT()":
                        if (clientUUID.equals("null")) {
                            sendMessage("PUT()", "0", "Unauthorized action");
                            break;
                        }
                        try {
                            String[] fileInfo = contents.split(",");
                            filename = contents;
                            //String storagePath = handler.getStoragePath();
                            //receiveFile("./src/main/resources/clientDirs/output.txt");
                            storagePath = handler.getStoragePath();
                            filename = storagePath+fileInfo[0];
                            String filesize = fileInfo[1];
                            receiveFile(filename,filesize);
                        } catch (Exception e) {
                            sendMessage("PUT()", "0", "Unable to upload");
                        }
                        break;

                    default:
                        sendMessage("ERROR()", "0", "Could not understand request");
                }
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void startFileHandler(String clientName, String contents) throws IOException {
        FileHandler handler = new FileHandler();
        String[] cmd = contents.split(";;");
        String reqType = cmd[0];

        switch (reqType) {
            case "LIST()":
                String clientFiles = handler.listFiles(clientName);
                sendMessage("LIST()", "1", clientFiles);
                break;
            default:
                break;
        }
    }

    public clientMessage receiveMessage() {
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

    public void sendMessage(String requestType, String requestStatus, String contents) {
        try {
            serverOutput = new PrintWriter(s.getOutputStream(), true);
            serverMessage msg = new serverMessage(s.getInetAddress().toString(), requestType, requestStatus, contents); // Change to server message
            serverOutput.println(msg.createMessage());
            //serverOutput.close();

            //serverOutput.write("1\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public void sendFile(File clientFile) throws IOException {
        long length = clientFile.length();
        byte[] bytes = new byte[8 * 1024];
        InputStream in = new FileInputStream(clientFile);
        OutputStream out = s.getOutputStream();
        int count;
        while ((count = in.read(bytes)) > 0) {
            out.write(bytes, 0, count);
        }
    }

     */

    private String genUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private void createNewClient(String input) {
        String[] creds = input.split("/");
        String uname = creds[0];
        String passwd = creds[1];
        try {
            if (cs.clientExists(uname)) {
                sendMessage("CREATEUSER()", "-1", "Client with username " + uname + " already exists");
            } else {
                cs.addClient(uname, passwd);
                sendMessage("CREATEUSER()", "1", "User " + uname + " successfully added");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Closes current connection to client
    private void closeConnection() throws IOException, SocketException {
        System.out.println("[Server]: Connection to client closed");
        s.close();
    }

    private void validateClient(String input) {
        /*
        Status codes:
        -2: Client with username exists
        -1: Username not found
        0 : Password incorrect
        1 : Validated client
         */
        String[] creds = input.split("/");
        String username = creds[0];
        String password = creds[1];
        try {
            if (cs.clientExists(username)) {
                if (cs.verifyPassword(password)) { // Client is authenticated
                    sendMessage("LOGIN()", "1", genUUID());
                    setClientName(username);
                    System.out.println("[Server]: " + username + " authenticated");
                    //return true;
                } else { // Password is wrong
                    sendMessage("LOGIN()", "0", "Incorrect password");
                    //return false;
                }
            } else { // No user was found with given name
                sendMessage("LOGIN()", "-1", "No user was found");
                //return false;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            sendMessage("ERROR()", "-1", e.getMessage());
        }
        //return false;
    }

    private void receiveFile(String fileName, String fileSize) throws Exception {
        /*
        //serverInput.ready();
        File clientFile = new File(fileName);
        long length = clientFile.length();
        byte[] bytes = new byte[8*1024];
        dataInput = new FileInputStream(clientFile);
        OutputStream out = s.getOutputStream();
        int count;
        while((count = dataInput.read(bytes)) > 0){
            out.write(bytes,0,count);
        }
        dataOutput.close();
        dataInput.close();
         */
        InputStream dis = new DataInputStream(s.getInputStream());
        OutputStream fos = new FileOutputStream(fileName);
        int size = Integer.parseInt(fileSize);
        byte[] buffer = new byte[size];

        int read = 0;
        int bytesRead=0;

        while((read = dis.read(buffer)) > 0){
            System.out.println("[Server]: Writing");
            fos.write(buffer,0,read);
            bytesRead = bytesRead + read;
            System.out.println(bytesRead+"/"+size);
            if(size > bytesRead){
                continue;
            }else {break;}

        }
        System.out.println("[Server]: done");
    }

    public void sendFile(String filePath){
        try {
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            File clientFile = new File(filePath);
            byte[] buffer = new byte[(int)clientFile.length()];
            long filesize = clientFile.length();
            String fileSize = clientFile.length()+"";

            sendMessage("GET()", "1", fileSize);
            FileInputStream fis = new FileInputStream(clientFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(buffer,0,buffer.length);
            OutputStream os = s.getOutputStream();
            os.write(buffer,0,buffer.length);
            os.flush();
            System.out.println("[Server]: done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }
}

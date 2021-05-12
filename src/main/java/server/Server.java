package server;

import message.clientMessage;
import message.serverMessage;
import storage.ClientStorage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
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
    InputStream dataInput = null;
    OutputStream dataOutput = null;
    private ClientStorage cs = new ClientStorage();
    private FileHandler handler = new FileHandler();
    private String currClientUUID;
    private String currClientAddress = null;
    private String clientName;
    private clientMessage prev = null;
    //private String sep = ";;";
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
                //FileHandler handler = new FileHandler();
                String requestType = clientMsg.getRequestType();
                String contents = clientMsg.getMessageContents();
                String clientUUID = clientMsg.getUuid();
                String storagePath;
                String filename;
                String status;
                switch (requestType) {
                    case "EXIT()": closeConnection();                   break;
                    case "CREATEUSER()": createNewClient(contents);     break;
                    case "LOGIN()": loginClient(contents);              break;
                    case "LIST()": listClientFiles(contents);           break;
                    case "GET()": sendFile(contents);                   break;
                    case "PUT()": receiveFile(contents);                break;
                    case "DIR()": createDir(contents);                  break;
                    case "DEL()": deleteFile(contents);                 break;
                    default: sendError("Unrecognized action"); break;
                }
                clientMsg = null;
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendError(String errorMsg) {
        try {
            serverOutput = new PrintWriter(s.getOutputStream(), true);
            serverMessage msg = new serverMessage(s.getInetAddress().toString(), "ERROR()", "0", errorMsg);
            serverOutput.println(msg.createMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private void listClientFiles(String msgContents) {
        if (!validateClient()) {
            //sendMessage("ERROR()", "0", "Unauthorized action");
            sendError("Unauthorized action");
            return;
        }
        try {
            String clientFiles = handler.listFiles(msgContents);
            sendMessage("FILES()", "1", clientFiles);
            //sendMessage("LIST()", "1", clientFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loginClient(String input) {
        String[] creds = input.split("/");
        String username = creds[0];
        String password = creds[1];
        try {
            if (cs.clientExists(username)) {
                if (cs.verifyPassword(password)) { // Client is authenticated
                    String uuid = genUUID();
                    sendMessage("LOGIN()", "1", uuid);
                    setClientName(username);
                    setCurrClientUUID(uuid);
                    //System.out.println("[Server]: " + username + " authenticated");
                    //return true;
                } else { // Password is wrong
                    sendMessage("ERROR()", "0", "Incorrect password");
                    //return false;
                }
            } else { // No user was found with given name
                sendMessage("ERROR()", "-1", "No user was found");
                //return false;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            sendMessage("ERROR()", "-1", e.getMessage());
        }
        //return false;
    }

    private boolean validateClient() {
        String uuid = getCurrClientUUID();
        return uuid != null;
    }

    //private void receiveFile(String fileName, String fileSize) throws Exception {
    private void receiveFile(String contents) {
        if (!validateClient()) {
            sendError("Unauthorized");
            return;
        }
        try {
            String[] fileInfo = contents.split(",");
            //filename = contents;
            String storagePath = handler.getStoragePath();
            String fileName = storagePath + fileInfo[0];
            String fileSize = fileInfo[1];
            //receiveFile(filename,filesize);
            InputStream dis = new DataInputStream(s.getInputStream());
            OutputStream fos = new FileOutputStream(fileName);
            int size = Integer.parseInt(fileSize);
            byte[] buffer = new byte[size];

            int read = 0;
            int bytesRead = 0;

            while ((read = dis.read(buffer)) > 0) {
                //System.out.println("[Server]: Writing");
                fos.write(buffer, 0, read);
                bytesRead = bytesRead + read;
                //System.out.println(bytesRead+"/"+size);
                if (size == bytesRead) {
                    break;
                }
                //else {break;}
            }
            sendMessage("PUT()","1","File uploaded");
        } catch (Exception e) {
            //sendMessage("ERROR()", "0", "Unable to upload");
            sendError("Unable to upload");
        }
    }

    public void sendFile(String filePath) {
        String storagePath = handler.getStoragePath();
        String fileName = storagePath + filePath;
        if (!validateClient()) {
            sendError("Unauthorized");
            return;
        }
        try {
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            File clientFile = new File(fileName);
            byte[] buffer = new byte[(int) clientFile.length()];
            long filesize = clientFile.length();
            String fileSize = clientFile.length() + "";

            sendMessage("GET()", "1", fileSize);
            FileInputStream fis = new FileInputStream(clientFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(buffer, 0, buffer.length);
            OutputStream os = s.getOutputStream();
            os.write(buffer, 0, buffer.length);
            os.flush();
            sendMessage("GET()", "1", "Success!");
            //System.out.println("[Server]: done");

        } catch (Exception e) {
            e.printStackTrace();
            //sendError("Unable to download");
        }
    }

    public void deleteFile(String path){
        String storagePath = handler.getStoragePath();
        File toDelete = new File(storagePath+"/"+path);
        if(toDelete.delete()){
            sendMessage("DEL()", "1", "File deleted");
        }else{
            sendError("Unable to delete file");
        }
    }

    public void createDir(String dirPath){
        if(!validateClient()){
            sendError("Unauthorized");
        }
        String storagePath = handler.getStoragePath();
        File newDir = new File(storagePath+dirPath);
        if(!newDir.exists()){
            newDir.mkdir();
            sendMessage("DIR()","1","Directory created");
        }else{
               sendError("Directory already exists");
           }
        }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }



    public void setCurrClientUUID(String currClientUUID) {
        this.currClientUUID = currClientUUID;
    }

    public String getCurrClientUUID() {
        return currClientUUID;
    }
}

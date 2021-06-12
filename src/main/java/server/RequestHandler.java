package server;

import builder.SecureState;
import message.clientMessage;
import message.serverMessage;
import storage.ClientStorage;
import storage.DB;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

class RequestHandler extends Thread implements RequestHandlerInterface {
    private Socket s;
    InputStream dataInput = null;
    OutputStream dataOutput = null;
    private ClientStorage cs = new ClientStorage();
    private FileHandler handler = new FileHandler();
    private String currClientUUID;
    private String currClientAddress = null;
    private String clientName;
    private clientMessage prev = null;
    private BufferedReader serverInput;
    private PrintWriter serverOutput;
    private clientMessage clientMsg;
    private int msgNum;
    private ArrayList<serverMessage> msgList;
    boolean secure = SecureState.getINSTANCE().isSecure();
    boolean running;

    /*
    starts a request handler on given socket
     */
    RequestHandler(Socket socket) {
        this.s = socket;
    }

    /*
    sets the running status of the request handler
     */
    @Override
    public void setRunning(boolean running) {
        this.running = running;
    }

    /*
    returns true if the request handler is runnin, false if not
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /*
    starts the request handler main loop
     */
    @Override
    public void run() {
        setRunning(true);
        while (isRunning()) {
            try {
                messageHandler();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }

    /*
    Is called during the main loop if the request handler,
    interprets the message from client and starts the correct response function
     */
    @Override
    public void messageHandler() throws IOException {
        //System.out.println(msgNum);
        clientMessage clientMsg = receiveMessage();
        //FileHandler handler = new FileHandler();
        String requestType = clientMsg.getRequestType();
        String contents = clientMsg.getMessageContents();
        //String clientUUID = clientMsg.getUuid();

        switch (requestType) {
            case "EXIT()":
                closeConnection();
                break;
            case "CREATEUSER()":
                //registerClient(contents);
                registerClient(contents);
                break;
            case "LOGIN()":
                loginClient(contents);
                break;
            case "LIST()":
                listClientFiles(contents);
                break;
            case "GET()":
                sendFile(contents);
                break;
            case "PUT()":
                receiveFile(contents);
                break;
            case "DIR()":
                createDir(contents);
                break;
            case "DEL()":
                deleteFile(contents);
                break;
            case "RENAME()":
                renameFile(contents);
                break;
            case "UPDATE()":
                updateCredentials(contents);
                break;
            default:
                sendError("Unrecognized action");
                break;
        }
    }

    /*
    receives messages from the client.
    returns the message received as a clientMessage object.
     */
    @Override
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

    /*
    Sends a message to the client
    input: request type: specifies the request type of the message, request status = specifies the status of the message, contents = contents of the message.
     */
    @Override
    public void sendMessage(String requestType, String requestStatus, String contents) {
        try {
            serverOutput = new PrintWriter(s.getOutputStream(), true);
            serverMessage msg = new serverMessage(s.getInetAddress().toString(), requestType, requestStatus, contents); // Change to server message
            serverOutput.println(msg.createMessage());
            serverOutput.flush();
            msgNum += 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    sends a message to the client of the type: "ERROR"
    input: errorMsg = contents of the error message
     */
    @Override
    public void sendError(String errorMsg) {
        try {
            serverOutput = new PrintWriter(s.getOutputStream(), true);
            serverMessage msg = new serverMessage(s.getInetAddress().toString(), "ERROR()", "0", errorMsg);
            serverOutput.println(msg.createMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    sends a message to the client of the type: "ERROR", with specified status number
    input: errorMsg = contents of the error message, errortype = status of the error message
     */
    @Override
    public void sendError(String errorType, String errorMsg) {
        try {
            serverOutput = new PrintWriter(s.getOutputStream(), true);
            serverMessage msg = new serverMessage(s.getInetAddress().toString(), "ERROR()", errorType, errorMsg);
            serverOutput.println(msg.createMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    return the users UUID as a string
     */
    @Override
    public String genUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /*
    splits the input into sublists on the character "/"
     */
    @Override
    public String[] splitInput(String input){
        return input.split("/");
    }

    /*
     Registers a new client in the database
     input: string with username and password of the new client
     sends a message to the client if the action succeeded or not
      */
    @Override
    public void registerClient(String input){
        String[] creds = splitInput(input);
        String username = creds[0];
        String password = creds[1];
        try {
            if(DB.clientExists(username)){
                sendError("Username already registered");
            }else {
                DB.addClient(username, password);
                sendMessage("CREATEUSER()", "1", "User "+username+" created");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println("ouf");
        }
    }

    /*
       closes the connection to the client
        */
    @Override
    public void closeConnection() throws IOException, SocketException {
        System.out.println("[Server]: Connection to client closed");
        s.close();
    }

    /*
    sends a message to the client with all the file names and directories for that client
    input: msgContents = username of the client
     */
    @Override
    public void listClientFiles(String msgContents) {
        if (!validateClient()) {
            //sendMessage("ERROR()", "0", "Unauthorized action");
            sendError("Unauthorized action");
            return;
        }
        try {
            String clientFiles = handler.listFiles(msgContents);
            if (clientFiles == null) {
                //System.out.println("null");
                sendMessage("ERROR()", "0", " ");
            }
            //System.out.println(clientFiles);
            sendMessage("FILES()", "1", clientFiles);
            //sendMessage("LIST()", "1", clientFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException a){
            sendError("Directory does not exist");
        }
    }

    /*
    generates a session cookie
    returns the random value as a string
     */
    public String genCookie(){
        int min = 10000;
        int max = 99999;
        int random = (int)(Math.random()*(max-min+1)+min);
        return String.valueOf(random);

    }

    /*
    checks credentials of a client and logs in if credentials match those in the database.
    input: input = username/password
    sends back a message with the uuid if login successfully, or error if not
     */
    public void loginClient(String input) {
        String[] creds = input.split("/");
        String username = creds[0];
        String password = creds[1];
        //cs.clientLogin(username, password);
        try {
            //String s = cs.clientQuery(username, password);
            String s = DB.clientQuery(username, password);
            if (s.equals("")) {
                sendError("0", "No user was found");

            } //else if (!cs.verifyPassword(password)) {
            else if(!DB.verifyPassword(username, password)){
                sendError("0", "Incorrect password");
            } else {
                //String uuid = genUUID();
                String uuid = genCookie();
                setClientName(username);
                setCurrClientUUID(uuid);
                if(!DB.clientDirExists(username)){
                    DB.createClientDir(username);
                    //System.out.println("Directory does not exist");
                }
                sendMessage("LOGIN()", "1", uuid);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
    updates one of the credentials(username or password) of a user.
    input: contents = specifies which of the credentials to update and the new value(username/newName)
    sends a message to the client specifying the outcome of the action
     */
    @Override
    public void updateCredentials(String contents){
        if(!validateClient()){
            sendError("Unauthorized action");
            return;
        }
        String[] fields = contents.split("/");
        String command = fields[0];
        try{
            String newCred = fields[1];
            switch (command){
                case "username":
                    if(DB.updateUsername(newCred, getClientName())){
                        sendMessage("UPDATE()", "1", "Updated username");
                    }else {
                        sendError("Unable to update password");
                    }
                    break;
                case "password":
                    if(DB.updatePassword(newCred, getClientName())){
                        System.out.println("yey");
                    }
                    break;
                default:
                    System.out.println("huh");
                    break;
            }


        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    /*
    checks if the current client has an uuid.
    return true if client has uuid, false if not
     */
    @Override
    public boolean validateClient() {
        String uuid = getCurrClientUUID();
        return uuid != null;
    }


    /*
    reads a file from the client and stores it at a specified location in the users directory.
    input: contents = local path,size of the file
    sends a message to the client specifying the outcome of the action
     */
    @Override
    public void receiveFile(String contents) {
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
            sendMessage("PUT()", "1", "File uploaded");
        } catch (Exception e) {
            //sendMessage("ERROR()", "0", "Unable to upload");
            sendError("Unable to upload");
        }
    }

    /*
    sends a specified file to the user.
    input: filepath = path to file to send
    sends a message to the user when the file is done sending
     */
    @Override
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
            //os.flush();
            sendMessage("GET()", "2", "Successfully downloaded file!");
            //System.out.println("[Server]: done");

        } catch (Exception e) {
            e.printStackTrace();
            //sendError("Unable to download");
        }
    }

    /*
    deletes a specified file on the server
    input: path = path o the file
    sends a message to the client if the deletion was a success or not
     */
    @Override
    public void deleteFile(String path) {
        String storagePath = handler.getStoragePath();
        File toDelete = new File(storagePath + "/" + path);
        if (toDelete.delete()) {
            sendMessage("DEL()", "1", "File deleted");
        } else {
            sendError("Unable to delete file");
        }
    }

    /*
    creates a new directory in the clients repository.
    input: dirPath = path to the ne directory
     */
    @Override
    public void createDir(String dirPath) {
        if (!validateClient()) {
            sendError("Unauthorized");
        }
        String storagePath = handler.getStoragePath();
        File newDir = new File(storagePath + dirPath);
        if (!newDir.exists()) {
            newDir.mkdir();
            sendMessage("DIR()", "1", "Directory created");
        } else {
            sendError("Directory already exists");
        }
    }


    /*
    renames a specified file on the server
    input: filePath = path to the file and path to new file with the new filename
     */
    @Override
    public void renameFile(String filePath) {
        if (!validateClient()) {
            sendError("Unauthorized");
        }
        String storagePath = handler.getStoragePath();
        String[] fromClient = filePath.split("/");
        String newName = fromClient[0];
        File oldFile = new File(storagePath + fromClient[1]);
        File newFile = new File(storagePath + fromClient[0]);
        if (oldFile.renameTo(newFile)) {
            sendMessage("RENAME()", "1", "Renamed file");
        } else {
            sendError("Could not rename file");
        }
    }

    @Override
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String getClientName() {
        return clientName;
    }


    @Override
    public void setCurrClientUUID(String currClientUUID) {
        this.currClientUUID = currClientUUID;
    }

    @Override
    public String getCurrClientUUID() {
        return currClientUUID;
    }

    @Override
    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    @Override
    public int getMsgNum() {
        return msgNum;
    }
}

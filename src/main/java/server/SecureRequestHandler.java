package server;

import encryption.ServerSSE;
import message.clientMessage;
import message.serverMessage;
import org.sqlite.SQLiteException;
import storage.ClientStorage;
import storage.DB;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.*;

class SecureRequestHandler extends Thread implements RequestHandlerInterface{
    private Socket s;
    private ClientStorage cs = new ClientStorage();
    private FileHandler handler = new FileHandler();
    private String currClientUUID;
    private String clientName;
    private clientMessage prev = null;
    private BufferedReader serverInput;
    private PrintWriter serverOutput;
    private int msgNum=0;
    private LinkedList<serverMessage> sendList = new LinkedList<>();
    private LinkedList<clientMessage> receiveList = new LinkedList<>();
    boolean running;

    SecureRequestHandler(Socket socket) {
        this.s = socket;
    }


    //Start the secure request handler loop
    @Override
    public void run() {
        setRunning(true);

        while (isRunning()){
            try {
                messageHandler();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /*
    Is called during the main loop if the secure request handler,
    interprets the message from client and starts the correct response function
     */
    @Override
    public void messageHandler() throws IOException{
        clientMessage clientMsg = receiveMessage();
        String requestType = clientMsg.getRequestType();
        String contents = clientMsg.getMessageContents();
        switch (requestType) {
            case "EXIT()":
                closeConnection();
                break;
            case "CREATEUSER()":
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
            case "SEARCH()":
                searchFiles(contents);
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
            case "LOOKUP()":
                lookupExists(contents);
                break;
            default:
                sendError("Unrecognized action");
                break;
        }
    }

    /*
    Checks if the lookup file exists for the current user.
    input: contents = message from client, format: username
    responds the user with true or false depending on the existence of the lookup file
     */
    private void lookupExists(String contents) {
        String storagePath = handler.getStoragePath();
        String filePath = storagePath + contents + "/.lookup";
        File tmp = new File(filePath);
        if (tmp.isFile()){
            sendMessage("LOOKUP()", "1", "true");
        }
        else {
            sendMessage("LOOKUP()", "1", "false");
        }

    }

    /*
    Searches through all files of the current user and checks if any of them contains the given search word.
    input: searchtoken = token for searching, format: encrypted search word + key for checking correctness.
    sends all documents that contains searchword back to user.
     */
    public void searchFiles(String searchToken){
        List<File> files = handler.listAllFiles(handler.getClientPath(getClientName()));
        ServerSSE sse = new ServerSSE();
        List<File> accepted = new ArrayList<>();

        for(File file : files){
            if(file.getName().equals(".lookup")){
                accepted.add(file);
                continue;
            }

            if(sse.checkMatch(file, searchToken)){
                accepted.add(file);
            }
        }
        //only .lookup in accepted files = no files with searchword
        if(accepted.size() == 1){
            sendError("No match");
            return;
        }


        sendMessage("GET()", "1", Integer.toString(accepted.size()));
        for(File file : accepted){
            try {
                File clientFile = file;
                byte[] buffer = new byte[(int) clientFile.length()];
                String fileSize = clientFile.length() + "";

                sendMessage("GET()", "1", fileSize + "/" + file.getName());
                FileInputStream fis = new FileInputStream(clientFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(buffer, 0, buffer.length);
                OutputStream os = s.getOutputStream();
                os.write(buffer, 0, buffer.length);
                os.flush();

                Thread.sleep(500);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sendMessage("GET()", "1", "Successfully downloaded file!");
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
            addReceiveList(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

   /*
   returns the list of received messages from client
    */
    public LinkedList<clientMessage> getReceiveList() {
        return receiveList;
    }

    /*
    updates the list of messages from client
     */
    public void setReceiveList(LinkedList<clientMessage> receiveList) {
        this.receiveList = receiveList;
    }

    /*
    adds a new list of received messages from client
     */
    public void addReceiveList(clientMessage message) {
        LinkedList<clientMessage> newList = getReceiveList();
        if(newList.size()>= 100){
            newList.removeLast();
        }
        newList.addFirst(message);
        setReceiveList(newList);
    }

    /*
    removes the current list of received messages from client
     */
    public void removeReceiveListItem(){
        getReceiveList().removeLast();
    }

    /*
    input: the message number for the wanted message
    returns a message from the list of received messages from client
     */
    public clientMessage getReceiveListItem(int messageNum){
        return getReceiveList().get(messageNum);
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
            addSendList(msg);
            incrementMsgNum();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    returns the list of messages sent to the client
     */
    public LinkedList<serverMessage> getSendList(){
        return sendList;
    }

    /*
    adds a new list of messages sent to the client
     */
    public void addSendList(serverMessage message){
        getSendList().add(message);
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
            addSendList(msg);

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
    public String[] splitInput(String input) {
        return input.split("/");
    }

    /*
    Registers a new client in the database
    input: string with username and password of the new client
    sends a message to the client if the action succeeded or not
     */
    @Override
    public void registerClient(String input) {
        String[] creds = splitInput(input);
        String username = creds[0];
        String password = creds[1];
        try{
            DB.secureAddClient(username,password);
            sendMessage("CREATEUSER()", "1", "User "+username+" created");
        } catch (SQLiteException e){
            sendError("User already exists");
        } catch (SQLException e) {
           sendError("An error occurred");
        }
    }

    /*
    closes the connection to the client
     */
    @Override
    public void closeConnection() throws IOException, SocketException {
        s.close();
    }

    /*
    sends a message to the client with all the file names and directories for that client
    input: msgContents = username of the client
     */
    @Override
    public void listClientFiles(String msgContents) {
        if(!validateClient()){
            sendError("Unauthorized action");
            return; }
        try {
            String clientFiles = handler.listFiles(msgContents);
            if(clientFiles == null){
                sendMessage("ERROR()", "0", "Empty directory");
                return;
            }
            sendMessage("FILES()", "1", clientFiles);

        }catch(IOException e){
            sendError("An error occured");
        }catch(NullPointerException a){
            sendError("No such directory");
        }

    }

    /*
    checks credentials of a client and logs in if credentials match those in the database.
    input: input = username/password
    sends back a message with the uuid if login successfully, or error if not
     */
    @Override
    public void loginClient(String input) {
        String[] creds = splitInput(input);
        String username = creds[0];
        String password = creds[1];
        if (DB.secureLogin(username, password)){
            String uuid = genUUID();
            setClientName(username);
            setCurrClientUUID(uuid);
            if(!DB.clientDirExists(username)){
                DB.createClientDir(username);
            }
            sendMessage("LOGIN()", "1", uuid);
        }else{
            sendError("Login()","Credentials does not match");
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
        String newCred = fields[1];
        switch (command){
            case "username":
                try {
                    DB.secureUpdateUsername(newCred, getClientName());
                    sendMessage("UPDATE()", "1", "Updated username");
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendError("Unable to update username");
                }
                break;
            case "password":
                try {
                    DB.secureUpdatePassword(newCred, getClientName());
                    sendMessage("UPDATE()", "1", "Updated password");
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendError("Unable to update password");
                }
                break;
            default:
                sendError("Unrecognized action");
                break;
            }
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
            String storagePath = handler.getStoragePath();
            String fileName = storagePath + fileInfo[0];
            String fileSize = fileInfo[1];
            InputStream dis = new DataInputStream(s.getInputStream());
            OutputStream fos = new FileOutputStream(fileName);
            int size = Integer.parseInt(fileSize);
            byte[] buffer = new byte[size];

            int read = 0;
            int bytesRead = 0;

            while ((read = dis.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
                bytesRead = bytesRead + read;
                if (size == bytesRead) {
                    break;
                }
            }
            sendMessage("PUT()", "1", "File uploaded");
        } catch (Exception e) {
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
            File clientFile = new File(fileName);
            byte[] buffer = new byte[(int) clientFile.length()];
            String fileSize = clientFile.length() + "";

            sendMessage("GET()", "1", fileSize);
            FileInputStream fis = new FileInputStream(clientFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(buffer, 0, buffer.length);
            OutputStream os = s.getOutputStream();
            os.write(buffer, 0, buffer.length);
            os.flush();
            sendMessage("GET()", "2", "Successfully downloaded file!");

        } catch (Exception e) {
            e.printStackTrace();
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
        String clientPath = handler.getClientPath(getClientName());
        File toDelete = new File(clientPath + path);
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

    /*
    sets client name
     */
    @Override
    public void setClientName(String clientName) {
        this.clientName = clientName;

    }

    /*
    gets client name
     */
    @Override
    public String getClientName() {
        return clientName;
    }

    /*
    sets the currents clients uuid
     */
    @Override
    public void setCurrClientUUID(String currClientUUID) {
        this.currClientUUID = currClientUUID;
    }

    /*
    gets the current client uuid
     */
    @Override
    public String getCurrClientUUID() {
        return currClientUUID;
    }

    /*
    sets the number of the current message
     */
    @Override
    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    /*
    gets the message number of the current message
     */
    @Override
    public int getMsgNum() {
        return msgNum;
    }

    /*
    increments the current message number
     */
    public void incrementMsgNum(){
        setMsgNum(getMsgNum()+1);
    }

    /*
    sets the running status of the server
     */
    @Override
    public void setRunning(boolean running) {
        this.running = running;

    }

    /*
    checks if the server is running or not
     */
    @Override
    public boolean isRunning() {
        return running;
    }
    
}

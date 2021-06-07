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

    @Override
    public void messageHandler() throws IOException{
        clientMessage clientMsg = receiveMessage();
        String requestType = clientMsg.getRequestType();
        String contents = clientMsg.getMessageContents();
        switch (requestType) {
            case "EXIT()":
                closeConnection();
                break;
            case "CREATEUSER()": // TODO
                registerClient(contents);
                break;
            case "LOGIN()":
                loginClient(contents);
                break;
            case "LIST()": // TODO
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

    private void lookupExists(String contents) {
        String storagePath = handler.getStoragePath();
        String filePath = storagePath + contents + "/.lookup";
        File tmp = new File(filePath );
        System.out.println(filePath);
        if (tmp.isFile()){
            sendMessage("LOOKUP()", "1", "true");
        }
        else {
            sendMessage("LOOKUP()", "1", "false");
        }

    }

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

    public LinkedList<clientMessage> getReceiveList() {
        return receiveList;
    }

    public void setReceiveList(LinkedList<clientMessage> receiveList) {
        this.receiveList = receiveList;
    }

    public void addReceiveList(clientMessage message) {
        LinkedList<clientMessage> newList = getReceiveList();
        if(newList.size()>= 100){
            newList.removeLast();
        }
        newList.addFirst(message);
        setReceiveList(newList);
    }

    public void removeReceiveListItem(){
        getReceiveList().removeLast();
    }


    public clientMessage getReceiveListItem(int messageNum){
        return getReceiveList().get(messageNum);
    }



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

    public LinkedList<serverMessage> getSendList(){
        return sendList;
    }

    public void addSendList(serverMessage message){
        getSendList().add(message);
    }


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


    @Override
    public String genUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


    @Override
    public String[] splitInput(String input) {
        return input.split("/");
    }

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

    @Override
    public void closeConnection() throws IOException, SocketException {
        s.close();
    }

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

    @Override
    public boolean validateClient() {
        String uuid = getCurrClientUUID();
        return uuid != null;
    }

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

    public void incrementMsgNum(){
        setMsgNum(getMsgNum()+1);
    }


    @Override
    public void setRunning(boolean running) {
        this.running = running;

    }

    @Override
    public boolean isRunning() {
        return running;
    }




}

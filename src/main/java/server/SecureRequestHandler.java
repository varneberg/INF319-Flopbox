package server;

import builder.SecureState;
import encryption.ServerSSE;
import message.clientMessage;
import message.serverMessage;
import storage.ClientStorage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecureRequestHandler extends Thread implements RequestHandlerInterface{
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
    private clientMessage clientMsg;
    private int msgNum;
    private ArrayList<serverMessage> msgList;
    boolean secure = SecureState.getINSTANCE().isSecure();
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
        //FileHandler handler = new FileHandler();
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
            case "SEARCH()": // TODO Searchable encryption
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
            default:
                sendError("Unrecognized action");
                break;
        }
    }

    public void searchFiles(String searchtoken){
        List<File> files = handler.listAllFiles(handler.getClientPath(getClientName()));
        ServerSSE sse = new ServerSSE();
        List<File> accepted = new ArrayList<>();
        for(File file : files){
            if(file.getName().equals(".lookup")){
                accepted.add(file);
                continue;
            }

            if(sse.checkMatch(file, searchtoken)){
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
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                File clientFile = file;
                byte[] buffer = new byte[(int) clientFile.length()];
                long filesize = clientFile.length();
                String fileSize = clientFile.length() + "";

                sendMessage("GET()", "1", fileSize + "/" + file.getName());
                FileInputStream fis = new FileInputStream(clientFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(buffer, 0, buffer.length);
                OutputStream os = s.getOutputStream();
                os.write(buffer, 0, buffer.length);
                os.flush();

                //System.out.println("[Server]: done");

            } catch (Exception e) {
                e.printStackTrace();
                //sendError("Unable to download");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

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
    @Override
    public void sendError(String errorMsg) {

    }

    @Override
    public void sendError(String errorType, String errorMsg) {

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
        System.out.println(password);

    }

    @Override
    public void closeConnection() throws IOException, SocketException {

    }

    @Override
    public void listClientFiles(String msgContents) {

    }

    @Override
    public void loginClient(String input) {
        String[] creds = splitInput(input);
        String username = creds[0];
        String password = creds[1];
        sendMessage("LOGIN()","1", genUUID());

    }

    @Override
    public boolean validateClient() {
        return false;
    }

    @Override
    public void receiveFile(String contents) {

    }

    @Override
    public void sendFile(String filePath) {

    }

    @Override
    public void deleteFile(String path) {

    }

    @Override
    public void createDir(String dirPath) {

    }

    @Override
    public void renameFile(String filePath) {

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

    }

    @Override
    public String getCurrClientUUID() {
        return null;
    }

    @Override
    public void setMsgNum(int msgNum) {

    }

    @Override
    public int getMsgNum() {
        return 0;
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

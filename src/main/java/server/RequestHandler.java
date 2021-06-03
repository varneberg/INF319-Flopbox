package server;

import builder.SecureState;
import message.clientMessage;
import message.serverMessage;
import storage.ClientStorage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
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
    //private String sep = ";;";
    private BufferedReader serverInput;
    private PrintWriter serverOutput;
    private clientMessage clientMsg;
    private int msgNum;
    private ArrayList<serverMessage> msgList;
    boolean secure = SecureState.getINSTANCE().isSecure();
    boolean running;


    RequestHandler(Socket socket) {
        this.s = socket;
    }

    @Override
    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        setRunning(true);
        /*
        if (secure) {
            while (isRunning()) {
                try {
                    secureMessageHandler();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            */
            while (isRunning()) {
                try {
                    messageHandler();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    /*
    @Override
    public void run() {
        boolean running = true;
        try {
            while (running) {
                //System.out.println(msgNum);
                clientMessage clientMsg = receiveMessage();
                //FileHandler handler = new FileHandler();
                String requestType = clientMsg.getRequestType();
                String contents = clientMsg.getMessageContents();
                String clientUUID = clientMsg.getUuid();
                String storagePath;
                String filename;
                String status;
                switch (requestType) {
                    case "EXIT()":
                        closeConnection();
                        running = false;
                        break;
                    case "CREATEUSER()":
                        createNewClient(contents);
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
                    default:
                        sendError("Unrecognized action");
                        break;
                }
                //clientMsg = null;
                //clientMsg = null;
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

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
            default:
                sendError("Unrecognized action");
                break;
        }
    }

    public void secureMessageHandler() throws IOException {
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
            case "LOGIN()": // TODO
                loginClient(contents);
                break;
            case "LIST()": // TODO
                listClientFiles(contents);
                break;
            case "GET()":
                sendFile(contents);
                break;
            case "SEARCH()": // TODO Searchable encryption
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
        try {
            serverOutput = new PrintWriter(s.getOutputStream(), true);
            serverMessage msg = new serverMessage(s.getInetAddress().toString(), "ERROR()", "0", errorMsg);
            serverOutput.println(msg.createMessage());
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
    public String[] splitInput(String input){
        return input.split("/");
    }

    @Override
    public void registerClient(String input) {
        //String[] creds = input.split("/");
        String[] creds = splitInput(input);
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
    @Override
    public void closeConnection() throws IOException, SocketException {
        System.out.println("[Server]: Connection to client closed");
        s.close();
    }

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
                sendMessage("FILES()", "0", " ");
            }
            sendMessage("FILES()", "1", clientFiles);
            //sendMessage("LIST()", "1", clientFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loginClient(String input) {
        String[] creds = input.split("/");
        String username = creds[0];
        String password = creds[1];
        //cs.clientLogin(username, password);
        try {
            String s = cs.clientQuery(username, password);
            if (s.equals("")) {
                sendError("-1", "No user was found");

            } else if (!cs.verifyPassword(password)) {
                sendError("0", "Incorrect password");

            } else {
                String uuid = genUUID();
                setClientName(username);
                setCurrClientUUID(uuid);
                sendMessage("LOGIN()", "1", uuid);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean validateClient() {
        String uuid = getCurrClientUUID();
        return uuid != null;
    }


    //private void receiveFile(String fileName, String fileSize) throws Exception {
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
}

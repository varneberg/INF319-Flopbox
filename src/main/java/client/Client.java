package client;


import message.clientMessage;
import message.serverMessage;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;


public class Client {
    //public class Client implements Runnable{

    private String name; // TODO setName on validated login
    private String sep = "//s//";
    String uuid = "null";
    Thread t;
    int port;
    Socket s;
    String clientPath = null;
    private static DataOutputStream dataOutput = null;
    private static DataInputStream dataInput = null;
    private static String storagePath = "src/main/resources/clientStorage/";
    BufferedReader clientInput = null;
    PrintWriter clientOutput = null;
    serverMessage serverMsg = null;


    public Client(int port) {
        this.port = port;
    }

    public Client(String address, int port) {
        try {
            this.port = port;
            s = new Socket(address, port);
            this.uuid = uuid;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public serverMessage receiveMessage() {
        serverMessage msg = null;
        try {
            clientInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
            msg = new serverMessage();
            msg.receiveMessage(clientInput.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverMsg = msg;
        return msg;
    }

    public void sendMessage(String requestType, String contents) {
        try {
            //ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());
            clientOutput = new PrintWriter(s.getOutputStream(), true);
            clientMessage message = new clientMessage(s.getInetAddress().toString(), getUuid(), requestType, contents);
            clientOutput.println(message.createMessage());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getServerMessageStatus() {
        return serverMsg.getRequestStatus();
    }

    public String getServerMessageContents() {
        return serverMsg.getMessageContents();
    }

    public void printServerContents(){
        System.out.println(serverMsg.getMessageContents());

    }
    // TODO add server response as return
    public String createUser(String username, String password) {
        //sendServer("CREATEUSER()");
        String credentials = username + "/" + password;
        //sendServer("CREATEUSER()",credentials);
        sendMessage("CREATEUSER()", credentials);
        serverMessage servermsg = receiveMessage();
        return servermsg.getRequestStatus();

        //return serverResponse;

    }



    public boolean isAuthenticated(String username, String password) {
        if (getUuid().equals("null")) {
            return false;
        } else {
            return true;
        }
    }

    public void login(String username, String password) {
        //sendAuthentication(username, password);
        //String input = receiveServer();
        sendMessage("LOGIN()", username + "/" + password);
        serverMessage servermsg = receiveMessage();
        String status = servermsg.getRequestStatus();
        String contents = servermsg.getMessageContents();
        if (status.equals("1")) {
            setUuid(contents);
            setName(username);
        }
        //System.out.println(status + " " + contents);
        //return status;
    }

    // Receive names for files stored on server
    public String[] receiveFileNames() {
        sendMessage("FILES()", "LIST()");
        serverMessage msg = receiveMessage();
        String rawFilenames = msg.getMessageContents();
        String[] fileNames = rawFilenames.split("\t");
        return fileNames;
    }

    public File getFile(String fileName){
        sendMessage("FILES()", "GET()" + sep + fileName);
        return null;
    }

    public void putFile(){}

    public boolean registerClient(String name, String password) {
        return false;
    }

    public void sendFile(File filename) throws Exception{
        String fullPath = storagePath + filename;

        int bytes = 0;
        File file = new File(fullPath);
        String path = file.getAbsolutePath();

        FileInputStream fileInputStream = new FileInputStream(fullPath);

        // send file size
        dataOutput.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutput.write(buffer, 0, bytes);
            dataOutput.flush();
        }
        fileInputStream.close();
    }

    public String getName() {
        return this.name;
    }

    public void setSocket(Socket s) {
        this.s = s;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}

package client;


import builder.SecureState;
import message.clientMessage;
import message.serverMessage;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import encryption.*;


public class Client {
    //public class Client implements Runnable{

    private String name; // TODO setName on validated login
    String uuid = null;
    Thread t;
    int port;
    Socket s;
    private static String storagePath = "src/main/resources/clientStorage/";
    private BufferedReader clientInput = null;
    private PrintWriter clientOutput = null;
    private serverMessage serverMsg = null;
    boolean secure = SecureState.getINSTANCE().isSecure();
   // private DataOutput dataOutput=null;


    //private DataInputStream dataInput=null;


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

    public void receiveMessage() {
        serverMessage msg = null;
        try {
            clientInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
            msg = new serverMessage();
            msg.receiveMessage(clientInput.readLine());
            setServerMsg(msg);

            msg = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
        //serverMsg = msg;
        //return msg;
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


    public void createUser(String username, String password) {
        //sendServer("CREATEUSER()");
        if(secure){password = SHA256.getDigest(password);}
        String credentials = username + "/" + password;
        //sendServer("CREATEUSER()",credentials);
        sendMessage("CREATEUSER()", credentials);
        //serverMessage servermsg = receiveMessage();
        receiveMessage();
        //return servermsg.getRequestStatus();

        //return serverResponse;
    }


    public boolean isAuthenticated(String username, String password) {
        if (getUuid() == null) {
            return false;
        } else {
            return true;
        }
    }
    public boolean isAuthenticated() {
        if (getUuid() == null) {
            return false;
        } else {
            return true;
        }
    }

    public void login(String username, String password) {
        //sendAuthentication(username, password);
        //String input = receiveServer();
        if(secure){ password = SHA256.getDigest(password); }
        sendMessage("LOGIN()", username + "/" + password);
        //serverMessage servermsg = receiveMessage();
        receiveMessage();
        //String status = servermsg.getRequestStatus();
        String status = getServerMessageStatus();
        //String contents = servermsg.getMessageContents();
        String contents = getServerMessageContents();
        if (status.equals("1")) {
            setUuid(contents);
            setName(username);
        }
        //System.out.println(status + " " + contents);
        //return status;
    }

    // Receive names for files stored on server
    public String[] getFileNames(String folderPath) {
        sendMessage("LIST()", folderPath);
        String[] filenames= null;
        //serverMessage msg = receiveMessage();
        try {
            receiveMessage();

            //String rawFilenames = msg.getMessageContents();
            String rawFilenames = getServerMessageContents();
            filenames = rawFilenames.split(",");
            return filenames;
        } catch (Exception e){
            filenames = new String[]{""};
            return filenames;
        }
    }

    public List<String> getFileArray(String folderPath){
        List<String> filenames = null;
        sendMessage("LIST()", folderPath);
        try{
            receiveMessage();
            filenames = new ArrayList<String>();
            String rawFileNames = getServerMessageContents();
            String fileNames[] = rawFileNames.split(",");
            return Arrays.asList(fileNames);

        } catch (Exception e){
            filenames = new ArrayList<String>();
            return filenames;
        }

    }

    public File getFile(String fileName){
        sendMessage("FILES()", "GET()"  + fileName);
        return null;
    }

    public void putFile(String localPath, String serverPath){
        try {
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            File clientFile = new File(localPath);
            byte[] buffer = new byte[(int)clientFile.length()];
            long filesize = clientFile.length();
            String fileInfo = serverPath + ","+ filesize;
            sendMessage("PUT()", fileInfo);
            FileInputStream fis = new FileInputStream(clientFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(buffer,0,buffer.length);
            OutputStream os = s.getOutputStream();
            os.write(buffer,0,buffer.length);
            os.flush();
            receiveMessage();
            //System.out.println("[Client]: done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createDir(String folderPath){
        sendMessage("DIR()", folderPath);
        receiveMessage();
    }

    public void getFile(String serverPath, String downloadPath) throws IOException {
        sendMessage("GET()", serverPath);
        receiveMessage();
        int size = Integer.parseInt(getServerMessageContents());
        InputStream dis = new DataInputStream(s.getInputStream());
        OutputStream fos = new FileOutputStream(downloadPath);
        byte[] buffer = new byte[size];

        int read = 0;
        int bytesRead=0;

        while((read = dis.read(buffer)) > 0){
            //System.out.println("[Client]: Writing");
            fos.write(buffer,0,read);
            bytesRead = bytesRead + read;
            //System.out.println(bytesRead+"/"+size);
            //if(size >= bytesRead){
            //    continue;
            //}else {break;}

        }
        receiveMessage();
        //System.out.println("[Client]: done");
    }

    public void deleteFile(String pathToFile){
        sendMessage("DEL()", pathToFile);
        receiveMessage();
    }

    public void renameFile(String newName, String pathToFile){
        sendMessage("RENAME()", newName+"/"+pathToFile);
        receiveMessage();
    }

    public String getName() {
        return this.name;
    }

    public String getBaseDir(){
        return getName()+"/";
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean validRequest(){
        if ("1".equals(getServerMessageStatus())) {
            return true; }
        else {
            return false; }
    }

    public void setServerMsg(serverMessage serverMsg) {
        this.serverMsg = serverMsg;
    }

    public serverMessage getServerMsg() { return serverMsg; }


    public String getServerMessageStatus() {
        return serverMsg.getRequestStatus();
    }

    public String getServerMessageContents() {
        return serverMsg.getMessageContents();
    }

    public String getServerMessageType(){
        return serverMsg.getRequestType();
    }

    public void printServerContents(){
        System.out.println(serverMsg.getMessageContents());
    }

    public void setSocket(Socket s) {
        this.s = s;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}

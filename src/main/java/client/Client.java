package client;


import builder.SecureState;
import message.clientMessage;
import message.serverMessage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import encryption.*;

public class Client {

    private String name; // TODO setName on validated login
    String uuid = null;
    Thread t;
    int port;
    Socket s;
    private static String tmpFolder = "./src/main/resources/tmp/";
    private BufferedReader clientInput = null;
    private PrintWriter clientOutput = null;
    private serverMessage serverMsg = null;
    boolean secure = SecureState.getINSTANCE().isSecure();

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
    public Client(String address, int port, String name, String uuid){
        try{
            this.port = port;
            s = new Socket(address, port);
            this.name = name;
            this.uuid = uuid;
        }catch (IOException e){
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
    }

    public void sendMessage(String requestType, String contents) {
        try {
            clientOutput = new PrintWriter(s.getOutputStream(), true);
            clientMessage message = new clientMessage(s.getInetAddress().toString(), getUuid(), requestType, contents);
            clientOutput.println(message.createMessage());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createUser(String username, String password) {
        if(secure){password = SHA256.getDigest(password);}
        String credentials = username + "/" + password;
        sendMessage("CREATEUSER()", credentials);
        receiveMessage();
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
        if(secure){ password = SHA256.getDigest(password); }
        sendMessage("LOGIN()", username + "/" + password);
        receiveMessage();
        String status = getServerMessageStatus();
        String contents = getServerMessageContents();

        if (status.equals("1")) {
            setUuid(contents);
            setName(username);
        }
    }

    public void logout(){
        setUuid(null);
    }

    public void changeUsername(String newUsername){
        String toSend = "username/"+newUsername;
        sendMessage("UPDATE()", toSend);
        receiveMessage();

    }

    public void changePassword(String newPassword){
        if(secure){
            newPassword = SHA256.getDigest(newPassword);
        }
        String toSend = "password/"+newPassword;
        sendMessage("UPDATE()", toSend);

    }

    // Receive names for files stored on server
    public String[] getFileNames(String folderPath) {
        sendMessage("LIST()", folderPath);
        String[] filenames= null;
        try {
            receiveMessage();

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

    public void putFile(String localPath, String serverPath){
        if(secure){
            ClientSSE sse = new ClientSSE(getName());
            sendMessage("LOOKUP()", getName());
            receiveMessage();
            if(getServerMessageContents().equals("true")){
                try {
                    getFile(getName() +"/.lookup", tmpFolder + ".lookup");
                    File tmp = new File(tmpFolder +".lookup");
                    sse.setLookup(tmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            File encrypted = sse.encryptFile(new File(localPath));
            File lookup = sse.getLookup();
            uploadFile(encrypted.getAbsolutePath(), serverPath);
            uploadFile(lookup.getAbsolutePath(), getName() + "/" + lookup.getName());
        }
        else{
            uploadFile(localPath, serverPath);
        }

    }

    private void uploadFile(String localPath, String serverPath){
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
            receiveMessage();;

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

        while(bytesRead < size){
            read = dis.read(buffer);
            if(getServerMessageStatus().equals("2")){
                break;
            }
            fos.write(buffer, 0, read);
            bytesRead = bytesRead + read;
        }
        receiveMessage();
    }

    public void getMultipleFiles(String searchWord) throws IOException {
        String downloadPath = "./src/main/resources/tmp/";

        ClientSSE sse = new ClientSSE(getName());
        String searchToken = sse.generateSearchToken(searchWord);
        File lookup;
        sendMessage("SEARCH()", searchToken);
        receiveMessage();
        int numberOfFiles = Integer.parseInt(getServerMessageContents());
        List<String> filesToBeDecrypted = new ArrayList<>();
        for(int i=0;i<numberOfFiles;i++){
            receiveMessage();
            String response = getServerMessageContents();
            String[] sizeAndName = response.split("/");
            int size = Integer.parseInt(sizeAndName[0]);
            String fileName = sizeAndName[1];
            InputStream dis = new DataInputStream(s.getInputStream());
            OutputStream fos = new FileOutputStream(downloadPath + fileName);
            byte[] buffer = new byte[size];

            int read = 0;
            int bytesRead=0;

            while(bytesRead < size){
                read = dis.read(buffer);
                fos.write(buffer, 0, read);
                bytesRead = bytesRead + read;
            }

            System.out.println("Downloaded file: " + fileName);

            if(fileName.equals(".lookup")){
                lookup = new File(downloadPath + fileName);
                sse.setLookup(lookup);
            }
            else{
                filesToBeDecrypted.add(downloadPath + fileName);
            }
        }
        receiveMessage();

        for(String file : filesToBeDecrypted){
            File current = sse.decryptFile(new File(file));
        }
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
        return "1".equals(getServerMessageStatus());
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

    public Socket getS() {
        return s;
    }

    public void search(String search) {
        if(secure){
            try {
                getMultipleFiles(search);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            System.out.println("NO!");
        }
    }
}

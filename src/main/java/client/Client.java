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

    /*
    creates a new client on a given port number
     */
    public Client(int port) {
        this.port = port;
    }

    /*
    creates a new client on a given port number and a specified address
     */
    public Client(String address, int port) {
        try {
            this.port = port;
            s = new Socket(address, port);
            this.uuid = uuid;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /*
    creates a new client on a given port number and a specified address with a given name and uuid
     */
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

    /*
    reads a message from the server
     */
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

    /*
    sends a message to the server.
    input: requestType = type of request to send, contents = main contents of the message
     */
    public void sendMessage(String requestType, String contents) {
        try {
            clientOutput = new PrintWriter(s.getOutputStream(), true);
            clientMessage message = new clientMessage(s.getInetAddress().toString(), getUuid(), requestType, contents);
            clientOutput.println(message.createMessage());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    sends a request to the server to add the user to the database.
    receives status message from the server
    input: username = username of the user, password = password of the user
     */
    public void createUser(String username, String password) {
        if(secure){password = SHA256.getDigest(password);}
        String credentials = username + "/" + password;
        sendMessage("CREATEUSER()", credentials);
        receiveMessage();
    }

    /*
    checks if the server has authenticated the user.
    returns true of the user is authenticated, or false if not
     */
    public boolean isAuthenticated() {
        if (getUuid() == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
    sends a login request to the server. sets uuid and username if response from server was successful
    input: username = username of the user, password = password of the user
     */
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

    /*
    logs out the user by deleting the uuid
     */
    public void logout(){
        setUuid(null);
    }

    /*
    sends a request to the server to change username
    input: newUsername = the new username
     */
    public void changeUsername(String newUsername){
        String toSend = "username/"+newUsername;
        sendMessage("UPDATE()", toSend);
        receiveMessage();

    }

    /*
    sends a request to the server to change password for the user.
    input: newPassword = the new password for the user.
     */
    public void changePassword(String newPassword){
        if(secure){
            newPassword = SHA256.getDigest(newPassword);
        }
        String toSend = "password/"+newPassword;
        sendMessage("UPDATE()", toSend);

    }

    /*
    Receive names for files stored in a given directory on server
    input: folderpath = path of the folder to get filenames from
    returns the names in a string array
     */
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

    /*
    Gets the list of file names in a specified folder for a client
    input: folderPath = path for the folder to get the filenames from
    returns the names in a list of strings
     */
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

    /*
    uploads a file to a specified location on the clients directory on the server.
    if the secure version is active, the lookupfile is downloaded and updated, then uploaded with the original file
    input: localPath = path to the file to be uploaded, serverPath = path on the server to upload the file
     */
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

    /*
    uploads file to the server, helper function used by put file
    input: localPath = path to the file to be uploaded, serverPath = path on the server to upload the file
     */
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

    /*
    requests to create a new directory to the server.
    input: folderPath = path to the new folder on the server
     */
    public void createDir(String folderPath){
        sendMessage("DIR()", folderPath);
        receiveMessage();
    }

    /*
    sends a get request for a file to the server, if successful stores the file on a specified location
    input: serverPath = path to the file requested on the server, downloadPath = path for the downloaded file to be stored
     */
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

    /*
    creates a seacrhtoken and requests an encrypted search to the server,
     if a match was found the files are downloaded and store in a folder specified in the function.
     input: searchword = the word to be searched for in the files on the server
     */
    public void getMultipleFiles(String searchWord) throws IOException {
        String downloadPath = "./src/main/resources/tmp/";

        ClientSSE sse = new ClientSSE(getName());
        String searchToken = sse.generateSearchToken(searchWord);
        File lookup;
        sendMessage("SEARCH()", searchToken);
        receiveMessage();
        String res = getServerMessageContents();
        if(res.equals("No match")){
            System.out.println("No Match");
            return;
        }
        int numberOfFiles = Integer.parseInt(res);
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

    /*
    requests to delete a file on the server
    input: pathtoFile = path to the file being requested to delete
     */
    public void deleteFile(String pathToFile){
        sendMessage("DEL()", pathToFile);
        receiveMessage();
    }

    /*
    requests to rename a file on the server
    input: newName = new name of the file, pathToFile = path to the file to be renamed
     */
    public void renameFile(String newName, String pathToFile){
        sendMessage("RENAME()", newName+"/"+pathToFile);
        receiveMessage();
    }

    /*
    gets the name of the client
     */
    public String getName() {
        return this.name;
    }

    /*
    gets base directory path of the client on the server
     */
    public String getBaseDir(){
        return getName()+"/";
    }

    /*
    sets the name of the client
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
    checks if the last server message was 1, which means successful
    returns true if last request was success, false if not
     */
    public boolean validRequest(){
        return "1".equals(getServerMessageStatus());
    }

    /*
    sets the next server message contents
     */
    public void setServerMsg(serverMessage serverMsg) {
        this.serverMsg = serverMsg;
    }

    /*
    get the last server message
     */
    public serverMessage getServerMsg() { return serverMsg; }

    /*
    gets the status of the last server message
     */
    public String getServerMessageStatus() {
        return serverMsg.getRequestStatus();
    }

    /*
    gets the contents of the last server message
     */
    public String getServerMessageContents() {
        return serverMsg.getMessageContents();
    }

    /*
    gets the message type of the last server message
     */
    public String getServerMessageType(){
        return serverMsg.getRequestType();
    }

    /*
    prints the contents of the last server message
     */
    public void printServerContents(){
        System.out.println(serverMsg.getMessageContents());
    }

    /*
    sets the socket being used
     */
    public void setSocket(Socket s) {
        this.s = s;
    }

    /*
    checks if the system is set to secure mode or not
    true if secure, false if not
     */
    public boolean isSecure() {
        return secure;
    }

    /*
    sets the uuid of the user
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /*
    gets the uuid of the user
     */
    public String getUuid() {
        return uuid;
    }

    /*
    gets the socket being used
     */
    public Socket getS() {
        return s;
    }

    /*
    if the system is in secure mode, the function generates and sends a search request to the server
    if not, prints an error message
     */
    public void search(String search) {
        if(secure){
            try {
                getMultipleFiles(search);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            System.out.println("Not in secure mode");
        }
    }
}

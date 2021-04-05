package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable{
    String name;
    String password;
    Thread t;
    String message;
    int port;
    private Socket s;
    private static DataOutputStream dataOutput = null;
    private static DataInputStream dataInput = null;
    private static String storagePath = "src/main/resources/clientStorage/";



    public Client(int port, String name, String password) {

        this.port = port;
        this.name = name;
        this.password = password;
    }
    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run(){
        try{
            s = new Socket("localhost",port);
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter clientOutput = new PrintWriter(s.getOutputStream(), true);

            boolean auth = false;
            String uname = "tes123";
            String password = "test";
            String creds = uname +"\t"+password;

            // Send to server
            clientOutput.println(creds);
            String serverMessage = serverInput.readLine();
            System.out.println(serverMessage);
            /*
            while(true) {
                sendCredentials();
                String authmsg = receiveServer();
                if(authmsg.equals("1")){
                    System.out.println("[Server]: Valid credentials");
                    break;
                } if(authmsg.equals("-1")){
                    System.out.println("[Server]: Incorrect password");
                } if(authmsg.equals("0")){
                    System.out.println("[Server]: No user was found with given name");
                } else { continue; }
            }
             */

            //s.close();
            //closeConnection();

        }catch(Exception e){
            System.out.println(e);
        }
    }

    // Send data to server
    private void sendServer(String message) throws IOException {
        //DataOutputStream out = new DataOutputStream(s.getOutputStream());
        dataOutput = new DataOutputStream(s.getOutputStream());
        dataOutput.writeByte(1);
        dataOutput.writeUTF(message);
        dataOutput.writeByte(-1);
        dataOutput.flush();
        /*
        out.writeByte(1);
        out.writeUTF(message);
        out.writeByte(-1);
        out.flush();
        /*dataOutput = new DataOutputStream(socket.getOutputStream());
        dataOutput.writeBytes(message + '\n');
        dataOutput.flush();
         */
    }

    // Receive data from server
    private String receiveServer() throws IOException {
        //DataInputStream inp = new DataInputStream(s.getInputStream());
        //byte msgStream = inp.readByte();
        //return inp.readUTF();
        dataInput = new DataInputStream(s.getInputStream());
        byte msgStream = dataInput.readByte();
        String inp = dataInput.readUTF();
        return inp;
    }

    // Receive names for files stored on server
    private String receiveFileNames() throws IOException {
        String[] filename;
        String nameString = "";
        String inp = receiveServer();
        //System.out.println(inp);
        return inp;
    }

    // Closes current connection to server
    private void closeConnection() throws IOException{
        sendServer("exit()");
        System.out.println("[Client]: Connection to server closed");
        s.close();
    }


    private void sendCredentials() throws IOException {
        System.out.println("[Client]: Please enter your credentials\n");
        Scanner sc = new Scanner(System.in);

        System.out.println("Username:");
        String uname = sc.nextLine();
        System.out.println("Password:");
        String passwd= sc.nextLine();

        String creds = uname +"\n"+ passwd;
        sendServer(creds);
    }

    private boolean authenticate() {
        return false;
    }

    private void sendFile(String filename) throws Exception{
        String fullPath = storagePath + filename;

        int bytes = 0;
        File file = new File(fullPath);
        String path = file.getAbsolutePath();

        FileInputStream fileInputStream = new FileInputStream(fullPath);

        // send file size
        dataOutput.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutput.write(buffer,0,bytes);
            dataOutput.flush();
        }
        fileInputStream.close();
    }


    public String getMessage() { return message; }

    public String getPassword() { return password; }

    public String getName() { return name; }

}

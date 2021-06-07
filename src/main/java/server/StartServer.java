package server;

import builder.SecureState;
import client.Client;
import encryption.ClientSSE;
import encryption.MD5;
import encryption.SHA256;
import malicious.maliciousSQL;
import server.Server;
import storage.ClientStorage;
import storage.DB;

import java.io.File;
import java.io.IOException;

public class StartServer {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //SecureState.getINSTANCE().setSecure(true);
        SecureState.getINSTANCE().setSecure(true);
        maliciousSQL msql = new maliciousSQL();
        ClientStorage cs = new ClientStorage();
        DB.initDB();
        DB.createClientTable();
        DB.createSecureClientTable();
        //DB.secureDeleteAllClients();
        //System.out.println(DB.SecureListClients());
        //System.out.println(cs.listAllClients());
        int port = 6666;
        String address = "localhost";
        Server server = new Server(port);
        server.startServer();
        String testString = "123";



        Client client = new Client(address, port);
        String username = "burp";
        String password = "123";
        client.login(username, password);

        System.out.println(client.isAuthenticated());
        client.printServerContents();

        //File f1 = new File("./src/test/testFiles/test1.txt");
        //client.putFile("./src/test/testFiles/test1.txt", "burp/test1.txt");

        ClientSSE sse = new ClientSSE("123");
        //sse.encryptFile(f1);

        System.out.println(sse.generateSearchToken("abc").length());
        //client.putFile("./src/test/testFiles/test2.txt", "burp/test2.txt");



        //String t = cs.clientQuery("'--' OR 1=1", "123sdga");
        //System.out.println(cs.clientQuery(msql.bypassAuth(), msql.bypassAuth()));

        //client.getFile("brok/img4.png", "./src/main/resources/clientDirs/brok/img5.png");
        //client.login(username,password);
        //client.putFile("./src/main/resources/clientDirs/brok/img3.png",username+"/img4.png");
        //client.deleteFile(username+"/img4.png");
        //client.getFile("brok/img4.png", "./src/main/resources/clientDirs/brok/img5.png");
        //client.deleteFile(username+"/img4.png");
        //client.getFileNames(username);
        //client.printServerContents();
        //client.createDir(username+"/dummy9.txt");
        //client.deleteFile("brok/img4.png");
        //client.receiveMessage();
        //client.printServerContents();


        //client.putDir(username+"/durr");
    }
}

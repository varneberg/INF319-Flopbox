package server;

import client.Client;
import encryption.ClientSSE;
import encryption.ServerSSE;
import storage.ClientStorage;
import storage.DB;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class server_main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ClientStorage cs = new ClientStorage();
        DB.initDB();
        DB.createClientTable();
        //cs.listAllClients();

        int port = 6666;
        String address = "localhost";
        Server server = new Server(port);
        server.startServer();


        Client client = new Client(address, port);
        String username = "tesiboi";
        String password = "test";




        ClientSSE clientsse = new ClientSSE(username);

        File test = new File("test.txt");

        //System.out.println(test.length());

        FileWriter fw = new FileWriter(test);
        fw.write("I am aware you can store no more than +127 in a java byte, and the documentation says that a byte is only 8 bits but here I am told that it actually takes up the same amount of memory as an int, and therefore is just a Type that helps in code comprehension and not efficiency.");
        fw.close();
        File f = clientsse.encryptFile(test);

        //System.out.println(f.length());

        //Scanner s = new Scanner(f);
        //while(s.hasNext()){
            //System.out.println(s.next());
        //}
        //s.close();


        File dec = clientsse.decryptFile(f);

        //System.out.println(dec.length());

        //Scanner sc = new Scanner(dec);

        //while(sc.hasNext()){
            //System.out.println(sc.nextLine());
        //}

        ServerSSE serversse = new ServerSSE();

        String token1 = clientsse.generateSearchToken("int,");
        //String token2 = clientsse.generateSearchToken("abc");
        //System.out.println(token1);
        System.out.println(serversse.checkMatch(f, token1));


        //client.createUser(username, password);
        //client.printServerContents();
        //client.login(username,password);
        //client.printServerContents();

        //client.printServerContents();
        //client.putFile("./src/main/resources/clientDirs/brok/img3.png",username+"/img4.png");
        //client.deleteFile(username+"/img4.png");
        //client.getFile("brok/img4.png", "./src/main/resources/clientDirs/brok/img5.png");
        //client.deleteFile(username+"/img4.png");
        //client.getFileNames(username);

        //client.printServerContents();
        //client.printServerContents();
        //client.printServerContents();


        //client.createDir(username+"/dummy9.txt");
        //client.deleteFile("brok/img4.png");
        //client.receiveMessage();
        //client.printServerContents();

        //client.putDir(username+"/durr");
    }
}

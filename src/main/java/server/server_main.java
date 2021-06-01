package server;

import client.Client;
import encryption.ClientSSE;
import storage.ClientStorage;
import storage.DB;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
        String[] token = clientsse.generateSearchToken("test");
        System.out.println(Arrays.toString(token));
        File test = new File("test.txt");
        FileWriter fw = new FileWriter(test);
        fw.write("Firstly, the string produced is not properly xor'd in the sense that you cannot get your original string back by xor'ing it with the key again (unless your key was guaranteed to be equal to or longer than the messages which would be very strange) making the code completely misrepresent the concept of xor'ing. Secondly, you are not guaranteed to get valid string bytes by simply xoring characters, so your output string may contain invalid byte sequences.");
        fw.close();
        File f = clientsse.encryptFile(test);


        Scanner s = new Scanner(f);
        while(s.hasNext()){
            System.out.println(s.next());
        }
        s.close();
        File fi = clientsse.getLookup();

        Scanner sc = new Scanner(fi);

        while(sc.hasNext()){
            System.out.println(sc.nextLine());
        }



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

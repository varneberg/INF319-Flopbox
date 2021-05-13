package server;

import client.Client;
import server.Server;
import storage.ClientStorage;
import storage.DB;

import java.io.File;
import java.io.IOException;

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
        //client.createUser(username, password);
        //client.printServerContents();
        client.login(username,password);
        //client.printServerContents();
        //client.putFile("./src/main/resources/clientDirs/brok/img3.png",username+"/img4.png");
        //client.deleteFile(username+"/img4.png");
        //client.getFile("brok/img4.png", "./src/main/resources/clientDirs/brok/img5.png");
        //client.deleteFile(username+"/img4.png");
        client.getFileNames(username);

        client.printServerContents();
        //client.printServerContents();
        //client.printServerContents();


        //client.createDir(username+"/dummy9.txt");
        //client.deleteFile("brok/img4.png");
        //client.receiveMessage();
        //client.printServerContents();

        //client.putDir(username+"/durr");
    }
}

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
        String username = "test123";
        String password = "test";
        client.login(username,password);
        //client.putFile("./src/main/resources/clientDirs/brok/img3.png","brok/img4.png");
        //client.getFile("brok/img4.png", "./src/main/resources/clientDirs/brok/img5.png");
        client.createDir(username+"/flurpi");
        client.receiveMessage();
        client.printServerContents();

        //client.putDir(username+"/durr");
    }
}

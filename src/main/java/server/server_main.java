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
        String localPath = "./src/main/resources/clientDirs/test123/img.png";
        String serverPath = "brok/img.png";
        String toPath = "./src/main/resources/clientDirs/tmp/output3.png";
        String fromPath = "brok/output2.txt";
        //client.getFile(fromPath, toPath);
        //client.putFile(localPath, serverPath);
        client.getFile("brok/img.png", toPath);
        //client.getFile(serverPath, localPath);
        //client.getFile(serverPath,localPath);
    }
}

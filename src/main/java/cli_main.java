import client.Client;
import server.Server;
import storage.ClientStorage;
import storage.DB;

import java.io.IOException;

public class cli_main {
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
        String[] files = client.receiveFileNames(client.getName()+"dir1");
        for(String i : files){
            System.out.println(i);
        }
        //client.receiveFileNames();
        //client.receiveFileNames();
        //client.receiveFileNames();
        //client.getFile("dir2/dummy7.txt");



        server.stopServer();

    }
}

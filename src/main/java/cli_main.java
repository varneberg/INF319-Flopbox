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
        client.login(username, password);
        client.sendMessage("FILES()", "LIST()");
        //System.out.println(client.getUuid());
        //client.sendAuthentication(username, password);
        //client.sendMessage("LOGIN()", "flurp" +"/" +"test");
        server.stopServer();

    }
}

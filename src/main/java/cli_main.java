import client.Client;
import server.Server;
import storage.ClientStorage;
import storage.DB;

public class cli_main {
    public static void main(String[] args) {
        ClientStorage cs = new ClientStorage();
        DB.initDB();
        DB.createClientTable();
        cs.listAllClients();

        int port = 6666;
        String address = "localhost";
        Server server = new Server(port);
        server.startServer();

        Client client = new Client(address, port);
        String username = "test123";
        String password = "test";
        client.sendAuthentication(username, password);

        server.stopServer();

    }
}

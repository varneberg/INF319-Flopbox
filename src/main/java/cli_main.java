import client.Client;
import server.Server;
import storage.DB;

public class cli_main {
    public static void main(String[] args) {
        int port = 6666;
        DB.initDB();
        DB.createClientTable();
        String address = "localhost";
        Server server = new Server(port);

        server.startServer();

        Client client = new Client(address, port);
        client.sendServer("Hello");

        server.stopServer();

    }
}

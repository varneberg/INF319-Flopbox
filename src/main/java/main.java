import java.io.IOException;

public class main {
    public static void main(String[] args) {
        int port = 6666;
        Server server = new Server(port);
        server.startServer();
        Client client = new Client();

        /*Client client2 = new Client();
        client2.name = "Client 2";
        client2.port = port;
        client2.message = "sup";
        */

        client.run();
        server.stopServer();

    }
}

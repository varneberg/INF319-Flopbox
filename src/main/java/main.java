public class main {
    public static void main(String[] args) {
        Server server = new Server();
        Client client = new Client();
        server.run();
        client.run();

    }
}

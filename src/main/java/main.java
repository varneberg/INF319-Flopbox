import server.Server;
import storage.DB;


public class main {
    private static int port = 5555;
    private Server server = null;


    public static void main(String[] args) {
        DB.initDB();
        DB.createClientTable();
        Server server = new Server(port);
        server.startServer();

        Gui gui = new Gui();
        gui.startGui(args, server, port, "localhost");

    }
}
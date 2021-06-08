import server.Server;
import storage.DB;

import java.sql.SQLException;


public class main {
    private static int port = 5555;
    private Server server = null;


    public static void main(String[] args) {
        DB.initDB();
        try {
            DB.createClientTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Server server = new Server(port);
        server.startServer();

        Gui gui = new Gui(args, server, port, "localhost");

    }
}
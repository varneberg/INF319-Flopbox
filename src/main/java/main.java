import client.Client;
import server.Server;
import storage.initDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class main {
    private static int port = 5555;


    public static void main(String[] args) throws SQLException {
        initDB.createDatabase();
        initDB.createUserTable();
        Server server = new Server(port);

        server.startServer();

        clientLoop(server);

        server.stopServer();

    }

    public static void clientLoop(Server server){

        Scanner sc = new Scanner(System.in);
        Client current = null;
        String input = "init";
        while(input != "exit") {
            System.out.println("Command: ");
            input = sc.nextLine();
            if(input.equals("create")){
                System.out.println("Username: ");
                String username = sc.nextLine();
                System.out.println("Password: ");
                String pass = sc.nextLine();

                if (server.clientExists(username)){
                    System.out.println("This user already exists");
                }
                else{

                    Client client = new Client(port, username, pass);
                    server.addClient(client);
                    current = client;
                    System.out.println("You are now logged in as "+ current.getName());
                }

            }

            /*
            while(!current.equals(null)){

                System.out.println("You are logged in as " + current.getName());
            }
            */

        }

    }
}

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class main extends Application{
    private static int port = 5555;

    public static void main(String[] args) {

        launch(args);


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


            while(current != null){

                if(input.equals("login")){
                    System.out.println("Username: ");
                    String username = sc.nextLine();
                    System.out.println("Password: ");
                    String password = sc.nextLine();

                    Client temp = server.login(username, password);
                    if(temp != null){
                        current = temp;
                        System.out.println("You are logged in as " + current.getName());
                    }
                    else{
                        System.out.println("Login failed");
                    }
                }


                while(current != null){
                    System.out.println("Command: ");
                    input = sc.nextLine();


                    if(input.equals("logout")){
                        System.out.println("Logged out of " + current.getName());
                        current = null;
                    }
                }

            }


        }

    }
    @Override
    public void start(Stage primaryStage){

        Server server = new Server(port);
        server.startServer();


        // create a button with specified text
        Button button = new Button("Say 'Hello World'");

        // set a handler that is executed when the user activates the button
        // e.g. by clicking it or pressing enter while it's focused
        button.setOnAction(e -> {
            //Open information dialog that says hello
            Client c = new Client(port, "navn", "pass");
            System.out.println("client: " + c.getName());

        });

        Label label1 = new Label("Name:");
        TextField textField = new TextField ();
        HBox hb = new HBox();
        hb.getChildren().addAll(label1, textField);
        hb.setSpacing(10);

        // the root of the scene shown in the main window
        StackPane root = new StackPane();

        // add button as child of the root
        root.getChildren().add(button);

        // create a scene specifying the root and the size
        Scene scene = new Scene(root, 500, 300);

        // add scene to the stage
        primaryStage.setScene(scene);

        // make the stage visible
        primaryStage.show();

    }

    /*
    @Override
    public void start(Stage primaryStage) throws Exception {


        Server server = new Server(port);
        server.startServer();
        startUI((primaryStage));

        clientLoop(server);

        server.stopServer();

    }

     */
}

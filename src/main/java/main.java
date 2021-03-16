import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.text.*;

public class main extends Application{
    private static int port = 5555;
    private Stage primaryStage = null;
    private Server server = null;

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
        this.primaryStage = primaryStage;

        Server server = new Server(port);
        this.server = server;
        server.startServer();

        login();

    }

    public void login(){

        //Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        //main text
        final Text main_text = new Text(10,50,"Flopbox");
        GridPane.setConstraints(main_text, 1,0);
        grid.getChildren().add(main_text);

        //Defining the Name text field
        final TextField username_field = new TextField();
        username_field.setPromptText("Enter username");
        username_field.setPrefColumnCount(10);
        username_field.getText();
        GridPane.setConstraints(username_field, 0, 1);
        grid.getChildren().add(username_field);

        //Defining the password text field
        final PasswordField password_field = new PasswordField();
        password_field.setPromptText("Enter password");
        GridPane.setConstraints(password_field, 0, 2);
        grid.getChildren().add(password_field);



        //Defining the Submit button
        Button login_button = new Button("Login");
        GridPane.setConstraints(login_button, 1, 1);
        grid.getChildren().add(login_button);

        //Defining the Register button
        Button register_button = new Button("Register");
        GridPane.setConstraints(register_button, 1, 2);
        grid.getChildren().add(register_button);

        //info text
        final Text info_text = new Text();
        GridPane.setConstraints(info_text, 0,3);
        grid.getChildren().add(info_text);


        // set a handler that is executed when the user activates the button
        // e.g. by clicking it or pressing enter while it's focused
        login_button.setOnAction(e -> {
            if(!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {
                 Client current = server.login(username_field.getText(), password_field.getText());
                 if(current != null){
                     logged_in(current);
                 }
                 else{
                     info_text.setText("Wrong login information");
                 }
            }
            else{
                info_text.setText("Empty input field");
            }
        });

        register_button.setOnAction(e -> {
            if(!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {
                Client current = new Client(port, username_field.getText(), password_field.getText());
                if(!server.clientExists(current.getName())){
                    server.addClient(current);
                    logged_in(current);
                }
                else{
                    info_text.setText("User already exists");
                }

            }
            else{
                info_text.setText("Empty input field");
            }
        });

        // create a scene specifying the root and the size
        Scene scene = new Scene(grid, 500, 300);

        // add scene to the stage
        primaryStage.setScene(scene);

        // make the stage visible
        primaryStage.show();

    }

    public void logged_in(Client current){
        //Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        //main text
        final Text main_text = new Text(10,50,"Flopbox");
        GridPane.setConstraints(main_text, 1,0);
        grid.getChildren().add(main_text);

        //Defining the Logout button
        Button logout_button = new Button("dont");
        GridPane.setConstraints(logout_button, 0, 2);
        grid.getChildren().add(logout_button);


        //info text
        final Text info_text = new Text("You are logged in as: " + current.getName());
        GridPane.setConstraints(info_text, 0,1);
        grid.getChildren().add(info_text);

        logout_button.setOnAction(e -> {
            login();
        });


        // create a scene specifying the root and the size
        Scene stage = new Scene(grid, 500, 300);

        // add scene to the stage
        primaryStage.setScene(stage);

        // make the stage visible
        primaryStage.show();

    }

}

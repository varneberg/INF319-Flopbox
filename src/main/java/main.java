import client.Client;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.text.*;
import server.Server;
import storage.DB;
import javafx.util.Callback;


public class main extends Application {
    private static int port = 5555;
    private Stage primaryStage = null;
    private Server server = null;


    public static void main(String[] args) {
        DB.initDB();
        DB.createClientTable();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Server server = new Server(port);
        this.server = server;
        server.startServer();

        login();
    }

    public void login() {

        //Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        //main text
        final Text main_text = new Text(10, 50, "Flopbox");
        GridPane.setConstraints(main_text, 1, 0);
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
        GridPane.setConstraints(info_text, 0, 3);
        grid.getChildren().add(info_text);


        // set a handler that is executed when the user activates the button
        // e.g. by clicking it or pressing enter while it's focused
        login_button.setOnAction(e -> {
            if (!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {
                Client current = new Client(port);
                if (current.authenticateClient(username_field.getText(), password_field.getText())) {
                    logged_in(current);
                } else {
                    info_text.setText("Wrong login information");
                }
            } else {
                info_text.setText("Empty input field");
            }
        });

        register_button.setOnAction(e -> {
            if (!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {

                Client current = new Client(port);
                if (current.registerClient(username_field.getText(), password_field.getText())) {
                    logged_in(current);
                } else {
                    info_text.setText("User already exists");
                }

            } else {
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

    public void logged_in(Client current) {
        //Creating a GridPane container
        StackPane grid = new StackPane();
        GridPane general = new GridPane();
        general.setPadding(new Insets(10, 10, 10, 10));
        general.setVgap(10);
        general.setHgap(10);

        grid.setPadding(new Insets(10, 10, 10, 10));


        //main text
        final Text main_text = new Text(10, 50, "Flopbox");
        GridPane.setConstraints(main_text, 1, 0);
        general.getChildren().add(main_text);

        //Defining the Logout button
        Button logout_button = new Button("dont");
        GridPane.setConstraints(logout_button, 0, 2);
        general.getChildren().add(logout_button);


        //info text
        final Text info_text = new Text("You are logged in as: " + current.getName());
        GridPane.setConstraints(info_text, 0, 1);
        general.getChildren().add(info_text);

        logout_button.setOnAction(e -> {
            login();
        });


        create_list(grid);


        grid.getChildren().addAll(general);

        // create a scene specifying the root and the size
        Scene stage = new Scene(grid, 500, 300);

        // add scene to the stage
        primaryStage.setScene(stage);

        // make the stage visible
        primaryStage.show();

    }

    private static class Cell {
        private String name;
        private int price;
        public String getName() {
            return name;
        }
        public int getPrice() {
            return price;
        }
        public Cell(String name, int price) {
            super();
            this.name = name;
            this.price = price;
        }
    }

    public void create_list(StackPane root) {
        ObservableList<main.Cell> data = FXCollections.observableArrayList();
        data.addAll(new main.Cell("Cheese", 123), new main.Cell("Horse", 456), new main.Cell("Jam", 789));
        final ListView<main.Cell> listView = new ListView<main.Cell>(data);
        listView.setCellFactory(new Callback<ListView<main.Cell>, ListCell<main.Cell>>() {
            @Override
            public ListCell<main.Cell> call(ListView<main.Cell> listView) {
                return new main.CustomListCell();
            }
        });



        root.getChildren().add(listView);
        primaryStage.setScene(new Scene(root, 200, 250));
        primaryStage.show();
    }
    private class CustomListCell extends ListCell<main.Cell> {
        private HBox content;
        private Text name;
        private Text price;
        public CustomListCell() {
            super();
            name = new Text();
            price = new Text();
            VBox vBox = new VBox(name, price);
            content = new HBox(new Label("[Graphic]"), vBox);
            content.setSpacing(10);
        }
        @Override
        protected void updateItem(main.Cell item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) { // <== test for null item and empty parameter
                name.setText(item.getName());
                price.setText(String.format("%d $", item.getPrice()));
                setGraphic(content);
            } else {
                setGraphic(null);
            }
        }
    }

}
/*
class CustomListView extends Application {
    private static class Cell {
        private String name;
        private int price;
        public String getName() {
            return name;
        }
        public int getPrice() {
            return price;
        }
        public Cell(String name, int price) {
            super();
            this.name = name;
            this.price = price;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ObservableList<Cell> data = FXCollections.observableArrayList();
        data.addAll(new Cell("Cheese", 123), new Cell("Horse", 456), new Cell("Jam", 789));

        final ListView<Cell> listView = new ListView<Cell>(data);
        listView.setCellFactory(new Callback<ListView<Cell>, ListCell<Cell>>() {
            @Override
            public ListCell<Cell> call(ListView<Cell> listView) {
                return new CustomListCell();
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(listView);
        primaryStage.setScene(new Scene(root, 200, 250));
        primaryStage.show();
    }

    private class CustomListCell extends ListCell<Cell> {
        private HBox content;
        private Text name;
        private Text price;

        public CustomListCell() {
            super();
            name = new Text();
            price = new Text();
            VBox vBox = new VBox(name, price);
            content = new HBox(new Label("[Graphic]"), vBox);
            content.setSpacing(10);
        }

        @Override
        protected void updateItem(Cell item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) { // <== test for null item and empty parameter
                name.setText(item.getName());
                price.setText(String.format("%d $", item.getPrice()));
                setGraphic(content);
            } else {
                setGraphic(null);
            }
        }
    }

}


 */
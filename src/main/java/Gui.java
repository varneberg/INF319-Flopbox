import client.Client;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import server.Server;
import java.io.File;

public class Gui extends Application{
    private static int port;
    private Stage primaryStage = null;
    private Server server = null;
    private String address;

    public Gui(){
    }

    public void startGui(String[] args, Server server, int port, String address){
        this.port = port;
        this.address = address;
        this.server = server;
        launch(args);
    }

    /*
    public Gui(String[] args, Server server, int port, String address){
        this.port = port;
        this.address = address;
        this.server = server;
        launch(args);
    }


     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        login();
    }

    private void login() {

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

        //info text
        final Text error_text = new Text();
        GridPane.setConstraints(error_text, 0, 4);
        grid.getChildren().add(error_text);


        // set a handler that is executed when the user activates the button
        // e.g. by clicking it or pressing enter while it's focused
        login_button.setOnAction(e -> {
            if (!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {
                Client current = new Client(address, port);
                current.login(username_field.getText(), password_field.getText());
                if (current.getServerMessageStatus().equals("1")) {
                    logged_in(current);
                } else {
                    info_text.setText(current.getServerMessageContents());
                }
            } else {
                info_text.setText("Empty input field");
            }
        });

        register_button.setOnAction(e -> {
            if (!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {

                Client current = new Client("localhost", port);
                if (current.createUser(username_field.getText(), password_field.getText()) == "1") {
                    logged_in(current);
                } else {
                    info_text.setText("User already exists");
                }

            } else {
                info_text.setText("Empty input field");
            }
        });

        // create a scene specifying the root and the size
        Scene scene = new Scene(grid, 750,500);

        // add scene to the stage
        primaryStage.setScene(scene);

        // make the stage visible
        primaryStage.show();

    }

    private void logged_in(Client current) {
        current.receiveFileNames();

        //Creating a GridPane container
        BorderPane grid = new BorderPane();
        GridPane general = new GridPane();
        general.setPadding(new Insets(10, 10, 10, 10));
        general.setVgap(10);
        general.setHgap(10);

        grid.setPadding(new Insets(10, 10, 10, 10));


        //main text
        final Text main_text = new Text(10, 50, "Flopbox");
        GridPane.setConstraints(main_text, 1, 0);
        general.getChildren().add(main_text);



        //info text
        final Text info_text = new Text("You are logged in as: " + current.getName());
        GridPane.setConstraints(info_text, 0, 1);
        general.getChildren().add(info_text);


        //Defining the upload file button
        Button upload_file_button = new Button("Upload File");
        GridPane.setConstraints(upload_file_button, 0, 2);
        general.getChildren().add(upload_file_button);

        //Defining the upload folder button
        Button upload_folder_button = new Button("Upload Folder");
        GridPane.setConstraints(upload_folder_button, 0, 3);
        general.getChildren().add(upload_folder_button);

        //info text
        final Text error_text = new Text();
        GridPane.setConstraints(error_text, 0, 4);
        general.getChildren().add(error_text);

        //Defining the Logout button
        Button logout_button = new Button("Log out");
        GridPane.setConstraints(logout_button, 0, 5);
        general.getChildren().add(logout_button);


        logout_button.setOnAction(e -> {
            login();
        });

        upload_file_button.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    current.sendFile(file);
                } catch (Exception exception) {
                    error_text.setText("Cant upload file");

                }
            }
        });

        String[] s = {"a", "b", "c", "d", "e"};
        Gui.FileList serverFiles = new Gui.FileList(grid, "center", s);

        grid.setLeft(general);



        //grid.getChildren().addAll(general);

        // create a scene specifying the root and the size
        Scene stage = new Scene(grid, 750, 500);

        // add scene to the stage
        primaryStage.setScene(stage);

        // make the stage visible
        primaryStage.show();

    }

    private static class FileList {
        private ListView<Gui.FileList.Cell> listView;

        public FileList(BorderPane root, String orientation, String[] paths) {
            ObservableList<Gui.FileList.Cell> data = FXCollections.observableArrayList();
            addItems(data, paths);
            final ListView<Gui.FileList.Cell> listView = new ListView<Gui.FileList.Cell>(data);
            listView.setCellFactory(new Callback<ListView<Gui.FileList.Cell>, ListCell<Gui.FileList.Cell>>() {
                @Override
                public ListCell<Gui.FileList.Cell> call(ListView<Gui.FileList.Cell> listView) {
                    return new Gui.FileList.CustomListCell();
                }
            });

            this.listView = listView;
            setOrientation(root, orientation);
        }

        private void addItems(ObservableList<Gui.FileList.Cell> data, String[] paths){
            for (String item : paths){
                data.add(new Gui.FileList.Cell(item));
            }
        }

        private void setOrientation(BorderPane root, String orientation){
            if (orientation == "right"){
                root.setRight(listView);
            }
            else if(orientation == "center"){
                root.setCenter(listView);
            }

        }

        public ListView<Gui.FileList.Cell> getListView(){
            return listView;
        }
        private static class Cell {
            private String name;

            public String getName() {
                return name;
            }

            public Cell(String name) {
                super();
                this.name = name;

            }
        }

        private class CustomListCell extends ListCell<Gui.FileList.Cell> {
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
            protected void updateItem(Gui.FileList.Cell item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) { // <== test for null item and empty parameter
                    name.setText(item.getName());
                    setGraphic(content);
                } else {
                    setGraphic(null);
                }
            }
        }
    }

}

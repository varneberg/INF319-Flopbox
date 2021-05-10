import client.Client;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import server.Server;
import javafx.scene.image.Image;

import javax.swing.text.html.ImageView;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

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


    public Gui(String[] args, Server server, int port, String address){
        this.port = port;
        this.address = address;
        this.server = server;
        launch(args);
    }



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

        VBox title = new VBox(10);
        title.setPadding(new Insets(10,10,10,10));
        title.setAlignment(Pos.BASELINE_CENTER);
        //main text
        final Text main_text = new Text(10, 50, "Flopbox");
        title.getChildren().add(main_text);
        grid.getChildren().add(title);

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
        final Text error_text = new Text();
        GridPane.setConstraints(error_text, 0, 3);
        grid.getChildren().add(error_text);


        // set a handler that is executed when the user activates the button
        // e.g. by clicking it or pressing enter while it's focused
        login_button.setOnAction(e -> {
            Client current = new Client(address, port);
            if (!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {

                current.login(username_field.getText(), password_field.getText());
                if (current.getServerMessageStatus().equals("1")) {
                    logged_in(current);
                }
                error_text.setText(current.getServerMessageContents());
            }
        });

        register_button.setOnAction(e -> {

            Client current = new Client("localhost", port);
            if (!username_field.getText().isEmpty() && !password_field.getText().isEmpty()) {
                current.createUser(username_field.getText(), password_field.getText());

                if (current.getServerMessageStatus().equals("1")) {
                    logged_in(current);
                }

            }
            error_text.setText(current.getServerMessageContents());

        });

        // create a scene specifying the root and the size
        Scene scene = new Scene(grid, 750,500);

        // add scene to the stage
        primaryStage.setScene(scene);

        // make the stage visible
        primaryStage.show();

    }

    private void logged_in(Client current) {
        //Creating a GridPane container
        BorderPane grid = new BorderPane();
        GridPane general = new GridPane();
        general.setPadding(new Insets(10, 10, 10, 10));
        general.setVgap(10);
        general.setHgap(10);

        grid.setPadding(new Insets(0, 20, 20, 20));

        VBox title = new VBox(10);
        title.setPadding(new Insets(10,10,10,10));
        title.setAlignment(Pos.BASELINE_CENTER);
        //main text
        final Text main_text = new Text(10, 50, "Flopbox");
        title.getChildren().add(main_text);
        grid.setTop(title);


        //info text
        final Text info_text = new Text("You are logged in as: " + current.getName());
        GridPane.setConstraints(info_text, 0, 1);
        general.getChildren().add(info_text);


        //Defining the upload file button
        Button upload_file_button = new Button("Upload File");
        GridPane.setConstraints(upload_file_button, 0, 2);
        general.getChildren().add(upload_file_button);

        //Defining the upload folder button
        Button download_file_button = new Button("Download File");
        GridPane.setConstraints(download_file_button, 0, 3);
        general.getChildren().add(download_file_button);

        //info text
        final Text error_text = new Text();
        GridPane.setConstraints(error_text, 0, 4);
        general.getChildren().add(error_text);


        //Defining the Logout button
        Button logout_button = new Button("Log out");
        GridPane.setConstraints(logout_button, 0, 5);
        general.getChildren().add(logout_button);


        Gui.FileList serverFiles = new Gui.FileList(grid, current);
        grid.setLeft(general);


        logout_button.setOnAction(e -> {
            login();
        });


        upload_file_button.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);

            try {
                current.putFile(serverFiles.getCurrentDir(),file.getAbsolutePath());
            } catch (Exception exception) {
                error_text.setText(current.getServerMessageContents());
            }
            serverFiles.refresh(current.getFileNames(serverFiles.getCurrentDir()));
        });


        download_file_button.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File dest = fileChooser.showSaveDialog(primaryStage);
            if (dest != null) {
                try {
                    File file = current.getFile(serverFiles.getSelectedFile());
                    Files.copy(file.toPath(), dest.toPath());
                } catch (IOException ex) {
                    // handle exception...
                }
            }
        });


        // create a scene specifying the root and the size
        Scene stage = new Scene(grid, 750, 500);

        // add scene to the stage
        primaryStage.setScene(stage);

        // make the stage visible
        primaryStage.show();

    }

    private static class FileList {
        private ListView<Gui.FileList.Cell> listView;
        private ArrayList<String> paths;
        private String currentDir;
        private String rootDir;
        private BorderPane root;
        private String orientation;
        private String selectedFile = null;
        private Client current;
        private TextField search_field;
        private Button search_button;
        private Text search_text;

        public FileList(BorderPane root, Client current) {
            this.current = current;
            this.paths = new ArrayList<>(Arrays.asList(current.getFileNames(current.getName())));
            this.root = root;
            this.orientation = orientation;
            currentDir = current.getName() + "/";
            rootDir = currentDir;
            this.search_text = new Text("Go directly to directory:");
            this.search_field = new TextField();
            search_field.setPromptText("E.g firstDir/wantedDir/");
            search_field.setFocusTraversable(false);
            this.search_button = new Button("Go to directory");
            fillList();
        }

        public void refresh(String[] paths){
            this.paths = new ArrayList<>(Arrays.asList(paths));
            fillList();
        }

        private void fillList() {
            String[] dir = paths.toArray(new String[0]);
            String[] dirWithBack = new String[dir.length +1];
            ObservableList<Cell> data = FXCollections.observableArrayList();
            String[] sorted;
            if(!currentDir.equals(rootDir)){
                for (int i=0;i<dir.length;i++){
                    dirWithBack[i] = dir[i];
                }
                dirWithBack[dirWithBack.length -1] = "<--";
                sorted = sortDirectory(dirWithBack);

            }
            else {
                sorted = sortDirectory(dir);
            }
            addItems(data, sorted);

            final ListView<Cell> listView = new ListView<Cell>(data);
            listView.setCellFactory(new Callback<ListView<Cell>, ListCell<Cell>>() {
                @Override
                public ListCell<Cell> call(ListView<Cell> listView) {
                    return new CustomListCell();
                }
            });

            this.listView = listView;
            setOrientation(this.root);
        }


        private String[] sortDirectory(String[] directory){
            ArrayList<String> sorted = new ArrayList<>();
            boolean back = false;

            for (String s : directory){
                String type = determineType(s);

                if(type == "directory"){
                    sorted.add(0, s);
                }
                else if (type == "file"){
                    sorted.add(s);
                }
                if (type == "back"){
                    back = true;
                }

            }
            if(back){
                sorted.add(0, "<--");
            }
            return sorted.toArray(new String[0]);
        }

        private void handleClick(String newDirectory){
            String type = determineType(newDirectory);

            if(type == "back"){
                backDirectory();
            }
            else if (type == "directory"){
                nextDirectory(newDirectory);
            }
            else if (type == "file"){
                selectedFile = currentDir + newDirectory;
            }
        }

        private void nextDirectory(String directory) {
            this.currentDir += directory;
            String[] newPaths = current.getFileNames(currentDir);
            refresh(newPaths);
        }

        private String determineType(String element){
            if(element.equals("<--")){
                return "back";
            }

            if (element.charAt(element.length()-1) == '/'){
                return "directory";
            }
            return "file";
        }

        private void backDirectory() {
            String[] temp = currentDir.split("/");
            String newCurrent = "";
            for(int i=0;i<temp.length - 1;i++){
                newCurrent += temp[i] + "/";
            }
            this.currentDir = newCurrent;
            String[] newPaths = current.getFileNames(currentDir);
            refresh(newPaths);
        }

        protected String getSelectedFile(){
            return selectedFile;
        }

        protected String getCurrentDir(){
            return currentDir;
        }

        private void addItems(ObservableList<Gui.FileList.Cell> data, String[] paths){

            for (String item : paths){
                Image image = getImage(item);
                data.add(new Gui.FileList.Cell(item,image));
            }
        }

        private Image getImage(String filename){
            String type = determineType(filename);
            Image image;
            try {
                if (type == "folder") {
                    InputStream stream = new FileInputStream("./src/main/resources/Images/folder.png");
                    return new Image(stream);
                }
            }
            catch (Exception exception){

            }
            return null;
        }

        private void setOrientation(BorderPane root){

            VBox fileExplorer = new VBox(10);
            setupSearchArea(fileExplorer);
            root.setCenter(fileExplorer);

        }

        private void setupSearchArea(VBox fileExplorer){
            HBox search = new HBox(10);
            search.getChildren().addAll(search_text, search_field, search_button);
            fileExplorer.getChildren().addAll(search, listView);
            search_button.setOnAction(e -> {

                try {
                    this.currentDir = rootDir + search_field.getText();
                    String[] newPaths = current.getFileNames(currentDir);
                    refresh(newPaths);
                }
                catch (Exception ex){

                }
            });
        }

        public ListView<Gui.FileList.Cell> getListView(){
            return listView;
        }

        private static class Cell {
            private String name;
            private Image image;

            public String getName() {
                return name;
            }

            public Cell(String name, Image image) {
                super();
                this.image = image;
                this.name = name;
            }
        }

        private class CustomListCell extends ListCell<Gui.FileList.Cell> {
            private HBox content;
            private Text name;
            private Text description;
            private Image image;

            public CustomListCell() {
                super();
                name = new Text();
                description = new Text();
                //ImageView icon = new ImageView(image);
                VBox vBox = new VBox(name, description);
                content = new HBox(new Label("[Icon]"), vBox);

                content.setOnMouseClicked((mouseEvent -> {
                    handleClick(name.getText());
                }));

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

package builder;

import client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileScreen {
    public MenuItem menu_logOut;
    public Text txt_response;
    public TextField field_search;
    public Button btn_search;
    public ListView<String> file_list;
    public ScrollPane scroll_pane;
    public VBox file_vbox;
    public MenuBar file_menu;
    public MenuItem menu_exit;
    public ContextMenu menu_file;
    public MenuItem item_newDir;
    public MenuItem item_download;
    public MenuItem item_delete;
    public MenuItem item_upload;
    public MenuItem item_rename;
    public MenuItem item_back;
    public ImageView img_home;
    public ImageView img_back;
    public ContextMenu popup_file;

    ClientHandler handler = ClientHandler.getInstance();
    Client client = handler.getClient();
    private List<String> files;
    private String currentDir;// = client.getBaseDir();


    public void initialize() {
        //displayFiles(client.getName());

        if (handler.getFirstEntry()) {
            try{
                displayFiles(client.getBaseDir());
                handler.setFirstEntry(false);
            }catch (NullPointerException e){
                txt_response.setText("No files found");
            }
            //file_list.requestFocus();
        }
    }


    public ObservableList<String> getFiles(String directory) {
        files = client.getFileArray(directory);
        if (client.validRequest()) {
            ObservableList<String> fileList = FXCollections.observableArrayList(files);
            setCurrentDir(directory);
            return fileList;
        }
        return null;
    }

    public boolean isError(String message) {
        String messageType = handler.getClient().getServerMessageType();
        if (messageType.equals("ERROR()")) {
            txt_response.setText(handler.getClient().getServerMessageContents());
            return true;
        }
        return false;

    }

    public ObservableList<String> receiveFileNames(String directory){
        ObservableList<String> inputList = null;
        files = handler.getClient().getFileArray(directory);
        if (isError(handler.getClient().getServerMessageType())) {
            txt_response.setText(handler.getClient().getServerMessageContents());
        }
        else if(handler.getClient().validRequest()){
            inputList = FXCollections.observableArrayList(files);
            return inputList;
            }
        return null;
    }

    public void displayFiles(ObservableList<String> files){
        try {
            file_list.setItems(files);
        }catch (NullPointerException e){
            e.printStackTrace();
        }


    }

    public void displayFiles(String directory) {
        if (isError(handler.getClient().getServerMessageType())) {
            txt_response.setText(handler.getClient().getServerMessageContents());
            return;
        }
        files = client.getFileArray(directory);
        if (handler.getClient().validRequest()) {
            ObservableList<String> olist = FXCollections.observableArrayList(files);
            if (!olist.isEmpty()) {
                file_list.setItems(olist);
                setCurrentDir(directory);
            }

            //setFile_list(file_list);
        }
    }


    public void setFile_list(ListView<String> file_list) {
        this.file_list = file_list;
    }


    public String getSelectedItem() {
        return file_list.getSelectionModel().getSelectedItem();
    }


    public void nextDir(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() >= 2) {
            String selected = getSelectedItem();
            if (isDir(selected)) {
                String newDir = getCurrentDir() + selected;
                //System.out.println(newDir);
                displayFiles(newDir);
                if (client.validRequest()) {
                    setCurrentDir(newDir);
                }
            }
        }
    }
    public String getPrevDir(){
        String currentDir = getCurrentDir();
        if(currentDir.equals(handler.getClient().getBaseDir())){
            return handler.getClient().getBaseDir();
        }
        String[] allDirs = currentDir.split("/");
        String lastDir = allDirs[allDirs.length-1];
        return currentDir.substring(0,currentDir.indexOf(lastDir));
    }

    public void prevDir(MouseEvent actionEvent) {
        String currentDir = getCurrentDir();
        if (currentDir.equals(handler.getClient().getBaseDir())) {
            return;
        }
        String[] allDirs = currentDir.split("/");
        String lastDir = allDirs[allDirs.length - 1];
        String prevDir = currentDir.substring(0, currentDir.indexOf(lastDir));
        displayFiles(prevDir);
        ///String lastDir = allDirs[allDirs.length-2];
    }


    public void dirHome(MouseEvent actionEvent) {
        setCurrentDir(client.getBaseDir());
        displayFiles(client.getBaseDir());
    }

    public boolean isDir(String dir) {
        char lastChar = dir.charAt(dir.length() - 1);
        return dir.endsWith("/");
    }

    public void search(ActionEvent actionEvent) {
        ClientHandler handler = ClientHandler.getInstance();
        String search = field_search.getText();
        handler.getClient().search(search);
        /*
        displayFiles(client.getName());
        String newDir = field_search.getText();
        try{
            displayFiles(handler.getClient().getBaseDir()+newDir);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

         */
    }


    public FileChooser fileSelector(String title) {
        FileChooser chooser = new FileChooser();
        //chooser.showOpenDialog();
        chooser.setTitle(title);
        //chooser.showOpenDialog(App.getPrimaryStage());
        return chooser;

    }

    @FXML
    private void gotoFiles() throws IOException {
        App.setRoot("/fxml/login_screen.fxml");
    }

    @FXML
    private void gotoLogin() throws IOException {
        App.setRoot("/fxml/login_screen.fxml");
    }


    public void logOut(ActionEvent actionEvent) throws IOException {
        handler.setClient(null);
        handler.setFirstEntry(true);
        gotoLogin();
    }

    public void quit(ActionEvent actionEvent) throws IOException {
        logOut(actionEvent);
        System.exit(0);
    }

    public void menuNewDirectory(ActionEvent actionEvent) {
        String dir = getSelectedItem();
        //String toDir = getCurrentDir();
        HBox layout = new HBox();
        //GridPane grid = new GridPane();
        Stage popup = new Stage();

        popup.setAlwaysOnTop(true);
        popup.setMaxHeight(100);
        popup.setMinHeight(50);
        popup.setMaxWidth(1000);
        popup.setMinWidth(150);
        popup.setTitle("Name new directory");

        TextField text_dir = new TextField();
        text_dir.setAlignment(Pos.CENTER_LEFT);

        Button btn_newDir = new Button("Save");
        btn_newDir.setAlignment(Pos.CENTER_RIGHT);

        Scene scene = new Scene(layout);
        layout.getChildren().addAll(text_dir, btn_newDir);
        popup.setScene(scene);
        popup.show();


        btn_newDir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handler.getClient().createDir(getCurrentDir() + text_dir.getText());
                popup.close();
            }

            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    handler.getClient().createDir(getCurrentDir() + text_dir.getText());
                    popup.close();
                }

            }
        });
        displayFiles(getCurrentDir());
    }

    public void printServerResponse() {
        txt_response.setText(handler.getClient().getServerMessageContents());
    }

    public void menuDownloadFile(ActionEvent actionEvent) {
        String toGet = getSelectedItem();
        try {
            if (!isDir(toGet)) {
                FileChooser chooser = fileSelector("Save file");
                chooser.setInitialFileName(toGet);
                File location = chooser.showSaveDialog(App.getPrimaryStage());
                String saveLoc = location.toString();
                System.out.println(saveLoc);
                handler.getClient().getFile(getCurrentDir() + toGet, saveLoc);
                System.out.println(getCurrentDir());
                printServerResponse();


            } else if (isDir(toGet)) {
                txt_response.setText("Please choose a file");
            }
        } catch (Exception a) {
            a.printStackTrace();
        }
    }

    public void menuUploadFile(ActionEvent actionEvent) {
        try {
            String uploadDir = getSelectedItem();
            FileChooser chooser = fileSelector("Upload file");
            File location = chooser.showOpenDialog(App.getPrimaryStage());
            //File location = chooser.showOpenMultipleDialog(App.getPrimaryStage());
            String fileName = location.toString();
            handler.getClient().putFile(fileName, getCurrentDir() + location.getName());
            printServerResponse();
            displayFiles(getCurrentDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void menuDeleteFile(ActionEvent actionEvent) {
        String toDelete = getSelectedItem();
        client.deleteFile(toDelete);
        txt_response.setText(client.getServerMessageContents());
        displayFiles(getCurrentDir());
    }


    public void menuRenameFile(ActionEvent actionEvent) {
        String toRename = getSelectedItem();
        System.out.println(toRename);
        String toDir = getCurrentDir();
        HBox layout = new HBox();
        GridPane grid = new GridPane();
        Stage popup = new Stage();

        popup.setAlwaysOnTop(true);
        popup.setMaxHeight(100);
        popup.setMinHeight(50);
        popup.setMaxWidth(1000);
        popup.setMinWidth(150);
        popup.setTitle("New file name");

        TextField txt_name = new TextField();
        txt_name.setAlignment(Pos.CENTER_LEFT);

        Button btn_newName = new Button("Rename");
        btn_newName.setAlignment(Pos.CENTER_RIGHT);

        Scene scene = new Scene(layout);
        layout.getChildren().addAll(txt_name, btn_newName);
        popup.setScene(scene);
        popup.show();


        btn_newName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handler.getClient().renameFile(txt_name.getText(), toRename);
                popup.close();
            }

            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    handler.getClient().renameFile(txt_name.getText(), toRename);
                    popup.close();
                }

            }
        });
        displayFiles(getCurrentDir());
        printServerResponse();
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public void ExpandCurrentDir(String newDir) {
        this.currentDir += newDir;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public void RetractCurrentDir() {
        StringBuilder sb = new StringBuilder();
        String[] dir = getCurrentDir().split("/");
    }

    public void menuPrevDir(ActionEvent actionEvent) {
        String currentDir = getCurrentDir();
        String[] allDirs = currentDir.split("/");
        String lastDir = allDirs[allDirs.length - 1];
        String prevDir = currentDir.substring(0, currentDir.indexOf(lastDir));
        displayFiles(prevDir);
    }


    public void gotoDir(ActionEvent actionEvent) {
        if (field_search.getText().equals("")) {
            displayFiles(client.getBaseDir());
            return;
        }
        ObservableList<String> inp = getFiles(field_search.getText());
        //displayFiles(field_search.getText().strip());
        if(isError(handler.getClient().getServerMessageType())){
            printServerResponse();
            //displayFiles(getCurrentDir());
            return;
        }

        String searchDir = getCurrentDir()+field_search.getText().strip()+"/";
        ObservableList<String> files = receiveFileNames(searchDir);
        displayFiles(files);
    }

}
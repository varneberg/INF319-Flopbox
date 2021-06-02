package builder;

import client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileScreen extends loginScreen{
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
    Client client = handler.getClient() ;
    private List<String> files;
    private String currentDir;// = client.getBaseDir();


    public void initialize(){
        //displayFiles(client.getName());
        if (handler.getFirstEntry()){
            displayFiles(client.getBaseDir());
            handler.setFirstEntry(false);
        }
    }

    public ObservableList<String> getFiles(String directory){
        files = client.getFileArray(directory);
        if(client.validRequest()){
            ObservableList<String> fileList = FXCollections.observableArrayList(files);
            setCurrentDir(directory);
            return fileList;
        }
        return null;
    }

    public void displayFiles(String directory){
        files = client.getFileArray(directory);
        if(client.validRequest()) {
            ObservableList<String> olist = FXCollections.observableArrayList(files);
            file_list.setItems(olist);
            //setFile_list(file_list);
            setCurrentDir(directory);
        }
    }


    @Override
    public List<String> getFileList() {
        return super.getFileList();
    }

    public void setFile_list(ListView<String> file_list) {
        this.file_list = file_list;
    }

    public String getSelectedItem(){
        return file_list.getSelectionModel().getSelectedItem();
    }

    public void nextDir(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() >= 2){
            String selected = getSelectedItem();
            if(isDir(selected)){
                String newDir = getCurrentDir()+selected;
                displayFiles(newDir);
                if(client.validRequest()){
                    setCurrentDir(newDir);
                }
            }
        }
    }

    public void prevDir(MouseEvent actionEvent) {
        String currentDir = getCurrentDir();
        String[] allDirs = currentDir.split("/");
        String lastDir = allDirs[allDirs.length-1];
        String prevDir = currentDir.substring(0,currentDir.indexOf(lastDir));
        displayFiles(prevDir);
        ///String lastDir = allDirs[allDirs.length-2];
    }

    public void dirHome(MouseEvent actionEvent) {
        displayFiles(client.getBaseDir());
    }

    public boolean isDir(String dir){
        char lastChar = dir.charAt(dir.length()-1);
        return dir.endsWith("/");
    }

    public void search(ActionEvent actionEvent) {
        ClientHandler handler = ClientHandler.getInstance();
        displayFiles(client.getName());
    }


    public FileChooser fileSelector(String title){
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

    public void newDirectory(ActionEvent actionEvent) {
        String dir = getSelectedItem();
        String toDir = getCurrentDir();
        HBox layout = new HBox();
        Stage popup = new Stage();
        TextField dirName = new TextField();
        dirName.setAlignment(Pos.BASELINE_LEFT);
        Button saveDir = new Button("Save");
        saveDir.setAlignment(Pos.BASELINE_RIGHT);
        Scene scene = new Scene(layout,250,100);
        layout.getChildren().addAll(dirName, saveDir);
        popup.setScene(scene);
        popup.showAndWait();


    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String toGet = getSelectedItem();
        if(!isDir(toGet)) {
            FileChooser chooser = fileSelector("Save file");
            File location = chooser.showSaveDialog(App.getPrimaryStage());
            String saveLoc = location.toString();
            handler.getClient().getFile(getCurrentDir() + toGet, saveLoc);
            txt_response.setText(client.getServerMessageContents());
        }else if(isDir(toGet)) {
            txt_response.setText("Not able to download directories");
        }
    }

    public void uploadFile(ActionEvent actionEvent) {
        String uploadDir = getSelectedItem();
        FileChooser chooser = fileSelector("Upload file");
        System.out.println(getCurrentDir());
        System.out.println(uploadDir);
    }

    public void deleteFile(ActionEvent actionEvent) {
        String toDelete = getSelectedItem();
        client.deleteFile(toDelete);
        txt_response.setText(client.getServerMessageContents());
    }


    public void renameFile(ActionEvent actionEvent) {
        String toRename = getSelectedItem();
        System.out.println(toRename);
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

    public void RetractCurrentDir(){
        StringBuilder sb = new StringBuilder();
        String[] dir =  getCurrentDir().split("/");
    }

}

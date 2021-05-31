package builder;

import client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;

public class FileScreen extends loginScreen{
    public MenuItem menu_logOut;
    public Menu menu_quit;
    public Text txt_response;
    public TextField field_search;
    public Button btn_search;
    public ScrollPane file_pane;
    public ListView<String> file_list;
    public ScrollPane scroll_pane;
    public VBox file_vbox;
    public MenuBar file_menu;

    ClientHandler handler = ClientHandler.getInstance();
    Client client = handler.getClient() ;
    private List<String> files;


    public void initialize(){
        displayFiles(client.getName());
    }

    public void displayFiles(String directory){
        files = client.getFileArray(directory);
        ObservableList<String> olist = FXCollections.observableArrayList(files);
        file_list.setItems(olist);
        //file_list.setItems(olist);
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

    public void search(ActionEvent actionEvent) {
        ClientHandler handler = ClientHandler.getInstance();
        displayFiles(client.getName());
    }

    @FXML
    private void gotoFiles() throws IOException {
        App.setRoot("/fxml/login_screen.fxml");
    }

    @FXML
    private void gotoLogin() throws IOException {
        App.setRoot("/fxml/login_screen.fxml");
    }

    public void test(MouseEvent inputMethodEvent) {
        displayFiles(client.getName());
    }
}

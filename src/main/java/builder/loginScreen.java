package builder;
import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class loginScreen {
    public Text txt_register;
    private Stage stage;
    // Log in screen
    public Text txt_response;
    public TextField field_search;
    public Button btn_login;
    public TextField field_user;
    public TextField field_passwd;
    public Button btn_register;

    // File screen
    public Button btn_search;
    public ScrollPane file_pane;
    public MenuItem menu_logOut;
    public Menu menu_quit;
    public FileScreen fs;

    String address = "localhost";
    int port = 6666;
    Client client = new Client(address, port);
    ClientHandler handler = ClientHandler.getInstance();
    private String username;
    private String password;
    private String UUID;
    private List<String> fileList;


    //private static builderGUI instance;
    /*
    public static builderGUI getInstance(){
        return instance;
    }
     */

    public void login(ActionEvent event) {
        try {
            if (field_user.getText().trim().isEmpty()) {
                //txt_response.setText("Please enter a username");
                printRed("Please enter a password");
            }
            if (field_passwd.getText().trim().isEmpty()) {
                txt_response.setText("Please enter a password");
                printRed("Please enter a password");
            } else {
                this.username = field_user.getText();
                this.password = field_passwd.getText();
                client.login(username, password);
                if(client.isAuthenticated()){
                    //setUUID();
                    //handler.setClient(client);
                    //App.setRoot("/fxml/file_screen.fxml");
                    gotoFiles();
                } else {
                    printRed(client.getServerMessageContents());
                    //printServerResponse();
                    //txt_response.setText(client.getServerMessageContents());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //login_response.setText(client.getServerMessageContents());
    }

    @FXML
    private void gotoFiles() {
        try {
            ClientHandler handler = ClientHandler.getInstance();
            handler.setClient(client);
            App.setRoot("/fxml/file_screen.fxml");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void printRed(String input){
        txt_response.setText(input);
        txt_response.setFill(Color.RED);
        //txt_response.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
    }

    public void printGreen(String input){
        txt_response.setText(input);
        txt_response.setFill(Color.GREEN);
        txt_response.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
    }



    @FXML
    private void gotoLogin() throws IOException {
        App.setRoot("/fxml/login_screen.fxml");
    }

    @FXML
    private void gotoScreen(String fxml){
        App.setRoot(fxml);
    }

    public void register(ActionEvent event){
        username = field_user.getText();
        password = field_passwd.getText();
        client.createUser(username, password);
        //client.printServerContents();
        txt_response.setText(client.getServerMessageContents());
    }


    public void register_click(MouseEvent event){
        String username = getField_user();
        String password = getField_passwd();
        client.createUser(username, password);
        if(client.validRequest()){
            printGreen(client.getServerMessageContents());

        } else {
            printRed(client.getServerMessageContents());

        }
        //txt_response.setText(client.getServerMessageContents());

    }


    public String getField_user() {
        if(field_user.getText().isEmpty()){
            txt_response.setText("Please provide a username");
        }
        return field_user.getText();
    }


    public String getField_passwd(){
        if(field_user.getText().isEmpty()){
            txt_response.setText("Please provide a password");
        }
        return field_passwd.getText();
    }


    public void viewFiles(String dir){
        client.getFileNames(dir);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUUID() {
        return client.getUuid();
    }

    public Client getClient() {
        return client;

    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public List<String> getFileList() {
        return fileList;
    }

    public void returnIsPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            //System.out.println("enter");
            btn_login.fire();
        }
    }
}


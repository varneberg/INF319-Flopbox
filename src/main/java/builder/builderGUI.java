package builder;
import client.Client;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import server.Server;

import java.io.IOException;
import java.util.Objects;

public class builderGUI extends Application {
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

    String address = "localhost";
    int port = 6666;
    Client client = new Client(address, port);
    private String username;
    private String password;
    private String UUID;



    private static builderGUI instance;

    public builderGUI(){
        instance = this;
    }
    public static builderGUI getInstance(){
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        try{
            stage=primaryStage;
            //gotoLogin();
            setScene("/fxml/login_screen.fxml");
            //gotoFiles();
            primaryStage.show();
            //btn_login.isDefaultButton();

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Server server = new Server(6666);
        server.startServer();
        launch(args);
    }

    public void gotoLogin(){
        try{
            replaceScene("/fxml/login_screen.fxml");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void gotoFiles(){
        try{
            replaceScene("/fxml/file_screen.fxml");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Parent setScene(String fxml) throws Exception{
        Parent page = (Parent) FXMLLoader.load(Objects.requireNonNull(builderGUI.class.getResource(fxml)), null, new JavaFXBuilderFactory());
        //Scene scene = stage.getScene();
        Scene scene = new Scene(page, 700, 450);
        stage.setScene(scene);
        stage.sizeToScene();
        return page;
    }


    public Parent replaceScene(String fxml) throws Exception{
        Parent page = (Parent) FXMLLoader.load(Objects.requireNonNull(builderGUI.class.getResource(fxml)), null, new JavaFXBuilderFactory());
        Scene scene = stage.getScene();
        if(scene == null){
            scene = new Scene(page);
            //scene.getStylesheets()
            stage.setScene(scene);
        } else{
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
        return page;
    }

    public void login(ActionEvent event) {
        try {
            if (field_user.getText().trim().isEmpty()) {
                txt_response.setText("Please enter a username");
            }
            if (field_passwd.getText().trim().isEmpty()) {
                txt_response.setText("Please enter a password");
            } else {
                this.username = field_user.getText();
                this.password = field_passwd.getText();
                client.login(username, password);
                if (validRequest(client.getServerMessageStatus())) {
                    setUsername(username);
                    if(client.getServerMessageType().equals("LOGIN()")) {
                        setUUID(client.getServerMessageContents());
                        client.getFileNames(username);
                        client.printServerContents();
                        switchToFileScreen(event);
                    }

                } else {
                    txt_response.setText(client.getServerMessageContents());
                }

            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //login_response.setText(client.getServerMessageContents());
    }

    public void switchToFileScreen(ActionEvent event)throws IOException{
        Parent blah = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/file_screen.fxml")));
        Scene scene = new Scene(blah);
        stage = (Stage)((Node) event.getSource()).getScene().getWindow();

        stage.setScene(scene);
        stage.show();
    }

    public void register(ActionEvent event){
        username = field_user.getText();
        password = field_passwd.getText();
        client.createUser(username, password);
        client.printServerContents();
    }

    public boolean validRequest(String requestStatus){
        if ("1".equals(requestStatus)) {
            return true; }
        else {
            return false; }
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

    public void returnIsPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            //System.out.println("enter");
            btn_login.fire();
        }
    }
}

package builder;
import client.Client;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import server.Server;

import java.io.IOException;
import java.util.Objects;

public class builderGUI extends Application {
    private Stage stage;

    public Button btn_login;
    public TextField field_user;
    public TextField field_passwd;
    public Button btn_register;
    public Text login_response;
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
        //builderGUI gui = new builderGUI();
        //Server server = new Server(6666);
        //server.startServer();

        /*
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/login_screen.fxml"));
        primaryStage.setTitle("Flopbox");
        Scene scene = new Scene(loader.load());
        Window window = scene.getWindow();
        primaryStage.setScene(scene);
        primaryStage.show();

         */
        try{
            stage=primaryStage;
            gotoLogin();
            //gotoFiles();
            primaryStage.show();

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


    private Parent replaceScene(String fxml) throws Exception{
        Parent page = (Parent) FXMLLoader.load(Objects.requireNonNull(builderGUI.class.getResource(fxml)), null, new JavaFXBuilderFactory());
        Scene scene = stage.getScene();
        if(scene == null){
            scene = new Scene(page, 700, 450);
            //scene.getStylesheets()
            stage.setScene(scene);
        } else{
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
        return page;
    }

    public void login(ActionEvent event) throws IOException {

        this.username = field_user.getText();
        this.password = field_passwd.getText();

        client.login(username, password);
        login_response.setText(client.getServerMessageContents());
        if (validRequest(client.getServerMessageStatus())) {
            //gotoFiles();
            /*
            Parent blah = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/file_screen.fxml")));
            Scene scene = new Scene(blah);
            stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

             */
            fileScreen(event);

        }
        //login_response.setText(client.getServerMessageContents());
    }

    public void fileScreen(ActionEvent event)throws IOException{
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
            return true;
        }
        return false;
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

}

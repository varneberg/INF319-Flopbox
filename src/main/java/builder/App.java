package builder;
import client.Client;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import server.Server;
import storage.DB;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {
    private static Scene scene;
    private static Stage primaryStage;
    Client client;


    @Override
    public void start(Stage primaryStage) {
        try {
            scene = new Scene(loadFXML("/fxml/login_screen.fxml"));
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    static void setRoot(String fxml) {
        try {
            scene.setRoot(loadFXML(fxml));
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    @FXML
    private static Parent loadFXML(String fxml) throws IOException{
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        return loader.load();
    }

    public static void main(String[] args) throws SQLException {
        boolean secure = false;
        SecureState.getINSTANCE().setSecure(secure);
        ClientHandler handler = new ClientHandler();
        //DB.initDB();
        //DB.createClientTable();
        //DB.createSecureClientTable();
        //Server server = new Server(6666);
        //server.startServer();
        launch(args);
    }

}


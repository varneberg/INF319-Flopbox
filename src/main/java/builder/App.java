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

import java.io.IOException;

public class App extends Application {
    private static Scene scene;
    private static Stage primaryStage;
    Client client;


    @Override
    public void start(Stage primaryStage) throws Exception{
        scene = new Scene(loadFXML("/fxml/login_screen.fxml"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    static void setRoot(String fxml) throws IOException{
        scene.setRoot(loadFXML(fxml));
    }


    private static Parent loadFXML(String fxml) throws IOException{
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        return loader.load();
    }

    public static void main(String[] args) {
        SecureState.getINSTANCE().setSecure(false);
        ClientHandler handler = new ClientHandler();
        Server server = new Server(6666);
        server.startServer();
        launch(args);
    }

}


import builder.ClientHandler;
import builder.SecureState;
import client.Client;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.Server;
import storage.DB;
import java.io.IOException;
import java.sql.SQLException;

public class main extends Application {
    private static Scene scene;
    private static Stage primaryStage;
    Client client;


    /*
    starts the gui
     */
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

    /*
    loads the gui from the fmx file created with scene builder
     */
    @FXML
    private static Parent loadFXML(String fxml) throws IOException{
        FXMLLoader loader = new FXMLLoader(builder.App.class.getResource(fxml));
        return loader.load();
    }

    /*
    creates the database, starts the server and launches the gui
     */
    public static void main(String[] args) throws SQLException {
        boolean secure = true;
        SecureState.getINSTANCE().setSecure(secure);
        ClientHandler handler = new ClientHandler();
        DB.initDB();
        DB.createClientTable();
        DB.createSecureClientTable();
        Server server = new Server(6666);
        server.startServer();
        launch(args);
    }

}


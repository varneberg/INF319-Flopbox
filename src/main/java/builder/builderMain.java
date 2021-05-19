package builder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import server.Server;

public class builderMain extends Application {
    private Stage primaryStage = null;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //builderGUI gui = new builderGUI();
        //Server server = new Server(6666);
        //server.startServer();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/login_screen.fxml"));
        primaryStage.setTitle("Flopbox");
        Scene scene = new Scene(loader.load());
        Window window = scene.getWindow();
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        Server server = new Server(6666);
        server.startServer();
        launch(args);

    }
}

package builder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import server.Server;

public class builderMain extends Application {
    private Stage primaryStage = null;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Server server = new Server(6666);
        server.startServer();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/inf319-sb.fxml"));
        primaryStage.setTitle("Flopbox");
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);

    }
}

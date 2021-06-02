package builder;

import java.io.IOException;

public final class AppController {

    public AppController(){}
    public void changeScreen(String fxml) throws IOException {
        App.setRoot(fxml);

    }
}

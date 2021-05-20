package builder;

import client.Client;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Objects;

public class fileScreen extends builderGUI {
    private static Client client;
    public Text txt_response;
    public TextField field_search;
    public Button btn_search;
    public ScrollPane file_pane;
    public MenuItem menu_logOut;
    public Menu menu_quit;


    public void run(){
        client.getFileNames(getUsername());
        client.printServerContents();
    }

    public static void main(String[] args) {
        String[] files = client.getFileNames(getInstance().getUsername() + "/");

        for(String s :files){

            System.out.println(s);
        }




    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;

    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public String getUUID() {
        return super.getUUID();
    }

    public void returnPressed(KeyEvent keyEvent){
        if(keyEvent.getCode() == KeyCode.ENTER){
            btn_search.fire();
        }
    }

    @Override
    public Parent setScene(String fxml) throws Exception {
        return super.setScene(fxml);
    }
}

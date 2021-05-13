package builder;
import client.Client;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class loginScreen {
    public Label login_client;
    public Button btn_login;
    public Button btn_register;
    public TextField field_passwd;
    public TextField field_user;
    String address = "localhost";
    int port = 6666;
    Client client = new Client(address, port);
    private String username;
    private String password;
    private String UUID;



    public void login(ActionEvent event){
        username = field_user.getText();
        password = field_passwd.getText();
        client.login(username, password);
        //client.printServerContents();
    }

    public void register(ActionEvent event){
        String username = field_user.getText();
        String password = field_passwd.getText();
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

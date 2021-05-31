package builder;

import client.Client;

import java.io.IOException;

public final class ClientHandler {
    private Client client;
    private final static ClientHandler INSTANCE = new ClientHandler();

    public ClientHandler(){}

    public static ClientHandler getInstance(){
        return INSTANCE;
    }

    public void setClient(Client c){
        this.client = c;
    }
    public Client getClient(){
        return this.client;
    }

    public void changeScreen(String fxml) throws IOException {
        App.setRoot(fxml);
    }

}

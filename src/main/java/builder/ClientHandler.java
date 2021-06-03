package builder;

import client.Client;

import java.io.IOException;

public final class ClientHandler {
    private boolean firstEntry=true;
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

    public void setFirstEntry(boolean firstEntry) {
        this.firstEntry = firstEntry;
    }
    public boolean getFirstEntry(){
        return firstEntry;
    }

}

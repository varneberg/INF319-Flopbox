package builder;

import client.Client;

public class fileScreen {
    public static fileScreen instance;
    private Client client;

    public fileScreen(Client client){
        instance = this;
        this.client = client;

    }
    public static fileScreen getInstance(){return instance;}

    public void start(){}
}

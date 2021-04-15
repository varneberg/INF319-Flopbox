package server;

import storage.ClientStorage;

public class FileHandler{
    ClientStorage cs = new ClientStorage();


    public String listClientFiles(String username){
        String path = "./src/main/resources/clientDirs/"+username+"/";
        String[] files = cs.listClientFiles(path);
        String output = "";
        for (int i = 0; i < files.length; i++) {
            output += files[i] + ":";
        }
        return output;

    }
}

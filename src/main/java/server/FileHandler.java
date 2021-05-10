package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandler {
    private String sep = ",";

    private static String storagePath = "./src/main/resources/clientDirs/";

    public String listFiles(String filePath) throws IOException {
        String[] paths;
        String dir = "./src/main/resources/clientDirs/" + filePath;
        File f = new File(dir);
        StringBuilder fileString = new StringBuilder();
        try {
            for (File fi : f.listFiles()){
                if (fi.isDirectory()) {
                    fileString.append(fi.getName()).append("/").append(sep);
                } else if (fi.isFile()) {
                    fileString.append(fi.getName()).append(sep);
                }
            }
        } catch (NullPointerException e) {
           fileString.append("No file(s) were found");
        }
        return fileString.toString();
    }

    public void storeFile(File clientFile){

    }

    public File getFile(String clientName, String filePath){
        String clientPath = getClientPath(clientName);
        String pathToFile = clientPath+filePath;
        //System.out.println(pathToFile);
        return new File(pathToFile);

    }

    public String getClientPath(String clientName){
        return storagePath+clientName+"/";
    }
    public String getStoragePath(){return storagePath;}
}

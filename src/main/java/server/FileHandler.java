package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
                if (fi.getName().equals(".lookup")){
                    continue;
                }
                if (fi.isDirectory()) {
                    fileString.append(fi.getName()).append("/").append(sep);
                } else if (fi.isFile()) {
                    fileString.append(fi.getName()).append(sep);
                }
            }
            if(fileString.length() == 0){
                fileString.append("Empty Directory");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileString.toString();
    }

    public List<File> listAllFiles(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<File>();

        // get all the files from a directory
        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
                System.out.println(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                resultList.addAll(listAllFiles(file.getAbsolutePath()));
            }
        }
        //System.out.println(fList);
        return resultList;
    }

    public void storeFile(File clientFile){

    }

    public void createDir(String dirName){
        File newDir = new File(storagePath+dirName);
        if(!newDir.exists()){
            newDir.mkdir();
        }
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

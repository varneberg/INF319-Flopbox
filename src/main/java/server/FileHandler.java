package server;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileHandler {
    private String sep = ",";

    private static String storagePath = "./src/main/resources/clientDirs/";

    public String listFiles(String filePath) throws IOException, NullPointerException {
        String dir = "./src/main/resources/clientDirs/" + filePath;
        File f = new File(dir);
        StringBuilder fileString;
        List<String> dirList = new ArrayList<>();
        List<String> fileList = new ArrayList<>();
        fileString = new StringBuilder();

            for (File fi : f.listFiles()) {
                if (f.listFiles().length == 0) {
                    return "Empty Directory";
                }
                if (fi.getName().equals(".lookup")) {
                    continue;
                }
                if (fi.isDirectory()) {
                    fileString.append(fi.getName()).append("/").append(sep);
                    dirList.add(fi.getName());
                } else if (fi.isFile()) {
                    fileString.append(fi.getName()).append(sep);
                    fileList.add(fi.getName());
                }
            }
            if (fileString.length() == 0) {
                fileString.append("Empty Directory");
            }
            return combineLists(dirList, fileList);
    }

    public String combineLists(List<String> dirList, List<String> fileList){
        StringBuilder sb = new StringBuilder();
        Collections.sort(dirList);
        for (String s : dirList) {
            String toAdd = s +"/"+sep;
            sb.append(toAdd);
        }
        Collections.sort(fileList);
        for (String s : fileList){
            String toAdd = s + sep;
            sb.append(toAdd);
        }
        return sb.toString();

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

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
        /*
        Path path = Paths.get(dir);
        List<Path> result;
        try (Stream<Path> walk= Files.walk(path, 1)) {
            result = walk
                    //.filter(Files::isDirectory)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        //String[] fileArr = result.toString().split(",");
        StringBuilder fileString = new StringBuilder();

        System.out.println(result);
        for (String s :result.toString().split(",")){
            fileString.append(sep);//s.substring(s.indexOf(clientname + "/") + clientname.length() + 1)).append(sep);
        }
        fileString = new StringBuilder(fileString.toString().replace("]", ""));
        return fileString.toString();

         */
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
}

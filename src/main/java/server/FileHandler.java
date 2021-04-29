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
    private String sep = "\t";

    private static String storagePath = "./src/main/resources/clientDirs/";

    public String listFiles(String clientname) throws IOException {
        String dir = "./src/main/resources/clientDirs/" + clientname + "/";
        Path path = Paths.get(dir);
        List<Path> result;
        try (Stream<Path> walk= Files.walk(path, 15)) {
            result = walk
                    //.filter(Files::isDirectory)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        //String[] fileArr = result.toString().split(",");
        StringBuilder fileString = new StringBuilder();

        for (String s :result.toString().split(",")){
            fileString.append(s.substring(s.indexOf(clientname + "/") + clientname.length() + 1)).append(sep);
        }
        fileString = new StringBuilder(fileString.toString().replace("]", ""));
        return fileString.toString();
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

package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandler {

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
        String fileArr = result.toString();
        return fileArr;
        //return fileArr.replace("[","").replace("]","").replace("," , ":").replace(" ","");

        //return arrToString(fileArr);
    }

    public String arrToString(String[] arr){
        String fileString = "";

        for (String i: arr) {
            fileString += i + ":";
        }
        fileString.strip();
        return fileString;
    }
    
}

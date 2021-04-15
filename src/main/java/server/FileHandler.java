package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandler {

    public String[] listFiles(String clientname) throws IOException {
        String dir = "./src/main/resources/clientDirs/" + clientname + "/";
        Path path = Paths.get(dir);
        List<Path> result;
        try (Stream<Path> walk= Files.walk(path, 15)) {
            result = walk
                    //.filter(Files::isDirectory)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }

        String[] fileString = result.toString().split(",");
        return fileString;
    }
}

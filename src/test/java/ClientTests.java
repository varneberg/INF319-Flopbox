import builder.SecureState;
import client.Client;
import encryption.SHA256;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import server.Server;
import storage.DB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@TestMethodOrder(OrderAnnotation.class)
public class ClientTests {
    int port = 5000;
    String serverAddress = "localhost";
    Client testClient;
    private static String testDir = "./src/test/testFiles/";
    private static String testfile = "./src/test/testFiles/testfile.txt";
    private String username = "testuser";
    private String password = "password";
    Server server = new Server(port);
    private static boolean setupDone = false;


    @Before
    public void setUp() throws SQLException {
        if(setupDone){
            return;
        }
        SecureState.getINSTANCE().setSecure(false);
        DB.initDB();
        DB.createClientTable();
        server.startServer();
        setupDone = true;
        //testClient = new Client(serverAddress, port);
    }

    public void startServer(){
        server.startServer();
    }

    @Test
    @Order(1)
    public void registerTest() throws SQLException {
        //startServer();
        if(DB.clientExists(username)) {
            DB.deleteClient(username);
        }
        testClient = new Client(serverAddress, port);
        testClient.createUser(username, password);
        assertEquals("1",testClient.getServerMessageStatus());
        //stopServer();

    }


    @Test
    @Order(2)
    public void loginTest(){
        //DB.initDB();
        //DB.createClientTable();
        //server.startServer();
        //startServer();
        testClient = new Client(serverAddress,port);
        testClient.login(username, password);
        assertEquals("1",testClient.getServerMessageStatus());
        //stopServer();
        //server.stopServer();
    }

    @Test
    @Order(3)
    public void getFileNamesTest(){
        testClient = new Client(serverAddress, port);
        testClient.login(username, password);
        List<String> filenames = testClient.getFileArray(testClient.getName());
        assertEquals("FILES()", testClient.getServerMessageType());
        assertNotNull(filenames);

    }


    public void writeFileContent(File f) throws IOException {
        FileWriter writer = new FileWriter(f);
        for (int i = 0; i < 20; i++) {
            writer.write("abc\n");
        }
        writer.close();
    }

    public void createTestFile() throws IOException {
        File clientFile = new File(testfile);
        if(!clientFile.exists()){
            clientFile.delete();
        }else {
            clientFile.createNewFile();
        }
        writeFileContent(clientFile);
    }

    public String getFileContents(String file) throws IOException {
        //File file = new File(testFile);
        Path fileName = Path.of(file);
        return Files.readString(fileName);
    }

    @Test
    @Order(4)
    public void uploadFileTest() throws IOException {
        createTestFile();
        testClient = new Client(serverAddress, port);
        testClient.login(username,password);
        File tf = new File(testfile);
        testClient.putFile(testfile, testClient.getBaseDir()+"testfile.txt");
        assertTrue(testClient.validRequest());

    }

    @Test
    @Order(5)
    public void downloadFileTest() throws IOException {
        createTestFile();
        String putContents = getFileContents(testfile);
        testClient = new Client(serverAddress, port);
        String dwnFile = testDir+"/testfile2.txt";
        testClient.login(username, password);
        testClient.putFile(testfile, testClient.getBaseDir()+"testfile.txt");
        testClient.getFile(testClient.getBaseDir()+"testfile.txt",dwnFile);
        String getContents = getFileContents(dwnFile);
        //assertTrue(testClient.validRequest());
        assertEquals(getContents, putContents);
    }

    @Test
    public void logoutTest(){
        testClient = new Client(serverAddress, port);
        testClient.login(username, password);
        testClient.logout();
        assertNull(testClient.getUuid());
    }



    public void deleteClientTest(){
        try {
            assertTrue(DB.clientExists(username));
            DB.deleteClient(username);
            assertFalse(DB.clientExists(username));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @AfterEach
    public void logOut(){
        if(testClient.isAuthenticated()) {
            testClient.logout();
        }
    }

    @AfterAll
    public void stopServer(){
        server.stopServer();
    }



}

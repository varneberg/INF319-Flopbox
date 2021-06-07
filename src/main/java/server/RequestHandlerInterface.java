package server;

import message.clientMessage;
import org.sqlite.SQLiteException;

import java.io.IOException;
import java.net.SocketException;

public interface RequestHandlerInterface extends Runnable {
    void setRunning(boolean running);

    boolean isRunning();

    @Override
    void run();

    void messageHandler() throws IOException;

    clientMessage receiveMessage();

    void sendMessage(String requestType, String requestStatus, String contents);

    void sendError(String errorMsg);

    void sendError(String errorType, String errorMsg);

    String genUUID();

    String[] splitInput(String input);

    void registerClient(String input) throws SQLiteException;

    // Closes current connection to client
    void closeConnection() throws IOException, SocketException;

    void listClientFiles(String msgContents);

    void loginClient(String input);

    void updateCredentials(String input);

    boolean validateClient();

    //private void receiveFile(String fileName, String fileSize) throws Exception {
    void receiveFile(String contents);

    void sendFile(String filePath);

    void deleteFile(String path);

    void createDir(String dirPath);

    void renameFile(String filePath);

    void setClientName(String clientName);

    String getClientName();

    void setCurrClientUUID(String currClientUUID);

    String getCurrClientUUID();

    void setMsgNum(int msgNum);

    int getMsgNum();
}

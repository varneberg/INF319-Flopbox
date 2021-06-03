package server;

import builder.SecureState;
import message.clientMessage;
import message.serverMessage;
import storage.ClientStorage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Server extends Thread {

    private ServerSocket ss;
    final int port;
    private boolean running = false;
    SecureState secure = SecureState.getINSTANCE();


    public Server(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            ss = new ServerSocket(port);
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("[Server]: Listening for a connection...");
        while (running) {
            try {
                //Call accept() to receive the next connection
                Socket socket = ss.accept();
                // Pass the socket to the request handler thread for processing
                if(secure.isSecure()){
                    SecureRequestHandler secureRequestHandler = new SecureRequestHandler(socket);
                    secureRequestHandler.start();
                }else {
                    RequestHandler requestHandler = new RequestHandler(socket);
                    requestHandler.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}



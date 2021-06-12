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

    /*
    initializes a server instance on a given port
     */
    public Server(int port) {
        this.port = port;
    }

    /*
    starts the server
     */
    public void startServer() {
        try {
            ss = new ServerSocket(port);
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    stops the server and sets its status to not running
     */
    public void stopServer() {
        running = false;
        this.interrupt();
    }

    /*
    starts the server, waits for clients to connect
     */
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



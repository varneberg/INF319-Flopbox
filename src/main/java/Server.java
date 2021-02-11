import java.io.*;
import java.net.*;
public class Server extends Thread{

    private ServerSocket ss;
    private int port;
    private boolean running = false;

    public Server(int port){
        this.port = port;
    }

    public void startServer(){
        try {
            ss = new ServerSocket(port);
            this.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopServer(){
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        while(running){
            try{
                System.out.println("Listening for a connection");
                //Call accept() to receive the next connection
                Socket socket = ss.accept();
                // Pass the socket to the request handler thread for processing
                RequestHandler requestHandler = new RequestHandler(socket);
                requestHandler.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /*
    public static void main( String[] args ) {
        if( args.length == 0 ) {
            System.out.println( "Usage: SimpleSocketServer <port>" );
            System.exit( 0 );
        }

        int port = Integer.parseInt( args[ 0 ] );
        System.out.println( "Start server on port: " + port );

        Server server = new Server(port);
        server.startServer();

        // Automatically shutdown in 1 minute
        try {
            Thread.sleep( 60000 );
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        server.stopServer();
    }*/
}


class RequestHandler extends Thread{
    private Socket socket;
    RequestHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){ // What the server does when a client connects
        try{
            System.out.println("Received a connection");
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hello client!");
            out.flush();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            System.out.println(in.readUTF());
            socket.close();

            System.out.println( "Connection closed" );
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        }
    }

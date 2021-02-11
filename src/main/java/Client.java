import java.io.*;
import java.net.*;
public class Client implements Runnable{
    String name;
    Thread t;
    String message;
    int port;

    public Client(){
        t = new Thread(this, "client");
        System.out.println("New client on thread: " + t);
        t.start();
    }

    @Override
    public void run(){
        try{
            Socket s = new Socket("localhost",port);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            dout.writeUTF("test");
            dout.flush();
            dout.close();
            s.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }
}
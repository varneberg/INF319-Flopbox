import java.io.*;
import java.net.*;
public class Client implements Runnable{
    String name;
    Thread t;
    public Client(){
        t = new Thread(this, "client");
        System.out.println("New thread: " + t);
        t.start();
    }

    @Override
    public void run(){
        try{
            Socket s = new Socket("localhost",6666);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            dout.writeUTF("Hello Server");
            dout.flush();
            dout.close();
            s.close();
        }catch(Exception e){System.out.println(e);}
    }

}  
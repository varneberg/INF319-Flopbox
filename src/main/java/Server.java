import java.io.*;
import java.net.*;
public class Server implements Runnable {

    Thread t;
    public Server(){
        t = new Thread(this, "sever");
        System.out.println("New thread: " + t);
        t.start();
    }

    @Override
    public void run(){
        try{
            ServerSocket ss=new ServerSocket(6666);
            Socket s=ss.accept();//establishes connection
            DataInputStream dis=new DataInputStream(s.getInputStream());
            String str=(String)dis.readUTF();
            System.out.println("message= "+str);
            ss.close();
        }catch(Exception e){System.out.println(e);}

    }
}  
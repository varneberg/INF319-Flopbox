import java.io.*;
import java.net.*;
public class Client implements Runnable{
    String name;
    Thread t;
    String message;
    int port;
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;


    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run(){
        try{
            Socket s = new Socket("localhost",port);
            dataInputStream = new DataInputStream(s.getInputStream());
            dataOutputStream = new DataOutputStream(s.getOutputStream());

            sendFile("src/main/resources/clientStorage/send1.txt");
            sendFile("src/main/resources/clientStorage/send2.txt");

            dataInputStream.close();
            dataInputStream.close();
            s.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void sendFile(String filename) throws Exception{
        int bytes = 0;
        File file = new File(filename);
        String path = file.getAbsolutePath();

        FileInputStream fileInputStream = new FileInputStream(path);

        // send file size
        dataOutputStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0,bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();

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



/*


// testkode
import java.io.*;
        import java.net.Socket;

public class Client {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args) {
        try(Socket socket = new Socket("localhost",5000)) {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            sendFile("../../../storage/clientStorage/send1.txt");
            sendFile("../../../storage/clientStorage/send2.txt");

            dataInputStream.close();
            dataInputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void sendFile(String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file size
        dataOutputStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0,bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
}*/

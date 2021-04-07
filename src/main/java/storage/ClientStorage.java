package storage;

import client.Client;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class ClientStorage {
    private Connection connect() {
       String url = "jdbc:sqlite:./flopbox.db";
       Connection con = null;
       try {
           con = DriverManager.getConnection(url);
       } catch (SQLException e){
           System.out.println(e.getMessage());
       }
       return con;
    }

    public void listAllClients(){
       String sql = "SELECT * FROM clients";
       try (Connection con = this.connect();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
           while(rs.next()){
               System.out.println(rs.getInt("id") + "\t" +
                       rs.getString("uname") + "\t" +
                       rs.getString("password") + "\t" +
                       rs.getString("directory"));
           }
       } catch (SQLException e){
           System.out.println(e.getMessage());
       }
    }

    // Checks if a given client exists in the database
    public boolean clientExists(String uname)  throws SQLException{
       String sql =  "SELECT *"
               + "FROM clients "
               + "WHERE uname = '" + uname
               + "'";
       try (Connection con = this.connect();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
           return(rs.next());

       }
    }

    public boolean verifyPassword(String passwd) throws SQLException {
        String sql = "SELECT *"
                + "FROM clients "
                + "WHERE password = '" + passwd
                + "'";
        try (Connection con = this.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return (rs.next());
        }
    }

    // Return client entry from database
    public void getClient(String uname) throws SQLException {
        //String clientName = client.getName();
        String sql = "SELECT *"
                    + "FROM clients "
                    + "WHERE uname = '" + uname
                    + "'";

        try (Connection con = this.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
            while(rs.next()){
                System.out.println(rs.getInt("id") + "\t"
                        + rs.getString("uname") + "\t"
                        + rs.getString("password") + "\t"
                        + rs.getString("directory"));
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


    public void addClient(String uname, String password) throws SQLException {
        String sql = "INSERT INTO clients(uname, password, directory) VALUES(?,?,?)";
        String dir = "/" + uname + "/";
        try(Connection con = this.connect();
            PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, uname);
            pstmt.setString(2, password);
            pstmt.setString(3, dir);
            pstmt.executeUpdate();
            createClientDir(uname);
        } catch (SQLException e){
            //System.out.println(e.getMessage());
            System.out.println("Client " + uname + " already in database");
        }
    }


    // Broken
    public void updateClientName(String old_name,String new_name) throws SQLException {
        String sql = "UPDATE clients"
                + "SET uname = ? "
                + "WHERE uname = '" + old_name
                + "'";
        try(Connection con = this.connect();
            PreparedStatement pstmt = con.prepareStatement(sql)){

            pstmt.setString(1, new_name);
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    static String storagePath = "./src/main/resources/clientDirs/";
    public void createClientDir(String clientName) {
        File f = new File("./src/main/resources/clientDirs/" + clientName + '/');
        if (!f.exists()) {
            System.out.println("Directory does not exists, creating new");
            f.mkdir();
        }
    }

    // List all files client has in directory
    public String[] listClientFiles(String clientName) {
        String[] fileNames;
        File f = new File("./src/main/resources/clientDirs/" + clientName + '/');
        fileNames = f.list();
        return fileNames;
    }

    // Add client file
    public void clientAddFile(String clientName, String file){
        String dir = "./src/main/resources/clientDirs/" + clientName + "/";
        File f = new File(dir);
    }

    // Adds 10 dummy files
    public void clientAddDummyFiles(String clientName, int numFiles) {
        String dir = "./src/main/resources/clientDirs/" + clientName + "/";
        for(int i = 0; i <= numFiles; i++){
            String fileName = "dummy"+i+".txt";
            try {
                File f = new File(dir + fileName);
                if(!f.exists()){
                    f.createNewFile();
                }
            } catch (IOException e){
                System.out.println(e.getStackTrace());
            }

        }
    }

    public void deleteAllClients(){
        String sql = "DELETE FROM clients";
        try (Connection con = this.connect();
             Statement stmt = con.createStatement()) {
             stmt.execute(sql);
             System.out.println("All clients deleted");
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void deleteClient(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection con = this.connect();
             PreparedStatement  pstmt = con.prepareStatement(sql)){
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}

package storage;

import client.Client;

import java.sql.*;

public class clientStorage {
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

    public void getClient(String clientName) throws SQLException {
        //String clientName = client.getName();
        String sql = "SELECT *"
                    + "FROM clients "
                    + "WHERE uname = '" + clientName
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

    public void addClient(String clientName, String passwd) throws SQLException {
        String sql = "INSERT INTO clients(uname, password, directory) VALUES(?,?,?)";
        String dir = "/" + clientName + "/";
        try(Connection con = this.connect();
            PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, clientName);
            pstmt.setString(2, passwd);
            pstmt.setString(3, dir);
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void addClient(Client client) throws SQLException {
        String sql = "";
    }

    public void removeClient(Client client) throws SQLException { }

    public void updateClient(Client client) throws SQLException { }

}

package storage;

import java.io.File;
import java.sql.*;

import client.Client;

public class initDB {

    static File dbFile = new File("./flopbox.db");
    static String url = "jdbc:sqlite:./" + dbFile.toString();

    public static void createDatabase(){
        //File dbFile = new File("./flopbox.db");
        //String url = "jdbc:sqlite:./" + dbFile.toString();
        if(!dbFile.exists()) {
            //String url = "jdbc:sqlite:./" + dbFile.toString();

            try (Connection con = DriverManager.getConnection(url)) {
                if (con == null) {
                    DatabaseMetaData meta = con.getMetaData();
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }else{
            System.out.println("Database exists");
        }
    }

    public static void createUserTable(){
        String sql = "CREATE TABLE IF NOT EXISTS Client(username VARCHAR(6), password VARCHAR(100), directory VARCHAR(200)";
        try (Connection con = DriverManager.getConnection(url); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)){}

    } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}

package storage;

import java.io.File;
import java.sql.*;


public class initDB {


    //static File dbFile = new File("./flopbox.db");
    static String url = "jdbc:sqlite:./flopbox.db";

    public static void createNewDB(){
        try (Connection con = DriverManager.getConnection(url)) {
            if (con != null) {
                DatabaseMetaData meta = con.getMetaData();
                System.out.println(meta.getDriverName());
                System.out.println("Database created");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createClientTable(){
        String sql = "CREATE TABLE IF NOT EXISTS clients (\n"
                + "     id integer PRIMARY KEY AUTOINCREMENT, \n"
                + "     uname varchar(6) UNIQUE NOT NULL,\n"
                + "     password varchar(256),\n"
                + "     directory varchar(100)\n"
                + ");";

        try (Connection con = DriverManager.getConnection(url);
            Statement stmt = con.createStatement()) {
            stmt.execute(sql);
            System.out.println("clients table created");

        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }

}
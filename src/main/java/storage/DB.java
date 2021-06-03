package storage;
import builder.SecureState;
import org.sqlite.SQLiteException;

import java.io.File;
import java.sql.*;

public class DB {

    //static File dbFile = new File("./flopbox.db");
    static String url = "jdbc:sqlite:./flopbox.db";
    boolean secure = SecureState.getINSTANCE().isSecure();
    static String storagePath = "./src/main/resources/clientDirs/";

    public static void initDB(){
        try (Connection con = DriverManager.getConnection(url)) {
            if (con != null) {
                DatabaseMetaData meta = con.getMetaData();
                System.out.println("Database created " +"(" + meta.getDriverName() + ")");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Connection connect(){
        String url = "jdbc:sqlite:./flopbox.db";
        Connection con = null;
        try{
            con = DriverManager.getConnection(url);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return con;
    }

    public static void createClientTable(){
        String output = null;
        String sql = "CREATE TABLE IF NOT EXISTS clients (\n"
                + "     id integer PRIMARY KEY AUTOINCREMENT, \n"
                + "     uname varchar(30) UNIQUE NOT NULL,\n"
                + "     password varchar(256),\n"
                + "     directory varchar(100)\n"
                + ");";

        try (Connection con = DriverManager.getConnection(url);
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
            System.out.println("[Server]: Clients table created");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createSecureClientTable(){
        String sql =
                "CREATE TABLE IF NOT EXISTS secureClients (\n"
                + "     id integer PRIMARY KEY AUTOINCREMENT, \n"
                + "     uname varchar(30) UNIQUE NOT NULL,\n"
                + "     password varchar(256),\n"
                + "     directory varchar(100)\n"
                + ");";
        try(Connection con = DriverManager.getConnection(url);
            Statement stmt = con.createStatement()){
            stmt.execute(sql);
            System.out.println("[Server]: SecureClients table created");

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static String SecureListClients() {
        Connection con = connect();
        //String query = "select * from "+secureClientTable;
        String query = "select * from " + getSecureClientTable();
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                //StringBuilder sb = new StringBuilder();
                int id = rs.getInt("id");
                String varchar = rs.getString("uname");
                String password = rs.getString("password");
                String directory = rs.getString("directory");
                sb
                        .append(id).append('\t')
                        .append(varchar).append('\t')
                        .append(password).append('\t')
                        .append(directory).append('\n');
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void secureAddClient(String uname, String password) throws SQLException, SQLiteException{
        String sql = "INSERT INTO secureClients(uname, password, directory) VALUES(?,?,?)";
        String dir = "/" + uname + "/";
        Connection con = connect();
        try(PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, uname);
            pstmt.setString(2, password);
            pstmt.setString(3, dir);
            pstmt.executeUpdate();
            secureCreateClientDir(uname);
            System.out.println("[Server]: Created user " + uname);
        }
    }

    private static void secureCreateClientDir(String uname) {
        File f = new File(getStoragePath(uname));
        if(!f.exists()){
            f.mkdir();
        }
    }
    public static void secureDeleteAllClients(){
        String query = "delete from " + getSecureClientTable();
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.executeUpdate();
            System.out.println("[Server]: Deleted all clients");
        }catch(SQLException e){
            e.printStackTrace();
        }

    }

    public static boolean secureLogin(String uname, String password){
        String query =
                "select * "
                + "from " + getSecureClientTable() + " where uname = ?"
                + "and password = ?";
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
           ps.setString(1,uname);
           ps.setString(2,password);
           ResultSet rs = ps.executeQuery();
           return rs.next();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static String getStoragePath(String uname) {
        return storagePath + uname + "/";
    }

    public static String getClientTable() {
        return "clients";
    }

    public static String getSecureClientTable() {
        return "secureClients";
    }
}
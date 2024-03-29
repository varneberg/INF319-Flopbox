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
        //try (Connection con = DriverManager.getConnection(url)) {
        try (Connection con = connect()) {
            if (con != null) {
                DatabaseMetaData meta = con.getMetaData();
                System.out.println("Database created " +"(" + meta.getDriverName() + ")");
                con.close();
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

    // Adds SQL default credentials. Only for demonstrative purposes
    public static void addDefaultCredentials() throws SQLException{
            if(clientExists("admin")) {
                deleteClient("admin");
            }
        addClient("admin", "admin");
    }

    // Creates the client table for the SQL database
    public static void createClientTable() throws SQLException {
        String output = null;
        String sql = "CREATE TABLE IF NOT EXISTS clients (\n"
                + "     id integer PRIMARY KEY AUTOINCREMENT, \n"
                + "     uname varchar(30) UNIQUE NOT NULL,\n"
                + "     password varchar(256),\n"
                + "     directory varchar(100)\n"
                + ");";

        //try (Connection con = DriverManager.getConnection(url);
        Connection con = connect();
        Statement stmt = con.createStatement();
        stmt.execute(sql);
        con.close();
        System.out.println("[Server]: Clients table created");
        addDefaultCredentials();
    }

    // Creates a secure client table for SQL database
    public static void createSecureClientTable() throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS secureClients (\n"
                + "     id integer PRIMARY KEY AUTOINCREMENT, \n"
                + "     uname varchar(30) UNIQUE NOT NULL,\n"
                + "     password varchar(256),\n"
                + "     directory varchar(100)\n"
                + ");";
        //try(Connection con = DriverManager.getConnection(url);
        Connection con = connect();
        Statement stmt = con.createStatement();
        stmt.execute(sql);
        System.out.println("[Server]: SecureClients table created");
        con.close();


    }

    // List clients from secure client table
    // Output: A string with client entries from the secure database
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

    public static boolean clientExists(String uname) throws SQLException{
        String query = "SELECT *"
                + "FROM clients "
                + "WHERE uname = '" + uname
                +"'";
        Connection con = connect();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        con.close();
        return rs.next();
    }

    public static void addClient(String username, String passwd) throws SQLException{
        String dir = "/"+username+"/";
        String query = "INSERT INTO clients(uname,password,directory)"
                + "VALUES (?,?,?)";
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1,username);
            ps.setString(2,passwd);
            ps.setString(3,dir);
            ps.executeUpdate();
            createClientDir(username);
            con.close();

    }

    public static void secureAddClient(String uname, String password) throws SQLException, SQLiteException{
        String sql = "INSERT INTO secureClients(uname, password, directory) VALUES(?,?,?)";
        String dir = "/" + uname + "/";
        Connection con = connect();
        PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, uname);
            pstmt.setString(2, password);
            pstmt.setString(3, dir);
            pstmt.executeUpdate();
            createClientDir(uname);
            System.out.println("[Server]: Created user " + uname);
            con.close();
    }

    public static void createClientDir(String uname) {
        File f = new File(getStoragePath(uname));
        if(!f.exists()){
            f.mkdir();
        }
    }

    public static boolean clientDirExists(String dir){
        File f = new File(getStoragePath(dir));
        return f.exists();
    }

    public static void renameDirectory(String clientName, String newName){
        String oldDir = getStoragePath(clientName);
        String newDir = storagePath+newName;
        File od = new File(oldDir);
        File nd = new File(newDir);
        if(od.renameTo(nd)){
            System.out.println("Renamed directory");
        }else {
            System.out.println("Error occured");
        }
    }

    public static void secureDeleteAllClients(){
        String query = "delete from " + getSecureClientTable();
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.executeUpdate();
            deleteAllClientDirectories();
            System.out.println("[Server]: Deleted all clients");
            con.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void deleteClient(String username) throws SQLException {
        String query = "DELETE FROM clients WHERE uname = ?";
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,username);
            ps.executeUpdate();
        }
        File userDir = new File(getStoragePath(username));
        deleteClientDirectory(userDir);
        con.close();
    }

    public static void secureDeleteClient(String clientName) throws SQLException {
        String query = "DELETE FROM secureClients WHERE uname = ?";
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,clientName);
            ps.executeUpdate();
            System.out.println("[Server]: Deleted user "+clientName);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        con.close();
        File userDir = new File(getStoragePath(clientName));
        deleteClientDirectory(userDir);
        deleteDir(userDir.getName());
    }

    public static void deleteClientDirectory(File userDir){
        //String userPath = getStoragePath(uname);
        //File userDir = new File(userPath);
        for(File f: userDir.listFiles()){
            if(f.isDirectory()){
                deleteClientDirectory(f);
            }else{
                f.delete();
            }
        }
        userDir.delete();
    }

    public static void deleteAllClientDirectories(){
        File sp = new File(storagePath);
        for(File f : sp.listFiles()){
            deleteClientDirectory(f);
        }
        System.out.println("Deleted all client directories");
        
    }

    public static void deleteDir(String dir){
        File f = new File(storagePath + dir);
        if(f.isDirectory()){
            f.delete();
        }

    }

    public static boolean secureLogin(String uname, String password){
        String query =
                "select * "
                + "from secureClients  where uname = ?"
                + "and password = ?";
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
           ps.setString(1,uname);
           ps.setString(2,password);
           ResultSet rs = ps.executeQuery();
           con.close();
           return rs.next();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static boolean updateUsername(String newName, String clientName){
        String query = "UPDATE clients set uname = '"+ newName +"'"
                +" WHERE uname = '"+clientName+ "'";
        Connection con = connect();
        try(Statement stmt=con.createStatement()){
            stmt.executeUpdate(query);
            renameDirectory(clientName, newName);
            return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(String contents, String clientName){
        return true;
    }

    public static void secureUpdateUsername(String newName, String clientName) throws SQLException {
        String query = "UPDATE secureClients set uname = ? WHERE uname = ?";
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, newName);
        ps.setString(2, clientName);
        ps.executeUpdate();
        renameDirectory(clientName, newName);
    }

    public static void secureUpdatePassword(String newPassword, String clientName) throws SQLException {
        System.out.println(newPassword);
        String query = "UPDATE secureClients set uname = ? WHERE uname = ?";
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, newPassword);
        ps.setString(2, clientName);
        ps.executeUpdate();

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

    public static String clientQuery(String username, String password) throws SQLException {
        String out = "";
        String sql =
                "SELECT * FROM clients " +
                        "WHERE uname = '"+ username + "'" +
                        " AND password = '" + password + "'";
        //"SELECT *" + "FROM clients " + "WHERE uname = '" + username + "'";
        Connection con = connect();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            out += rs.getInt("id") + "\t"
                    + rs.getString("uname") + "\t"
                    + rs.getString("password") + "\t"
                    + rs.getString("directory");
            }
        con.close();
        return out;
    }

    public static boolean verifyPassword(String username, String password) throws SQLException {
        String sql = "SELECT *"
                + "FROM clients "
                + "WHERE uname = '" + username + "'"
                +"AND password = '" + password + "'";
        Connection con = connect();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        con.close();
        return rs.next();


    }
}
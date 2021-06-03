package malicious;

public class maliciousSQL {
    public String bypassAuth(){
        String query = "'OR 1=1;";
        return query;
    }
}

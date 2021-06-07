package message;

public class clientMessage {
    private String localaddress=null;
    private String uuid = null;
    private String requestType=null;
    private String messageContents=null;
    private String sep = ";;";

    public clientMessage(String localaddress, String uuid, String requestType, String messageContent){
        this.localaddress = localaddress;
        this.uuid = uuid;
        this.requestType = requestType;
        this.messageContents = messageContent;
    }

    public clientMessage(){}

    public String createMessage(){
        String message = getLocaladdress() + sep
                + getUuid() + sep
                + getRequestType() + sep
                + getMessageContents();
        return message;
    }

    public void receiveMessage(String input){
        String[] msg = input.split(sep);
        setLocaladdress(msg[0]);
        setUuid(msg[1]);
        setRequestType(msg[2]);
        setMessageContents(msg[3]);

    }

    public void setLocaladdress(String localaddress) {
        this.localaddress = localaddress;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setMessageContents(String messageContents) {
        this.messageContents = messageContents;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getLocaladdress() {
        return localaddress;
    }

    public String getMessageContents() {
        return messageContents;
    }

    public String getRequestType() {
        return requestType;
    }


}

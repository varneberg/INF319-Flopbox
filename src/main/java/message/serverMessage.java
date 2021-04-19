package message;

public class serverMessage {
    private String serverAddress = null;
    private String requestType = null;
    private String requestStatus = null;
    private String messageContents = null;
    private char[] messageContentsChar = null;



    public serverMessage(String serverAddress, String requestType, String requestStatus, String messageContents){
        this.serverAddress = serverAddress;
        this.requestType = requestType;
        this.requestStatus = requestStatus;
        this.messageContents = messageContents;
    }

    public serverMessage(String serverAddress, String requestType, String requestStatus, char[] messageContentsChar){
        this.serverAddress = serverAddress;
        this.requestType = requestType;
        this.requestStatus = requestStatus;
        this.messageContentsChar = messageContentsChar;
    }

    public serverMessage(){}

    public String createMessage(){
        String message = getServerAddress() + ":"
                + getRequestType() + ":"
                + getRequestStatus() + ":"
                + getMessageContents();
        return message;
    }


    public void receiveMessage(String input){
        String[] msg = input.split(":");
        setServerAddress(msg[0]);
        setRequestType(msg[1]);
        setRequestStatus(msg[2]);
        setMessageContents(msg[3]);
    }





    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setMessageContents(String messageContents) {
        this.messageContents = messageContents;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }


    public String getRequestType() {
        return requestType;
    }

    public String getMessageContents() {
        return messageContents;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public char[] getMessageContentsChar() {
        return messageContentsChar;
    }
}

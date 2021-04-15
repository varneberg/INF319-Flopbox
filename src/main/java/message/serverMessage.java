package message;

public class serverMessage {
    private String serverAddress = null;
    private String requestType = null;
    private String requestStatus = null;
    private String messageContents = null;



    public serverMessage(String serverAddress, String requestType, String requestStatus, String messageContents){
        this.serverAddress = serverAddress;
        this.requestType = requestStatus;
        this.requestStatus = requestStatus;
        this.messageContents = messageContents;
    }

    public serverMessage(){}

    public String createMessage(){
        String message = getServerAddress() + ":"
                + getRequestType() + ":"
                + getRequestStatus() + ":"
                + getMessageContents();
        return message;
    }

    public void receiveMessage(String readLine) { }

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
}

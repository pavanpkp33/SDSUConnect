package cs646.edu.sdsu.cs.connectemallTab.model;

import java.util.Date;

/**
 * Created by Pkp on 04/11/17.
 */

public class ChatMessage {

    private String sender;
    private String message;
    private Date timeStamp;

    public ChatMessage(String senderName, String messageBody, Date time){
        this.sender = senderName;
        this.message = messageBody;
        this.timeStamp = time;
    }

    public ChatMessage(){

    }
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}

package cs646.edu.sdsu.cs.connectemallTab.model;

/**
 * Created by Pkp on 04/10/17.
 */

public class ChatList {

    public ChatList(){

    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    private String chatId;

    public String getChatId() {
        return chatId;
    }

   public ChatList(String chatId){
       this.chatId = chatId;
   }
}

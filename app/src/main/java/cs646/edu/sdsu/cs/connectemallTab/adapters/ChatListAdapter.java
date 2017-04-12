package cs646.edu.sdsu.cs.connectemallTab.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatList;

/**
 * Created by Pkp on 04/10/17.
 */

public class ChatListAdapter extends BaseAdapter {
    FirebaseAuth authRef = FirebaseAuth.getInstance();
    Context c;
    ArrayList<ChatList> chatList;

    public ChatListAdapter(Context ctx, ArrayList<ChatList> list){
        this.c = ctx;
        this.chatList = list;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String userNameDisplay  = "";
        ChatList chatId = chatList.get(position);
        String userName = authRef.getCurrentUser().getDisplayName();
        String chat = chatId.getChatId();
        String [] users = chat.split("_");
        if(users[1].equalsIgnoreCase(userName)){
            userNameDisplay += users[2];
        }else{
            userNameDisplay += users[1];
        }
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_list, parent, false);

        }
        TextView chatHeader = (TextView) convertView.findViewById(R.id.tvChatUserName);
        chatHeader.setText(userNameDisplay);

        return convertView;
    }
}

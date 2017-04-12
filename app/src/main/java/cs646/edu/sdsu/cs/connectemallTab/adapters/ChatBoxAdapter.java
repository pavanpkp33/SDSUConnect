package cs646.edu.sdsu.cs.connectemallTab.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatMessage;

/**
 * Created by Pkp on 04/11/17.
 */

public class ChatBoxAdapter extends BaseAdapter {
    String loggedInUser;
    private ArrayList<ChatMessage> messageList;
    Context ctx;

    public ChatBoxAdapter(Context c, ArrayList<ChatMessage> list, String user){
       this.loggedInUser = user;
        this.ctx = c;
        this.messageList = list;
    }
    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tvMessageIn, tvMessageOut;
        ChatMessage message = messageList.get(position);

        LayoutInflater inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int resource;

       if(message.getSender().equalsIgnoreCase(loggedInUser)){
           resource = R.layout.message_out;
       }else{
           resource = R.layout.message_in;
       }

        View view =  inflater.inflate(resource, parent, false);

        switch(resource){
            case R.layout.message_out:
                tvMessageOut = (TextView) view.findViewById(R.id.tvMessageOut);
                tvMessageOut.setText(message.getMessage());
                break;

            case R.layout.message_in:
                tvMessageIn = (TextView) view.findViewById(R.id.tvMessageIn);
                tvMessageIn.setText(message.getMessage());
                break;
        }

        return view;
    }
}

package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.adapters.ChatBoxAdapter;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatList;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatBoxFragment extends Fragment implements View.OnClickListener{
    FirebaseAuth authInstance;
    FloatingActionButton btnSend;
    FirebaseDatabase dbRef;
    Button btnBack;
    TextView tvChatUserName;
    EditText etChatBox;
    ListView lvChatBox;
    ArrayList<ChatMessage> messageList ;
    ChatBoxAdapter chatAdapter;
    DatabaseReference chatRef;
    String chatId, loggedInUser;


    public ChatBoxFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        authInstance = FirebaseAuth.getInstance();
        View v =  inflater.inflate(R.layout.fragment_chat_box, container, false);

        dbRef = FirebaseDatabase.getInstance();
        chatRef = dbRef.getReference("chats");
        loggedInUser = authInstance.getCurrentUser().getDisplayName();
        messageList = new ArrayList<>();
        chatAdapter = new ChatBoxAdapter(getContext(), messageList, loggedInUser);

        btnSend = (FloatingActionButton) v.findViewById(R.id.btnSend);
        lvChatBox = (ListView) v.findViewById(R.id.lvChatBox);
        tvChatUserName = (TextView) v.findViewById(R.id.tvChatboxTitle) ;
        etChatBox = (EditText) v.findViewById(R.id.etChatBox);
        btnBack = (Button) v.findViewById(R.id.btnChatBack);
        Bundle bdl =getArguments();
        chatId = bdl.getString("chatId");
        String [] chatName = chatId.split("_");
        
        if(chatName[1].equalsIgnoreCase(authInstance.getCurrentUser().getDisplayName())){
            tvChatUserName.setText(chatName[2]);
        }else{
            tvChatUserName.setText(chatName[1]);
        }
        
        lvChatBox.setAdapter(chatAdapter);
        btnBack.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        getChats();
        return v;
    }

    private void getChats() {

        chatRef.child(chatId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        messageList.clear();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){

                            ChatMessage chatObj = ds.getValue(ChatMessage.class);
                            if(!chatObj.getSender().equalsIgnoreCase("system")){
                                messageList.add(chatObj);
                            }

                        }

                        chatAdapter.notifyDataSetChanged();
                        lvChatBox.setSelection(chatAdapter.getCount()-1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnChatBack:
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                fm.popBackStack ();
                break;
            case R.id.btnSend:
                sendMessage();
        }

    }

    private void sendMessage() {

        String message = etChatBox.getText().toString();
        if(!message.isEmpty()){
            String messageKey =  chatRef.push().getKey();
            chatRef.child(chatId).child(messageKey).setValue(new ChatMessage(loggedInUser, message, new Date()));
            etChatBox.setText("");
        }
    }
}

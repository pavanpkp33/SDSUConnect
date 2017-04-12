package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.adapters.ChatListAdapter;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment implements AdapterView.OnItemClickListener{

    FirebaseDatabase fireDB;
    FirebaseAuth fireAuth;
    DatabaseReference dbRef;
    ListView lvChatList;
    ArrayList<ChatList> listChats;
    ChatListAdapter cAdapter;
    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);
        fireAuth = FirebaseAuth.getInstance();
        fireDB = FirebaseDatabase.getInstance();
        dbRef = fireDB.getReference("users");
        listChats = new ArrayList<>();
        cAdapter = new ChatListAdapter(getContext(), listChats);

        lvChatList = (ListView) v.findViewById(R.id.lvChatList);
        lvChatList.setAdapter(cAdapter);
        lvChatList.setOnItemClickListener(this);
        getChatList();

        return v;
    }

    public void getChatList(){
        final String authUser = fireAuth.getCurrentUser().getDisplayName();
        dbRef.child(authUser)
                .addValueEventListener(new ValueEventListener() {

                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                            listChats.clear();
                           for(DataSnapshot ds : dataSnapshot.getChildren()){

                               ChatList chatObj = ds.getValue(ChatList.class);

                               if(!chatObj.getChatId().equalsIgnoreCase(authUser)){
                                   listChats.add(0,chatObj);
                               }
                           }


                       cAdapter.notifyDataSetChanged();
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               }
        );
    }

    public void openChatWindow(String chatId){
        FragmentManager fmg = getFragmentManager();
        FragmentTransaction fmgT = fmg.beginTransaction();
        ChatBoxFragment chatBoxFrag = new ChatBoxFragment();
        Bundle bdl = new Bundle();
        bdl.putString("chatId", chatId);
        chatBoxFrag.setArguments(bdl);
        fmgT.addToBackStack("CBOX");
        fmgT.replace(R.id.chatContainer, chatBoxFrag, "CBOX");

        fmgT.commit();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ChatList listObj = (ChatList) parent.getItemAtPosition(position);
        openChatWindow(listObj.getChatId());
    }
}
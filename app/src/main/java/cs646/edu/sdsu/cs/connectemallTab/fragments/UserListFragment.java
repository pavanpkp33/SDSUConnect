package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.UserHomeActivity;
import cs646.edu.sdsu.cs.connectemallTab.adapters.UserAdapter;
import cs646.edu.sdsu.cs.connectemallTab.helpers.Constants;
import cs646.edu.sdsu.cs.connectemallTab.helpers.VolleyHandler;
import cs646.edu.sdsu.cs.connectemallTab.model.Users;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserListFragment extends Fragment implements AbsListView.OnScrollListener, View.OnClickListener, AdapterView.OnItemClickListener {

    Button btnReloadList, btnCallMapFrag;
    ListView userListView;
    UserAdapter userAdapter;
    ArrayList<Users> userList;
    ProgressDialog progressDialog;
    UserHomeActivity parentInstance;

    private boolean userScrolled = false;

    public UserListFragment() {
        // Required empty public constructor
    }



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentInstance = (UserHomeActivity) getActivity();
        progressDialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_user_list, container, false);

        userListView = (ListView) v.findViewById(R.id.userListView);
        btnReloadList = (Button) v.findViewById(R.id.btnRefreshList);
        btnCallMapFrag = (Button) v.findViewById(R.id.btnMapView);

        btnCallMapFrag.setOnClickListener(this);
        btnReloadList.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        userList = new ArrayList<>();

        if(!parentInstance.isFILTER_SET()){
            parentInstance.getNextId();
        }

        userAdapter = new UserAdapter(getContext(),parentInstance.getDataSource());

        userListView.setAdapter(userAdapter);
        userListView.setOnScrollListener(this);
        userListView.setOnItemClickListener(this);

        userAdapter.notifyDataSetChanged();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    public void updateView(){
        userAdapter.notifyDataSetChanged();

    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            userScrolled = false;
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING ||
                scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            userScrolled = true;

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if(userScrolled){
            try{
                if (userListView.getLastVisiblePosition() == userListView.getAdapter().getCount() - 1
                        && userListView.getChildAt(userListView.getChildCount() - 1).getBottom() <= userListView.getHeight()) {
                    int lastId = parentInstance.getDataSource().get(totalItemCount-1).getId();
                    //  parentInstance.getDataFromServer(lastId, lastId-100);
                    String message = "Loading more users..";
                    parentInstance.displaySnack(message);
                    if(!parentInstance.isFILTER_SET()){
                        Cursor c = parentInstance.checkLocalDB(lastId, lastId-100);
                        if(c.moveToFirst()){
                            //Since the data is inconsistent, If the DB has 90% data, we display DB. Else, get from server
                            if(c.getInt(0) > (0.9*Constants.MIN_REC_COUNT)){

                                if(parentInstance.getDataFromDB(lastId, lastId-100)) updateView();
                            }else{

                                parentInstance.getDataFromServer(lastId, lastId-100);
                            }
                        }
                    }else{

                        FragmentManager manager = getFragmentManager();
                        FilterFragment fragment = (FilterFragment) manager.findFragmentByTag("FILTER");

                        if(fragment != null){
                            //  fragment.setNextId(lastId);
                            fragment.onScrollCheckLocalDbAndServer(lastId);
                        }
                    }



                }
            }catch (Exception e){
                e.printStackTrace();
            }


        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnRefreshList:
                parentInstance.displaySnack("Reloading List...");
                parentInstance.getDataSource().clear();
                parentInstance.setFILTER_SET(false);
                FragmentManager manager = getFragmentManager();
                UserListFragment fragment = (UserListFragment) manager.findFragmentByTag("LIST");
                FilterFragment filFrag = (FilterFragment) manager.findFragmentByTag("FILTER");
                filFrag.resetFilter();
                onActivityCreated(null);

                break;
            case R.id.btnMapView:
                FragmentManager mapFragManage = getFragmentManager();
                FragmentTransaction mapFragManageTrans = mapFragManage.beginTransaction();
                UserMapFragment uMapFrag = new UserMapFragment();
                mapFragManageTrans.replace(R.id.userDetailsContainer, uMapFrag, "MAP").commit();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Users user = (Users) parent.getItemAtPosition(position);
        checkAndStartChat(user.getNickname());



    }

    private void checkAndStartChat(String selectedUser) {

        if(!selectedUser.isEmpty()){
            FirebaseAuth  authInstance = FirebaseAuth.getInstance();
            String user1 = authInstance.getCurrentUser().getDisplayName();
            String chatId = parentInstance.generateChatId(user1, selectedUser);
            parentInstance.checkUserExisits(user1, selectedUser, chatId);
        }

    }
}

package cs646.edu.sdsu.cs.connectemallTab.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.fragments.ChatBoxFragment;
import cs646.edu.sdsu.cs.connectemallTab.fragments.UserListFragment;
import cs646.edu.sdsu.cs.connectemallTab.fragments.UserMapFragment;
import cs646.edu.sdsu.cs.connectemallTab.fragments.ViewFragment;
import cs646.edu.sdsu.cs.connectemallTab.helpers.Constants;
import cs646.edu.sdsu.cs.connectemallTab.helpers.DataHelper;
import cs646.edu.sdsu.cs.connectemallTab.helpers.VolleyHandler;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatList;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatMessage;
import cs646.edu.sdsu.cs.connectemallTab.model.Users;

public class UserHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView tvUsername;

    ContentValues insertValues;
    private FirebaseAuth authInstance;
    int nextId = 0, existingId = 0;
    public boolean FILTER_SET = false;
    private ArrayList<Users> dataSource = new ArrayList<>();
    private FirebaseDatabase database;

    SharedPreferences sharedPref;
    DatabaseReference dbRef, chatRef;


    DataHelper dbHelper;
    private String FILTER_URL = Constants.BASE_URL_USERS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(Constants.PREF_NEXTID, this.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users");
        chatRef = database.getReference("chats");
        existingId = sharedPref.getInt(Constants.NEXTID_KEY, 0);
        authInstance = FirebaseAuth.getInstance();
        if(authInstance.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        setContentView(R.layout.activity_user_home);

        dbHelper = new DataHelper(getApplicationContext());
        try{
            dbHelper.openConnetion();
        }catch (Exception e){
            e.printStackTrace();
        }
        //getNextId();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        tvUsername = (TextView) headerView.findViewById(R.id.tvUserNameNav);
        if(authInstance.getCurrentUser() != null){
            tvUsername.setText(authInstance.getCurrentUser().getDisplayName().toString());
        }

        FragmentManager fmg = getSupportFragmentManager();
        FragmentTransaction fmgT = fmg.beginTransaction();

        ViewFragment viewFrag = new ViewFragment();
        fmgT.replace(R.id.viewFragmentContainer, viewFrag);
        fmgT.commit();
    }

    private void initRecordDownload(int nId) {

        if(existingId > 0){

            reloadList(nId);

        }else{

            //get top 100 records from server.
            getDataFromServer(nId, (nId-Constants.MIN_REC_COUNT));
            sharedPref.edit().putInt(Constants.NEXTID_KEY, (nId-Constants.MIN_REC_COUNT+1)).commit();

        }


    }

    private void reloadList(int nId) {
        dataSource.clear();
        int maxId =0, updateCount = 0;
       //get the max id and compare it with server next id. Get records if less than 100.
        Cursor c =  dbHelper.select(Constants.QUERY_ID_MAX, null);
        if(c.moveToFirst()){
               maxId = c.getInt(0);
        }
        updateCount = nId - maxId;
        if( nId > maxId){
            //Records have been added. Time to update local DB
            if( (updateCount) > Constants.MIN_REC_COUNT){
                getDataFromServer(nId, maxId);
            }else{
                getDataFromServer(nId, nId - Constants.MIN_REC_COUNT);
                sharedPref.edit().putInt(Constants.NEXTID_KEY, (nId-Constants.MIN_REC_COUNT+1)).commit();
            }
        }


    }

    public void getDataFromServer(int beforeId, int afterId) {

        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        String url= Constants.BASE_URL_USERS_REVERSE+"&beforeid="+beforeId+"&afterid="+afterId;

        JsonArrayRequest jsonRequest= new JsonArrayRequest(url,new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    for(int i = 0; i <response.length(); i++){
                        Users user = new Users();
                        insertValues = new ContentValues();
                        JSONObject jObj = response.getJSONObject(i);
                        insertValues.put(Constants.KEY_ID, jObj.getInt("id"));
                        insertValues.put(Constants.KEY_NICKNAME, jObj.getString("nickname"));
                        insertValues.put(Constants.KEY_YEAR, jObj.getInt("year"));
                        insertValues.put(Constants.KEY_COUNTRY, jObj.getString("country"));
                        insertValues.put(Constants.KEY_STATE, jObj.getString("state"));
                        insertValues.put(Constants.KEY_CITY, jObj.getString("city"));
                        insertValues.put(Constants.KEY_LATITUDE, jObj.getDouble("latitude"));
                        insertValues.put(Constants.KEY_LONGITUDE, jObj.getDouble("longitude"));
                        insertValues.put(Constants.KEY_TIMESTAMP, jObj.getString("time-stamp"));
                        dbHelper.insert(insertValues);

                        user.setId(jObj.getInt("id"));
                        user.setNickname(jObj.getString("nickname"));
                        user.setYear(jObj.getInt("year"));
                        user.setCountry(jObj.getString("country"));
                        user.setState(jObj.getString("state"));
                        user.setCity(jObj.getString("city"));
                        user.setLatitude(jObj.getDouble("latitude"));
                        user.setLongitude(jObj.getDouble("longitude"));

                        dataSource.add(user);
                    }
                    FragmentManager manager = getSupportFragmentManager();
                    UserListFragment fragment = (UserListFragment) manager.findFragmentByTag("LIST");
                    System.out.println(fragment);
                    if(fragment != null){
                        fragment.updateView();
                    }else{
                        System.out.println("Loading more data "+ dataSource.size());

                        UserMapFragment mapFrag = (UserMapFragment) manager.findFragmentByTag("MAP");
                        mapFrag.getIntialData();
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }




            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)

            {

                error.printStackTrace();
            }
        });
        requestQueueObj.add(jsonRequest);



    }

    public Cursor checkLocalDB(int beforeId, int afterId){
        String [] params = { Integer.toString(afterId), Integer.toString(beforeId) };
        return dbHelper.select(Constants.QUERY_CHECK_RANGE, params );
    }

    public boolean getDataFromDB(int beforeId, int afterId){
        String [] params = { Integer.toString(afterId), Integer.toString(beforeId) };
        Cursor c = dbHelper.select(Constants.QUERY_GET_RANGE, params );
        if(c.moveToFirst()){
            while(!c.isAfterLast()){
                Users userObj = new Users();
                userObj.setId(c.getInt(0));
                userObj.setNickname(c.getString(1));
                userObj.setCountry(c.getString(2));
                userObj.setState(c.getString(3));
                userObj.setCity(c.getString(4));
                userObj.setYear(c.getInt(5));
                userObj.setLatitude(c.getDouble(6));
                userObj.setLongitude(c.getDouble(7));

                dataSource.add(userObj);
                c.moveToNext();

            }
        }
        FragmentManager manager = getSupportFragmentManager();
        UserMapFragment mapFrag = (UserMapFragment) manager.findFragmentByTag("MAP");
        if(mapFrag != null){
            mapFrag.getIntialData();
        }

        return true;

    }

    public void getNextId() {
        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        String url= Constants.NEXT_ID;

        StringRequest jsonRequest= new StringRequest(url,new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {

                nextId = Integer.parseInt(response);
                initRecordDownload(nextId);
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)

            {


                error.printStackTrace();

            }
        });
        requestQueueObj.add(jsonRequest);


    }

    @Override
    protected void onDestroy() {
        dbHelper.closeConnection();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String generateChatId(String user1, String user2){

        String chatId = "chat";
        int compare = user1.compareToIgnoreCase(user2);
        if(compare < 0){
            chatId += "_"+user2+"_"+user1;
        }
        else{
            chatId += "_"+user1+"_"+user2;
        }

        return chatId;
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            authInstance.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public String getFILTER_URL() {
        return FILTER_URL;
    }

    public void setFILTER_URL(String FILTER_URL) {
        this.FILTER_URL = FILTER_URL;
    }

    public boolean isFILTER_SET() {
        return FILTER_SET;
    }

    public void setFILTER_SET(boolean FILTER_SET) {
        this.FILTER_SET = FILTER_SET;
    }

    public ArrayList<Users> getDataSource() {
        return dataSource;
    }

    public void checkUserExisits(final String user1, final String user2, final String chatId) {

        System.out.println("CHAT ID: "+ chatId);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(user2).exists()){
                    if(!user1.equalsIgnoreCase(user2)){
                        if(dataSnapshot.child(user2).child(chatId).exists()){
//                            System.out.println("Ready for chat sir");
                            FragmentManager fmg = getSupportFragmentManager();
                            FragmentTransaction fmgT = fmg.beginTransaction();
                            ChatBoxFragment chatBoxFrag = new ChatBoxFragment();
                            Bundle bdl = new Bundle();
                            bdl.putString("chatId", chatId);
                            chatBoxFrag.setArguments(bdl);
                            fmgT.addToBackStack("CBOX");
                            fmgT.replace(R.id.chatContainer, chatBoxFrag, "CBOX");

                            fmgT.commit();
                        }else{
                            System.out.println("NOT READY! CREATING!");
                            dbRef.child(user2).child(chatId).setValue(new ChatList(chatId));
                            dbRef.child(user1).child(chatId).setValue(new ChatList(chatId));
                            chatRef.child(chatId).child(chatId).setValue(new ChatMessage("system", "Welcome", new Date()));
                            FragmentManager fmg = getSupportFragmentManager();
                            FragmentTransaction fmgT = fmg.beginTransaction();
                            ChatBoxFragment chatBoxFrag = new ChatBoxFragment();
                            Bundle bdl = new Bundle();
                            bdl.putString("chatId", chatId);
                            chatBoxFrag.setArguments(bdl);
                            fmgT.addToBackStack("CBOX");
                            fmgT.replace(R.id.chatContainer, chatBoxFrag, "CBOX");

                            fmgT.commit();
                        }
                    }

                }else{
                    //System.out.println("User does not exist in firebase");
                    Snackbar.make(findViewById(android.R.id.content), "User unavailable for Chat.", Snackbar.LENGTH_SHORT)
                            .show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do nothing
                System.out.println(databaseError.getMessage());
            }

        });
    }

    public DataHelper getDbHelper() {
        return dbHelper;
    }


}

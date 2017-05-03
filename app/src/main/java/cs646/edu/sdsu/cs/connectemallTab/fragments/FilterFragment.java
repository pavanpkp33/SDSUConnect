package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.UserHomeActivity;
import cs646.edu.sdsu.cs.connectemallTab.helpers.Constants;
import cs646.edu.sdsu.cs.connectemallTab.helpers.VolleyHandler;
import cs646.edu.sdsu.cs.connectemallTab.model.Users;

/**
 * A simple {@link Fragment} subclass.
 */
public class FilterFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{

    Button btnApply, btnClear;
    Spinner spCountry, spState;
    int pageSize = Constants.MIN_REC_COUNT, page = 0;
    String selectedCountry, selectedState,URL_FILTER = Constants.BASE_URL_USERS_REVERSE, QUERY_STRING, QUERY_STRING_SCROLL;

    int selectedCountryPos;
    int selectedStatePos;

    int nextId = 0;
    EditText etYear;
    UserHomeActivity parentInstance;
    ArrayList<String> countryList = new ArrayList<>(), stateList = new ArrayList<>();
    ArrayAdapter<String> countryAdapter, stateAdapter;
    ContentValues insertValues;
    public FilterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentInstance = (UserHomeActivity) getActivity();
        countryList.add("None selected");
        stateList.add("None selected");
        countryAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, countryList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, stateList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_filter, container, false);
        parentInstance.setFILTER_SET(false);
        btnApply = (Button) v.findViewById(R.id.btnApplyFilter);
        btnClear = (Button) v.findViewById(R.id.btnClearFilter);
        etYear = (EditText) v.findViewById(R.id.etFilterYear);
        spCountry = (Spinner) v.findViewById(R.id.spFilterCountry);
        spState = (Spinner) v.findViewById(R.id.spFilterState);

        spCountry.setAdapter(countryAdapter);
        spState.setAdapter(stateAdapter);

        btnApply.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        spCountry.setSelection(0,false);
        spCountry.setOnItemSelectedListener(this);
        spState.setSelection(0, false);
        spState.setOnItemSelectedListener(this);


        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCountryData();
    }

    private void getCountryData() {

        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        String url= Constants.URL_FETCH_COUNTRIES;
        JsonArrayRequest jsonRequest= new JsonArrayRequest(url,new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    for(int  i =0; i< response.length(); i++){
                        countryList.add(response.getString(i));
                    }
                }catch (JSONException e){
                    System.out.println("ERROR");
                }
                spCountry.postInvalidate();



            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("ERROR","ERROR");
            }
        });
        requestQueueObj.add(jsonRequest);
    }

    private void getStateList(String selectedCountry) {

        stateList.clear();
        stateList.add("None selected");
        spState.setSelection(0);
        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        String url="http://bismarck.sdsu.edu/hometown/states?country="+selectedCountry;
        JsonArrayRequest jsonRequest= new JsonArrayRequest(url,new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    for(int  i =0; i< response.length(); i++){
                        stateList.add(response.getString(i));

                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
                spState.postInvalidate();




            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Log.d("ERROR",error.getMessage());
            }
        });
        requestQueueObj.add(jsonRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnApplyFilter:
                if(spCountry.getSelectedItem().toString().contains("None") &&
                        spState.getSelectedItem().toString().contains("None") &&
                        etYear.getText().toString().isEmpty())
                {
                    parentInstance.displaySnack("Please select atleast one filter value");


                }else
                {
                    InputMethodManager imm = (InputMethodManager) parentInstance.getSystemService(getContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    URL_FILTER = Constants.BASE_URL_USERS_REVERSE;
                    page = 0;
                    filterData();
                }

                break;
            case R.id.btnClearFilter:
                resetFilter();
        }
    }

    public void resetFilter() {

        parentInstance.setFILTER_SET(false);
        parentInstance.getDataSource().clear();
        page = 0;
        URL_FILTER = Constants.BASE_URL_USERS_REVERSE;
        QUERY_STRING = "";
        parentInstance.getNextId();
        etYear.setText("");
        spCountry.setSelection(0, false);
        spState.setSelection(0, false);
    }

    private void filterData() {
        parentInstance.displaySnack("Filtering Data.. Please wait!");
        parentInstance.setFILTER_SET(true);

        if(!spState.getSelectedItem().toString().contains("None")){
            setZoomLevel(selectedState+", "+selectedCountry);
            parentInstance.setZoomLevel(6);

        }else if(!spCountry.getSelectedItem().toString().contains("None")){
            setZoomLevel(selectedCountry);
            parentInstance.setZoomLevel(4);
        }
        getNextId();


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){

            case R.id.spFilterCountry:
                selectedCountry = parent.getItemAtPosition(position).toString();
                selectedCountryPos = position;
                if(selectedCountryPos !=0){
                    getStateList(selectedCountry);
                }else{
                    spState.setSelection(0);
                }

                break;

            case R.id.spFilterState:

                selectedState = parent.getItemAtPosition(position).toString();
                selectedStatePos = position;

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String  buildURL() {
        boolean isCountryApplied = false;

        URL_FILTER+= "&page="+page+"&pagesize="+pageSize+"&beforeid="+nextId;
        if(!spCountry.getSelectedItem().toString().contains("None")){
            URL_FILTER += "&country="+selectedCountry;
            QUERY_STRING = "SELECT * FROM USERS WHERE ID < "+nextId+" AND country = '"+selectedCountry+"' ORDER BY ID DESC LIMIT 100";
            QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND country = '"+selectedCountry+"' ORDER BY ID DESC LIMIT 100";
            isCountryApplied = true;
        }
        if(!spState.getSelectedItem().toString().contains("None")){
            try {
                URL_FILTER += "&state="+URLEncoder.encode(selectedState, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            QUERY_STRING = "SELECT * FROM USERS WHERE ID < "+nextId+" AND country = '"+selectedCountry+"' AND country = '"+selectedState+"' ORDER BY ID DESC LIMIT 100";
            QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND country = '"+selectedCountry+"' AND country = '"+selectedState+"' ORDER BY ID DESC LIMIT 100";
        }
        if(!etYear.getText().toString().equals("")){
            String filterYear = etYear.getText().toString();
            URL_FILTER += "&year="+filterYear;
            if(isCountryApplied){

                QUERY_STRING = "SELECT * FROM USERS WHERE ID < "+nextId+" AND country = '"+filterYear+"' AND country = '"+selectedState+"" +
                        "' AND country = "+filterYear+
                        " ORDER BY ID DESC LIMIT 100";

                QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND country = '"+filterYear+"' AND country = '"+selectedState+"" +
                        "' AND country = "+filterYear+
                        " ORDER BY ID DESC LIMIT 100";
            }else{

                QUERY_STRING = "SELECT * FROM USERS WHERE ID < "+nextId+" AND year = "+filterYear+" ORDER BY ID DESC LIMIT 100";
                QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND year = "+filterYear+" ORDER BY ID DESC LIMIT 100";
            }

        }

        return URL_FILTER;
    }

    public void getNextId() {
        parentInstance.displayLoading("Filtering data....");
        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        String url= Constants.NEXT_ID;

        StringRequest jsonRequest= new StringRequest(url,new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {

                nextId = Integer.parseInt(response);
                checkLocalDbAndServer(nextId);
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

    private void checkLocalDbAndServer(int nextId) {
        //this only when filter button is pressed
        buildURL();
        parentInstance.getDataSource().clear();
        int maxId = 0;
        Cursor c =  parentInstance.getDbHelper().select(Constants.QUERY_ID_MAX, null);
        if(c.moveToFirst()){
            maxId = c.getInt(0);
        }
       int updateCount = (nextId-1) - maxId;
        if(updateCount > 0){

            getFilteredDataFromServer();
        }else{
           Cursor dbCursor = checkLocalDB();
            try{
                if(dbCursor.getCount() == 100){
                    // put to array list

                    while(dbCursor.moveToNext()){
                        Users userObj = new Users();
                        userObj.setId(dbCursor.getInt(0));
                        userObj.setNickname(dbCursor.getString(1));
                        userObj.setCountry(dbCursor.getString(2));
                        userObj.setState(dbCursor.getString(3));
                        userObj.setCity(dbCursor.getString(4));
                        userObj.setYear(dbCursor.getInt(5));
                        userObj.setLatitude(dbCursor.getDouble(6));
                        userObj.setLongitude(dbCursor.getDouble(7));

                        parentInstance.getDataSource().add(userObj);

                    }
                    parentInstance.hideLoading();
                    refreshFragments();
                }else{
                    getFilteredDataFromServer();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    private void refreshFragments() {
        FragmentManager manager = getFragmentManager();
        UserListFragment fragment = (UserListFragment) manager.findFragmentByTag("LIST");

        if(fragment != null){
            fragment.updateView();
        }else{

            UserMapFragment mapFrag = (UserMapFragment) manager.findFragmentByTag("MAP");
            mapFrag.getIntialData();
        }
    }

    public Cursor checkLocalDB(){

        return parentInstance.getDbHelper().select(QUERY_STRING, null );
    }

    public void getFilteredDataFromServer() {

        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        String url= URL_FILTER;

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
                        parentInstance.getDbHelper().insert(insertValues);

                        user.setId(jObj.getInt("id"));
                        user.setNickname(jObj.getString("nickname"));
                        user.setYear(jObj.getInt("year"));
                        user.setCountry(jObj.getString("country"));
                        user.setState(jObj.getString("state"));
                        user.setCity(jObj.getString("city"));
                        user.setLatitude(jObj.getDouble("latitude"));
                        user.setLongitude(jObj.getDouble("longitude"));

                        parentInstance.getDataSource().add(user);
                    }
                    parentInstance.hideLoading();
                  refreshFragments();

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

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public void onScrollCheckLocalDbAndServer(int lastId) {
           // nextId = lastId;
            Cursor dbCursor = checkLocalDBScroll(lastId);

        if(dbCursor.getCount() == 100){
                // put to array list

                    while(dbCursor.moveToNext()){
                        Users userObj = new Users();
                        userObj.setId(dbCursor.getInt(0));
                        userObj.setNickname(dbCursor.getString(1));
                        userObj.setCountry(dbCursor.getString(2));
                        userObj.setState(dbCursor.getString(3));
                        userObj.setCity(dbCursor.getString(4));
                        userObj.setYear(dbCursor.getInt(5));
                        userObj.setLatitude(dbCursor.getDouble(6));
                        userObj.setLongitude(dbCursor.getDouble(7));

                        parentInstance.getDataSource().add(userObj);

                    }
                parentInstance.hideLoading();
                refreshFragments();
            }else{
                page++;
                buildURLOnScroll(lastId, page);
                getFilteredDataFromServer();
            }


    }

    private void buildURLOnScroll(int lastId, int pageNum) {

        boolean isCountryApplied = false;
        URL_FILTER = Constants.BASE_URL_USERS_REVERSE;
        URL_FILTER+= "&page="+pageNum+"&pagesize="+pageSize+"&beforeid="+nextId;
        if(!spCountry.getSelectedItem().toString().contains("None")){
            URL_FILTER += "&country="+selectedCountry;

            QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND country = '"+selectedCountry+"' ORDER BY ID DESC LIMIT 100";
            isCountryApplied = true;
        }
        if(!spState.getSelectedItem().toString().contains("None")){
            try {
                URL_FILTER += "&state="+ URLEncoder.encode(selectedState, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND country = '"+selectedCountry+"' AND country = '"+selectedState+"' ORDER BY ID DESC LIMIT 100";
        }
        if(!etYear.getText().toString().equals("")){
            String filterYear = etYear.getText().toString();
            URL_FILTER += "&year="+filterYear;
            if(isCountryApplied){


                QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND country = '"+filterYear+"' AND country = '"+selectedState+"" +
                        "' AND country = "+filterYear+
                        " ORDER BY ID DESC LIMIT 100";
            }else{


                QUERY_STRING_SCROLL = "SELECT * FROM USERS WHERE ID < ? AND year = "+filterYear+" ORDER BY ID DESC LIMIT 100";
            }

        }


    }

    public Cursor checkLocalDBScroll(int lastId){
        String [] arr = {Integer.toString(lastId)};
        return parentInstance.getDbHelper().select(QUERY_STRING_SCROLL, arr );
    }

    private void setZoomLevel(String value) {

        Geocoder geoLocator = new Geocoder(getContext());
        try {
            ArrayList<Address> addressList = (ArrayList<Address>) geoLocator.getFromLocationName( value,1);
            for(Address c : addressList){
                if(c.hasLatitude() && c.hasLongitude()){
                    parentInstance.setFilterLatitude(c.getLatitude());
                    parentInstance.setFilterLongitude(c.getLongitude());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

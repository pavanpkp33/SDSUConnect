package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cs646.edu.sdsu.cs.connectemallTab.activities.UserHomeActivity;
import cs646.edu.sdsu.cs.connectemallTab.helpers.Constants;
import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.RegisterActivity;
import cs646.edu.sdsu.cs.connectemallTab.model.ChatList;
import cs646.edu.sdsu.cs.connectemallTab.model.Users;
import cs646.edu.sdsu.cs.connectemallTab.helpers.VolleyHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserRegistrationFrag extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, View.OnFocusChangeListener, DatabaseReference.CompletionListener{
    ProgressDialog progressDialog;
    private ArrayList<String> countryList = new ArrayList<String>(),
            stateList = new ArrayList<String>();
    private ArrayList<Address> addressList;
    private ArrayAdapter<String> countryAdapter, stateAdapter;
    private String selectedCountry = null, selectedStateValue = null;
    private int selectedState = 0;
    private double Lat, Lng;
    private boolean isSpinnerTouched = false;
    RegisterActivity instance;
    EditText etUserName, etPassword, etCity, etYear, etEmail;
    Button btnSetCity, btnSave;
    Spinner spCountry, spState;
    RelativeLayout loaderLayout;

    private FirebaseAuth authInstance;
    FirebaseDatabase database ;
    DatabaseReference userDB;

    public UserRegistrationFrag() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        authInstance = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getContext());
        instance = (RegisterActivity) getActivity();
        countryList.add("Select a Country");
        stateList.add("Select a State");
        countryAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, countryList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, stateList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addressList = new ArrayList<Address>();

        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("users");




    }


    @Override
    public void onStart() {
        super.onStart();

        Lat = instance.getLat();
        Lng = instance.getLng();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(!instance.isConnected()){
            Toast.makeText(getContext(),"No Internet Connection",Toast.LENGTH_LONG).show();
        }

        View v = inflater.inflate(R.layout.fragment_user_registration, container, false);

        //  int localSelection = selectedState;

        etUserName = (EditText) v.findViewById(R.id.etUserName);
        etPassword = (EditText) v.findViewById(R.id.etPassword);
        etCity = (EditText) v.findViewById(R.id.etCity);
        etYear = (EditText) v.findViewById(R.id.etYear);
        etEmail = (EditText) v.findViewById(R.id.etEmailId);

        btnSetCity = (Button) v.findViewById(R.id.btnSetLatLng);
        btnSave = (Button) v.findViewById(R.id.btnSave);

        spCountry = (Spinner)v.findViewById(R.id.spCountry);
        spState = (Spinner)v.findViewById(R.id.spState);
        loaderLayout = (RelativeLayout) v.findViewById(R.id.loaderUserFrag);

        progressDialog.setMessage("Loading.. Please wait");
        progressDialog.show();
        getCountryData();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spCountry.setAdapter(countryAdapter);
        spState.setAdapter(stateAdapter);

        etEmail.setOnFocusChangeListener(this);
        etUserName.setOnFocusChangeListener(this);
        btnSave.setOnClickListener(this);
        btnSetCity.setOnClickListener(this);
        spCountry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });
        spState.setSelection(0,false);
        spCountry.setSelection(0, false);
        spCountry.setOnItemSelectedListener(this);
        spState.setOnItemSelectedListener(this);
    }

    private void getCountryData() {
       // loaderLayout.setVisibility(View.VISIBLE);
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
                    e.printStackTrace();
                }
            progressDialog.dismiss();
            //    loaderLayout.setVisibility(View.GONE);


            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("ERROR", "ERROR");
            }
        });
        requestQueueObj.add(jsonRequest);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    if(isSpinnerTouched){
        switch (parent.getId()){

            case R.id.spCountry:

                if(position != 0){
                    selectedCountry = parent.getItemAtPosition(position).toString();
                    getStateList(selectedCountry);

                }
                break;

            case R.id.spState:
                selectedState = position;
                selectedStateValue = parent.getItemAtPosition(position).toString();
                isSpinnerTouched = false;
                break;
        }
    }


    }

    private void getStateList(String selectedCountry) {
        progressDialog.setMessage("Loading.. Please wait!");
        progressDialog.show();
       // loaderLayout.setVisibility(View.VISIBLE);
        stateList.clear();
        stateList.add("Select a State");
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

                //loaderLayout.setVisibility(View.GONE);
                progressDialog.dismiss();


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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        return;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.btnSetLatLng:

                InputMethodManager imm = (InputMethodManager) instance.getSystemService(getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                instance.setCountryData(countryList);
                instance.setStateData(stateList);

                FragmentManager fMg = getFragmentManager();
                FragmentTransaction fMgt = fMg.beginTransaction();
                MapFragment mapFrag = new MapFragment();
                Bundle bdl = new Bundle();
                bdl.putString("country", selectedCountry);
                bdl.putString("state", selectedStateValue);
                mapFrag.setArguments(bdl);
                fMgt.replace(R.id.userFragContainer,mapFrag);
                fMgt.addToBackStack("User");
                fMgt.commit();

                break;

            case R.id.btnSave:
                validateAndSave();
        }
    }

    private void validateAndSave() {
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int currentYear = calendar.get(Calendar.YEAR);
        int enteredYear = 0;
        if(!etYear.getText().toString().equals("")){
            enteredYear = Integer.parseInt(etYear.getText().toString());
        }
        if(etEmail.getText().toString().equals("")){
            etEmail.setError("Enter an email id");
            etEmail.requestFocus();
        }else if(etUserName.getText().toString().equals("")){
            etUserName.setError("Enter a nick name");
            etUserName.requestFocus();
        }else if(etPassword.getText().toString().equals("")){
            etPassword.setError("Enter password");
            etPassword.requestFocus();
        }else if(etPassword.getText().toString().length() < 3){
            etPassword.setError("Minimum 3 characters required!");
            etPassword.requestFocus();
        }else if(spCountry.getSelectedItem().toString().contains("Select")){
            Toast.makeText(getContext(),"Select a Country", Toast.LENGTH_SHORT).show();
        }else if(spState.getSelectedItem().toString().contains("Select")){
            Toast.makeText(getContext(),"Select a State", Toast.LENGTH_SHORT).show();
        }else if(etCity.getText().toString().equals("")){
            etCity.setError("Enter a city");
            etCity.requestFocus();
        }else if(etYear.getText().toString().equals("")){
            etYear.setError("Enter an Year");
            etYear.requestFocus();
        }else if(enteredYear < 1970 || enteredYear > currentYear){
            etYear.setError("Values should be between 1970-"+currentYear);
            etYear.requestFocus();
        }else{

            postData();
            //postFireBase();
        }

    }

    private void postFireBase() {
        progressDialog.setMessage("Registering user.. Please wait");

        //loaderLayout.setVisibility(View.VISIBLE);
        String emailId = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        authInstance.createUserWithEmailAndPassword(emailId, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            addUserName(etUserName.getText().toString(), task.getResult().getUser());
                            resetForm();
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Message: "+task.getException().getMessage(), Toast.LENGTH_SHORT)
                                    .show();


                        }
                    }
                }
        );
    }

    private void addUserName(String nickName, FirebaseUser user) {
        progressDialog.setMessage("Updating Nickname..");
        final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nickName)
                .build();

        userDB.child(nickName).child(nickName).setValue(new ChatList(nickName));
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "User registration successfull! Redirecting..", Toast.LENGTH_SHORT)
                                    .show();
                            Intent intent = new Intent(getActivity(), UserHomeActivity.class);
                            instance.finish();
                            startActivity(intent);
                        }
                    }
                });
    }


    //Post data to server
    private void postData() {
        progressDialog.setMessage("Contacting Bismarck..");
        progressDialog.show();
        String url = Constants.URL_ADD_USER;
        String nickname = etUserName.getText().toString();
        String password = etPassword.getText().toString();
        String country =  spCountry.getSelectedItem().toString();
        String state = spState.getSelectedItem().toString();
        String city = etCity.getText().toString();
        int year = Integer.parseInt(etYear.getText().toString());
        double latitude=0.0, longitude= 0.0;
        if(( Lat!= 0) && (Lng != 0)){
            latitude = Lat;
            longitude = Lng;
        }else{
            Geocoder geoLocator = new Geocoder(getContext());
            try {
                addressList = (ArrayList<Address>) geoLocator.getFromLocationName(state+", "+country, 1);
                for(Address c : addressList){
                    if(c.hasLatitude() && c.hasLongitude()){
                        latitude = c.getLatitude();
                        longitude = c.getLongitude();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final JSONObject reqBody = new JSONObject();
        try {
            reqBody.put("nickname", nickname);
            reqBody.put("password", password);
            reqBody.put("country", country);
            reqBody.put("state",state);
            reqBody.put("city", city);
            reqBody.put("year", year);
            reqBody.put("latitude", latitude);
            reqBody.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, reqBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if(response.get("message").toString().equals("ok")){
                       // Toast.makeText(getContext(),"User added Successfully!", Toast.LENGTH_LONG).show();
                        postFireBase();

                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Oops! Something went wrong. Try again!", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Oops! Something went wrong. Try again!", Toast.LENGTH_SHORT).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Oops! Something went wrong. Try again!", Toast.LENGTH_SHORT).show();


            }
        });

        requestQueueObj.add(jsonRequest);
    }

    //Check for duplicate user name
    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        switch (v.getId()){
            case R.id.etUserName:
                if(!etUserName.getText().toString().equals("")){
                    checkDuplicateUser(etUserName.getText().toString());
                }

                break;

        }


    }


    private void resetForm(){

        etUserName.setText("");
        etPassword.setText("");
        etCity.setText("");
        etYear.setText("");
        etEmail.setText("");
        spCountry.setSelection(0, false);
        spState.setSelection(0, false);
        instance.setLng(0);
        instance.setLat(0);
    }

    private void checkDuplicateUser(String nickName) {
        //loaderLayout.setVisibility(View.VISIBLE);
        progressDialog.setMessage("Checking for duplicate Nickname...");
        progressDialog.show();
        RequestQueue requestQueueObj= VolleyHandler.getInstance().getReqQueue();
        String url=Constants.URL_CHECK_NICKNAME+nickName;
        StringRequest stringRequest= new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                if(response.equals("true")){
                    etUserName.setError("Nickname Exists!!");
                    etUserName.requestFocus();
                    return;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
             progressDialog.dismiss();


            }
        });
        requestQueueObj.add(stringRequest);

    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError == null){
            //loaderLayout.setVisibility(View.GONE);
            progressDialog.dismiss();
            Toast.makeText(getContext(), "User Inserted to database", Toast.LENGTH_SHORT).show();
        }

        else {
          //  loaderLayout.setVisibility(View.GONE);
            progressDialog.dismiss();
            Log.e("rew", databaseError.getMessage());
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
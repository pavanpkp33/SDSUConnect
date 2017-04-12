package cs646.edu.sdsu.cs.connectemallTab.fragments;


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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.UserHomeActivity;
import cs646.edu.sdsu.cs.connectemallTab.helpers.Constants;
import cs646.edu.sdsu.cs.connectemallTab.helpers.VolleyHandler;

/**
 * A simple {@link Fragment} subclass.
 */
public class FilterFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{

    Button btnApply, btnClear;
    Spinner spCountry, spState;
    String selectedCountry, selectedState;
    int selectedCountryPos, selectedStatePos;
    EditText etYear;
    UserHomeActivity parentInstance;
    ArrayList<String> countryList = new ArrayList<>(), stateList = new ArrayList<>();
    ArrayAdapter<String> countryAdapter, stateAdapter;

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
                InputMethodManager imm = (InputMethodManager) parentInstance.getSystemService(getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                filterData();
                break;
            case R.id.btnClearFilter:
                resetFilter();
        }
    }

    private void resetFilter() {

        etYear.setText("");
        spCountry.setSelection(0, false);
        spState.setSelection(0, false);
    }

    private void filterData() {

        parentInstance.setFILTER_SET(true);
        parentInstance.setFILTER_URL(buildURL());
//        if(!spState.getSelectedItem().toString().contains("None")){
//            setZoomLevel(selectedState+", "+selectedCountry);
//            parentInstance.setZoomLeve(6);
//
//        }else if(!spCountry.getSelectedItem().toString().contains("None")){
//            setZoomLevel(selectedCountry);
//            parentInstance.setZoomLeve(4);
//        }
        FragmentManager fMg = getFragmentManager();
        FragmentTransaction fMgt = fMg.beginTransaction();
        UserListFragment userList = new UserListFragment();
        fMgt.replace(R.id.userDetailsContainer, userList, "LIST");
        fMgt.commit();

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

    private String buildURL() {
        boolean isCountryApplied = false;
        String URL_FILTER = Constants.BASE_URL_USERS;
        if(!spCountry.getSelectedItem().toString().contains("None")){
            URL_FILTER += "?country="+selectedCountry;
            isCountryApplied = true;
        }
        if(!spState.getSelectedItem().toString().contains("None")){
            URL_FILTER += "&state="+selectedState;

        }
        if(!etYear.getText().toString().equals("")){
            String filterYear = etYear.getText().toString();
            if(isCountryApplied){
                URL_FILTER += "&year="+filterYear;
            }else{
                URL_FILTER += "?year="+filterYear;
            }

        }

        return URL_FILTER;
    }
}

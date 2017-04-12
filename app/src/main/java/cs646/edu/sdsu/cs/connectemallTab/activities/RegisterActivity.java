package cs646.edu.sdsu.cs.connectemallTab.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.fragments.UserRegistrationFrag;
import cs646.edu.sdsu.cs.connectemallTab.helpers.ContextHelper;

public class RegisterActivity extends AppCompatActivity {

    private double Lng = 0;
    private double Lat = 0;


    private FirebaseAuth authInstance;
    private int selectedCountry = 0;
    private int selectedState = 0;
    private ArrayList<String> countryData, stateData;

    double filterLatitude = 0, filterLongitude = 0;
    int zoomLeve = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ContextHelper contextHelper = new ContextHelper(this);
        authInstance = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);


        if(isConnected()){
            FragmentManager fmg = getSupportFragmentManager();
            FragmentTransaction fmgT = fmg.beginTransaction();

            UserRegistrationFrag userReg = new UserRegistrationFrag();
            fmgT.replace(R.id.userFragContainer, userReg);
            fmgT.commit();
        }else{
            Toast.makeText(this,"No Internet Connection", Toast.LENGTH_LONG).show();
        }

    }


    //Method to check whether the device is connected to internet or not.
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }



    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLng() {
        return Lng;
    }

    public void setLng(double lng) {
        Lng = lng;
    }


    public int getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(int selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public int getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(int selectedState) {
        this.selectedState = selectedState;
    }

    public ArrayList<String> getCountryData() {
        return countryData;
    }

    public void setCountryData(ArrayList<String> countryData) {

        this.countryData = countryData;
    }

    public ArrayList<String> getStateData() {

        return stateData;
    }

    public void setStateData(ArrayList<String> stateData) {

        this.stateData = stateData;
    }


    public int getZoomLeve() {
        return zoomLeve;
    }

    public void setZoomLeve(int zoomLeve) {
        this.zoomLeve = zoomLeve;
    }

    public FirebaseAuth getAuthInstance() {
        return authInstance;
    }

}

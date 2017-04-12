package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.RegisterActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback{

    Button setCity;
    MapView cityMaps;
    MarkerOptions marker;
    Marker markerRef;
    double latitudeValue =0, longitudeValue=0, initLatitude = 0, initLongitude = 0;
    private GoogleMap gMaps;
    RegisterActivity instance;
    String coutrySelected, stateSelected;
    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = (RegisterActivity) getActivity();
        Bundle bdl = getArguments();
        coutrySelected = bdl.getString("country");
        stateSelected = bdl.getString("state");

        if (coutrySelected != null && stateSelected != null) {
            Geocoder geoLocator = new Geocoder(getContext());
            try {
                ArrayList<Address> addressList = (ArrayList<Address>) geoLocator.getFromLocationName(stateSelected+", "+coutrySelected, 1);
                for(Address c : addressList){
                    if(c.hasLatitude() && c.hasLongitude()){
                        initLatitude = c.getLatitude();
                        initLongitude = c.getLongitude();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(!instance.isConnected()){
            Toast.makeText(getContext(),"No Internet Connection",Toast.LENGTH_LONG).show();
        }
        View v =  inflater.inflate(R.layout.fragment_map, container, false);
        setCity = (Button) v.findViewById(R.id.btnSaveLatLng);
        setCity.setOnClickListener(this);
        cityMaps = (MapView) v.findViewById(R.id.mpCity);
        cityMaps.onCreate(savedInstanceState);
        cityMaps.onResume();
        cityMaps.getMapAsync(this);

        return v;
    }

    @Override
    public void onClick(View v) {

        instance.setLat(latitudeValue);
        instance.setLng(longitudeValue);
        Toast.makeText(getContext(),"Latitude Longitude Values set",Toast.LENGTH_SHORT).show();
        FragmentManager fMg = getFragmentManager();
        FragmentTransaction fMgt = fMg.beginTransaction();
        fMg.popBackStack();
        fMgt.commit();

    }

    @Override
    public void onMapClick(LatLng latLng) {
        latitudeValue = latLng.latitude;
        longitudeValue = latLng.longitude;
        Toast.makeText(getContext(),"Selected Lat Long :"+latitudeValue+" / "+longitudeValue,Toast.LENGTH_SHORT).show();
        placeMarker(latitudeValue, longitudeValue);

    }

    private void placeMarker(double lat, double lng) {

        if(markerRef != null){ //if marker exists (not null or whatever)
            markerRef.setPosition(new LatLng(lat, lng));
        }
        else{
            markerRef = gMaps.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title("Selected City")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    .draggable(true));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng)).zoom(12).build();
            gMaps.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // latitude and longitude

        gMaps = googleMap;

        if(initLatitude != 0 && initLongitude != 0){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(initLatitude, initLongitude)).zoom(6).build();
            gMaps.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

        }else{
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(0, 0)).zoom(1).build();
            gMaps.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }

        gMaps.setOnMapClickListener(this);


    }

    @Override
    public void onDestroy() {

        super.onDestroy();

    }
}

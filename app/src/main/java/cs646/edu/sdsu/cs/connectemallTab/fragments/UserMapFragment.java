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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.UserHomeActivity;
import cs646.edu.sdsu.cs.connectemallTab.helpers.Constants;
import cs646.edu.sdsu.cs.connectemallTab.model.Users;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserMapFragment extends Fragment implements View.OnClickListener , OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener{

    Button btnListView, btnLoadMore;
    MapView userMapView;
    private GoogleMap gMaps;
    UserHomeActivity parentInstance;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentInstance = (UserHomeActivity) getActivity();
    }

    public UserMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v =  inflater.inflate(R.layout.fragment_user_map, container, false);
        userMapView = (MapView) v.findViewById(R.id.userMapView);
        btnListView = (Button) v.findViewById(R.id.btnListView);
        btnLoadMore = (Button) v.findViewById(R.id.btnLoadMore);

        btnLoadMore.setOnClickListener(this);
        btnListView.setOnClickListener(this);
        userMapView.onCreate(savedInstanceState);
        userMapView.onResume();
        userMapView.getMapAsync(this);

        return v;
    }



    public void getIntialData() {
        ProgressDialog pgBar = new ProgressDialog(getContext());
        pgBar.setMessage("Loading users.. Please wait");
        pgBar.show();
        if(gMaps != null){
            gMaps.clear();
        }

        for(Users u :  parentInstance.getDataSource()){
            createMarker(u.getLatitude(), u.getLongitude(), u.getNickname(), u.getYear(), 0);
        }
        userMapView.invalidate();
        pgBar.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnListView:
                FragmentManager mapFragManage = getFragmentManager();
                FragmentTransaction mapFragManageTrans = mapFragManage.beginTransaction();
                UserListFragment uListFrag = new UserListFragment();
                mapFragManageTrans.replace(R.id.userDetailsContainer, uListFrag, "LIST").commit();
                break;

            case R.id.btnLoadMore:

                int size = parentInstance.getDataSource().size();
                int lastId = parentInstance.getDataSource().get(size-1).getId();
                Cursor c = parentInstance.checkLocalDB(lastId, lastId-100);
                if(c.moveToFirst()){
                    //Since the data is inconsistent, If the DB has 90% data, we display DB. Else, get from server
                    if(c.getInt(0) > (0.9* Constants.MIN_REC_COUNT)){

                        if(parentInstance.getDataFromDB(lastId, lastId-100)) userMapView.invalidate();

                    }else{

                        parentInstance.getDataFromServer(lastId, lastId-100);
                    }
                }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMaps = googleMap;
        gMaps.setOnInfoWindowClickListener(this);
        getIntialData();

    }

    private void createMarker(double latitude, double longitude, String title, int snippet, int iconResID) {

        gMaps.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title("Name - "+title)
                .snippet("Year - "+snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        String [] arr = marker.getTitle().split(" - ");


    }
    public void refreshView(){

        userMapView.postInvalidate();
    }
}

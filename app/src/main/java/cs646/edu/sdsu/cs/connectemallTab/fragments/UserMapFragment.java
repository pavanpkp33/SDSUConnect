package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.UserHomeActivity;
import cs646.edu.sdsu.cs.connectemallTab.model.Users;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserMapFragment extends Fragment implements View.OnClickListener , OnMapReadyCallback{

    Button btnListView;
    MapView userMapView;
    private GoogleMap gMaps;
    UserHomeActivity parentInstance;
    ArrayList<Users> list;

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
        btnListView.setOnClickListener(this);
        userMapView.onCreate(savedInstanceState);
        userMapView.onResume();
        userMapView.getMapAsync(this);

        return v;
    }



    private void getIntialData() {
        list = parentInstance.getDataSource();
        for(Users u : list){
            createMarker(u.getLatitude(), u.getLongitude(), u.getNickname(), u.getYear(), 0);
        }
        userMapView.invalidate();
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

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMaps = googleMap;
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

}

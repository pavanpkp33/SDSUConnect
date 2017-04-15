package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.app.ProgressDialog;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
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
    ProgressDialog pgBar;
    ArrayList<Users> threadList;
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
        pgBar = new ProgressDialog(getContext());
        pgBar.setMessage("Loading users.. Please wait");
        pgBar.show();
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

        if(parentInstance.isFILTER_SET()){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(parentInstance.getFilterLatitude(), parentInstance.getFilterLongitude())).zoom(parentInstance.getZoomLevel()).build();
            gMaps.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
            parentInstance.setZoomLevel(1);
            parentInstance.setFilterLongitude(0);
            parentInstance.setFilterLatitude(0);
        }else{
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(parentInstance.getFilterLatitude(), parentInstance.getFilterLongitude())).zoom(parentInstance.getZoomLevel()).build();
            gMaps.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
        if(gMaps != null){
            gMaps.clear();
        }
        threadList = new ArrayList<>();
        for(Users u :  parentInstance.getDataSource()){
            if(u.getLatitude() == 0.0 || u.getLongitude() == 0.0){
                threadList.add(u);
            }else{
                createMarker(u.getLatitude(), u.getLongitude(), u.getNickname(), u.getYear(), 0);
            }

        }
        new GetUsers().execute(threadList);
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
                if(!parentInstance.isFILTER_SET()) {
                    Cursor c = parentInstance.checkLocalDB(lastId, lastId - 100);
                    if (c.moveToFirst()) {
                        //Since the data is inconsistent, If the DB has 90% data, we display DB. Else, get from server
                        if (c.getInt(0) > (0.9 * Constants.MIN_REC_COUNT)) {

                            if (parentInstance.getDataFromDB(lastId, lastId - 100))
                                userMapView.invalidate();

                        } else {

                            parentInstance.getDataFromServer(lastId, lastId - 100);
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
        FirebaseAuth authInstance = FirebaseAuth.getInstance();
        String user1 = authInstance.getCurrentUser().getDisplayName();
        String chatId = parentInstance.generateChatId(user1, arr[1]);
        parentInstance.checkUserExisits(user1, arr[1], chatId);

    }
    public void refreshView(){

        userMapView.postInvalidate();
    }

    private class GetUsers extends AsyncTask<ArrayList<Users>, Users, ArrayList<Users>> {



        @Override
        protected ArrayList<Users> doInBackground(ArrayList<Users>... params) {

            parentInstance.displaySnack("Geo-coding process start.");

            try{

                for(int i = 0; i < params[0].size(); i++){
                    Users user = new Users();
                    user = params[0].get(i);

                    String country = user.getCountry();
                    String state = user.getState();
                    Geocoder geoLocator = new Geocoder(getContext());
                    try {
                        ArrayList<Address>   addressList = (ArrayList<Address>) geoLocator.getFromLocationName(state+", "+country, 1);
                        for(Address c : addressList){
                            if(c.hasLatitude() && c.hasLongitude()){
                                user.setLatitude(c.getLatitude());
                                user.setLongitude(c.getLongitude());
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    publishProgress(user);
                }


            }catch (Exception e){
                e.printStackTrace();
            }


            return  null;
        }

        @Override
        protected void onProgressUpdate(Users... values) {

            createMarker(values[0].getLatitude(), values[0].getLongitude(), values[0].getNickname(), values[0].getYear(), 0);
            userMapView.postInvalidate();
        }

        @Override
        protected void onPostExecute(ArrayList<Users> aVoid) {
            parentInstance.displaySnack("Geo coding completed!");

        }
    }


}

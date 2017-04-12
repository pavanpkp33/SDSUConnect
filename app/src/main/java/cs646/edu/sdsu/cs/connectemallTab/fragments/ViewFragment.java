package cs646.edu.sdsu.cs.connectemallTab.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cs646.edu.sdsu.cs.connectemallTab.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewFragment extends Fragment {


    public ViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        FragmentManager fmg = getFragmentManager();
        FragmentTransaction fmgT = fmg.beginTransaction();

        FilterFragment filterFrag = new FilterFragment();
        ChatListFragment cListFrag = new ChatListFragment();
        ChatBoxFragment cBoxFrag = new ChatBoxFragment();
        UserListFragment uListFrag = new UserListFragment();
        fmgT.replace(R.id.filterContainer, filterFrag);
        fmgT.replace(R.id.userDetailsContainer, uListFrag, "LIST");
        fmgT.replace(R.id.chatContainer, cListFrag, "CLIST");
        fmgT.commit();
        progressDialog.dismiss();
        return inflater.inflate(R.layout.fragment_view, container, false);
    }

}

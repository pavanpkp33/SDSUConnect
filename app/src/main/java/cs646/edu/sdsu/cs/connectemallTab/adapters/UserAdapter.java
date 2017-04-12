package cs646.edu.sdsu.cs.connectemallTab.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.model.Users;

/**
 * Created by Pkp on 3/17/2017.
 */

public class UserAdapter extends BaseAdapter {

    Context c;
    ArrayList<Users> userList;

    public UserAdapter(Context context, ArrayList<Users> list){
        c = context;
        userList = list;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Users user = userList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.user_row, parent, false);

        }

        TextView tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
        TextView tvYear = (TextView) convertView.findViewById(R.id.tvYear);
        TextView tvCity = (TextView) convertView.findViewById(R.id.tvCity);
        TextView tvState = (TextView) convertView.findViewById(R.id.tvState);
        TextView tvCountry = (TextView) convertView.findViewById(R.id.tvCountry);

        try{
            tvUserName.setText(user.getNickname());
            tvYear.setText(Integer.toString(user.getYear()));
            tvCity.setText(user.getCity());
            tvState.setText(user.getState());
            tvCountry.setText(user.getCountry());
            notifyDataSetChanged();
        }catch(Exception e){
           e.printStackTrace();
        }




        return convertView;
    }
}

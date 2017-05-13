package com.polito.mad17.madmax.activities.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.Map;

public class HashMapFriendsAdapter extends BaseAdapter {
    private final ArrayList mData;

    public HashMapFriendsAdapter(Map<String, User> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }


    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    public void deleteItem (String userID)
    {
        mData.remove(userID);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, User> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, User> item = getItem(position);

        TextView name=(TextView)result.findViewById(R.id.tv_name);
        TextView balance=(TextView)result.findViewById(R.id.tv_balance);
        name.setText(item.getValue().getName() + " " + item.getValue().getSurname());
        balance.setVisibility(View.GONE);



        return result;
    }
}

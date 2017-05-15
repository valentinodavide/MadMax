package com.polito.mad17.madmax.activities.groups;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by alessandro on 07/05/17.
 */

public class HashMapGroupsAdapter extends BaseAdapter {
    private final ArrayList mData;

    public HashMapGroupsAdapter(Map<String, Group> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }


    public void update(Map<String, Group> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, Group> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, Group> item = getItem(position);

        TextView name=(TextView)result.findViewById(R.id.tv_name);
        name.setText(item.getValue().getName());

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupID = getItem(position).getKey();
            }
        });





        return result;
    }


}

package com.polito.mad17.madmax.Activities;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polito.mad17.madmax.Entities.Group;
import com.polito.mad17.madmax.Entities.User;
import com.polito.mad17.madmax.R;

import java.util.ArrayList;
import java.util.HashMap;

import static com.polito.mad17.madmax.Entities.Group.members;
import static com.polito.mad17.madmax.R.mipmap.group;
import static com.polito.mad17.madmax.R.string.groups;

public class GroupsActivity extends AppCompatActivity {

    public static HashMap<String, Group> groups = new HashMap<>();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);


        Button friendsbutton = (Button) findViewById(R.id.friendsbutton);

        friendsbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Context context = GroupsActivity.this;
                Class destinationActivity = FriendsActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                startActivity(intent);

            }
        });



        listView = (ListView) findViewById(R.id.lv_list_groups);

        for(int i = 0; i <= 20; i++)
        {
            Group group = new Group(String.valueOf(i), "Group" + i, "imgGroup" + i);

            for(int j = 0; j <= 4; j++)
            {
                User user = new User(String.valueOf(j), "Name1", "Surname1", "ImageProfile1");
                group.getMembers().put(user.getID(), user);
            }

            Log.d("DEBUG", group.toString());
            groups.put(group.getID(), group);
        }

        ListAdapter listAdapter = new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) { }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) { }

            @Override
            public int getCount() {
                return groups.size();
            }

            @Override
            public Object getItem(int position) {
                return groups.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if(convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.group_item, parent, false);
                }

                Log.d("DEBUG", groups.get(String.valueOf(position)).toString());
                Group group = groups.get(String.valueOf(position));

                // ImageView groupImage = (ImageView) convertView.findViewById(R.id.img_group);
                // groupImage.setImageResource(group.getImage());

                TextView groupName = (TextView) convertView.findViewById(R.id.tv_group_name);
                groupName.setText(group.getName());
                groupName.setTag(group.getID());

                TextView numberNotifications = (TextView) convertView.findViewById(R.id.tv_group_num_notifications);
                numberNotifications.setText(group.getNumberNotifications().toString());

                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };

        listView.setAdapter(listAdapter);
    }

    public void onClickOpenGroup(View view) {

        TextView groupName = (TextView) view;
        Log.d("DEBUG", groupName.getTag().toString());

//      Intent myIntent = new Intent(GroupsActivity.this, GroupDetailsActivity.class);
        Intent myIntent = new Intent(GroupsActivity.this, GroupExpenses.class);
        myIntent.putExtra("groupID", groupName.getTag().toString()); //Optional parameters
        GroupsActivity.this.startActivity(myIntent);
    }
}

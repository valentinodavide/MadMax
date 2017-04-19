package com.polito.mad17.madmax.activities;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class FriendDetailActivity extends AppCompatActivity {

    private ListView lv;
    User u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_friend_detail);

        View view = getLayoutInflater().inflate(R.layout.activity_friend_detail, null);

        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView surname=(TextView)view.findViewById(R.id.surname);
        TextView balancetext=(TextView) view.findViewById(R.id.balancetext);
        TextView balance=(TextView) view.findViewById(R.id.balance);


        //Extract data from bundle
        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString("userID");

        u = GroupsActivity.users.get(Integer.parseInt(id)-1);
        String n = u.getName();
        String s = u.getSurname();
        Integer p = Integer.parseInt(u.getProfileImage());
        Double b = GroupsActivity.myself.getBalanceWithUsers().get(u.getID());

        //final HashMap<String, Group> mygroups = GroupsActivity.myself.getSharedGroupsMap(u);
        final ArrayList<Group> mygroups = GroupsActivity.myself.getSharedGroupsList(u);
        //final HashMap<String, Group> mygroups= GroupsActivity.myself.getUserGroups();

        photo.setImageResource(p);
        name.setText(n);
        surname.setText(s);

        DecimalFormat df = new DecimalFormat("#.##");
        balance.setText(df.format(Math.abs(b)) + " €");



        if (b > 0)
        {
            balancetext.setText("YOU SHOULD RECEIVE");
        }
        else if (b < 0)
        {
            balancetext.setText("YOU OWE");
        }
        else
        {
            balancetext.setText("NO DEBTS");
        }


        setContentView(view);

        lv = (ListView) findViewById(R.id.lv);








        final BaseAdapter myadapter = new BaseAdapter() {


            @Override
            public int getCount() {
                return mygroups.size();
            }

            @Override
            public Object getItem(int position) {
                return mygroups.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView==null) {
                    convertView=getLayoutInflater().inflate(R.layout.item_group,parent,false);
                }


                //Group group = mygroups.get(String.valueOf(position));
                Group group = mygroups.get(position);




                ImageView groupImage = (ImageView) convertView.findViewById(R.id.img_group);
                String p = group.getImage();
                int photoId = Integer.parseInt(p);
                groupImage.setImageResource(photoId);

                TextView groupName = (TextView) convertView.findViewById(R.id.tv_group_name);
                groupName.setText(group.getName());
                groupName.setTag(group.getID());

                TextView groupbalance = (TextView) convertView.findViewById(R.id.tv_group_debt);


                //mydebt = mio debito con il gruppo
                Double mygroupdebt = GroupsActivity.myself.getBalanceWithGroups().get(group.getID());

                DecimalFormat df = new DecimalFormat("#.##");


                if (mygroupdebt > 0)
                {
                    groupbalance.setText("+ " + df.format(mygroupdebt) + " €");
                    groupbalance.setBackgroundResource(R.color.greenBalance);

                }
                else if (mygroupdebt < 0)
                {
                    groupbalance.setText("- " + df.format(Math.abs(mygroupdebt)) + " €");
                    groupbalance.setBackgroundColor(Color.rgb(255,0,0));
                }
                else
                {
                    groupbalance.setText("" + df.format(mygroupdebt) + " €");
                    groupbalance.setBackgroundResource(R.color.greenBalance);
                }

                return convertView;
            }
        };

        lv.setAdapter(myadapter);








    }
}

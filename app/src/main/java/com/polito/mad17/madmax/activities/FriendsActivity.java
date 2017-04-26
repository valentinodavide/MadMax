package com.polito.mad17.madmax.activities;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;

public class FriendsActivity extends AppCompatActivity {




    private ListView lv;
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        /*
        mDatabase = FirebaseDatabase.getInstance().getReference();

        User u = new User ("utent1", "username", "name", "String surname", "String email", "String password", "String profileImage");
        mDatabase.child("users").child("u01").setValue(u);

        */

        Button groupsbutton = (Button) findViewById(R.id.groupsbutton);

        groupsbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Context context = FriendsActivity.this;
                Class destinationActivity = GroupsActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                startActivity(intent);

            }
        });




        lv = (ListView) findViewById(R.id.lv);










        final BaseAdapter myadapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return GroupsActivity.users.size();
            }

            @Override
            public Object getItem(int position) {
                return GroupsActivity.users.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView==null) {
                    convertView=getLayoutInflater().inflate(R.layout.item_friend,parent,false);
                }

                //collego le view agli elementi del layout
                ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
                TextView name=(TextView)convertView.findViewById(R.id.name);
                TextView balance = (TextView)convertView.findViewById(R.id.balancetext);

                User f = GroupsActivity.users.get(position);



                //setto le view con gli elementi estratti dal FriendItem
                String p = f.getProfileImage();
                int photoId = Integer.parseInt(p);

                photo.setImageResource(photoId);
                name.setText(f.getName() + " " + f.getSurname());

                //mydebt = mio debito con il membro f
                Double mydebt = GroupsActivity.myself.getBalanceWithUsers().get(f.getID());

                DecimalFormat df = new DecimalFormat("#.##");


                if (mydebt > 0)
                {
                    balance.setText("+ " + df.format(mydebt) + " €");
                    balance.setBackgroundResource(R.color.greenBalance);

                }
                else if (mydebt < 0)
                {
                    balance.setText("- " + df.format(Math.abs(mydebt)) + " €");
                    balance.setBackgroundColor(Color.rgb(255,0,0));
                }
                else
                {
                    balance.setText("" + df.format(mydebt) + " €");
                    balance.setBackgroundResource(R.color.greenBalance);

                }

                return convertView;
            }
        };

        lv.setAdapter(myadapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                //FriendItem item = (FriendItem) myadapter.getItem(position);
                User item = (User) myadapter.getItem(position);



                //put elements needed by FriendDetailActivity in a Bundle
                Bundle bundle = new Bundle();
                /*
                bundle.putInt("photoid", Integer.parseInt(item.getProfileImage()));
                bundle.putString("name", item.getName());
                bundle.putString("surname", item.getSurname());
                bundle.putDouble("balance", GroupsActivity.myself.getBalanceWithUsers().get(item.getID()));
                bundle.putSerializable("groups", GroupsActivity.myself.getUserGroups());
                */


                bundle.putString("userID", item.getID());


                Context context = FriendsActivity.this;
                Class destinationActivity = FriendDetailActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });



    }


}

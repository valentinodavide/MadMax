package com.polito.mad17.madmax.Activities;

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

import com.polito.mad17.madmax.R;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    Integer[] imgid={
            R.drawable.obama,
            R.drawable.putin
    };

    String[] names={
            "Barack",
            "Valdimir",

    };
    String[] surnames={
            "Obama",
            "Putin"
    };

    Integer[] balances = {
            33,
            -24
    };



    class FriendItem {

        //Attributes
        private String name, surname;
        private Integer photo_id, balance;


        //Constructor
        public FriendItem(String name, String surname, Integer photo_id, Integer balance) {
            this.name = name;
            this.surname = surname;
            this.photo_id = photo_id;
            this.balance = balance;

        }

        //Getters
        public String getName() {
            return name;
        }
        public String getSurname() {return surname;}
        public Integer getPhoto_id() {return photo_id;}
        public Integer getBalance() {return balance;}
    }


    private ListView lv;
    private ArrayList<FriendItem> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //toolbar.setTitle("Friends");

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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


        for (int i = 0; i < 20; i++) {

            FriendItem f;
            if (i%2 == 0)
            {
                f = new FriendItem(names[0], surnames[0], imgid[0], balances[0]);
            }
            else
            {
                f = new FriendItem(names[1], surnames[1], imgid[1], balances[1]);
            }

            friends.add(f);
        }






        final BaseAdapter myadapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return friends.size();
            }

            @Override
            public Object getItem(int position) {
                return friends.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView==null) {
                    convertView=getLayoutInflater().inflate(R.layout.friend_item,parent,false);
                }

                //collego le view agli elementi del layout
                ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
                TextView name=(TextView)convertView.findViewById(R.id.name);
                TextView surname=(TextView)convertView.findViewById(R.id.surname);
                TextView balance = (TextView)convertView.findViewById(R.id.balance);

                //prendo il FriendItem
                FriendItem f=friends.get(position);

                Integer prova = f.getBalance();

                //setto le view con gli elementi estratti dal FriendItem
                photo.setImageResource(f.getPhoto_id());
                name.setText(f.getName());
                surname.setText(f.getSurname());
                if (f.getBalance() > 0)
                {
                    balance.setText("+ " + f.getBalance() + "€");
                    balance.setBackgroundColor(Color.rgb(0,255,0));

                }
                else if (f.getBalance() < 0)
                {
                    balance.setText("- " + Math.abs(f.getBalance()));
                    balance.setBackgroundColor(Color.rgb(255,0,0));
                }
                else
                {
                    balance.setText("" + f.getBalance());
                    balance.setBackgroundColor(Color.rgb(0,255,0));

                }



                return convertView;
            }
        };

        lv.setAdapter(myadapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                /*
                Intent intent = new Intent(MainActivity.this, SendMessage.class);
                String message = "abc";
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
                */
                FriendItem item = (FriendItem) myadapter.getItem(position);



                //put elements needed by FriendDetailActivity in a Bundle
                Bundle bundle = new Bundle();
                bundle.putInt("photoid", item.getPhoto_id());
                bundle.putString("name", item.getName());
                bundle.putString("surname", item.getSurname());
                bundle.putInt("balance", item.getBalance());


                Context context = FriendsActivity.this;
                Class destinationActivity = FriendDetailActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });



    }


}

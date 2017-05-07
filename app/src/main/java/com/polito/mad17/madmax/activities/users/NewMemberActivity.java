package com.polito.mad17.madmax.activities.users;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewMemberActivity extends AppCompatActivity {

    private ListView lv;
    private DatabaseReference mDatabase;
    private HashMap<String, User> friends = new HashMap<>();
    //todo usare SharedPreferences invece della map globale alreadySelected
    public static HashMap<String, User> alreadySelected = new HashMap<>();
    private HashMapFriendsAdapter adapter;
    private String myselfID;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        Intent intent = getIntent();
        myselfID = intent.getStringExtra("UID");


        lv = (ListView) findViewById(R.id.members);

        mDatabase.child("users").child(myselfID).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapshot: dataSnapshot.getChildren())
                {
                    getFriend(friendSnapshot.getKey());
                }

                adapter = new HashMapFriendsAdapter(friends);
                lv.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        //When i click on one friend of the list
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {


                    User item = adapter.getItem(position).getValue();
                    friends.remove(item.getID());
                    adapter.update(friends);
                    adapter.notifyDataSetChanged();

                    alreadySelected.put(item.getID(), item);

                    Context context = NewMemberActivity.this;
                    Class destinationActivity = NewGroupActivity.class;
                    Intent intent = new Intent(context, destinationActivity);
                    intent.putExtra("UID", myselfID);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("userAdded", item);
                    intent.putExtras(bundle);

                    startActivity(intent);

            }
        });

    }

    public void getFriend(final String id)
    {
        mDatabase.child("users").child(id).addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User u = new User();
                u.setName(dataSnapshot.child("name").getValue(String.class));
                u.setSurname(dataSnapshot.child("surname").getValue(String.class));
                u.setID(dataSnapshot.getKey());

                //se l'amico letto da db non è già stato scelto, lo metto nella lista di quelli
                //che saranno stampati
                if (!alreadySelected.containsKey(u.getID()))
                {
                    friends.put(id, u);
                    adapter.update(friends);
                    adapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

}

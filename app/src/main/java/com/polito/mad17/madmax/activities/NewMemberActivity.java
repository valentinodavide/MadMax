package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.User;

public class NewMemberActivity extends AppCompatActivity {

    String groupID;
    private ListView lv;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");

        lv = (ListView) findViewById(R.id.members);



        FirebaseListAdapter<User> firebaseListAdapter = new FirebaseListAdapter<User>(
                this,   //activity contentente la ListView
                User.class,   //classe in cui viene messo il dato letto (?)
                R.layout.item_friend,   //layout del singolo item
                mDatabase.child("groups").child("0").child("members")  //nodo del db da cui leggo
        ) {
            @Override
            protected void populateView(View v, User model, int position) {

                Log.d("DEBUG", model.toString());
                TextView nametext = (TextView) v.findViewById(R.id.name);
                nametext.setText(model.getName() + " " + model.getSurname());

            }
        };


        lv.setAdapter(firebaseListAdapter);


    }

}

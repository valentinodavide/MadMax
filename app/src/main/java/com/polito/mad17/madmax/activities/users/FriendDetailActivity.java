package com.polito.mad17.madmax.activities.users;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.entities.Group;

import java.util.HashMap;

public class FriendDetailActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = FriendDetailActivity.class.getSimpleName();
    String friendID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        Log.d(TAG, "onCreate di FriendDetailActivity");

        //ID dell'amico di cui sto guardando il dettaglio
        Intent intent = getIntent();
        friendID = intent.getStringExtra("friendID");


        //Creo FriendDetailFragment a cui passo friendID
        if(findViewById(R.id.fragment_containter) != null)
        {
            Bundle bundle = new Bundle();
            bundle.putString("friendID", friendID);
            if(bundle != null) {

                Log.d(TAG, friendID);

                FriendDetailFragment friendDetailFragment = new FriendDetailFragment();
                friendDetailFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_containter, friendDetailFragment)
                        .commit();

            }

        }

    }


    @Override
    public void itemClicked(String fragmentName, String itemID) {
        Log.d(TAG, fragmentName + itemID);
        Intent intent = new Intent();
    }
}

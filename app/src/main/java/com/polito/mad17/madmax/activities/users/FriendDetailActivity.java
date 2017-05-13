package com.polito.mad17.madmax.activities.users;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;

public class FriendDetailActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = FriendDetailActivity.class.getSimpleName();

    private String friendID;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        Log.d(TAG, "onCreate di FriendDetailActivity");

        //ID dell'amico di cui sto guardando il dettaglio
        Intent intent = getIntent();
        friendID = intent.getStringExtra("friendID");
        userID = intent.getStringExtra("userID");

        //Creo FriendDetailFragment a cui passo friendID
        if(findViewById(R.id.fragment_containter) != null)
        {
            Bundle bundle = new Bundle();
            bundle.putString("friendID", friendID);

            Log.d(TAG, friendID);

            FriendDetailFragment friendDetailFragment = new FriendDetailFragment();
            friendDetailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_containter, friendDetailFragment)
                    .commit();
        }
    }


    @Override
    public void itemClicked(String fragmentName, String itemID) {
        Log.d(TAG, fragmentName + itemID);

        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra("groupID", itemID);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
}

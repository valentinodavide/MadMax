package com.polito.mad17.madmax.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.User;

public class FriendDetailActivity extends AppCompatActivity {

    private static final String TAG = FriendDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        Log.d(TAG, "onCreate di FriendDetailActivity");

        if(findViewById(R.id.fragment_containter) != null)
        {
            Bundle bundle = getIntent().getExtras();
            if(bundle != null) {
                if(bundle.containsKey("friendDetails")) {
                    Log.d(TAG, bundle.getParcelable("friendDetails").toString());

                    FriendDetailFragment friendDetailFragment = new FriendDetailFragment();
                    friendDetailFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                                               .add(R.id.fragment_containter, friendDetailFragment)
                                               .commit();
                }
            }

        }
    }
}

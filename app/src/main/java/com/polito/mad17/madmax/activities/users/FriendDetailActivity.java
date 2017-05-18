package com.polito.mad17.madmax.activities.users;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.BarDetailFragment;
import com.polito.mad17.madmax.activities.BasicActivity;
import com.polito.mad17.madmax.activities.DetailFragment;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.entities.Group;

import java.util.HashMap;

public class FriendDetailActivity extends BasicActivity implements OnItemClickInterface, OnItemLongClickInterface {

    private static final String TAG = FriendDetailActivity.class.getSimpleName();
    private String friendID;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadNavHeader();

    //    setContentView(R.layout.fragment_container); layout settato dalla basic activity

        Log.d(TAG, "onCreate di FriendDetailActivity");

        //ID dell'amico di cui sto guardando il dettaglio
        Intent intent = getIntent();
        friendID = intent.getStringExtra("friendID");
        userID = intent.getStringExtra("userID");

        //Creo FriendDetailFragment a cui passo friendID
        if(findViewById(R.id.collapsed_content) != null)
        {
            Bundle bundle = new Bundle();
            bundle.putString("friendID", friendID);

            Log.d(TAG, friendID);

            BarDetailFragment barDetailFragment = new BarDetailFragment();
            barDetailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.collapsed_content, barDetailFragment)
                    .commit();

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, detailFragment)
                    .commit();

            fab.setVisibility(View.GONE);
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

    //Apro popup menu quando ho tenuto premuto un friend o gruppo per 1 secondo
    @Override
    public void itemLongClicked(String fragmentName, String itemID, View v) {

        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

    }
}

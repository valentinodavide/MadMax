package com.polito.mad17.madmax.activities.users;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.BarDetailFragment;
import com.polito.mad17.madmax.activities.BasicActivity;
import com.polito.mad17.madmax.activities.DetailFragment;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.Collections;
import java.util.TreeMap;

public class FriendDetailActivity extends BasicActivity implements GroupsViewAdapter.ListItemClickListener, GroupsViewAdapter.ListItemLongClickListener, OnItemClickInterface {
    private static final String TAG = FriendDetailActivity.class.getSimpleName();
    private DatabaseReference databaseReference = FirebaseUtils.getDatabaseReference();

    private String friendID;
    private String userID;

    private TreeMap<String, Group> groups = new TreeMap<>(Collections.reverseOrder());

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private GroupsViewAdapter groupsViewAdapter;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        Log.d(TAG, "onCreate di FriendDetailActivity");

        //ID dell'amico di cui sto guardando il dettaglio
        Intent intent = getIntent();
        friendID = intent.getStringExtra("friendID");
        userID = intent.getStringExtra("userID");

//        toolbar = (Toolbar) findViewById(R.id.fd_toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setBackgroundColor(0x0000FF00);

        // Get a support ActionBar corresponding to this toolbar
//        ActionBar ab = getSupportActionBar();
        // Enable the Up button
//        ab.setDisplayHomeAsUpEnabled(true);

        Log.d(TAG, "Populated all the above data");

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
        }

        //Show shared groups
        databaseReference.child("users").child(MainActivity.getCurrentUID()).child("friends").child(friendID).child("sharedGroups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot sharedGroupSnapshot : dataSnapshot.getChildren())
                {
                    FirebaseUtils.getInstance().getGroup(sharedGroupSnapshot.getKey(), groups, groupsViewAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.toException());
            }
        });
    }

    //Per creare overflow button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remove_friend_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d (TAG, "Clicked item: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.one:
                Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show();
                return true;
            case android.R.id.home:
                Log.d (TAG, "Clicked up button on PendingExpenseDetailActivity");
                finish();
                Intent myIntent = new Intent(this, MainActivity.class);
                myIntent.putExtra("UID", MainActivity.getCurrentUID());
                myIntent.putExtra("currentFragment", 0);
                startActivity(myIntent);
                return(true);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(String clickedItemIndex)
    {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra("groupID", clickedItemIndex);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    @Override
    public boolean onListItemLongClick(String clickedItemIndex, View v) {
        return false;
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {
        Log.d(TAG, "itemClicked " + itemID);
    }
}

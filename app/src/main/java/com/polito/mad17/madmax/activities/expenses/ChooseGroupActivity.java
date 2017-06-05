package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.HashMap;
import java.util.TreeMap;

public class ChooseGroupActivity extends AppCompatActivity implements GroupsViewAdapter.ListItemClickListener {

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private static final String TAG = ChooseGroupActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private GroupsViewAdapter groupsViewAdapter;
    public static TreeMap<String, Group> groups = new TreeMap<>();
    private OnItemClickInterface onClickGroupInterface;


    //todo RIFARE USANDO GroupsFragment

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d (TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_group);

        RecyclerView.ItemDecoration divider = new InsetDivider.Builder(this)
                .orientation(InsetDivider.VERTICAL_LIST)
                .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                .color(getResources().getColor(R.color.colorDivider))
                .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                .overlay(true)
                .build();

        recyclerView = (RecyclerView) findViewById(R.id.rv_skeleton);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);

        groupsViewAdapter = new GroupsViewAdapter(getBaseContext(), this, groups, ChooseGroupActivity.TAG);
        recyclerView.setAdapter(groupsViewAdapter);

        //Ascolto i gruppi dello user
        databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Per ogni gruppo dello user
                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                {
                    //Se il gruppo è true, ossia è ancora tra quelli dello user
                    if (groupSnapshot.getValue(Boolean.class))
                        FirebaseUtils.getInstance().getGroup(groupSnapshot.getKey(), groups, groupsViewAdapter);
                    else
                    {
                        //tolgo il gruppo da quelli che verranno stampati, così lo vedo sparire realtime
                        groups.remove(groupSnapshot.getKey());
                        groupsViewAdapter.update(groups);
                        groupsViewAdapter.notifyDataSetChanged();

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.toException());
            }
        });
    }

    @Override
    public void onListItemClick(String groupID) {
        Log.d(TAG, "clickedItemIndex " + groupID);
        //onClickGroupInterface.itemClicked(getClass().getSimpleName(), groupID);
        Intent myIntent = new Intent(ChooseGroupActivity.this, NewExpenseActivity.class);
        myIntent.putExtra("userID", MainActivity.getCurrentUser().getID());
        myIntent.putExtra("groupID", groupID);
        myIntent.putExtra("callingActivity", "ChooseGroupActivity");
        myIntent.putExtra("groupName", groups.get(groupID).getName());
        myIntent.putExtra("groupImage", groups.get(groupID).getImage());
        startActivity(myIntent);
    }


    //to manage back button click
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        finish();
        Intent myIntent = new Intent(ChooseGroupActivity.this, MainActivity.class);
        myIntent.putExtra("UID", MainActivity.getCurrentUser().getID());
        myIntent.putExtra("currentFragment", 2);
        startActivity(myIntent);
    }

    //to manage up button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent myIntent = new Intent(ChooseGroupActivity.this, MainActivity.class);
                myIntent.putExtra("UID", MainActivity.getCurrentUser().getID());
                myIntent.putExtra("currentFragment", 2);
                startActivity(myIntent);
                return(true);
            default:
                return(super.onOptionsItemSelected(item));
        }

    }


}

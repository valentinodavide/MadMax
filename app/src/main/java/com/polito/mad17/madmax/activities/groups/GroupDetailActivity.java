package com.polito.mad17.madmax.activities.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.BarDetailFragment;
import com.polito.mad17.madmax.activities.BasicActivity;
import com.polito.mad17.madmax.activities.DetailFragment;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

public class GroupDetailActivity extends BasicActivity implements OnItemClickInterface, OnItemLongClickInterface {

    private static final String TAG = GroupDetailActivity.class.getSimpleName();

    private ImageView imageView;
    private TextView nameTextView;
    private TextView balanceTextView;
    private String groupID;
    private String userID;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private DatabaseReference groupRef;
    private Group groupDetails = new Group();
    private PopupMenu popup;
    private MenuItem one;

    private Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference();


        loadNavHeader();

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");

        Log.d(TAG, "onCreate di GroupDetailActivity. Group: " + groupID);

        Bundle bundle = new Bundle();
        bundle.putString("groupID", groupID);
        bundle.putString("userID", userID);

        if(findViewById(R.id.collapsed_content)!=null){

            Log.d(TAG, groupID);

            BarDetailFragment barDetailFragment = new BarDetailFragment();
            barDetailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.collapsed_content, barDetailFragment)
                    .commit();
        }

        if(findViewById(R.id.main) != null){

            Log.d(TAG, groupID);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, detailFragment)
                    .commit();
        }
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Intent intent;

        Log.d(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(fragmentName) {
            case "ExpensesFragment":
                Log.d(TAG, "hai cliccato sulla spesa: " + fragmentName +" "+ itemID);
                break;

            case "FriendsFragment":
                Log.d(TAG, "hai cliccato sul membro: " + fragmentName +" "+ itemID);
                intent = new Intent(this, FriendDetailActivity.class);
                intent.putExtra("friendID", itemID);
                intent.putExtra("userID", userID);
                startActivity(intent);
                break;
        }
    }

    //Apro popup menu quando ho tenuto premuto un friend o gruppo per 1 secondo
    @Override
    public void itemLongClicked(String fragmentName, final String itemID, View v) {

        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "FriendsFragment":

                //todo decidere se rendere disponibile la funzionalità elimina membro
                /*
                if (!itemID.equals(userID))
                {
                    popup = new PopupMenu(GroupDetailActivity.this, v, Gravity.RIGHT);

                    popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                    one = popup.getMenu().findItem(R.id.one);
                    one.setTitle("Remove Member");
                    popup.getMenu().findItem(R.id.two).setVisible(false);
                    popup.getMenu().findItem(R.id.three).setVisible(false);


                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            //Toast.makeText(GroupDetailActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                            removeMemberFirebase(itemID, groupID);
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
                */

                break;

            case "GroupsFragment":

                popup = new PopupMenu(GroupDetailActivity.this, v, Gravity.RIGHT);

                popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                one = popup.getMenu().findItem(R.id.one);
                one.setTitle("Remove friend");
                popup.getMenu().findItem(R.id.two).setVisible(false);
                popup.getMenu().findItem(R.id.three).setVisible(false);


                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(GroupDetailActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popup.show();//showing popup menu

            case "ExpensesFragment":

                popup = new PopupMenu(GroupDetailActivity.this, v, Gravity.RIGHT);

                popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                one = popup.getMenu().findItem(R.id.one);
                one.setTitle("Remove expense");
                popup.getMenu().findItem(R.id.two).setVisible(false);
                popup.getMenu().findItem(R.id.three).setVisible(false);


                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //Toast.makeText(GroupDetailActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        FirebaseUtils.getInstance().removeExpenseFirebase(itemID, getApplicationContext());
                        return true;
                    }
                });

                popup.show();//showing popup menu

                break;
        }
    }
}



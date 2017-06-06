package com.polito.mad17.madmax.activities.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
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
import com.polito.mad17.madmax.activities.DetailFragment;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.activities.expenses.ExpenseDetailActivity;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.SimpleDateFormat;

public class GroupDetailActivity extends AppCompatActivity implements OnItemClickInterface, OnItemLongClickInterface {

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
    private Button payButton;
    private BarDetailFragment barDetailFragment = new BarDetailFragment();

    static final int EXPENSE_DETAIL_REQUEST = 1;  // The request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");

        Log.d(TAG, "onCreate di GroupDetailActivity. Group: " + groupID);

        Bundle bundle = new Bundle();
        bundle.putString("groupID", groupID);
        bundle.putString("userID", userID);

        if(findViewById(R.id.collapsed_content) != null){

            Log.d(TAG, groupID);

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
                intent = new Intent(this, ExpenseDetailActivity.class);
                intent.putExtra("expenseID", itemID);
                intent.putExtra("userID", MainActivity.getCurrentUID());
                intent.putExtra("groupID", groupID);
                startActivityForResult(intent, EXPENSE_DETAIL_REQUEST);
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
                        Toast.makeText(GroupDetailActivity.this, getString(R.string.clicked) + " " + item.getTitle(),Toast.LENGTH_SHORT).show();
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

                        // add event for EXPENSE_REMOVE
                        databaseReference.child("expenses").child(itemID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User currentUser = MainActivity.getCurrentUser();
                                    Event event = new Event(
                                            groupID,
                                            Event.EventType.EXPENSE_REMOVE,
                                            currentUser.getName() + " " + currentUser.getSurname(),
                                            dataSnapshot.child("description").getValue(String.class)
                                    );
                                    event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                                    event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                                    FirebaseUtils.getInstance().addEvent(event);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, databaseError.toException());
                                }
                            }
                        );

                        return true;
                    }
                });

                popup.show();//showing popup menu

                break;
        }
    }

    //When i return from ExpenseDetailActivity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                userID = data.getStringExtra("userID");
                groupID = data.getStringExtra("groupID");
            }
        }
    }

    //overflow button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        Log.d (TAG, "Clicked item: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.one:
                Log.d (TAG, "clicked Modify group");
                intent = new Intent(this, GroupEdit.class);
                intent.putExtra("groupID", groupID);
                startActivity(intent);
                finish();
                return true;

            case R.id.two:
                Log.d (TAG, "clicked Remove group");
                FirebaseUtils.getInstance().removeGroupFirebase(userID, groupID, getApplicationContext());
                finish();
                return true;

            case android.R.id.home:
                Log.d (TAG, "Clicked up button on GroupDetailActivity");
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("UID", MainActivity.getCurrentUser().getID());
                intent.putExtra("currentFragment", 2);
                startActivity(intent);
                finish();
                return(true);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}





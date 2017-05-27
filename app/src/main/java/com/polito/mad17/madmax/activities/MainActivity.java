package com.polito.mad17.madmax.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.expenses.ChooseGroupActivity;
import com.polito.mad17.madmax.activities.expenses.PendingExpenseDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.activities.login.LoginSignUpActivity;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.HashMap;

import static android.widget.Toast.makeText;
import static com.polito.mad17.madmax.R.string.friends;

//import static com.polito.mad17.madmax.activities.groups.GroupsViewAdapter.groups;

public class MainActivity extends BasicActivity implements OnItemClickInterface, OnItemLongClickInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    public static FirebaseAuth auth;
    private DatabaseReference usersRef;
    private DatabaseReference groupRef;

    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private MenuItem one, two, three;
    private ViewPager viewPager;
    private TabLayout tabLayout;

 //   private ActionBarDrawerToggle drawerToggle;

    public static final int REQUEST_INVITE = 0;
    public static final int REQUEST_INVITE_GROUP = 0;

    private static User currentUser;
    private String currentUID, inviterUID, groupToBeAddedID;

    private HashMap<String, String> userFriends = new HashMap<>();
    private HashMap<String, String> userGroups = new HashMap<>();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        Log.i(TAG, "token: "+FirebaseInstanceId.getInstance().getToken());

        // waiting user data becomes available
        progressDialog = new ProgressDialog(this);
        progressDialog.show();

//        getDatabase();
        auth = FirebaseAuth.getInstance();

        databaseReference = firebaseDatabase.getReference();
        usersRef = databaseReference.child("users");
        groupRef = databaseReference.child("groups");

        DatabaseReference currentUserRef = null;

        // getting currentUID from Intent (from LoginSignUpActivity or EmailVerificationActivity)
        Intent intent = getIntent();
        if(intent.hasExtra("UID"))
        {
            currentUID = intent.getStringExtra("UID");
            Log.i(TAG, "currentUID da LoginSignUpActivity : " +  currentUID);

            // getting reference to the user from db
            currentUserRef = usersRef.child(currentUID);
        }
        else
        {
            if ((currentUID == null) || (currentUserRef == null))
            {
                Log.e(TAG, "Unable to retrieve logged user from db or UID is null, going back to Login");
                makeText(MainActivity.this, "Unable to retrieve user, please login again", Toast.LENGTH_LONG).show();

                // if the current user is not in the database or UID is null do the logout and restart from login
                auth.signOut();
                Intent intentToExit = new Intent(getApplicationContext(), LoginSignUpActivity.class);
                startActivity(intentToExit);
                finish();
            }
        }

        // getting invitation info if coming from LoginSignUpActivity after an Invitation
        if (intent.hasExtra("inviterUID"))
        {
            inviterUID = intent.getStringExtra("inviterUID");
            Log.i(TAG, "present inviterUID: " + inviterUID);
        }
        else
        {
            inviterUID = null;
        }

        if (intent.hasExtra("groupToBeAddedID"))
        {
            groupToBeAddedID = intent.getStringExtra("groupToBeAddedID");
            Log.i(TAG, "present groupToBeAddedID: " + groupToBeAddedID);
        }
        else
        {
            groupToBeAddedID = null;
        }

        // insert tabs and current fragment in the main layout
        mainView.addView(getLayoutInflater().inflate(R.layout.skeleton_tab, null));
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(friends));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.groups));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, String.valueOf(tab.getPosition()));
                updateFab(tab.getPosition());
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // in the main we don't want an expansible bar
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(false);
        //todo: capire come bloccare la barra nel main

        // attach a listener on all the current user data
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                currentUser = new User();
                currentUser.setID(currentUID);
                currentUser.setName(dataSnapshot.child("name").getValue(String.class));
                currentUser.setSurname(dataSnapshot.child("surname").getValue(String.class));
                currentUser.setProfileImage(dataSnapshot.child("image").getValue().toString());
                currentUser.setEmail(dataSnapshot.child("email").getValue(String.class));
                // get user friends's IDs
                for(DataSnapshot friend : dataSnapshot.child("friends").getChildren()){
                    currentUser.getUserFriends().put(friend.getKey(),null);
                }
                // get user groups's IDs
                for(DataSnapshot group : dataSnapshot.child("groups").getChildren()){
                    currentUser.getUserGroups().put(group.getKey(), null);
                }
                //todo mettere altri dati in myself?

                // control if user that requires the friendship is already a friend
                if (inviterUID != null) {
                    if(!currentUser.getUserFriends().containsKey(inviterUID)){
                        currentUser.addFriend(inviterUID);
                        makeText(MainActivity.this, "Now you have a new friend!", Toast.LENGTH_LONG).show();
                    }
                    else
                        makeText(MainActivity.this, "You and "+currentUser.getUserFriends().get(inviterUID).getName()+" are already friends!", Toast.LENGTH_LONG).show();
                }

                // control if user is already part of requested group
                if (groupToBeAddedID  != null) {
                    if(!currentUser.getUserGroups().containsKey(groupToBeAddedID))
                    {
//                        currentUser.joinGroup(groupToBeAddedID); //todo usare questa? non aggiorna il numero dei membri
                        makeText(MainActivity.this, "Now you are part of the group!", Toast.LENGTH_LONG).show();
                    }
                    else
                        makeText(MainActivity.this, "You are already part of "+currentUser.getUserGroups().get(groupToBeAddedID).getName(), Toast.LENGTH_LONG).show();
                }

                // load nav menu header data for the current user
                loadNavHeader();
                Log.d(TAG, "logged user name: "+currentUser.getName());
                Log.d(TAG, "logged user surname: "+currentUser.getSurname());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: come gestire?
                Log.d(TAG, "getting current user failed");
            }
        });
    }

    private void updateFab(int position){
        switch(position){
            case 0:
                // friends fragment
                Log.d(TAG, "fab 0");
                fab.setImageResource(R.drawable.person_add);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "my ID is " + MainActivity.getCurrentUser().getID());
                        String deepLink = getString(R.string.invitation_deep_link) + "?inviterUID=" + MainActivity.getCurrentUser().getID();

                        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                                .setDeepLink(Uri.parse(deepLink))
                                .setMessage(getString(R.string.invitation_message))
                                //                     .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                                .setCallToActionText(getString(R.string.invitation_cta))
                                .build();

                        startActivityForResult(intent, MainActivity.REQUEST_INVITE);
                    }
                });
                break;
            case 1:
                // groups fragment
                Log.d(TAG, "fab 1");
                fab.setImageResource(R.drawable.group_add);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, NewGroupActivity.class);
                        myIntent.putExtra("userAdded", currentUser);//("UID", currentUID);
                        //String tempGroupID = mDatabase.child("temporarygroups").push().getKey();
                        //inizialmente l'unico user è il creatore del gruppo stesso
                  //      User myself = new User(myselfID, "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null, "€");
                        //mDatabase.child("temporarygroups").child(tempGroupID).child("members").push();
                        //mDatabase.child("temporarygroups").child(tempGroupID).child("members").child(myself.getID()).setValue(myself);
                  //      NewGroupActivity.newmembers.put(currentUID, currentUser);  //inizialmente l'unico membro del nuovo gruppo sono io
                        //myIntent.putExtra("groupID", tempGroupID);
                        MainActivity.this.startActivity(myIntent);
                    }
                });
                break;
            case 2:
                // pending fragment
                Log.d(TAG, "fab 2");
                fab.setImageResource(R.drawable.edit);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, ChooseGroupActivity.class);
                        myIntent.putExtra("userAdded", currentUser);//("UID", currentUID);
                        MainActivity.this.startActivity(myIntent);
                    }
                });
                //fab.setClickable(false);
                break;
        }
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "FriendsFragment":
                intent = new Intent(this, FriendDetailActivity.class);
                intent.putExtra("friendID", itemID);
                intent.putExtra("userID", currentUser.getID());
                startActivity(intent);
                break;

            case "GroupsFragment":
                intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupID", itemID);
                intent.putExtra("userID", currentUser.getID());
                startActivity(intent);
                break;

            case "PendingExpensesFragment":
                intent = new Intent(this, PendingExpenseDetailActivity.class);
                intent.putExtra("expenseID", itemID);
                intent.putExtra("userID", currentUser.getID());
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

                PopupMenu popup = new PopupMenu(MainActivity.this, v, Gravity.RIGHT);

                popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                one = popup.getMenu().findItem(R.id.one);
                one.setTitle("Remove Friend");
                popup.getMenu().findItem(R.id.two).setVisible(false);
                popup.getMenu().findItem(R.id.three).setVisible(false);

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popup.show();//showing popup menu


                break;

            case "GroupsFragment":

                popup = new PopupMenu(MainActivity.this, v, Gravity.RIGHT);

                popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                one = popup.getMenu().findItem(R.id.one);
                one.setTitle("Leave this Group");
                two = popup.getMenu().findItem(R.id.two);
                two.setTitle("Remove this Group");
                popup.getMenu().findItem(R.id.three).setVisible(false);


                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();

                        switch ((String) item.getTitle()) {

                            case "Leave this Group":

                                Integer returnValue = FirebaseUtils.getInstance().leaveGroupFirebase(currentUID, itemID);

                                if(returnValue == 0)
                                {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Hai un credito verso questo gruppo.\nAbbandonare comunque?", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                                else if(returnValue == 1)
                                {
                                    makeText(getApplicationContext(), "Hai un debito verso questo gruppo.\nSalda il debito per poter abbandonare.", Toast.LENGTH_LONG).show();
                                }
                                else if(returnValue == 2)
                                {
                                    makeText(getApplicationContext(), "Nessuno debito, abbandono in corso.", Toast.LENGTH_LONG).show();
                                }
                                else if(returnValue == null)
                                {
                                    makeText(getApplicationContext(), "Bilancio del gruppo: non disponibile adesso.\nRiprovare.", Toast.LENGTH_LONG).show();
                                }

                                break;

                            case "Remove this Group":
                                FirebaseUtils.getInstance().removeGroupFirebase(currentUID, itemID, getApplicationContext());
                                break;
                        }

                        return true;
                    }
                });

                popup.show();//showing popup menu

                break;
        }
    }


    // check if permissions on reading storage must be asked: true only if API >= 23
    public static boolean shouldAskPermission(){
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    // avoid to call setPersistenceEnabled on a database where it was already called ->
    // when an instance of FirebaseDatabase is needed call this method to retrieve it
    public static FirebaseDatabase getDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
//            firebaseDatabase.setPersistenceEnabled(true);
        }

        return firebaseDatabase;
    }

    // return the instance of the current user logged into the app
    public static User getCurrentUser() {
        return currentUser;
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == MainActivity.REQUEST_INVITE){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                makeText(getApplicationContext(), "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }
    */

    /* in this way calls to getIntent() will return the latest intent that was used to start this activity
     * rather than the first intent */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        // getting currentUID from Intent (from LoginSignUpActivity or EmailVerificationActivity)
        Intent i = getIntent();
        Bundle extras = i.getExtras();

        if(extras != null) {
            if (extras.containsKey("UID"))
                currentUID = extras.getString("UID");
            else if (currentUID == null) {
                auth.signOut();
                Intent intent = new Intent(getApplicationContext(), LoginSignUpActivity.class);
                startActivity(intent);
                finish();
            }

            //       currentUID = "-KjTCeDmpYY7gEOlYuSo"; // mario rossi, tenuto solo per debug, sostituire a riga precedente per vedere profilo con qualcosa
            Log.d(TAG, "currentID: " + currentUID);

            // getting invitation info if coming from LoginSignUpActivity after an Invitation
            if (extras.containsKey("inviterUID")) {
                inviterUID = extras.getString("inviterUID");
                Log.i(TAG, "present inviterUID: " + inviterUID);
            } else {
                inviterUID = null;
            }

            if (extras.containsKey("groupToBeAddedID")) {
                groupToBeAddedID = extras.getString("groupToBeAddedID");
                Log.i(TAG, "present groupToBeAddedID: " + groupToBeAddedID);
            } else {
                groupToBeAddedID = null;
            }
        }


        // getting reference to the user from db
        DatabaseReference currentUserRef = usersRef.child(currentUID);

        if (currentUserRef == null) {
            Log.e(TAG, "unable to retrieve logged user from db");

            Toast.makeText(MainActivity.this, "unable to retrieve logged user from db", Toast.LENGTH_LONG).show();

            // if the current user is not in the database do the logout and restart from login
            auth.signOut();
            currentUID = null;
            Intent intent = new Intent(getApplicationContext(), LoginSignUpActivity.class);
            if(progressDialog.isShowing())
                progressDialog.dismiss();
            startActivity(intent);
            finish();
        }


        // control if there is a request to join a group
        if (groupToBeAddedID != null) {
            if (inviterUID != null) {
                if (!currentUser.getUserGroups().containsKey(groupToBeAddedID)) {
                    currentUser.joinGroup(groupToBeAddedID, inviterUID);
                    Toast.makeText(MainActivity.this, "Now you are part of the group!", Toast.LENGTH_LONG).show();
                } else
                    Log.i(TAG,"You are already part of the group " + groupToBeAddedID);
            }
        }
        else
            // control if ther is a friend request
            if (inviterUID != null) {
                if(!currentUser.getUserFriends().containsKey(inviterUID)){
                    currentUser.addFriend(inviterUID);
                    Toast.makeText(MainActivity.this, "Now you have a new friend!", Toast.LENGTH_LONG).show();
                }
                else
                    Log.i(TAG, "You and "+inviterUID+" are already friends!");
            }

        // attach a listener on all the current user data
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");

                // in the main we don't want an expansible bar
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
                appBarLayout.setExpanded(false);
                //todo: capire come bloccare la barra nel main

                /*if(!progressDialog.isShowing())
                    progressDialog.show();*/

                currentUser = new User();
                currentUser.setID(currentUID);
                currentUser.setName(dataSnapshot.child("name").getValue(String.class));
                currentUser.setSurname(dataSnapshot.child("surname").getValue(String.class));
                currentUser.setProfileImage(dataSnapshot.child("image").getValue(String.class));
                currentUser.setEmail(dataSnapshot.child("email").getValue(String.class));
                currentUser.setPassword(dataSnapshot.child("password").getValue(String.class));
                currentUser.setUsername(dataSnapshot.child("username").getValue(String.class));
                // get user friends's IDs
                for(DataSnapshot friend : dataSnapshot.child("friends").getChildren()){
                    currentUser.getUserFriends().put(friend.getKey(),null);
                }
                // get user groups's IDs
                for(DataSnapshot group : dataSnapshot.child("groups").getChildren()){
                    currentUser.getUserGroups().put(group.getKey(), null);
                }
                //todo mettere altri dati in myself?

                // load nav menu header data for the current user
                loadNavHeader();
                Log.d(TAG, "logged user name: "+currentUser.getName());
                Log.d(TAG, "logged user surname: "+currentUser.getSurname());

                MainActivityPagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(1);
                updateFab(1);

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: come gestire?
                Log.d(TAG, "getting current user failed");

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }
}
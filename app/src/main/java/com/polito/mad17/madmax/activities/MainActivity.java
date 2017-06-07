package com.polito.mad17.madmax.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.expenses.ChooseGroupActivity;
import com.polito.mad17.madmax.activities.expenses.ExpenseDetailActivity;
import com.polito.mad17.madmax.activities.expenses.PendingExpenseDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.activities.login.LoginSignUpActivity;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import static android.widget.Toast.makeText;
import static com.polito.mad17.madmax.R.string.friends;


public class MainActivity extends BasicActivity implements OnItemClickInterface, OnItemLongClickInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    public static FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener; // to track whenever user signs in or out
    private DatabaseReference usersRef, currentUserRef;
    private DatabaseReference groupRef;
    FirebaseUser currentFirebaseUser;

    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private MenuItem one, two, three;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Integer currentFragment;
    MainActivityPagerAdapter adapter;

    public static final int REQUEST_INVITE = 0;
    public static final int REQUEST_INVITE_GROUP = 1;
    public static final int REQUEST_NOTIFICATION = 2;


    private static User currentUser = null;
    private static String currentUID;
    private String inviterID, groupToBeAddedID;

    private HashMap<String, String> userFriends = new HashMap<>();
    private HashMap<String, String> userGroups = new HashMap<>();

    private Intent startingIntent;
    private ValueEventListener currentUserListener;
    private ProgressDialog progressDialog;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        FirebaseUtils.getInstance().setUp();
        firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
        databaseReference = FirebaseUtils.getDatabaseReference();
        usersRef = databaseReference.child("users");
        groupRef = databaseReference.child("groups");
        auth = FirebaseAuth.getInstance();

        // get data from firebase invite, if present
        startingIntent = getIntent();
        Uri data = startingIntent.getData();
        if(data != null){
            inviterID = data.getQueryParameter("inviterID");
            groupToBeAddedID = data.getQueryParameter("groupToBeAddedID");
        }
        else{
            // nretrieving data from the intent inviterID & groupToBeAddedID as the group ID where to add the current user
            if(startingIntent.hasExtra("inviterID")) {
                // to be used to set the current user as friend of the inviter
                Log.d(TAG, "there is an invite");
                inviterID = startingIntent.getStringExtra("inviterID");
                startingIntent.removeExtra("inviterID");
            }
            else
                inviterID = null;

            if(startingIntent.hasExtra("groupToBeAddedID")) {
                groupToBeAddedID = startingIntent.getStringExtra("groupToBeAddedID");
                startingIntent.removeExtra("groupToBeAddedID");
            }
            else
                groupToBeAddedID = null;
        }

        currentFragment = startingIntent.getIntExtra("currentFragment", 1);


        if(auth.getCurrentUser() == null || !auth.getCurrentUser().isEmailVerified()){
            if (auth.getCurrentUser() != null) {
                auth.signOut();
            }

            Intent doLogin = new Intent(getApplicationContext(), LoginSignUpActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            if(inviterID!= null)
                doLogin.putExtra("inviterID", inviterID);
            if(groupToBeAddedID!= null)
                doLogin.putExtra("groupToBeAddedID", groupToBeAddedID);

            startActivity(doLogin);
            overridePendingTransition(0,0); //0 for no animation
            finish();
        }

        if (auth.getCurrentUser() != null) {
            currentUID = auth.getCurrentUser().getUid();
        }

        // in the main we don't want an expansible bar
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(false);
        //todo: capire come bloccare la barra nel main


        // insert tabs and current fragment in the main layout
        mainView.addView(getLayoutInflater().inflate(R.layout.skeleton_tab, null));
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(friends));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.groups));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
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


        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        adapter = new MainActivityPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        startingIntent = getIntent();
        currentFragment = startingIntent.getIntExtra("currentFragment", 1);

        // start declaration of a listener on all the current user data -> attached in onStart()
        currentUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange currentUserref");

                if(currentUser == null) {
                    //makeText(MainActivity.this, "Ricreato user", Toast.LENGTH_SHORT).show(); // todo: di debug, da rimuovere
                    currentUser = new User();
                }

                currentUser.setID(currentUID);
                currentUser.setName(dataSnapshot.child("name").getValue(String.class));
                currentUser.setSurname(dataSnapshot.child("surname").getValue(String.class));
                currentUser.setUsername(dataSnapshot.child("username").getValue(String.class));
                currentUser.setProfileImage(dataSnapshot.child("image").getValue(String.class));
                currentUser.setEmail(dataSnapshot.child("email").getValue(String.class));

                Log.d(TAG, "taken basic data of currentUser " +  currentUser.toString());
                // get user friends's IDs
                for(DataSnapshot friend : dataSnapshot.child("friends").getChildren()){
                    currentUser.getUserFriends().put(friend.getKey(),null);
                }
                // get user groups's IDs
                for(DataSnapshot group : dataSnapshot.child("groups").getChildren()){
                    currentUser.getUserGroups().put(group.getKey(), null);
                }
                //todo mettere altri dati in myself?

                Log.d(TAG, "Taken friends and groups, now creating the adapter");

                if (currentFragment != null)
                {
                    viewPager.setCurrentItem(currentFragment);
                    updateFab(currentFragment);
                }
                else
                {
                    viewPager.setCurrentItem(1);
                    updateFab(1);
                }

                // load nav menu header data for the current user
                loadNavHeader();
                Log.d(TAG, "logged user name: "+currentUser.getName());
                Log.d(TAG, "logged user surname: "+currentUser.getSurname());

                Uri data = startingIntent.getData();
                if(data != null){
                    inviterID = data.getQueryParameter("inviterID");
                    groupToBeAddedID = data.getQueryParameter("groupToBeAddedID");
                }
                else{
                    // retrieving data from the intent inviterID & groupToBeAddedID as the group ID where to add the current user
                    if(startingIntent.hasExtra("inviterID")) {
                        // to be used to set the current user as friend of the inviter
                        Log.d(TAG, "there is an invite");
                        inviterID = startingIntent.getStringExtra("inviterID");
                        startingIntent.removeExtra("inviterID");
                    }

                    if(startingIntent.hasExtra("groupToBeAddedID")) {
                        groupToBeAddedID = startingIntent.getStringExtra("groupToBeAddedID");
                        startingIntent.removeExtra("groupToBeAddedID");
                    }
                }

                // control if user that requires the friendship is already a friend
                if (inviterID != null) {
                    if(!currentUser.getUserFriends().containsKey(inviterID)){
                        FirebaseUtils.getInstance().addFriend(inviterID);
                        inviterID = null;
                        makeText(MainActivity.this, getString(R.string.new_friend), Toast.LENGTH_LONG).show();
                    }
                    else
                        makeText(MainActivity.this, getString(R.string.already_friends), Toast.LENGTH_LONG).show();
                }

                // control if user is already part of requested group
                if (groupToBeAddedID  != null) {
                    if(!currentUser.getUserGroups().containsKey(groupToBeAddedID))
                    {
//                        currentUser.joinGroup(groupToBeAddedID); //todo usare questa? non aggiorna il numero dei membri
                        currentUser.getUserGroups().put(groupToBeAddedID, null);
                        FirebaseUtils.getInstance().joinGroupFirebase(currentUID, groupToBeAddedID);
                        groupToBeAddedID = null;
                        makeText(MainActivity.this, getString(R.string.join_group), Toast.LENGTH_LONG).show();
                    }
                    else
                        makeText(MainActivity.this, getString(R.string.already_in_group) + "\""+currentUser.getUserGroups().get(groupToBeAddedID).getName()+"\"", Toast.LENGTH_LONG).show();
                }

                if(startingIntent.hasExtra("notificationTitle")){
                    Intent notificationIntent = null;
                    switch(startingIntent.getStringExtra("notificationTitle")){
                        case "notification_invite":
                            if(startingIntent.hasExtra("groupID")){
                                notificationIntent = new Intent(getApplicationContext(), GroupDetailActivity.class);
                                notificationIntent.putExtra("groupID", startingIntent.getStringExtra("groupID"));
                            }
                            break;
                        case "notification_expense_added":
                            if(startingIntent.hasExtra("groupID")){
                                notificationIntent = new Intent(getApplicationContext(), ExpenseDetailActivity.class);
                                notificationIntent.putExtra("groupID", startingIntent.getStringExtra("groupID"));
                                if(startingIntent.hasExtra("expenseID")){
                                    notificationIntent.putExtra("expenseID", startingIntent.getStringExtra("expenseID"));
                                }
                            }
                            break;
                        case "notification_expense_removed":
                            if(startingIntent.hasExtra("groupID")){
                                notificationIntent = new Intent(getApplicationContext(), GroupDetailActivity.class);
                                notificationIntent.putExtra("groupID", startingIntent.getStringExtra("groupID"));
                            }
                            break;
                        case "notification_proposalExpense_added":
                            if(startingIntent.hasExtra("expenseID")){
                                notificationIntent = new Intent(getApplicationContext(), PendingExpenseDetailActivity.class);
                                notificationIntent.putExtra("expenseID", startingIntent.getStringExtra("expenseID"));
                            }
                            break;
                    }
                    if(notificationIntent != null){
                        notificationIntent.putExtra("userID", currentUID);
                        startingIntent.removeExtra("notificationTitle");
                        startActivityForResult(notificationIntent, REQUEST_NOTIFICATION);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: come gestire?
                Log.d(TAG, "getting current user failed");
            }
        };
        // end of listener declaration on all the current user data

         authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged");

                currentFirebaseUser = firebaseAuth.getCurrentUser();
                if(currentFirebaseUser != null && currentFirebaseUser.isEmailVerified()){
                    // getting reference to the user from db
                    currentUID = currentFirebaseUser.getUid();
                    currentUserRef = usersRef.child(currentUID);

                    //take refreshed toked and save it to use FCM
                    currentUserRef.child("token").setValue(FirebaseInstanceId.getInstance().getToken());
                    Log.d(TAG, "device token: "+FirebaseInstanceId.getInstance().getToken());
                    // attach a listener on all the current user data
                    currentUserRef.addValueEventListener(currentUserListener);
                }
                else{
                    Log.d(TAG, "current user is null, so go to login activity");
                    Intent goToLogin = new Intent(getApplicationContext(), LoginSignUpActivity.class);
                   // currentUser = null;
                    startActivity(goToLogin);
                    auth.removeAuthStateListener(authListener);
                    finish();
                }
            }
        };

        // attach the listener to the FirebaseAuth instance
        auth.addAuthStateListener(authListener);
    }

    private void updateFab(int position){
        switch(position){
            case 0:
                // friends fragment
                Log.d(TAG, "fab 0");
                fab.setImageResource(R.drawable.user_add);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "my ID is " + MainActivity.getCurrentUser().getID());
                        String deepLink = getString(R.string.invitation_deep_link) + "?inviterID=" + MainActivity.getCurrentUser().getID();

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
                        myIntent.putExtra("userAdded", currentUser);
                        MainActivity.this.startActivity(myIntent);
                    }
                });
                break;
            case 2:
                // pending fragment
                Log.d(TAG, "fab 2");
                fab.setImageResource(android.R.drawable.ic_input_add);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, ChooseGroupActivity.class);
                        myIntent.putExtra("userAdded", currentUser);//("UID", currentUID);
                        Log.d (TAG, "Sto per aprire ChooseGroupActivity");
                        MainActivity.this.startActivity(myIntent);
                    }
                });
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
           //     intent.putExtra("userID", currentUser.getID());
                Log.d (TAG, "Sto per aprire il dettaglio");
                startActivity(intent);
                break;
        }

    }

    //Apro popup menu quando ho tenuto premuto un friend o gruppo per 1 secondo
    @Override
    public void itemLongClicked(String fragmentName, final String itemID, final View v) {

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

                        FirebaseUtils.getInstance().removeFromFriends(currentUID, itemID);
                        Toast.makeText(MainActivity.this, getString(R.string.friend_removed) + item.getTitle(),Toast.LENGTH_SHORT).show();
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
                                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.credit_group), Toast.LENGTH_LONG);
                                    toast.show();
                                }
                                else if(returnValue == 1)
                                {
                                    makeText(getApplicationContext(), getString(R.string.debit_group), Toast.LENGTH_LONG).show();
                                }
                                else if(returnValue == 2)
                                {
                                    makeText(getApplicationContext(), getString(R.string.leaving), Toast.LENGTH_LONG).show();
                                }
                                else if(returnValue == null)
                                {
                                    makeText(getApplicationContext(), getString(R.string.balance_not_available), Toast.LENGTH_LONG).show();
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
            case "PendingExpensesFragment":
                popup = new PopupMenu(MainActivity.this, v, Gravity.RIGHT);
                popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                one = popup.getMenu().findItem(R.id.one);
                one.setTitle("Remove Pending");
                popup.getMenu().findItem(R.id.two).setVisible(false);
                popup.getMenu().findItem(R.id.three).setVisible(false);
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        databaseReference.child("proposedExpenses").child(itemID).child("creatorID").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(MainActivity.getCurrentUser().getID().matches(dataSnapshot.getValue(String.class))) {
                                    FirebaseUtils.getInstance().removePendingExpenseFirebase(itemID, getApplicationContext());
                                    // add event for PENDING_EXPENSE_REMOVE
                                    databaseReference.child("proposedExpenses").child(itemID)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    User currentUser = MainActivity.getCurrentUser();
                                                                                    Event event = new Event(
                                                                                            dataSnapshot.child("groupID").getValue(String.class),
                                                                                            Event.EventType.PENDING_EXPENSE_REMOVE,
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
                                }
                                else
                                    Toast.makeText(MainActivity.this,getString(R.string.not_creator),Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                        return  true;
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

    // return the instance of the current user logged into the app
    public static User getCurrentUser() {
        return currentUser;
    }

    // return the instance of the current user logged into the app
    public static String getCurrentUID() {
        return currentUID ;
    }

    /* in this way calls to getIntent() will return the latest intent that was used to start this activity
     * rather than the first intent */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

        if(authListener != null && currentUserRef!= null && currentUserListener != null){
            currentUserRef.removeEventListener(currentUserListener);
            auth.removeAuthStateListener(authListener);
        }
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
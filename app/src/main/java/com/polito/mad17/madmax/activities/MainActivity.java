package com.polito.mad17.madmax.activities;

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
    private String currentUID, inviterUID, groupToBeAddedID;

    private HashMap<String, String> userFriends = new HashMap<>();
    private HashMap<String, String> userGroups = new HashMap<>();

    private Intent startingIntent;
    private ValueEventListener currentUserListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        FirebaseUtils.getInstance().setUp();
        firebaseDatabase = getDatabase();
        databaseReference = firebaseDatabase.getReference();
        usersRef = databaseReference.child("users");
        groupRef = databaseReference.child("groups");
        auth = FirebaseAuth.getInstance();

        if((auth.getCurrentUser() == null) ||(!auth.getCurrentUser().isEmailVerified())){
            if (auth.getCurrentUser() != null) {
                auth.signOut();
            }

            startActivity(new Intent(getApplicationContext(), LoginSignUpActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            overridePendingTransition(0,0); //0 for no animation
            finish();
        }
        // getting currentUID from Intent (from LoginSignUpActivity or EmailVerificationActivity)
        startingIntent = getIntent();

        //todo a cosa serve?
        currentFragment = startingIntent.getIntExtra("currentFragment", 1);

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
                    currentUser.setID(currentUID);
                }
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

                adapter = new MainActivityPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

                viewPager.setAdapter(adapter);
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

                // retrieving data from the intent inviterUID & groupToBeAddedID as the group ID where to add the current user
                if(startingIntent.hasExtra("inviterUID")) {
                    // to be used to set the current user as friend of the inviter
                    Log.d(TAG, "there is an invite");
                    inviterUID = startingIntent.getStringExtra("inviterUID");
                    startingIntent.removeExtra("inviterUID");
                    if(startingIntent.hasExtra("groupToBeAddedID")) {
                        groupToBeAddedID = startingIntent.getStringExtra("groupToBeAddedID");
                        startingIntent.removeExtra("groupToBeAddedID");
                    }
                    else {
                        inviterUID = null;
                        groupToBeAddedID = null;
                        Log.d(TAG, "there is not an invite");
                    }
                }
                else {
                    inviterUID = null;
                    groupToBeAddedID = null;
                    Log.d(TAG, "there is not an invite");
                }
                // control if user that requires the friendship is already a friend
                if (inviterUID != null) {
                    if(!currentUser.getUserFriends().containsKey(inviterUID)){
                        currentUser.addFriend(inviterUID);
                        makeText(MainActivity.this, "Now you have a new friend!", Toast.LENGTH_LONG).show();
                    }
                    else
                        makeText(MainActivity.this, "You and inviter are already friends!", Toast.LENGTH_LONG).show();
                }

                // control if user is already part of requested group
                if (groupToBeAddedID  != null) {
                    if(!currentUser.getUserGroups().containsKey(groupToBeAddedID))
                    {
//                        currentUser.joinGroup(groupToBeAddedID); //todo usare questa? non aggiorna il numero dei membri
                        currentUser.getUserGroups().put(groupToBeAddedID, null);
                        FirebaseUtils.getInstance().joinGroupFirebase(currentUID, groupToBeAddedID);
                        makeText(MainActivity.this, "Now you are part of the group!", Toast.LENGTH_LONG).show();
                    }
                    else
                        makeText(MainActivity.this, "You are already part of "+currentUser.getUserGroups().get(groupToBeAddedID).getName(), Toast.LENGTH_LONG).show();
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
                if(currentFirebaseUser != null){
                    // getting reference to the user from db
                    currentUID = currentFirebaseUser.getUid();
                    currentUserRef = usersRef.child(currentUID);

                    //take refreshed toked and save it to use FCM
                    currentUserRef.child("token").setValue(FirebaseInstanceId.getInstance().getToken());

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
                intent.putExtra("userID", currentUser.getID());
                Log.d (TAG, "Sto per aprire il dettaglio");
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
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

        if(authListener != null){
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
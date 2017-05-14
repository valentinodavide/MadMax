package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.expenses.PendingExpensesFragment;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupsFragment;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.activities.login.LogInActivity;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.activities.users.FriendsFragment;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.polito.mad17.madmax.R.string.friends;

public class MainActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private FirebaseAuth auth;
    private static final int REQUEST_INVITE = 0;

    private static User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        final String currentUID, inviterUID, groupToBeAddedID;
        final boolean alreadyFriends, alreadyInGroup;
        String[] drawerOptions;
        //DrawerLayout drawerLayout;
        ListView drawerList;

        getDatabase();
        databaseReference = firebaseDatabase.getReference();
        auth = FirebaseAuth.getInstance();


        // getting currentUID from Intent (from LogInActivity or EmailVerificationActivity)
        Intent i = getIntent();
        currentUID = i.getStringExtra("UID");

        // getting invitation info if coming from LogInActivity after an Invitation
        if (i.hasExtra("inviterUID")) {
            inviterUID = i.getStringExtra("inviterUID");
        }
        else {
            inviterUID = null;
        }

        /*
        if (i.hasExtra("groupToBeAddedID")) {
            groupToBeAddedID = i.getStringExtra("groupToBeAddedID");
        }
        else {
            groupToBeAddedID = null;
        }*/


        final DatabaseReference usersRef = databaseReference.child("users");
        final DatabaseReference groupsRef = databaseReference.child("groups");

        // getting currentUserRef from db
        DatabaseReference currentUserRef = usersRef.child(currentUID);
        if (currentUserRef == null) {
            Log.e(TAG, "unable to retrieve logged user from db");

            Toast.makeText(MainActivity.this, "unable to retrieve logged user from db", Toast.LENGTH_LONG).show();
            return;
        }

        // creating an object for current user
        currentUser = new User(
            currentUserRef.getKey(),
            currentUserRef.child("username").toString(),
            currentUserRef.child("name").toString(),
            currentUserRef.child("surname").toString(),
            currentUserRef.child("email").toString(),
            currentUserRef.child("password").toString(),
            currentUserRef.child("profileImage").toString(),
            currentUserRef.child("defaultCurrency").toString()
        );


        final HashMap<String, Double> balanceWithUsers = currentUser.getBalanceWithUsers();
        final HashMap<String, User> userFriends = currentUser.getUserFriends();

        // retrieving friends of the logged user from db
        usersRef.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot friendsSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot friendSnapshot : friendsSnapshot.getChildren()) {
                    // populate balanceWithUsers for the logged User
                    String friendID = friendSnapshot.getKey();
                    DatabaseReference userFriendRef = friendSnapshot.getRef();

                    balanceWithUsers.put(
                            friendID,
                            Double.parseDouble(userFriendRef.child("balanceWithUser").toString())
                    );

                    // populate userFriends of the logged User
                    DatabaseReference friendRef = usersRef.child(friendID);
                    User friend = new User(
                            friendID,
                            friendRef.child("username").toString(),
                            friendRef.child("name").toString(),
                            friendRef.child("surname").toString(),
                            friendRef.child("email").toString(),
                            friendRef.child("profileImage").toString()
                    );

                    userFriends.put(
                            friendID,
                            friend
                    );

                    // todo siamo sicuri di volere shared_groups? sono da recuperare qui da db
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        final HashMap<String, Double> balanceWithGroups = currentUser.getBalanceWithGroups();
        final HashMap<String, Group> userGroups = currentUser.getUserGroups();

        // retrieving groups of the logged user from db
        usersRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot groupsSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    // populate balanceWithUsers for the logged User
                    String groupID = groupSnapshot.getKey();
                    DatabaseReference userGroupRef = groupSnapshot.getRef();

                    balanceWithGroups.put(
                            groupID,
                            Double.parseDouble(userGroupRef.child("balanceWithGroup").toString())
                    );

                    // populate userGroups of the logged User
                    DatabaseReference groupRef = groupsRef.child(groupID);
                    Group group = new Group(
                            groupID,
                            groupRef.child("name").toString(),
                            groupRef.child("image").toString(),
                            groupRef.child("description").toString(),
                            Integer.parseInt(groupRef.child("numberMembers").toString())
                    );

                    // todo qui ci sono ancora da recuperare le spese e i membri del gruppo corrente

                    userGroups.put(
                            groupID,
                            group
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        if (inviterUID != null) {
            alreadyFriends = userFriends.containsKey(inviterUID);

            if (!alreadyFriends) {
                currentUser.addFriend(inviterUID);
            }
        }

        /*
        if (groupToBeAddedID != null) {
            alreadyInGroup = userFriends.containsKey(groupToBeAddedID);

            if (!alreadyInGroup) {
                currentUser.joinGroup(groupToBeAddedID);
            }
        }*/


        drawerOptions = getResources().getStringArray(R.array.drawerItem);
        //drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList = (ListView)findViewById(R.id.left_drawer);

        // set the adapter for the Listview
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerOptions));

        // set the click's listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    Toast.makeText(MainActivity.this, "Logout selected", Toast.LENGTH_SHORT).show();
                    auth.signOut();

                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(position == 0){
                    Log.d(TAG, "my ID is " + currentUser.getID());
                    String deepLink = getString(R.string.invitation_deep_link) + "?inviterUID=" + currentUser.getID();

                    Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                            .setDeepLink(Uri.parse(deepLink))
                            .setMessage(getString(R.string.invitation_message))
     //                     .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                            .setCallToActionText(getString(R.string.invitation_cta))
                            .build();

                    startActivityForResult(intent, REQUEST_INVITE);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(friends));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.groups));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setCurrentItem(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, tab.getPosition() + "");
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo diverse azioni a seconda del fragment in cui mi trovo
                // getSupportFragmentManager().findFragmentByTag()
                Intent myIntent = new Intent(MainActivity.this, NewGroupActivity.class);
                myIntent.putExtra("UID", currentUser.getID());
                //String tempGroupID = mDatabase.child("temporarygroups").push().getKey();
                //inizialmente l'unico user è il creatore del gruppo stesso
                //User myself = new User(myselfID, "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null, "€");
                //mDatabase.child("temporarygroups").child(tempGroupID).child("members").push();
                //mDatabase.child("temporarygroups").child(tempGroupID).child("members").child(myself.getID()).setValue(myself);
                NewGroupActivity.newmembers.put(currentUser.getID(), currentUser);  //inizialmente l'unico membro del nuovo gruppo sono io
                //myIntent.putExtra("groupID", tempGroupID);
                MainActivity.this.startActivity(myIntent);
            }
        });



/*
        // Create an auto-managed GoogleApiClient with access to App Invites.
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e(TAG, connectionResult.toString());
                    }
                })
                .build();


        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);

                                    Log.d(TAG, "deepLink: " + deepLink);
                                    Log.d(TAG, "invitationId: " + invitationId);

                                    // Because autoLaunchDeepLink = true we don't have to do anything
                                    // here, but we could set that to false and manually choose
                                    // an Activity to launch to handle the deep link here.
                                    // ...
                                }
                            }
                        });

*/


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == REQUEST_INVITE){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);

                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }
            }
            else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                Toast.makeText(MainActivity.this, "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class PagerAdapter extends FragmentPagerAdapter {
        int numberOfTabs;

        FriendsFragment friendsFragment = null;
        GroupsFragment groupsFragment = null;
        PendingExpensesFragment pendingExpensesFragment = null;

        private PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {

            switch(position) {
                case 0:
                    Log.i(TAG, "here in case 0: FriendsFragment");
                    friendsFragment = new FriendsFragment();
                    return friendsFragment;
                case 1:
                    Log.i(TAG, "here in case 1: GroupsFragment");
                    groupsFragment = new GroupsFragment();
                    return groupsFragment;
                case 2:
                    Log.i(TAG, "here in case 2: PendingExpensesFragment");
                    pendingExpensesFragment = new PendingExpensesFragment();
                    return pendingExpensesFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return super.isViewFromObject(view, object);
        }
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {
        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        //FragmentManager fragmentManager = getSupportFragmentManager();
        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Intent intent;
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
        }
    }


    public String addExpenseFirebase(Expense expense) {
        //Aggiungo spesa a Firebase
        String eID = databaseReference.child("expenses").push().getKey();
        databaseReference.child("expenses").child(eID).setValue(expense);
        String timeStamp = SimpleDateFormat.getDateTimeInstance().toString();
        databaseReference.child("expenses").child(eID).child("timestamp").setValue(timeStamp);

        //Per ogni participant setto la quota che ha già pagato per questa spesa
        //e aggiungo spesa alla lista spese di ogni participant
        for (Map.Entry<String, Double> participant : expense.getParticipants().entrySet()) {
            //Se il participant corrente è il creatore della spesa
            if (participant.getKey().equals(expense.getCreatorID())) {
                //paga tutto lui
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(expense.getAmount());
            }
            else {
                //gli altri participant inizialmente non pagano niente
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(0);
            }

            //risetto fraction di spesa che deve pagare l'utente, visto che prima si sputtana
            databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("fraction").setValue(expense.getParticipants().get(participant.getKey()));

            //Aggiungo spesaID a elenco spese dello user
            //todo controllare se utile
            databaseReference.child("users").child(participant.getKey()).child("expenses").child(eID).setValue("true");
        }

        //Aggiungo spesa alla lista spese del gruppo
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").push();
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue("true");

        return eID;

        //u.updateBalance(expense);
        //updateBalanceFirebase(u, expense);
    }

    // update balance among other users and among the group this user is part of
    private void updateBalanceFirebase (final User u, final Expense expense) {
        // todo per ora fa il calcolo come se le spese fossero sempre equamente divise fra tutti i
        // todo     membri del gruppo (cioè come se expense.equallyDivided fosse sempre = true

        final String groupID = expense.getGroupID();

        Query query = databaseReference.child("groups").child(groupID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double amount = 0d; //spesa totale nel gruppo

                DataSnapshot groupSnapshot = dataSnapshot.child("groups").child(groupID);

                for (DataSnapshot expense : groupSnapshot.child("expenses").getChildren()) {
                    amount += expense.child("amount").getValue(Double.class);
                }

                Long membersCount = groupSnapshot.child("members").getChildrenCount();
                Double singlecredit = expense.getAmount() / membersCount;
                Double totalcredit = singlecredit * (membersCount -1); //credito totale che io ho verso tutti gli altri membri del gruppo
                //debito attuale dello user verso il gruppo
                Double actualdebts =  dataSnapshot.child("users").child(u.getID()).child("groups").child(groupID).child("balanceWithGroup").getValue(Double.class);

                if (actualdebts != null) {
                    //aggiorno il mio debito verso il gruppo
                    databaseReference.child("users").child(u.getID()).child("groups").child(groupID).child("balanceWithGroup").setValue(actualdebts+totalcredit);
                }
                else {
                    System.out.println("Group not found");
                }

                for (DataSnapshot member : groupSnapshot.child("members").getChildren()) {
                    //se non sono io stesso
                    if (!member.getKey().equals(u.getID())) {
                        Double balance = dataSnapshot.child("users").child(u.getID()).child("balancesWithUsers").child(member.getKey()).getValue(Double.class);
                        if (balance != null) {
                            databaseReference.child("users").child(u.getID()).child("balancesWithUsers").child(member.getKey()).setValue(balance+singlecredit);
                        }
                    }

                    //aggiorno debito dell'amico verso di me

                    //debito dell'amico verso di me
                    Double balance = dataSnapshot.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).getValue(Double.class);

                    if (balance != null) {
                        databaseReference.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).setValue(balance-singlecredit);

                    }
                    else {
                        System.out.println("Io non risulto tra i suoi debiti");
                        // => allora devo aggiungermi
                        databaseReference.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).setValue(-singlecredit);
                    }

                    //aggiorno il debito dell'amico verso il gruppo
                    balance = dataSnapshot.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").getValue(Double.class);

                    if (balance != null) {
                        databaseReference.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").setValue(balance-singlecredit);
                    }
                    else {
                        System.out.println("Gruppo non risulta tra i suoi debiti");
                        // => allora lo devo aggiungere
                        databaseReference.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").setValue(-singlecredit);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });


        /*
        Double total = g.getTotalExpense(); //spesa totale del gruppo aggiornata
        Double singlecredit = expense.getAmount() / g.getMembers().size();   //credito che io ho verso ogni singolo utente in virtù della spesa che ho fatto
        Double totalcredit = singlecredit * (g.getMembers().size() -1); //credito totale che io ho verso tutti gli altri membri del gruppo
        //es. se in un gruppo di 5 persone io ho pagato 10, ognuno mi deve 2
        //quindi totalcredit = 2*4 dove 4 è il n. di membri del gruppo diversi da me. In tutto devo ricevere 8.

        Double actualdebts = balanceWithGroups.get(g.getID());
        if (actualdebts != null) {
            //aggiorno il mio debito verso il gruppo
            balanceWithGroups.put(g.getID(), actualdebts + totalcredit);
        }
        else {
            System.out.println("Group not found");
        }

        //per ogni amico del gruppo in cui è stata aggiunta la spesa
        for (HashMap.Entry<String, User> friend : g.getMembers().entrySet()) {
            //se non sono io stesso
            if (!friend.getKey().equals(this.getID())) {
                //aggiorno mio credito verso di lui
                Double balance = balanceWithUsers.get(friend.getKey());
                if (balance != null) {
                    balanceWithUsers.put(friend.getKey(), balance+singlecredit);
                }
                else {
                    System.out.println("Friend not found");
                }

                //aggiorno debito dell'amico verso di me
                HashMap<String, Double> friendBalanceWithUsers = friend.getValue().getBalanceWithUsers();
                balance = friendBalanceWithUsers.get(this.getID());;

                if (balance != null) {
                    friend.getValue().getBalanceWithUsers().put(this.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Io non risulto tra i suoi debiti");
                    // => allora devo aggiungermi
                    friendBalanceWithUsers.put(this.getID(), -singlecredit);
                }

                //aggiorno debito dell'amico verso il gruppo
                HashMap<String, Double> friendBalanceWithGroups = friend.getValue().getBalanceWithGroups();
                balance = friendBalanceWithGroups.get(g.getID());
                if (balance != null) {
                    friend.getValue().getBalanceWithGroups().put(g.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Gruppo non risulta tra i suoi debiti");
                    // => allora lo devo aggiungere
                    friendBalanceWithGroups.put(g.getID(), -singlecredit);
                }
            }
        }

    }
    */
    /*
    //ritorna i soldi totali spesi dal gruppo (packake-private: visibilità di default)
    Double getTotalExpenseFirebase (String groupID) {

        Query query =  mDatabase.child("groups").child(groupID).child("expenses");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double total = 0d;

                for (DataSnapshot expenseSnapshot: dataSnapshot.getChildren())
                {
                    total += expenseSnapshot.child("amount").getValue(Double.class);
                }
                return total;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        for (HashMap.Entry<String, Expense> expense : expenses.entrySet()) {
            total += expense.getValue().getAmount();
        }

        return total;
    }
    }
    */


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
            firebaseDatabase.setPersistenceEnabled(true);
        }

        return firebaseDatabase;
    }

    // return the instance of the current user logged into the app
    public static User getCurrentUser() {
        return currentUser;
    }
}

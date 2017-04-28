package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
 //   private ActionBarDrawerToggle drawerToggle;
    private FirebaseAuth auth;
    private static final int REQUEST_INVITE = 0;

    public static HashMap<String, Group> groups = new HashMap<>();
    public static HashMap<String, User> users = new HashMap<>();
    public static User myself;

    Integer[] imgid={
            R.drawable.ale,
            R.drawable.davide,
            R.drawable.chiara,
            R.drawable.riki,
            R.drawable.rossella,
            R.drawable.vacanze,
            R.drawable.calcetto,
            R.drawable.casa,
            R.drawable.pasquetta,
            R.drawable.fantacalcio,
            R.drawable.alcolisti
    };

    Integer[] img_expense={
            R.drawable.expense1,
            R.drawable.expense2,
            R.drawable.expense3,
            R.drawable.expense4,
            R.drawable.expense5,
            R.drawable.expense6,
            R.drawable.expense7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (users.isEmpty())
        {
            //Create users
            User u0 = new User(String.valueOf(0), "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null);
            User u1 = new User(String.valueOf(1), "Alero3",         "Alessandro", "Rota",       "email1@email.it", "password1", String.valueOf(imgid[0]));
            User u2 = new User(String.valueOf(2), "deviz92",        "Davide", "Valentino",      "email2@email.it", "password2", String.valueOf(imgid[1]));
            User u3 = new User(String.valueOf(3), "missArmstrong",  "Chiara", "Di Nardo",       "email3@email.it", "password3", String.valueOf(imgid[2]));
            User u4 = new User(String.valueOf(4), "rickydivi",      "Riccardo", "Di Vittorio",  "email4@email.it", "password4", String.valueOf(imgid[3]));
            User u5 = new User(String.valueOf(5), "roxy",           "Rossella", "Mangiardi",    "email5@email.it", "password5", String.valueOf(imgid[4]));

            //Add users to database
//            mDatabase.child("users").child(u0.getID()).setValue(u0);
//            mDatabase.child("users").child(u1.getID()).setValue(u1);
//            mDatabase.child("users").child(u2.getID()).setValue(u2);
//            mDatabase.child("users").child(u3.getID()).setValue(u3);
//            mDatabase.child("users").child(u4.getID()).setValue(u4);
//            mDatabase.child("users").child(u5.getID()).setValue(u5);

            //Add to users list (needed to share data with other activities)
            users.put(u0.getID(), u0);
            users.put(u1.getID(), u1);
            users.put(u2.getID(), u2);
            users.put(u3.getID(), u3);
            users.put(u4.getID(), u4);
            users.put(u4.getID(), u5);

            Group g1 = new Group(String.valueOf(0), "Vacanze",      String.valueOf(imgid[5]), "description0");
            Group g2 = new Group(String.valueOf(1), "Calcetto",     String.valueOf(imgid[6]), "description1");
            Group g3 = new Group(String.valueOf(2), "Spese Casa",   String.valueOf(imgid[7]), "description2");
            Group g4 = new Group(String.valueOf(3), "Pasquetta",   String.valueOf(imgid[8]), "description3");
            Group g5 = new Group(String.valueOf(4), "Fantacalcio",   String.valueOf(imgid[9]), "description4");
            Group g6 = new Group(String.valueOf(5), "Alcolisti Anonimi",   String.valueOf(imgid[10]), "description5");

            //Add groups to database
//            mDatabase.child("groups").child(g1.getID()).setValue(g1);
//            mDatabase.child("groups").child(g2.getID()).setValue(g2);
//            mDatabase.child("groups").child(g3.getID()).setValue(g3);
//            mDatabase.child("groups").child(g4.getID()).setValue(g4);
//            mDatabase.child("groups").child(g5.getID()).setValue(g5);
//            mDatabase.child("groups").child(g6.getID()).setValue(g6);

            //Aggiungo utente a lista membri del gruppo e gruppo a lista gruppi nell'utente in Firebase
//            joinGroupFirebase(u0,g1);
//            joinGroupFirebase(u1,g1);
//            joinGroupFirebase(u2,g1);
//            joinGroupFirebase(u3,g1);
//            joinGroupFirebase(u4,g1);
//            joinGroupFirebase(u5,g1);
//
//
//            joinGroupFirebase(u0,g2);
//            joinGroupFirebase(u1,g2);
//            joinGroupFirebase(u2,g2);
//            joinGroupFirebase(u4,g2);
//
//            joinGroupFirebase(u0,g3);
//            joinGroupFirebase(u4,g3);
//
//            joinGroupFirebase(u0,g4);
//            joinGroupFirebase(u2,g4);
//
//            joinGroupFirebase(u0,g5);
//            joinGroupFirebase(u2,g5);
//
//            joinGroupFirebase(u0,g6);
//            joinGroupFirebase(u2,g6);


            //Add users to group
            u0.joinGroup(g1);
            u1.joinGroup(g1);
            u2.joinGroup(g1);
            u3.joinGroup(g1);
            u4.joinGroup(g1);
            u5.joinGroup(g1);

            u0.joinGroup(g2);
            u1.joinGroup(g2);
            u2.joinGroup(g2);
            u4.joinGroup(g2);

            u0.joinGroup(g3);
            u4.joinGroup(g3);

            u0.joinGroup(g4);
            u2.joinGroup(g4);

            u0.joinGroup(g5);
            u2.joinGroup(g5);

            u0.joinGroup(g6);
            u2.joinGroup(g6);

            groups.put(g1.getID(), g1);
            groups.put(g2.getID(), g2);
            groups.put(g3.getID(), g3);
            groups.put(g4.getID(), g4);
            groups.put(g5.getID(), g5);
            groups.put(g6.getID(), g6);

            //Spese in g1
            Expense e1 = new Expense(String.valueOf(0), "Nutella", "Cibo",          30d, "€",   String.valueOf(img_expense[0]), true, g1.getID());
            Expense e2 = new Expense(String.valueOf(1), "Spese cucina", "Altro",    20d, "€",   String.valueOf(img_expense[1]), true, g1.getID());
            //u0.addExpense(e1);
            //u3.addExpense(e2);
//            addExpenseFirebase(u0,e1);
//            addExpenseFirebase(u3,e2);

            //Spese in g2
            Expense e3 = new Expense(String.valueOf(2), "Partita", "Sport",         5d, "€",    String.valueOf(img_expense[2]), true, g2.getID());
            //u0.addExpense(e3);
//            addExpenseFirebase(u0,e3);

            //Spese in g3
            Expense e4 = new Expense(String.valueOf(3), "Affitto", "Altro",         500d, "€",  String.valueOf(img_expense[3]), true, g3.getID());
            //u4.addExpense(e4);
//            addExpenseFirebase(u4,e4);

            //Add expenses to Firebase

//            mDatabase.child("expenses").child(e1.getID()).setValue(e1);
//            mDatabase.child("expenses").child(e2.getID()).setValue(e2);
//            mDatabase.child("expenses").child(e3.getID()).setValue(e3);
//            mDatabase.child("expenses").child(e4.getID()).setValue(e4);

            myself = u0;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        drawerOptions = getResources().getStringArray(R.array.drawerItem);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
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
                    Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                            .setMessage(getString(R.string.invitation_message))
     //                       .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
     //                       .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                            .setCallToActionText(getString(R.string.invitation_cta))
                            .build();
                    startActivityForResult(intent, REQUEST_INVITE);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.friends));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.groups));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
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
                Intent myIntent = new Intent(MainActivity.this, NewGroupActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == REQUEST_INVITE){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "onActivityResult: failed sent");
            }
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        int numberOfTabs;

        public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    Log.d(TAG, "here in case 0");
                    FriendsFragment friendsFragment = new FriendsFragment();
                    return friendsFragment;
                case 1:
                    Log.d(TAG, "here in case 1");
                    GroupsFragment groups1Fragment = new GroupsFragment();
                    return groups1Fragment;
                case 2:
                    Log.d(TAG, "here in case 2");
                    PendingExpensesFragment pendingExpensesFragment = new PendingExpensesFragment();
                    return pendingExpensesFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Log.d(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "FriendsFragment":
                User friendDetail = users.get(itemID);
                bundle.putParcelable("friendDetail", friendDetail);

                intent = new Intent(this, FriendDetailActivity.class);
                intent.putExtra("friendDetails", friendDetail);
                startActivity(intent);

//                FriendDetailFragment friendDetailFragment = new FriendDetailFragment();
//                friendDetailFragment.setArguments(bundle);
//
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.replace(R.id.main_content, friendDetailFragment);

//                fragmentTransaction.commit();

                break;

            case "GroupsFragment":
                Group groupDetail = groups.get(itemID);
                bundle.putParcelable("groupDetails", groupDetail);

                intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupDetails", groupDetail);
                startActivity(intent);

                break;
        }
    }
}

package com.polito.mad17.madmax.activities.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.expenses.NewExpenseActivity;
import com.polito.mad17.madmax.activities.expenses.PendingExpensesFragment;
import com.polito.mad17.madmax.activities.expenses.ExpensesFragment;
import com.polito.mad17.madmax.activities.users.FriendsFragment;
import com.polito.mad17.madmax.entities.Group;

public class GroupDetailActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = GroupDetailActivity.class.getSimpleName();

    private ImageView imageView;
    private TextView nameTextView;
    private TextView balanceTextView;
    private String groupID;
    private String userID;

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private DatabaseReference groupRef;
    private Group groupDetails = new Group();

    private Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_group_detail);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");

        Log.d(TAG, "onCreate di GroupDetailActivity. Group: " + groupID);

        imageView = (ImageView) findViewById(R.id.img_photo);
        nameTextView = (TextView) findViewById(R.id.tv_group_name);
        balanceTextView = (TextView) findViewById(R.id.tv_balance);

        Log.d(TAG, groupID);

        //Show data of group (di Ale)
        databaseReference.child("groups").child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue(String.class);
                nameTextView.setText(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // retrieving group details for current group (di Chiara)
        groupRef = databaseReference.child("groups");
        groupRef.child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot groupSnapshot) {

                groupDetails.setName(groupSnapshot.child("name").getValue(String.class));
                groupDetails.setID(groupSnapshot.getKey());
                groupDetails.setNumberMembers(groupSnapshot.child("numberMembers").getValue(Integer.class));
                Log.d(TAG, "groupDetails " +  groupDetails.toString());

                nameTextView.setText(groupDetails.getName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        //Abbiamo due listener che fanno la stessa cosa! Togliere uno dei due


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.expenses));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.members));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.history));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        final GroupDetailActivity.PagerAdapter adapter = new GroupDetailActivity.PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
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
                // todo diverse azioni a seconda del fragment in cui mi trovo
                // getSupportFragmentManager().findFragmentByTag()
                Intent myIntent = new Intent(GroupDetailActivity.this, NewExpenseActivity.class);
                myIntent.putExtra("groupID", groupID);
                myIntent.putExtra("userID", userID);
                startActivity(myIntent);
            }
        });
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        int numberOfTabs;

        public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    Log.d(TAG, "here in case 0");
                    ExpensesFragment expensesFragment = new ExpensesFragment();
                    //riga qui sotto aggiunta da Ale...prima il bundle veniva caricato nella OnDataChange quindi
                    //qui era ancora null e mi crashava, non so come potesse funzionare prima...
                    //in generale non usare i dati settati nella OnDataChange fuori dalla OnDataChange
                    bundle.putString("groupID", groupID);
                    expensesFragment.setArguments(bundle);
                    return expensesFragment;
                case 1:
                    Log.d(TAG, "here in case 2");
                    FriendsFragment membersFragment = new FriendsFragment();
                    Bundle b = new Bundle();
                    b.putString("groupID", groupID);
                    membersFragment.setArguments(b);
                    //setargument creo bundle bunfle.setstring
                    return membersFragment;
                case 2:
                    Log.d(TAG, "here in case 1");
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

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return super.isViewFromObject(view, object);
        }
    }
    
    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Log.d(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(fragmentName) {
            case "ExpensesFragment":
                Log.d(TAG, "expense case " + fragmentName + itemID);
                break;

            case "GroupsFragment":
                break;
        }
    }
}

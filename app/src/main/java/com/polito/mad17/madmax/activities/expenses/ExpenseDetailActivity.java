package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.BasicActivity;
import com.polito.mad17.madmax.activities.ExpenseDetailPagerAdapter;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;

public class ExpenseDetailActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private PagerAdapter adapter;
    private static final String TAG = ExpenseDetailActivity.class.getSimpleName();
    private String groupID;
    private String userID;
    private String expenseID;
    private ImageView imageView;
    private TextView amountTextView;
    private TextView expenseNameTextView;
    private TextView creatorNameTextView;
    private Toolbar toolbar;
    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(0x0000FF00);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");
        expenseID = intent.getStringExtra("expenseID");

        // insert tabs and current fragment in the main layout
        //mainView.addView(getLayoutInflater().inflate(R.layout.activity_expense_detail, null));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.expense_detail));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.comments));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.expense_detail_view_pager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, String.valueOf(tab.getPosition()));
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });


        ExpenseDetailPagerAdapter adapter = new ExpenseDetailPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), expenseID);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);


        //Set data of upper part of Activity

        imageView = (ImageView) findViewById(R.id.img_photo);
        amountTextView = (TextView) findViewById(R.id.tv_amount);
        creatorNameTextView = (TextView) findViewById(R.id.tv_creator_name);
        expenseNameTextView = (TextView) findViewById(R.id.tv_pending_name);

        //Retrieve data of this expense
        databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String expenseName = dataSnapshot.child("description").getValue(String.class);
                Double amount = dataSnapshot.child("amount").getValue(Double.class);
                expenseNameTextView.setText(expenseName);

                DecimalFormat df = new DecimalFormat("#.##");
                amountTextView.setText(df.format(amount) + " €");

                //Retrieve name and surname of creator
                String creatorID = dataSnapshot.child("creatorID").getValue(String.class);
                databaseReference.child("users").child(creatorID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String surname = dataSnapshot.child("surname").getValue(String.class);
                        creatorNameTextView.setText(name + " " + surname);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


    //Per creare overflow button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expense_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // do something useful
                Log.d (TAG, "Clicked up button");
                Intent intent = new Intent();
                intent.putExtra("groupID", groupID);
                intent.putExtra("userID", userID);
                setResult(RESULT_OK, intent);
                finish();
                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }



}


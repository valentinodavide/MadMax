package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.TreeMap;

public class PendingExpenseDetailActivity extends AppCompatActivity implements VotersViewAdapter.ListItemClickListener {

    private static final String TAG = PendingExpenseDetailActivity.class.getSimpleName();
    private OnItemClickInterface onClickGroupInterface;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private VotersViewAdapter votersViewAdapter;
    private TreeMap<String, User> voters = new TreeMap<>(Collections.reverseOrder());    //gruppi condivisi tra me e friend
    private String expenseID;
    private String userID;
    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private ImageView imageView;
    private TextView amountTextView;
    private TextView expenseNameTextView;
    private TextView creatorNameTextView;
    private TextView groupTextView;
    private Toolbar toolbar;


    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d (TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_expense_detail);
        //setInterface((OnItemClickInterface) this);

        Intent intent = getIntent();
        expenseID = intent.getStringExtra("expenseID");
        userID = intent.getStringExtra("userID");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(0x0000FF00);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.img_photo);
        amountTextView = (TextView) findViewById(R.id.tv_amount);
        creatorNameTextView = (TextView) findViewById(R.id.tv_creator_name);
        groupTextView = (TextView) findViewById(R.id.tv_group_name);
        expenseNameTextView = (TextView) findViewById(R.id.tv_pending_name);

        recyclerView = (RecyclerView) findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        //todo mettere a posto
        votersViewAdapter = new VotersViewAdapter(this, voters);
        recyclerView.setAdapter(votersViewAdapter);

        //Retrieve data of this pending expense
        databaseReference.child("proposedExpenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String expenseName = dataSnapshot.child("description").getValue(String.class);
                String groupName = dataSnapshot.child("groupName").getValue(String.class);
                Double amount = dataSnapshot.child("amount").getValue(Double.class);
                expenseNameTextView.setText(expenseName);
                groupTextView.setText(groupName);
                amountTextView.setText(amount.toString());

                DecimalFormat df = new DecimalFormat("#.##");

                amountTextView.setText(df.format(amount) + " €");

                //Show list of voters for this pending expense
                for (DataSnapshot voterSnap : dataSnapshot.child("participants").getChildren())
                {
                    String vote = voterSnap.child("vote").getValue(String.class);
                    FirebaseUtils.getInstance().getFriend(voterSnap.getKey(), vote, voters, votersViewAdapter, creatorNameTextView);
                }
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

        Log.d (TAG, "Clicked item: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.one:
                Log.d (TAG, "clicked Modify expense");
                Toast.makeText(PendingExpenseDetailActivity.this,"Functionality still not available",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.two:
                Log.d (TAG, "clicked Remove Expense");
                FirebaseUtils.getInstance().removePendingExpenseFirebase(expenseID, getApplicationContext());

                // add event for PENDING_EXPENSE_REMOVE
                databaseReference.child("proposedExpenses").child(expenseID)
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

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                Log.d (TAG, "Clicked up button on PendingExpenseDetailActivity");
                finish();
                Intent myIntent = new Intent(PendingExpenseDetailActivity.this, MainActivity.class);
                myIntent.putExtra("UID", MainActivity.getCurrentUser().getID());
                myIntent.putExtra("currentFragment", 2);
                startActivity(myIntent);
                return(true);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(String groupID) {
        Log.d(TAG, "clickedItemIndex " + groupID);
        onClickGroupInterface.itemClicked(getClass().getSimpleName(), groupID);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        finish();
        Intent myIntent = new Intent(PendingExpenseDetailActivity.this, MainActivity.class);
        myIntent.putExtra("UID", MainActivity.getCurrentUser().getID());
        myIntent.putExtra("currentFragment", 2);
        startActivity(myIntent);
    }

}

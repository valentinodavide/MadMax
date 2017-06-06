package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private ImageView imageView;
    private TextView amountTextView;
    private TextView expenseNameTextView;
    private TextView creatorNameTextView;
    private TextView groupTextView;
    private Button moveExpenseButton;
    private Toolbar toolbar;
    String creatorID;


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
        userID = MainActivity.getCurrentUID(); // intent.getStringExtra("userID");

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
        moveExpenseButton = (Button) findViewById(R.id.btn_move_expense);

        //Click on button to move from pending to real expense
        moveExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Only the creator of pending expense can move it
                if (creatorID.equals(userID))
                {
                    databaseReference.child("proposedExpenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> participants = new ArrayList<String>();
                            String expenseName = dataSnapshot.child("description").getValue(String.class);
                            String groupID = dataSnapshot.child("groupID").getValue(String.class);
                            Double amount = dataSnapshot.child("amount").getValue(Double.class);
                            String currency = dataSnapshot.child("currency").getValue(String.class);
                            String creatorID = dataSnapshot.child("creatorID").getValue(String.class);
                            String expensePhoto = dataSnapshot.child("expensePhoto").getValue(String.class);
                            for (DataSnapshot participantSnap : dataSnapshot.child("participants").getChildren())
                                participants.add(participantSnap.getKey());

                            Expense newExpense = new Expense();
                            newExpense.setDescription(expenseName);
                            newExpense.setAmount(amount);
                            newExpense.setCurrency(currency);
                            newExpense.setGroupID(groupID);
                            newExpense.setCreatorID(creatorID);
                            newExpense.setEquallyDivided(true);
                            newExpense.setDeleted(false);
                            newExpense.setExpensePhoto(expensePhoto);
                            Double amountPerMember = 1 / (double) participants.size();

                            for (String participant : participants)
                            {
                                newExpense.getParticipants().put(participant, amountPerMember);
                                //Delete expense from his proposed expenses
                                databaseReference.child("users").child(participant).child("proposedExpenses").child(expenseID).setValue(false);
                            }

                            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                            newExpense.setTimestamp(timeStamp);

                            //Add expense to db
                            FirebaseUtils.getInstance().addExpenseFirebase(newExpense, null, null, getApplicationContext());

                            //Delete pending expense from proposed expenses list
                            databaseReference.child("proposedExpenses").child(expenseID).child("deleted").setValue(true);
                            //Delete pending expense from group
                            databaseReference.child("groups").child(groupID).child("proposedExpenses").child(expenseID).setValue(false);




                            Intent myIntent = new Intent(PendingExpenseDetailActivity.this, GroupDetailActivity.class);
                            myIntent.putExtra("groupID", groupID);
                            myIntent.putExtra("userID", userID);
                            finish();
                            startActivity(myIntent);



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                else
                {
                    Toast.makeText(PendingExpenseDetailActivity.this,"Only the proposer can move it to expenses",Toast.LENGTH_SHORT).show();
                    return;

                }
            }
        });


        RecyclerView.ItemDecoration divider = new InsetDivider.Builder(this)
                .orientation(InsetDivider.VERTICAL_LIST)
                .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                .color(ContextCompat.getColor(getApplicationContext(), R.color.colorDivider))
                .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                .overlay(true)
                .build();

        recyclerView = (RecyclerView) findViewById(R.id.rv_skeleton);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);

        //todo mettere a posto
        votersViewAdapter = new VotersViewAdapter(voters, this);
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
                creatorID = dataSnapshot.child("creatorID").getValue(String.class);
                databaseReference.child("users").child(creatorID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot creatorSnapshot) {
                        creatorNameTextView.setText(creatorSnapshot.child("name").getValue(String.class)+" "+creatorSnapshot.child("surname").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                DecimalFormat df = new DecimalFormat("#.##");

                amountTextView.setText(df.format(amount) + " €");

                //Show list of voters for this pending expense
                for (DataSnapshot voterSnap : dataSnapshot.child("participants").getChildren())
                {
                    String vote = voterSnap.child("vote").getValue(String.class);
                    FirebaseUtils.getInstance().getVoter(voterSnap.getKey(), vote, voters, votersViewAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

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
                final Intent intent = new Intent(this, MainActivity.class);

                databaseReference.child("proposedExpenses").child(expenseID).child("creatorID").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(MainActivity.getCurrentUID().matches(dataSnapshot.getValue(String.class))) {
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

                                                                            startActivity(intent);
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {
                                                                            Log.w(TAG, databaseError.toException());
                                                                        }
                                                                    }
                                    );
                        }
                        else
                            Toast.makeText(getApplicationContext(),"You are not the proposal creator",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
                return true;
            case android.R.id.home:
                Log.d (TAG, "Clicked up button on PendingExpenseDetailActivity");
                finish();
                Intent myIntent = new Intent(PendingExpenseDetailActivity.this, MainActivity.class);
                myIntent.putExtra("UID", MainActivity.getCurrentUID());
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
        myIntent.putExtra("UID", MainActivity.getCurrentUID());
        myIntent.putExtra("currentFragment", 2);
        startActivity(myIntent);
    }

}

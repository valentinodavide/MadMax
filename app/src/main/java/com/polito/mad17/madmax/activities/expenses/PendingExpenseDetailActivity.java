package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import com.polito.mad17.madmax.activities.ExpenseDetailPagerAdapter;
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

import static com.polito.mad17.madmax.R.id.fab;

public class PendingExpenseDetailActivity extends AppCompatActivity implements VotersViewAdapter.ListItemClickListener, NewCommentDialogFragment.NewCommentDialogListener{

    private static final String TAG = PendingExpenseDetailActivity.class.getSimpleName();
    private OnItemClickInterface onClickGroupInterface;
    private String groupID;
    private String expenseID;
    private String userID;
    private String expenseName;
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private ImageView imageView;
    private TextView amountTextView;
    private TextView expenseNameTextView;
    private TextView creatorNameTextView;
    private TextView groupTextView;
    private Button moveExpenseButton;
    private Toolbar toolbar;
    private String creatorID;
    private ViewPager viewPager;
    private FloatingActionButton fab;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d (TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_expense_detail);

        Intent intent = getIntent();
        expenseID = intent.getStringExtra("expenseID");
        userID = MainActivity.getCurrentUID(); // intent.getStringExtra("userID");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        updateFab(0);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(0x0000FF00);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

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
                updateFab(tab.getPosition());
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        ExpenseDetailPagerAdapter adapter = new ExpenseDetailPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), expenseID, TAG);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        imageView = (ImageView) findViewById(R.id.img_photo);
        amountTextView = (TextView) findViewById(R.id.tv_amount);
        creatorNameTextView = (TextView) findViewById(R.id.tv_creator_name);
        groupTextView = (TextView) findViewById(R.id.tv_group_name);
        expenseNameTextView = (TextView) findViewById(R.id.tv_pending_name);
        moveExpenseButton = (Button) findViewById(R.id.btn_move_expense);

        //Retrieve data of this pending expense
        databaseReference.child("proposedExpenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseName = dataSnapshot.child("description").getValue(String.class);
                String groupName = dataSnapshot.child("groupName").getValue(String.class);
                Double amount = dataSnapshot.child("amount").getValue(Double.class);
                expenseNameTextView.setText(expenseName);
                groupTextView.setText(groupName);
                amountTextView.setText(amount.toString());
                creatorID = dataSnapshot.child("creatorID").getValue(String.class);
                groupID = dataSnapshot.child("groupID").getValue(String.class);
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

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
                            expenseName = dataSnapshot.child("description").getValue(String.class);
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
                    Toast.makeText(PendingExpenseDetailActivity.this,getString(R.string.only_creator),Toast.LENGTH_SHORT).show();
                    return;

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //overflow button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expense_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        Log.d (TAG, "Clicked item: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.one:
                Log.d (TAG, "clicked Modify pending expense");
                intent = new Intent(this, ExpenseEdit.class);
                intent.putExtra("expenseID", expenseID);
                intent.putExtra("EXPENSE_TYPE", "PENDING_EXPENSE_EDIT");
                startActivity(intent);
                finish();
                return true;

            case R.id.two:
                Log.d (TAG, "clicked Remove pending expense");
                FirebaseUtils.getInstance().removePendingExpenseFirebase(expenseID, getApplicationContext());
                finish();
                return true;

            case android.R.id.home:
                Log.d (TAG, "Clicked up button on PendingExpenseDetailActivity");
                intent = new Intent(PendingExpenseDetailActivity.this, MainActivity.class);
                intent.putExtra("UID", MainActivity.getCurrentUID());
                intent.putExtra("currentFragment", 2);
                startActivity(intent);
                finish();
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

    private void updateFab(int position){
        switch(position){
            case 0:
                // expense detail fragment
                Log.d(TAG, "fab 0");
                fab.setVisibility(View.GONE);
                break;
            case 1:
                // comments fragment
                Log.d(TAG, "fab 1");
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "add a comment");
                        Bundle arguments = new Bundle();
                        arguments.putString("groupID", groupID);
                        arguments.putString("expenseID", expenseID);
                        arguments.putString("expenseName", expenseName);
                        arguments.putBoolean("isExpense", false);

                        NewCommentDialogFragment commentDialogFragment = new NewCommentDialogFragment();
                        commentDialogFragment.setArguments(arguments);
                        commentDialogFragment.show(getSupportFragmentManager(), "NewComment");
                    }
                });
                break;
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Toast.makeText(this,getString(R.string.saved),Toast.LENGTH_SHORT).show();
    }
}

package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.ExpenseDetailPagerAdapter;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;

import static java.lang.Math.abs;

public class ExpenseDetailActivity extends AppCompatActivity implements OnItemClickInterface, NewCommentDialogFragment.NewCommentDialogListener {

    private ViewPager viewPager;
    private PagerAdapter adapter;
    private static final String TAG = ExpenseDetailActivity.class.getSimpleName();
    private String groupID;
    private String userID;
    private String expenseID;
    private String expenseName;
    private ImageView imageView;
    private TextView amountTextView;
    private TextView expenseNameTextView;
    private TextView creatorNameTextView;
    private TextView balanceTextTextView;
    private TextView balanceTextView;
    private Button payExpenseButton;
    private Toolbar toolbar;
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FloatingActionButton fab;
    private Double expenseBalance;
    private String currency;
    private String expensePhoto;
    private String userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");
        expenseID = intent.getStringExtra("expenseID");

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

        //Set data of upper part of Activity
        imageView = (ImageView) findViewById(R.id.img_photo);
        amountTextView = (TextView) findViewById(R.id.tv_amount);
        creatorNameTextView = (TextView) findViewById(R.id.tv_creator_name);
        expenseNameTextView = (TextView) findViewById(R.id.tv_pending_name);
        balanceTextTextView = (TextView) findViewById(R.id.tv_balance_text);
        balanceTextView = (TextView) findViewById(R.id.tv_balance);
        payExpenseButton = (Button) findViewById(R.id.btn_pay_debt);

        userImage = MainActivity.getCurrentUser().getProfileImage();

        payExpenseButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d (TAG, "Clicked payButton");
                if (expenseBalance >= 0)
                {
                    Toast.makeText(ExpenseDetailActivity.this, getString(R.string.no_debts_to_pay_for_expense), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(ExpenseDetailActivity.this, PayExpenseActivity.class);
                    intent.putExtra("groupID", groupID);
                    intent.putExtra("userID", userID);
                    intent.putExtra("userImage", userImage);
                    intent.putExtra("debt", expenseBalance);
                    intent.putExtra("expenseID", expenseID);
                    intent.putExtra("expenseName", expenseName);
                    intent.putExtra("expenseCurrency", currency);
                    intent.putExtra("expenseImage", expensePhoto);
                    startActivity(intent);
                    finish();
                }
            }
        });

        //Retrieve data of this expense
        databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                expenseName = dataSnapshot.child("description").getValue(String.class);
                Double amount = dataSnapshot.child("amount").getValue(Double.class);
                currency = dataSnapshot.child("currency").getValue(String.class);
                expensePhoto = dataSnapshot.child("expensePhoto").getValue(String.class);
                expenseNameTextView.setText(expenseName);
                /*Glide.with(getApplicationContext()).load(dataSnapshot.child("expensePhoto").getValue(String.class)) //.load(dataSnapshot.child("image").getValue(String.class))
                        .placeholder(R.color.colorPrimary)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);*/

                DecimalFormat df = new DecimalFormat("#.##");
                amountTextView.setText(df.format(amount) + " " + currency);

                //Retrieve my balance for this expense
                Double dueImport = Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())) * dataSnapshot.child("amount").getValue(Double.class);
                Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);
                expenseBalance = alreadyPaid - dueImport;
                expenseBalance = Math.floor(expenseBalance * 100) / 100;


                if (expenseBalance > 0)
                {
                    balanceTextTextView.setText("For this expense you should receive");
                    balanceTextView.setText(expenseBalance.toString() + " " + currency);
                }
                else if (expenseBalance < 0)
                {
                    balanceTextTextView.setText("For this expense you should pay");
                    Double absBalance = abs (expenseBalance);
                    balanceTextView.setText(absBalance.toString() + " " + currency);
                }
                else if (expenseBalance == 0)
                {
                    balanceTextTextView.setText("For this expense you have no debts");
                    balanceTextView.setText("0" + " " + currency);
                }

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
                Log.d (TAG, "clicked Modify expense");
                intent = new Intent(this, ExpenseEdit.class);
                intent.putExtra("expenseID", expenseID);
                intent.putExtra("EXPENSE_TYPE", "EXPENSE_EDIT");
                startActivity(intent);
                finish();
                return true;

            case R.id.two:
                Log.d (TAG, "clicked Remove expense");
                FirebaseUtils.getInstance().removeExpenseFirebase(expenseID, getApplicationContext());
                finish();
                return true;

            case android.R.id.home:
                Log.d (TAG, "Clicked up button on ExpenseDetailActivity");
                intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("userID", userID);
                setResult(RESULT_OK, intent);
                finish();
                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {
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
                        arguments.putBoolean("isExpense", true);

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

    /*@Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }*/
}


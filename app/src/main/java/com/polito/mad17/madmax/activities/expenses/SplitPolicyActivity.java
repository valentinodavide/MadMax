package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SplitPolicyActivity extends AppCompatActivity implements SplittersViewAdapter.EditTextUpdateListener {

    private TextView totalTextView;
    private TextView amountTextView;
    TextView currencyTextView;
    TextView currencyAmountTextView;
    private String groupID;

    private static final String TAG = SplitPolicyActivity.class.getSimpleName();


    Double amount;  //costo della spesa
    String currency;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SplittersViewAdapter splittersViewAdapter;
    public static TreeMap<String, User> participants = new TreeMap<>(Collections.reverseOrder());
    DecimalFormat df = new DecimalFormat("#.##");
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private HashMap<String, Double> amountsList = new HashMap<>();  //lista delle cifre messe da ogni utente nell'EditText nell'adapter
    private Double totalSplit;  //totale delle quote divise finora dagli utenti









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_policy);

        totalTextView = (TextView) findViewById(R.id.total);    //somma quote già splittate
        amountTextView = (TextView) findViewById(R.id.tv_amount);   //costo della spesa
        currencyTextView = (TextView) findViewById(R.id.currency);
        currencyAmountTextView = (TextView) findViewById(R.id.currency_amount);


        Intent intent = getIntent();
        amount = intent.getDoubleExtra("amount", 0);
        currency = intent.getStringExtra("currency");
        groupID = intent.getStringExtra("groupID");
        amountsList = (HashMap<String, Double>) intent.getSerializableExtra("participants");
        Log.d (TAG, "I just entered SplitPolicyActivity. amountsList contains: ");
        for (Map.Entry<String, Double> entry : amountsList.entrySet())
        {
            Log.d (TAG, entry.getKey() + " " + entry.getValue());
        }

        totalSplit = intent.getDoubleExtra("totalSplit", 0d);


        totalTextView.setText(df.format(totalSplit));
        amountTextView.setText(df.format(amount));
        currencyTextView.setText(currency);
        currencyAmountTextView.setText(currency);


        participants.clear();

        //Retrieve info about members for this expense
        for (final Map.Entry<String, Double> entry : amountsList.entrySet())
        {
            databaseReference.child("users").child(entry.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User u = new User();
                    u.setName(dataSnapshot.child("name").getValue(String.class));
                    u.setSurname(dataSnapshot.child("surname").getValue(String.class));
                    u.setProfileImage(dataSnapshot.child("image").getValue(String.class));
                    u.setSplitPart(entry.getValue());
                    u.setExpenseCurrency(currency);
                    participants.put(entry.getKey(), u);
                    splittersViewAdapter.update(participants);
                    splittersViewAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        RecyclerView.ItemDecoration divider = new InsetDivider.Builder(SplitPolicyActivity.this)
                .orientation(InsetDivider.VERTICAL_LIST)
                .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                .color(ContextCompat.getColor(SplitPolicyActivity.this, R.color.colorDivider))
                .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                .overlay(true)
                .build();

        recyclerView = (RecyclerView) findViewById(R.id.rv_skeleton);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);


        splittersViewAdapter = new SplittersViewAdapter(participants, SplitPolicyActivity.this, this);
        recyclerView.setAdapter(splittersViewAdapter);










    }

    //Quando sovrascrivo un EditText
    @Override
    public void onListItemEditTextUpdate(HashMap<String, Double> amounts) {
        amountsList = amounts;

        totalSplit = 0d;
        for (Map.Entry<String, Double> entry : amountsList.entrySet())
        {
            totalSplit += entry.getValue();
        }

        totalTextView.setText(totalSplit.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.action_save)
        {


            //Se la somma delle cifre inserite è corretta
            if (Double.compare(totalSplit, amount) == 0)
            {

                Intent intent = new Intent(SplitPolicyActivity.this, NewExpenseActivity.class);
                intent.putExtra("amountsList", amountsList);
                intent.putExtra("totalSplit", totalSplit);
                setResult(RESULT_OK, intent);
                this.finish();

            }
            else
            {
                Toast.makeText(getBaseContext(), "Check amounts of participants", Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        //Clicked up button
        if (itemThatWasClickedId == android.R.id.home)
        {
            Intent intent = new Intent(SplitPolicyActivity.this, NewExpenseActivity.class);
            intent.putExtra("amountsList", amountsList);
            intent.putExtra("totalSplit", totalSplit);
            intent.putExtra("groupID", groupID);
            setResult(RESULT_OK, intent);
            super.onBackPressed();
            return true;
        }


            this.finish();
        return super.onOptionsItemSelected(item);

    }
}

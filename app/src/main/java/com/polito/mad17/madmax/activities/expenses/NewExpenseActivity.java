package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.groups.GroupExpensesActivity;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.User;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import static com.polito.mad17.madmax.R.string.amount;

public class NewExpenseActivity extends AppCompatActivity {

    private static final String TAG = NewExpenseActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private DatabaseReference groupRef;

    private EditText description;
    private EditText amount;
    private Spinner currency;

    private String groupID = null;
    private String userID = null;
    private Integer numberMembers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference();

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");
        numberMembers = intent.getIntExtra("numberMembers", 0);

        description = (EditText) findViewById(R.id.edit_description);
        amount = (EditText) findViewById(R.id.edit_amount);
        currency = (Spinner) findViewById(R.id.currency);

        // creating spinner for currencies
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.action_save) {

            //display message if text field is empty
            Toast.makeText(getBaseContext(), "Saved expense", Toast.LENGTH_SHORT).show();

            final Expense newExpense = new Expense();
            newExpense.setDescription(description.getText().toString());
            newExpense.setAmount(Double.valueOf(amount.getText().toString()));
            newExpense.setCurrency(currency.getSelectedItem().toString());
            newExpense.setGroupID(groupID);
            newExpense.setCreatorID(userID);
            newExpense.setEquallyDivided(true);

            final HashMap<String, Double> partecipants = new HashMap<>();
            Double amountPerMember = newExpense.getAmount();

            firebaseDatabase = FirebaseDatabase.getInstance();
            mDatabase = firebaseDatabase.getReference();
            groupRef = mDatabase.child("groups");
            groupRef.child(groupID).child("members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot membersSnapshot) {

                    Double amountPerMember = newExpense.getAmount() / membersSnapshot.getChildrenCount();

                    for(DataSnapshot member : membersSnapshot.getChildren())
                    {
                        partecipants.put(member.getKey(), amountPerMember);
                    }

                    newExpense.setParticipants(partecipants);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            addExpenseFirebase(newExpense);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String addExpenseFirebase(Expense expense) {

        //Aggiungo spesa a Firebase
        String eID = mDatabase.child("expenses").push().getKey();
        mDatabase.child("expenses").child(eID).setValue(expense);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        mDatabase.child("expenses").child(eID).child("timestamp").setValue(timeStamp);


        //Aggiungo spesa alla lista spese del gruppo
        mDatabase.child("groups").child(expense.getGroupID()).child("expenses").push();
        mDatabase.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue("true");

        return eID;
    }
}

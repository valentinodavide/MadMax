package com.polito.mad17.madmax.activities.groups;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.DecimalDigitsInputFilter;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.SettingsFragment;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;
import java.util.HashMap;

import static java.lang.Math.abs;
import static java.lang.Math.exp;

public class PayGroupActivity extends AppCompatActivity {

    private static final String TAG = PayGroupActivity
            .class.getSimpleName();

    private String groupID;
    private String userID;
    private String groupName;
    private Double debt;
    private EditText amountEditText;
    private TextView groupNameTextView;
    private Spinner currency;

    DecimalFormat df = new DecimalFormat("#.##");
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private Double myMoney;
    //key = currency
    //value = balance for that currency
    private HashMap<String, Double> totBalances = new HashMap<>();
    private String shownCurrency;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_group);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultCurrency = sharedPref.getString(SettingsFragment.DEFAULT_CURRENCY, "");

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");
        groupName = intent.getStringExtra("groupName");
        totBalances = (HashMap<String, Double>) intent.getSerializableExtra("totBalances");
        shownCurrency = intent.getStringExtra("shownCurrency");

        debt = totBalances.get(shownCurrency);
        debt = abs(Math.floor(debt * 100) / 100);

        currency = (Spinner) findViewById(R.id.currency);

        amountEditText = (EditText) findViewById(R.id.amount);
        amountEditText.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});

        groupNameTextView = (TextView) findViewById(R.id.tv_receiver);
        groupNameTextView.setText(groupName);

        amountEditText.setText(debt.toString());

        // creating spinner for currencies
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(adapter);

        // set the defaultCurrency value for the spinner based on the user preferences
        int spinnerPosition = adapter.getPosition(shownCurrency);
        currency.setSelection(spinnerPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        menu.findItem(R.id.action_save).setTitle("PAY");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d (TAG, "Clicked up button");
                Intent intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("userID", userID);
                startActivity(intent);
                return true;

            case R.id.action_save:
                Log.d (TAG, "Clicked pay");
                Double money = null;
                String text =amountEditText.getText().toString();
                if(!text.isEmpty())
                {
                    try
                    {
                        // it means it is double
                        money = Double.parseDouble(text);
                    }
                    catch (Exception e1)
                    {
                        // this means it is not double
                        e1.printStackTrace();
                    }
                }

                //Retrieve selected currency and debt for that currency
                shownCurrency = currency.getSelectedItem().toString();
                debt = totBalances.get(shownCurrency);
                //If you have no debts in that currency
                if (debt == null || debt >= 0)
                {
                    Toast.makeText(PayGroupActivity.this,getString(R.string.no_debts_currency),Toast.LENGTH_SHORT).show();
                    return  true;
                }
                debt = abs(Math.floor(debt * 100) / 100);

                if (money != null)
                {
                    Log.d(TAG, "payed " + money);
                    if (money > debt)
                    {
                        Toast.makeText(PayGroupActivity.this,getString(R.string.cannot_pay_more_currency),Toast.LENGTH_SHORT).show();
                        return  true;
                    }
                    else if (money <= 0)
                    {
                        Toast.makeText(PayGroupActivity.this,getString(R.string.no_payment),Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    else
                    {
                        //currency.getSelectedItem().toString()
                        payDebtForExpenses(userID, groupID, money, shownCurrency);
                        // todo add event for USER_PAY
                        intent = new Intent(this, GroupDetailActivity.class);
                        intent.putExtra("groupID", groupID);
                        intent.putExtra("userID", userID);
                        finish();
                        startActivity(intent);
                        return true;
                    }
                }
                else
                {
                    Log.d (TAG, "Error: money is null");
                }

                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    //money = cifra che ho a disposizione per ripianare i debiti (solo in una certa valuta!!)
    void payDebtForExpenses (final String userID, String groupID, Double money, final String currency)
    {
        myMoney = money;

        Log.d(TAG, "here in payDebtForExpenses");

        databaseReference.child("groups").child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot groupDataSnapshot) {

                final Boolean deleted = groupDataSnapshot.child("deleted").getValue(Boolean.class);

                //If group has not been deleted (should be useless here)
                if (deleted != null)
                {
                    for (DataSnapshot groupExpenseSnapshot : groupDataSnapshot.child("expenses").getChildren())
                    {
                        //Se ho ancora soldi per ripagare le spese
                        if (myMoney > 0)
                        {
                            Log.d(TAG, "myMoney " + myMoney);
                            //Considero solo le spese non eliminate dal gruppo
                            if (groupExpenseSnapshot.getValue(Boolean.class) == true)
                            {
                                //Adesso sono sicuro che la spesa non è stata eliminata
                                //Ascolto la singola spesa del gruppo
                                final String expenseID = groupExpenseSnapshot.getKey();
                                Log.d(TAG, "ripangando la spesa " + expenseID);

                                databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        //I can use money only to pay expenses in the currency of money!!
                                        if (dataSnapshot.child("currency").getValue(String.class).equals(currency))
                                        {
                                            String creatorID = dataSnapshot.child("creatorID").getValue(String.class);
                                            Double alreadyPaidByCreator = dataSnapshot.child("participants").child(creatorID).child("alreadyPaid").getValue(Double.class);

                                            Boolean involved = false; //dice se user contribuisce o no a quella spesa

                                            for (DataSnapshot participantSnapshot: dataSnapshot.child("participants").getChildren())
                                            {
                                                //todo poi gestire caso in cui utente viene tolto dai participant alla spesa
                                                if (participantSnapshot.getKey().equals(userID))
                                                    involved = true;
                                            }

                                            //se user ha partecipato alla spesa
                                            if (involved) {
                                                //alreadyPaid = soldi già messi dallo user per quella spesa
                                                //dueImport = quota che user deve mettere per quella spesa
                                                Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);
                                                Log.d(TAG, "Fraction: " + Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())));
                                                Double amount = dataSnapshot.child("amount").getValue(Double.class);
                                                Double dueImport = Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())) * amount;
                                                Double stillToPay = dueImport - alreadyPaid;

                                                //Se questa spesa non è già stata ripagata in toto
                                                if (stillToPay > 0) {
                                                    //Se ho ancora abbastanza soldi per ripagare in toto questa spesa, la ripago in toto AL CREATOR!!
                                                    if (myMoney >= stillToPay) {
                                                        //Quota già pagata DA ME per questa spesa aumenta
                                                        databaseReference.child("expenses").child(expenseID).child("participants").child(userID).child("alreadyPaid").setValue(dueImport);
                                                        //Quota già pagata DAL CREATOR per questa spesa diminuisce, perchè gli sto dando dei soldi
                                                        databaseReference.child("expenses").child(expenseID).child("participants").child(creatorID).child("alreadyPaid").setValue(alreadyPaidByCreator - stillToPay);

                                                        //Adesso ho meno soldi a disposizione, perchè li ho usati in parte per ripagare questa spesa
                                                        myMoney -= stillToPay;
                                                    }
                                                    //Altrimenti la ripago solo in parte
                                                    else {
                                                        databaseReference.child("expenses").child(expenseID).child("participants").child(userID).child("alreadyPaid").setValue(alreadyPaid + myMoney);
                                                        databaseReference.child("expenses").child(expenseID).child("participants").child(creatorID).child("alreadyPaid").setValue(alreadyPaidByCreator - myMoney);

                                                        myMoney = 0d;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
    }





}

package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.polito.mad17.madmax.activities.DecimalDigitsInputFilter;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.PayGroupActivity;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;

import static java.lang.Math.abs;


public class PayExpenseActivity extends AppCompatActivity {

    private static final String TAG = PayGroupActivity
            .class.getSimpleName();

    String groupID;
    String userID;
    String expenseID;
    String expenseName;
    Double debt;
    String currency;
    EditText amountEditText;
    TextView expenseNameTextView;
    TextView currencyTextView;
    ImageView userImage;
    ImageView expenseImage;

    String userImageURL;
    String expenseImageURL;

    DecimalFormat df = new DecimalFormat("#.##");
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private Double myMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_expense);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        userImageURL = intent.getStringExtra("userImage");
        groupID = intent.getStringExtra("groupID");
        expenseID = intent.getStringExtra("expenseID");
        expenseName = intent.getStringExtra("expenseName");
        expenseImageURL = intent.getStringExtra("expenseImage");

        debt = intent.getDoubleExtra("debt", 0);
        debt = abs(Math.floor(debt * 100) / 100);

        currency = intent.getStringExtra("expenseCurrency");

        amountEditText = (EditText) findViewById(R.id.amount);
        amountEditText.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});

        expenseNameTextView = (TextView) findViewById(R.id.tv_receiver);
        expenseNameTextView.setText(expenseName);

        amountEditText.setText(debt.toString());

        currencyTextView = (TextView) findViewById(R.id.currency);
        currencyTextView.setText(currency);

        userImage = (ImageView) findViewById(R.id.sender_photo);
        Glide.with(getLayoutInflater().getContext()).load(userImageURL)
                .centerCrop()
                .bitmapTransform(new CropCircleTransformation(getLayoutInflater().getContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(userImage);
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
                Log.d (TAG, "Clicked pay expense");
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
                if (money != null)
                {
                    if (money > debt)
                    {
                        Toast.makeText(PayExpenseActivity.this, getString(R.string.cannot_pay_more), Toast.LENGTH_SHORT).show();
                        return  true;
                    }
                    else if (money <= 0) {
                        Toast.makeText(PayExpenseActivity.this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
                        return  true;
                    }
                    else
                    {
                        Toast.makeText(PayExpenseActivity.this, getString(R.string.paid), Toast.LENGTH_SHORT).show();
                        payDebtForExpense(userID, expenseID, money);
                        // todo add event for USER_PAY
                        intent = new Intent(this, ExpenseDetailActivity.class);
                        intent.putExtra("groupID", groupID);
                        intent.putExtra("userID", userID);
                        intent.putExtra("expenseID", expenseID);
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

    //money = cifra che ho a disposizione per ripianare i debiti
    void payDebtForExpense (final String userID, final String expenseID, Double money)
    {
        myMoney = money;

        databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

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
                    if (involved)
                    {
                        //alreadyPaid = soldi già messi dallo user per quella spesa
                        //dueImport = quota che user deve mettere per quella spesa
                        Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);
                        Log.d (TAG, "Fraction: " + Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())));
                        Double amount = dataSnapshot.child("amount").getValue(Double.class);
                        Double dueImport = Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())) * amount;
                        Double stillToPay = dueImport - alreadyPaid;

                        //Se questa spesa non è già stata ripagata in toto
                        if (stillToPay > 0)
                        {
                            //Se ho ancora abbastanza soldi per ripagare in toto questa spesa, la ripago in toto AL CREATOR!!
                            if (myMoney >= stillToPay)
                            {
                                //Quota già pagata DA ME per questa spesa aumenta
                                databaseReference.child("expenses").child(expenseID).child("participants").child(userID).child("alreadyPaid").setValue(dueImport);
                                //Quota già pagata DAL CREATOR per questa spesa diminuisce, perchè gli sto dando dei soldi
                                databaseReference.child("expenses").child(expenseID).child("participants").child(creatorID).child("alreadyPaid").setValue(alreadyPaidByCreator-stillToPay);

                                //Adesso ho meno soldi a disposizione, perchè li ho usati in parte per ripagare questa spesa
                                myMoney -= stillToPay;
                            }
                            //Altrimenti la ripago solo in parte
                            else
                            {
                                databaseReference.child("expenses").child(expenseID).child("participants").child(userID).child("alreadyPaid").setValue(alreadyPaid+myMoney);
                                databaseReference.child("expenses").child(expenseID).child("participants").child(creatorID).child("alreadyPaid").setValue(alreadyPaidByCreator-myMoney);

                                myMoney = 0d;
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

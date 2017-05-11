package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupExpensesActivity;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.polito.mad17.madmax.R.string.amount;

public class NewExpenseActivity extends AppCompatActivity {

    private static final String TAG = NewExpenseActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private DatabaseReference groupRef;
    private StorageReference storageReference;

    private EditText description;
    private EditText amount;
    private Spinner currency;
    private ImageView expensePhoto;
    private ImageView billPhoto;

    private String groupID = null;
    private String userID = null;
    private Integer numberMembers = null;

    private int PICK_EXPENSE_PHOTO_REQUEST = 0;
    private int PICK_BILL_PHOTO_REQUEST = 1;
    private int EXPENSE_SAVED = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");
        numberMembers = intent.getIntExtra("numberMembers", 0);

        description = (EditText) findViewById(R.id.edit_description);
        amount = (EditText) findViewById(R.id.edit_amount);
        currency = (Spinner) findViewById(R.id.currency);
        expensePhoto = (ImageView) findViewById(R.id.img_expense);
        billPhoto = (ImageView) findViewById(R.id.img_bill);


        // creating spinner for currencies
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(adapter);

        expensePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "image clicked");

                // allow to the user the choose his profile image
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_EXPENSE_PHOTO_REQUEST);
                // now see onActivityResult
            }
        });

        billPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "image clicked");

                // allow to the user the choose his profile image
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_BILL_PHOTO_REQUEST);
                // now see onActivityResult
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");

        // first of all control if is the requested result and if it return something
        if(requestCode==PICK_EXPENSE_PHOTO_REQUEST && data != null && data.getData()!=null){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                expensePhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else  if(requestCode==PICK_BILL_PHOTO_REQUEST && data != null && data.getData()!=null){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                billPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

            Log.d(TAG, "Before first access to firebase");

            firebaseDatabase = FirebaseDatabase.getInstance();
            mDatabase = firebaseDatabase.getReference();
            groupRef = mDatabase.child("groups");
            groupRef.child(groupID).child("members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot membersSnapshot) {

                    Double amountPerMember = 1 / (double) membersSnapshot.getChildrenCount();

                    for(DataSnapshot member : membersSnapshot.getChildren())
                    {
                        newExpense.getParticipants().put(member.getKey(), amountPerMember);
                    }

                    addExpenseFirebase(newExpense);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public String addExpenseFirebase(final Expense expense) {
        Log.d(TAG, "addExpenseFirebase");

        //Aggiungo spesa a Firebase
        final String eID = mDatabase.child("expenses").push().getKey();
        mDatabase.child("expenses").child(eID).setValue(expense);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        mDatabase.child("expenses").child(eID).child("timestamp").setValue(timeStamp);

        StorageReference uExpensePhotoFilenameRef = storageReference.child("expenses").child(eID).child(eID+"_expensePhoto.jpg");

        // Get the data from an ImageView as bytes
        expensePhoto.setDrawingCacheEnabled(true);
        expensePhoto.buildDrawingCache();
        Bitmap bitmap = expensePhoto.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = uExpensePhotoFilenameRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // todo Handle unsuccessful uploads
                Log.e(TAG, "image upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                mDatabase.child("expenses").child(eID).child("expensePhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
            }
        });

        StorageReference uBillPhotoFilenameRef = storageReference.child("expenses").child(eID).child(eID+"_billPhoto.jpg");

        // Get the data from an ImageView as bytes
        expensePhoto.setDrawingCacheEnabled(true);
        expensePhoto.buildDrawingCache();
        bitmap = expensePhoto.getDrawingCache();
        baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();

        uploadTask = uBillPhotoFilenameRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // todo Handle unsuccessful uploads
                Log.e(TAG, "image upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                mDatabase.child("expenses").child(eID).child("billPhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
            }
        });

        Log.d(TAG, "creator expense " + expense.getCreatorID());

        //Per ogni participant setto la quota che ha già pagato per questa spesa
        //e aggiungo spesa alla lista spese di ogni participant
        for (Map.Entry<String, Double> participant : expense.getParticipants().entrySet())
        {
            Log.d(TAG, "partecipant " + participant.getKey());
            //Se il participant corrente è il creatore della spesa
            if (participant.getKey().equals(expense.getCreatorID()))
            {

                //paga tutto lui
                mDatabase.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(expense.getAmount());
            }
            else
            {
                //gli altri participant inizialmente non pagano niente
                mDatabase.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(0);
            }

            //risetto fraction di spesa che deve pagare l'utente, visto che prima si sputtana
            mDatabase.child("expenses").child(eID).child("participants").child(participant.getKey()).child("fraction").setValue(expense.getParticipants().get(participant.getKey()));


            //Aggiungo spesaID a elenco spese dello user
            //todo controllare se utile
            mDatabase.child("users").child(participant.getKey()).child("expenses").child(eID).setValue("true");
        }

        //Aggiungo spesa alla lista spese del gruppo
        mDatabase.child("groups").child(expense.getGroupID()).child("expenses").push();
        mDatabase.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue("true");

        return eID;

        //u.updateBalance(expense);
        //updateBalanceFirebase(u, expense);
    }
}

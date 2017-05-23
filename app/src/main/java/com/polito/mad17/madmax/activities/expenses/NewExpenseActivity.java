package com.polito.mad17.madmax.activities.expenses;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.SettingsFragment;
import com.polito.mad17.madmax.entities.Expense;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


public class NewExpenseActivity extends AppCompatActivity {

    private static final String TAG = NewExpenseActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    private EditText description;
    private EditText amount;
    private Spinner currency;
    private ImageView expensePhoto;
    private ImageView billPhoto;

    private String groupID = null;
    private String userID = null;
    private String callingActivity;
    private String groupName;
    //private Integer numberMembers = null;

    private int PICK_EXPENSE_PHOTO_REQUEST = 0;
    private int PICK_BILL_PHOTO_REQUEST = 1;
    //private int EXPENSE_SAVED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultCurrency = sharedPref.getString(SettingsFragment.DEFAULT_CURRENCY, "");

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userID = intent.getStringExtra("userID");
        callingActivity = intent.getStringExtra("callingActivity");
        groupName = intent.getStringExtra("groupName");

        description = (EditText) findViewById(R.id.edit_description);
        amount = (EditText) findViewById(R.id.edit_amount);
        currency = (Spinner) findViewById(R.id.currency);
        expensePhoto = (ImageView) findViewById(R.id.img_expense);
        billPhoto = (ImageView) findViewById(R.id.img_bill);

        // creating spinner for currencies
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(adapter);

        // set the defaultCurrency value for the spinner based on the user preferences
        int spinnerPosition = adapter.getPosition(defaultCurrency);
        currency.setSelection(spinnerPosition);

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
        if (requestCode == PICK_EXPENSE_PHOTO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                Glide.with(this).load(data.getData()) //.load(dataSnapshot.child("image").getValue(String.class))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(expensePhoto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // first of all control if is the requested result and if it return something
        if (requestCode == PICK_BILL_PHOTO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                Glide.with(this).load(data.getData()) //.load(dataSnapshot.child("image").getValue(String.class))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(billPhoto);
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
        DatabaseReference groupRef;
        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.action_save) {

            if(!validateForm())
                return true;
            //display message if text field is empty
            Toast.makeText(getBaseContext(), "Saved expense", Toast.LENGTH_SHORT).show();

            final Expense newExpense = new Expense();
            newExpense.setDescription(description.getText().toString());
            newExpense.setAmount(Double.valueOf(amount.getText().toString()));
            newExpense.setCurrency(currency.getSelectedItem().toString());
            newExpense.setGroupID(groupID);
            newExpense.setCreatorID(userID);
            newExpense.setEquallyDivided(true);
            newExpense.setDeleted(false);


            Log.d(TAG, "Before first access to firebase");

            groupRef = databaseReference.child("groups");
            groupRef.child(groupID).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot membersSnapshot) {

                    int participantsCount = 0;

                    //Attenzione! Non contare i membri eliminati tra i partecipanti alla spesa
                    for (DataSnapshot memberSnap : membersSnapshot.getChildren())
                    {
                        if (memberSnap.child("deleted").getValue().toString().contains("false"))
                            participantsCount ++;
                    }

                    Double amountPerMember = 1 / (double) participantsCount;

                    for(DataSnapshot member : membersSnapshot.getChildren())
                    {
                        //Aggiungo alla spesa solo i membri non eliminati dal gruppo
                        if (member.child("deleted").getValue().toString().contains("false"))
                                newExpense.getParticipants().put(member.getKey(), amountPerMember);
                    }

                    String timeStamp = SimpleDateFormat.getDateTimeInstance().toString();
                    newExpense.setTimestamp(timeStamp);

                    //Aggiungo una pending expense
                    if (callingActivity.equals("ChooseGroupActivity"))
                    {
                        newExpense.setGroupName(groupName);
                        addPendingExpenseFirebase(newExpense);

                    }
                    //Aggiungo una spesa normale
                    else
                    {
                        addExpenseFirebase(newExpense);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }


        this.finish();


        return super.onOptionsItemSelected(item);
    }

   /* riky: probabilmente non più necessaria
   // resize loaded image to avoid OutOfMemory errors
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

    }*/

    // check if both email and password form are filled
    private boolean validateForm() {
        Log.i(TAG, "validateForm");

        boolean valid  = true;

        String d = description.getText().toString();
        if (TextUtils.isEmpty(d)) {
            description.setError(getString(R.string.required));
            valid = false;
        } else {
            description.setError(null);
        }

        String a = amount.getText().toString();
        if (TextUtils.isEmpty(a)) {
            amount.setError(getString(R.string.required));
            valid = false;
        } else {
            amount.setError(null);
        }
        return valid;
    }

    public String addExpenseFirebase(final Expense expense) {
        Log.d(TAG, "addExpenseFirebase");

        //Aggiungo spesa a Firebase
        final String eID = databaseReference.child("expenses").push().getKey();
        databaseReference.child("expenses").child(eID).setValue(expense);


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
                databaseReference.child("expenses").child(eID).child("expensePhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
            }
        });

        StorageReference uBillPhotoFilenameRef = storageReference.child("expenses").child(eID).child(eID+"_billPhoto.jpg");

        // Get the data from an ImageView as bytes
        billPhoto.setDrawingCacheEnabled(true);
        billPhoto.buildDrawingCache();
        bitmap = billPhoto.getDrawingCache();
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
                databaseReference.child("expenses").child(eID).child("billPhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
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
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(expense.getAmount());
            }
            else
            {
                //gli altri participant inizialmente non pagano niente
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(0);
            }

            //risetto fraction di spesa che deve pagare l'utente, visto che prima si sputtana
            databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("fraction").setValue(expense.getParticipants().get(participant.getKey()));


            //Aggiungo spesaID a elenco spese dello user
            //todo controllare se utile
            databaseReference.child("users").child(participant.getKey()).child("expenses").child(eID).setValue(true);
        }

        //Aggiungo spesa alla lista spese del gruppo
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").push();
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue(true);

        return eID;

    }

    public void addPendingExpenseFirebase (Expense expense)
    {
        Log.d(TAG, "addPendingExpenseFirebase");


        //Aggiungo pending expense a Firebase
        final String eID = databaseReference.child("proposedExpenses").push().getKey();
        databaseReference.child("proposedExpenses").child(eID).setValue(expense);


        StorageReference uExpensePhotoFilenameRef = storageReference.child("proposedExpenses").child(eID).child(eID+"_expensePhoto.jpg");

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
                databaseReference.child("proposedExpenses").child(eID).child("expensePhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
            }
        });


        Log.d(TAG, "creator expense " + expense.getCreatorID());

        //Per ogni participant setto la quota che ha già pagato per questa spesa
        //e aggiungo spesa alla lista spese di ogni participant
        for (Map.Entry<String, Double> participant : expense.getParticipants().entrySet())
        {
            Log.d(TAG, "participant " + participant.getKey());

            //Setto voto nel participant a null
            databaseReference.child("proposedExpenses").child(eID).child("participants").child(participant.getKey()).child("vote").setValue("null");
            //Aggiungo campo deleted al participant
            databaseReference.child("proposedExpenses").child(eID).child("participants").child(participant.getKey()).child("deleted").setValue(false);
            //Aggiungo spesaID a elenco spese pending dello user
            databaseReference.child("users").child(participant.getKey()).child("proposedExpenses").child(eID).setValue(true);
        }

        //todo aggiungere spesa pending al gruppo
        //Aggiungo spesa alla lista spese del gruppo
        //databaseReference.child("groups").child(expense.getGroupID()).child("expenses").push();
        //databaseReference.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue(true);

        return;
    }


}

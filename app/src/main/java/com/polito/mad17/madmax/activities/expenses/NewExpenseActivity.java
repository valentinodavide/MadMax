package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.SettingsFragment;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;


public class NewExpenseActivity extends AppCompatActivity {

    private static final String TAG = NewExpenseActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
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
    private String groupImage;
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
        groupImage = intent.getStringExtra("groupImage");

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
                        if (!memberSnap.child("deleted").getValue(Boolean.class))
                        {
                            participantsCount++;
                        }
                    }

                    Double amountPerMember = 1 / (double) participantsCount;

                    for(DataSnapshot member : membersSnapshot.getChildren())
                    {
                        //Aggiungo alla spesa solo i membri non eliminati dal gruppo
                        if (!member.child("deleted").getValue(Boolean.class))
                        {
                            newExpense.getParticipants().put(member.getKey(), amountPerMember);
                        }
                    }

                    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                    newExpense.setTimestamp(timeStamp);

                    //Aggiungo una pending expense
                    if (callingActivity.equals("ChooseGroupActivity"))
                    {
                        newExpense.setGroupName(groupName);
                        if (groupImage != null)
                            newExpense.setGroupImage(groupImage);


                        FirebaseUtils.getInstance().addPendingExpenseFirebase(newExpense, expensePhoto, billPhoto);
                        //todo qui
                        Intent myIntent = new Intent(NewExpenseActivity.this, MainActivity.class);
                        myIntent.putExtra("UID", MainActivity.getCurrentUID());
                        myIntent.putExtra("currentFragment", 2);
                        startActivity(myIntent);

                        // add event for PENDING_EXPENSE_ADD
                        User currentUser = MainActivity.getCurrentUser();
                        Event event = new Event(
                                groupID,
                                Event.EventType.PENDING_EXPENSE_ADD,
                                currentUser.getName() + " " + currentUser.getSurname(),
                                newExpense.getDescription(),
                                newExpense.getAmount()
                        );
                        event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                        event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                        FirebaseUtils.getInstance().addEvent(event);

                    }
                    //Aggiungo una spesa normale
                    else
                    {
                        FirebaseUtils.getInstance().addExpenseFirebase(newExpense, expensePhoto, billPhoto);

                        // add event for EXPENSE_ADD
                        User currentUser = MainActivity.getCurrentUser();
                        Event event = new Event(
                                newExpense.getGroupID(),
                                Event.EventType.EXPENSE_ADD,
                                currentUser.getName() + " " + currentUser.getSurname(),
                                newExpense.getDescription(),
                                newExpense.getAmount()
                        );
                        event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                        event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                        FirebaseUtils.getInstance().addEvent(event);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
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
}

package com.polito.mad17.madmax.activities.expenses;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;


public class ExpenseEdit extends AppCompatActivity {

    private static final String TAG = ExpenseEdit.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the image
    private int PICK_BILL_REQUEST = 2; // to use for selecting the bill
    private boolean IMAGE_CHANGED = false;
    private boolean BILL_CHANGED = false;

    private ImageView expenseImageView;
    private ImageView expenseBillView;
    private EditText expenseDescriptionView;
    private EditText expenseAmountView;
    private Spinner expenseCurrencyView;
    private Button saveButton;
    //private ProgressDialog progressDialog;

    private Event.EventType EXPENSE_TYPE;
    private String expense_type;
    private Expense expense;

    @Override @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_expense);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        expenseImageView = (ImageView) this.findViewById(R.id.expense_image);
        expenseBillView = (ImageView) this.findViewById(R.id.expense_bill);
        expenseDescriptionView = (EditText) this.findViewById(R.id.expense_description);
        expenseAmountView = (EditText) this.findViewById(R.id.expense_amount);
        expenseCurrencyView = (Spinner) this.findViewById(R.id.expense_currency);
        saveButton = (Button) this.findViewById(R.id.btn_save);

        // creating spinner for currencies
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseCurrencyView.setAdapter(adapter);

        Intent intent = getIntent();
        String expenseID = intent.getStringExtra("expenseID");
        if (intent.getStringExtra("EXPENSE_TYPE").equals("EXPENSE_EDIT")) {
            EXPENSE_TYPE = Event.EventType.EXPENSE_EDIT;
            expense_type = "expenses";
        }
        else {
            EXPENSE_TYPE = Event.EventType.PENDING_EXPENSE_EDIT;
            expense_type = "proposedExpenses";
        }


        databaseReference.child(expense_type).child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expense = new Expense();
                expense.setID(dataSnapshot.getKey());
                expense.setDescription(dataSnapshot.child("description").getValue(String.class));
                expense.setAmount(dataSnapshot.child("amount").getValue(Double.class));
                expense.setCurrency(dataSnapshot.child("currency").getValue(String.class));
                expense.setExpensePhoto(dataSnapshot.child("expensePhoto").getValue(String.class));
                expense.setBillPhoto(dataSnapshot.child("billPhoto").getValue(String.class));

                final String groupID = dataSnapshot.child("groupID").getValue(String.class);

                expenseDescriptionView.setText(expense.getDescription());
                expenseAmountView.setText(String.valueOf(expense.getAmount()));

                // set the defaultCurrency value for the spinner based on the user preferences
                int spinnerPosition = adapter.getPosition(expense.getCurrency());
                expenseCurrencyView.setSelection(spinnerPosition);

                //progressDialog = new ProgressDialog(ProfileEdit.this);

                // loading expense photo (if present)
                String expenseImage = expense.getExpensePhoto();
                if (expenseImage != null && !expenseImage.equals("")) {
                    // Loading image
                    Glide.with(getApplicationContext()).load(expenseImage)
                            .centerCrop()
                            //.bitmapTransform(new CropCircleTransformation(this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(expenseImageView);
                }
                else {
                    // Loading image
                    expenseImageView.setImageResource(R.drawable.add_photo);
                    /* Glide.with(getApplicationContext()).load(R.drawable.add_photo)
                            .centerCrop()
                            //.bitmapTransform(new CropCircleTransformation(this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(expenseImageView);*/
                }
                expenseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "expense image clicked");

                        if (MainActivity.shouldAskPermission()) {
                            String[] perms = {"android.permission.READ_EXTERNAL_STORAGE"};

                            int permsRequestCode = 200;
                            requestPermissions(perms, permsRequestCode);
                        }
                        // allow to the user the choose image
                        Intent intent = new Intent();
                        // Show only images, no videos or anything else
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        // Always show the chooser (if there are multiple options available)
                        startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_IMAGE_REQUEST);
                        // now see onActivityResult
                    }
                });

                // loading expense bill (if present)
                String expenseBill = expense.getBillPhoto();
                if (expenseBill != null && !expenseBill.equals("")) {
                    // Loading image
                    Glide.with(getApplicationContext()).load(expenseBill)
                            .centerCrop()
                            //.bitmapTransform(new CropCircleTransformation(this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(expenseBillView);
                }
                else {
                    // Loading image
                    expenseImageView.setImageResource(R.drawable.add_photo);
                    /*Glide.with(getApplicationContext()).load(R.drawable.add_photo)
                            .centerCrop()
                            //.bitmapTransform(new CropCircleTransformation(this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(expenseBillView);*/
                }
                expenseBillView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "expense bill clicked");

                        if (MainActivity.shouldAskPermission()) {
                            String[] perms = {"android.permission.READ_EXTERNAL_STORAGE"};

                            int permsRequestCode = 200;
                            requestPermissions(perms, permsRequestCode);
                        }
                        // allow to the user the choose image
                        Intent intent = new Intent();
                        // Show only images, no videos or anything else
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        // Always show the chooser (if there are multiple options available)
                        startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_BILL_REQUEST);
                        // now see onActivityResult
                    }
                });

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "save clicked");
                        if (updateExpense(expense)) {
                            Toast.makeText(ExpenseEdit.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if (EXPENSE_TYPE.equals(Event.EventType.EXPENSE_EDIT)) {
                                intent = new Intent(getApplicationContext(), ExpenseDetailActivity.class);
                                intent.putExtra("groupID", expense.getGroupID());
                                intent.putExtra("expenseID", expense.getID());
                                intent.putExtra("userID", MainActivity.getCurrentUser().getID());
                            }
                            else {
                                intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("currentFragment", 2);
                            }
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");

        // first of all control if is the requested result and if it return something
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Glide.with(this).load(data.getData()).centerCrop()
                    //.bitmapTransform(new CropCircleTransformation(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(expenseImageView);

            IMAGE_CHANGED = true;
        }
        else if (requestCode == PICK_BILL_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Glide.with(this).load(data.getData()).centerCrop()
                    //.bitmapTransform(new CropCircleTransformation(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(expenseBillView);

            BILL_CHANGED = true;
        }
    }

    private boolean updateExpense(final Expense expense){
        Log.i(TAG, "update expense");

        if(!validateForm()) {
            Log.i(TAG, "submitted form is not valid");

            Toast.makeText(this, getString(R.string.invalid_form), Toast.LENGTH_SHORT).show();
            return false;
        }

        String newDescription = expenseDescriptionView.getText().toString();
        Double newAmount = Double.valueOf(expenseAmountView.getText().toString());
        String newCurrency = expenseCurrencyView.getSelectedItem().toString();

        if (!newDescription.isEmpty() && (expense.getDescription() == null || !expense.getDescription().equals(newDescription))) {
            expense.setDescription(newDescription);
            databaseReference.child(expense_type).child(expense.getID()).child("description").setValue(expense.getDescription());
        }

        if (!newAmount.isNaN() && (expense.getAmount() == null || !expense.getAmount().equals(newAmount))) {
            expense.setAmount(newAmount);
            databaseReference.child(expense_type).child(expense.getID()).child("amount").setValue(expense.getAmount());
        }

        if (!newCurrency.isEmpty() && (expense.getCurrency() == null || !expense.getCurrency().equals(newCurrency))) {
            expense.setCurrency(newCurrency);
            databaseReference.child(expense_type).child(expense.getID()).child("currency").setValue(expense.getCurrency());
        }

        if (IMAGE_CHANGED) {
            // for saving image
            StorageReference uExpenseImageImageFilenameRef = storageReference.child(expense_type).child(expense.getID()).child(expense.getID() + "_expensePhoto.jpg");

            // Get the data from an ImageView as bytes
            expenseImageView.setDrawingCacheEnabled(true);
            expenseImageView.buildDrawingCache();
            Bitmap bitmap = expenseImageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = uExpenseImageImageFilenameRef.putBytes(data);

            uploadTask
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // todo Handle unsuccessful uploads
                            Log.e(TAG, "image upload failed");
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            expense.setExpensePhoto(taskSnapshot.getMetadata().getDownloadUrl().toString());

                            databaseReference.child(expense_type).child(expense.getID()).child("expensePhoto").setValue(expense.getExpensePhoto());
                        }
                    });
        }

        if (BILL_CHANGED) {
            // for saving image
            StorageReference uExpenseBillImageFilenameRef = storageReference.child(expense_type).child(expense.getID()).child(expense.getID() + "billPhoto.jpg");

            // Get the data from an ImageView as bytes
            expenseBillView.setDrawingCacheEnabled(true);
            expenseBillView.buildDrawingCache();
            Bitmap bitmap = expenseBillView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = uExpenseBillImageFilenameRef.putBytes(data);

            uploadTask
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // todo Handle unsuccessful uploads
                            Log.e(TAG, "image upload failed");
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            expense.setBillPhoto(taskSnapshot.getMetadata().getDownloadUrl().toString());

                            databaseReference.child(expense_type).child(expense.getID()).child("billPhoto").setValue(expense.getExpensePhoto());
                        }
                    });
        }

        // add event for EXPENSE_EDIT / PENDING_EXPENSE_EDIT
        databaseReference.child(expense_type).child(expense.getID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User currentUser = MainActivity.getCurrentUser();
                        Event event = new Event(
                                dataSnapshot.child("groupID").getValue(String.class),
                                EXPENSE_TYPE,
                                currentUser.getName() + " " + currentUser.getSurname(),
                                dataSnapshot.child("description").getValue(String.class)
                        );
                        event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                        event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                        FirebaseUtils.getInstance().addEvent(event);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, databaseError.toException());
                    }
                });

        return true;
    }

    private boolean validateForm() {
        Log.i(TAG, "validateForm");
        boolean valid  = true;

        String description = expenseDescriptionView.getText().toString();
        if (TextUtils.isEmpty(description)) {
            expenseDescriptionView.setError(getString(R.string.required));
            valid = false;
        } else {
            expenseDescriptionView.setError(null);
        }

        String amount = expenseAmountView.getText().toString();
        if (TextUtils.isEmpty(amount)) {
            expenseAmountView.setError(getString(R.string.required));
            valid = false;
        } else {
            expenseAmountView.setError(null);
        }
        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        Log.d (TAG, "Clicked item: " + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d (TAG, "upButton clicked");

                if (EXPENSE_TYPE.equals(Event.EventType.EXPENSE_EDIT)) {
                    intent = new Intent(this, ExpenseDetailActivity.class);
                    intent.putExtra("groupID", expense.getGroupID());
                    intent.putExtra("expenseID", expense.getID());
                    intent.putExtra("userID", MainActivity.getCurrentUser().getID());
                }
                else {
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("currentFragment", 2);
                }
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

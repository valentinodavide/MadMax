package com.polito.mad17.madmax.activities.groups;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.polito.mad17.madmax.activities.expenses.ExpenseDetailActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;


public class GroupEdit extends AppCompatActivity {

    private static final String TAG = GroupEdit.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the image
    private boolean IMAGE_CHANGED = false;

    private ImageView groupImageView;
    private EditText groupNameView;
    private EditText groupDescriptionView;
    private Button saveButton;
    //private ProgressDialog progressDialog;

    private Group group;

    @Override @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_group);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupNameView = (EditText) this.findViewById(R.id.group_name);
        groupDescriptionView = (EditText) this.findViewById(R.id.group_description);
        groupImageView = (ImageView) this.findViewById(R.id.group_image);
        saveButton = (Button) this.findViewById(R.id.btn_save);

        final Intent intent = getIntent();
        final String groupID = intent.getStringExtra("groupID");

        Log.d("DAVIDE", "da GroupEdit: " + groupID);

        databaseReference.child("groups").child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                group = new Group();
                group.setID(dataSnapshot.getKey());
                group.setName(dataSnapshot.child("name").getValue(String.class));
                group.setDescription(dataSnapshot.child("description").getValue(String.class));
                group.setImage(dataSnapshot.child("image").getValue(String.class));

                groupNameView.setText(group.getName());
                groupDescriptionView.setText(group.getDescription());

                //progressDialog = new ProgressDialog(ProfileEdit.this);

                String groupImage = group.getImage();
                if (groupImage != null && !groupImage.equals("noImage")) {
                    // Loading image
                    Glide.with(getApplicationContext()).load(groupImage)
                            .centerCrop()
                            //.bitmapTransform(new CropCircleTransformation(this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(groupImageView);
                }
                else {
                    // Loading image
                    Glide.with(getApplicationContext()).load(R.drawable.group_default)
                            .centerCrop()
                            //.bitmapTransform(new CropCircleTransformation(this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(groupImageView);
                }

                groupImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "image clicked");

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

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "save clicked");
                        if (updateGroup(group)) {
                            Toast.makeText(GroupEdit.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), GroupDetailActivity.class);
                            i.putExtra("groupID", groupID);
                            i.putExtra("userID", MainActivity.getCurrentUID());
                            startActivity(i);
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
                    .into(groupImageView);

            IMAGE_CHANGED = true;
        }
    }

    private boolean updateGroup(final Group group){
        Log.i(TAG, "update group");

        if(!validateForm()) {
            Log.i(TAG, "submitted form is not valid");

            Toast.makeText(this, getString(R.string.invalid_form), Toast.LENGTH_SHORT).show();
            return false;
        }

        String newName = groupNameView.getText().toString();
        String newDescription = groupDescriptionView.getText().toString();

        if (!newName.isEmpty() && (group.getName() == null || !group.getName().equals(newName))) {
            group.setName(newName);
            databaseReference.child("groups").child(group.getID()).child("name").setValue(group.getName());
        }

        if (!newDescription.isEmpty() && (group.getDescription() == null || !group.getDescription().equals(newDescription))) {
            group.setDescription(newDescription);
            databaseReference.child("groups").child(group.getID()).child("description").setValue(group.getDescription());
        }

        if (IMAGE_CHANGED) {
            // for saving image
            StorageReference uGroupImageImageFilenameRef = storageReference.child("groups").child(group.getID()).child(group.getID() + "_groupImage.jpg");

            // Get the data from an ImageView as bytes
            groupImageView.setDrawingCacheEnabled(true);
            groupImageView.buildDrawingCache();
            Bitmap bitmap = groupImageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = uGroupImageImageFilenameRef.putBytes(data);

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
                        group.setImage(taskSnapshot.getMetadata().getDownloadUrl().toString());

                        databaseReference.child("groups").child(group.getID()).child("image").setValue(group.getImage());
                    }
                });
        }

        // add event for GROUP_EDIT
        User currentUser = MainActivity.getCurrentUser();
        Event event = new Event(
                group.getID(),
                Event.EventType.GROUP_EDIT,
                currentUser.getName() + " " + currentUser.getSurname(),
                group.getName()
        );
        event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
        event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
        FirebaseUtils.getInstance().addEvent(event);

        return true;
    }

    private boolean validateForm() {
        Log.i(TAG, "validateForm");

        boolean valid  = true;

        String name = groupNameView.getText().toString();
        if (TextUtils.isEmpty(name)) {
            groupNameView.setError(getString(R.string.notnull));
            valid = false;
        } else {
            groupNameView.setError(null);
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
                intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupID", group.getID());
                intent.putExtra("userID", MainActivity.getCurrentUID());
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

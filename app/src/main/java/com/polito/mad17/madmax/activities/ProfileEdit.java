package com.polito.mad17.madmax.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.CircleTransform;
import com.polito.mad17.madmax.entities.User;

import java.io.ByteArrayOutputStream;

public class ProfileEdit extends AppCompatActivity {

    private static final String TAG = ProfileEdit.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the image profile
    private boolean IMAGE_CHANGED = false;

    private ImageView profileImageView;
    private EditText nameView;
    private EditText surnameView;
    private EditText usernameView;
    private EditText emailView;
    private EditText passwordView;
    private Button saveButton;
    private ProgressDialog progressDialog;

    User currentUser = MainActivity.getCurrentUser();

    @Override @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        nameView = (EditText) this.findViewById(R.id.name);
        nameView.setText(currentUser.getName());

        surnameView = (EditText) this.findViewById(R.id.surname);
        surnameView.setText(currentUser.getSurname());

        usernameView = (EditText) this.findViewById(R.id.username);
        usernameView.setText(currentUser.getUsername());

        emailView = (EditText) this.findViewById(R.id.email);
        emailView.setText(currentUser.getEmail());

        passwordView = (EditText) this.findViewById(R.id.password);

        progressDialog = new ProgressDialog(this);

        profileImageView = (ImageView) this.findViewById(R.id.profile_image);
        if (!currentUser.loadImage(this, profileImageView)) {
            profileImageView.setImageResource(R.drawable.anonymous);
        }

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "image clicked");

                if (MainActivity.shouldAskPermission()) {
                    String[] perms = {"android.permission.READ_EXTERNAL_STORAGE"};

                    int permsRequestCode = 200;
                    requestPermissions(perms, permsRequestCode);
                }
                // allow to the user the choose his profile image
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_IMAGE_REQUEST);
                // now see onActivityResult
            }
        });

        saveButton = (Button) this.findViewById(R.id.btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "save clicked");
                updateAccount();

                Toast.makeText(ProfileEdit.this, "Saved", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
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
                    .bitmapTransform(new CircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profileImageView);

            IMAGE_CHANGED = true;
        }
    }

    private void updateAccount(){
        Log.i(TAG, "createAccount");

        if(!validateForm()) {
            Log.i(TAG, "submitted form is not valid");

            Toast.makeText(this, "Invalid form!", Toast.LENGTH_SHORT).show();
            return;
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Error while retriving current user from db");

            Toast.makeText(this, "Error while retriving current user from db",Toast.LENGTH_LONG).show();
            return;
        }

        String currentUserID = currentUser.getID();

        String newName = nameView.getText().toString();
        String newSurname = surnameView.getText().toString();
        String newUsername = usernameView.getText().toString();
        String newEmail = emailView.getText().toString();
        String newPassword = passwordView.getText().toString();

        if (!newEmail.isEmpty() && !currentUser.getEmail().equals(newEmail)) {
            user.updateEmail(emailView.toString());
            currentUser.setEmail(newEmail);

            progressDialog.setMessage("Sending email verification, please wait...");
            progressDialog.show();

            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Log.i(TAG, "verification email successful sent");
                    } else {
                        Log.e(TAG, "verification email not sent, exception: " + task.getException());
                    }
                }
            });
        }

        if (!newPassword.isEmpty() && !currentUser.getPassword().equals(User.encryptPassword(newPassword))) {
            user.updatePassword(newPassword);
            currentUser.setPassword(newPassword);

            databaseReference.child("users").child(currentUserID).child("password").setValue(currentUser.getPassword());
        }

        if (!newName.isEmpty() && (currentUser.getName() == null || !currentUser.getName().equals(newName))) {
            currentUser.setName(newName);

            databaseReference.child("users").child(currentUserID).child("name").setValue(currentUser.getName());
        }

        if (!newSurname.isEmpty() && (currentUser.getSurname() == null || !currentUser.getSurname().equals(newSurname))) {
            currentUser.setSurname(newSurname);

            databaseReference.child("users").child(currentUserID).child("surname").setValue(currentUser.getSurname());
        }

        if (!newUsername.isEmpty() && (currentUser.getUsername() == null || !currentUser.getUsername().equals(newUsername))) {
            currentUser.setUsername(newUsername);

            databaseReference.child("users").child(currentUserID).child("username").setValue(currentUser.getUsername());
        }

        if (IMAGE_CHANGED) {
            // for saving image
            StorageReference uProfileImageFilenameRef = storageReference.child("users").child(currentUserID).child(currentUserID + "_profileImage.jpg");

            // Get the data from an ImageView as bytes
            profileImageView.setDrawingCacheEnabled(true);
            profileImageView.buildDrawingCache();
            Bitmap bitmap = profileImageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = uProfileImageFilenameRef.putBytes(data);

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

                            currentUser.setProfileImage(taskSnapshot.getMetadata().getDownloadUrl().toString());
                        }
                    });
        }
    }

    private boolean validateForm() {
        Log.i(TAG, "validateForm");

        boolean valid  = true;

        String name = nameView.getText().toString();
        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.required));
            valid = false;
        } else {
            nameView.setError(null);
        }

        String surname = surnameView.getText().toString();
        if (TextUtils.isEmpty(surname)) {
            surnameView.setError(getString(R.string.required));
            valid = false;
        } else {
            surnameView.setError(null);
        }

        String username = usernameView.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.required));
            valid = false;
        } else {
            usernameView.setError(null);
        }

        /*String email = emailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.required));
            valid = false;
        } else if(!(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            emailView.setError("You should insert a valid mail");
            valid = false;
        } else {
            emailView.setError(null);
        }*/

        /*String password = passwordView.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.required));
            valid = false;
        } else if (passwordView.length() < 6){
            passwordView.setError(getString(R.string.weak_password));
            valid = false;
        } else {
            passwordView.setError(null);
        }*/

        return valid;
    }
}

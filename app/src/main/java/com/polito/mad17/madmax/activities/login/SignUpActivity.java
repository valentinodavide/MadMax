package com.polito.mad17.madmax.activities.login;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.entities.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference  storageReference = firebaseStorage.getReference();;

    private FirebaseAuth auth;

    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the image profile

    private EditText nameView;
    private EditText surnameView;
    private EditText usernameView;
    private EditText emailView;
    private EditText passwordView;
    private ImageView profileImageView;
    private ProgressDialog progressDialog;

    private String inviterUID = null;

    @Override
    @TargetApi(23) // used for letting AndroidStudio know that method requestPermissions() is called
    // in a controlled way: in particular it must be accessed only if API >= 23, and we guarantee it
    // via the static method MainActivity.shouldAskPermission()
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        TextView loginView;
        Button signupButton;

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar()

        Intent intent = getIntent();
        inviterUID = intent.getStringExtra("inviterUID");

        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        nameView = (EditText)findViewById(R.id.name);
        surnameView = (EditText)findViewById(R.id.surname);
        usernameView = (EditText)findViewById(R.id.username);
        emailView = (EditText)findViewById(R.id.email);
        passwordView = (EditText)findViewById(R.id.password);

        loginView = (TextView)findViewById(R.id.link_login);
        loginView.setPaintFlags(loginView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "login clicked");
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        profileImageView = (ImageView)findViewById(R.id.profile_image);
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

        signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "signup clicked");

                createAccount(emailView.getText().toString(), passwordView.getText().toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");

        // first of all control if is the requested result and if it return something
        if(requestCode==PICK_IMAGE_REQUEST && data != null && data.getData()!=null){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createAccount(String email, String password){
        Log.i(TAG, "createAccount");

        if(!validateForm()) {
            Log.i(TAG, "submitted form is not valid");

            Toast.makeText(SignUpActivity.this, "Invalid form!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Account creation, please wait...");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                Log.i(TAG,"createUserWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, Log the message to the LogCat. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    if(task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException){
                        Log.d(TAG,"account creation failed. Email already in use!");

                        Toast.makeText(SignUpActivity.this, "This email is already registered...account not created",Toast.LENGTH_LONG).show();
                    }
                    else if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                        Log.d(TAG,"account creation failed. Weak password!");

                        Toast.makeText(SignUpActivity.this, "Your password is too short!",Toast.LENGTH_LONG).show();
                    }
                    else {
                        Log.e(TAG, task.getException().getMessage());
                    }
                } else {
                    Log.d(TAG,"account creation succeded.");

                    sendVerificationEmail();
                }
            }
        });
    }


    private void sendVerificationEmail() {
        Log.i(TAG, "sendVerificationEmail");

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Error while retrieving current user from db");

            Toast.makeText(SignUpActivity.this, "Error while retrieving current user from db",Toast.LENGTH_LONG).show();
            return;
        }

        String UID = user.getUid();

        final User u = new User(
                UID,
                usernameView.getText().toString(),
                nameView.getText().toString(),
                surnameView.getText().toString(),
                emailView.getText().toString(),
                passwordView.getText().toString(),
                "",
                "€");
        if(inviterUID != null) {
            u.getUserFriends().put(inviterUID, null);
        }


        // for saving image
        StorageReference uProfileImageFilenameRef = storageReference.child("users").child(UID).child(UID+"_profileImage.jpg");

        // Get the data from an ImageView as bytes
        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = profileImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = uProfileImageFilenameRef.putBytes(data);

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

                u.setProfileImage(taskSnapshot.getMetadata().getDownloadUrl().toString());
            }
        });

        progressDialog.setMessage("Sending email verification, please wait...");
        progressDialog.show();

        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Log.i(TAG, "verification email successful sent");

                    Log.i(TAG, "insert new user into db");

                    HashMap<String,String> newUserEntry = new HashMap<>();

                    newUserEntry.put("email",       u.getEmail());
                    newUserEntry.put("password",    u.getPassword());
                    newUserEntry.put("friends",     u.getUserFriends().toString());
                    newUserEntry.put("groups",      u.getUserGroups().toString());
                    newUserEntry.put("image",       u.getProfileImage());
                    newUserEntry.put("name",        u.getName());
                    newUserEntry.put("surname",     u.getSurname());

                    databaseReference.child("users").child(u.getID()).setValue(newUserEntry);
                }
                else {
                    // todo delete the account and restart the activity
                    Log.e(TAG, "verification email not sent, exception: " + task.getException());
                }

                Intent intent = new Intent(getApplicationContext(), EmailVerificationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                Log.d(TAG, "created intent " + upIntent);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    Log.d(TAG, "shouldUpRecreateTask");
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    Log.d(TAG, "else");
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // check if both email and password form are filled
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

        String email = emailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.required));
            valid = false;
        } else if(!(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            emailView.setError("You should insert a valid mail");
            valid = false;
        } else {
            emailView.setError(null);
        }

        String password = passwordView.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.required));
            valid = false;
        } else {
            passwordView.setError(null);
        }
        return valid;
    }
}

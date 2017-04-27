package com.polito.mad17.madmax.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polito.mad17.madmax.R;

import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the image profile
    private static final String TAG = "SignUpActivity";

    private EditText nameView;
    private EditText surnameView;
    private EditText usernameView;
    private EditText emailView;
    private EditText passwordView;
    private TextView loginView;
    private ImageView profileImageView;
    private ProgressDialog progressDialog;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Log.d(TAG, "enter in signup activity");
        auth = FirebaseAuth.getInstance();

        nameView = (EditText)findViewById(R.id.name);
        surnameView = (EditText)findViewById(R.id.surname);
        usernameView = (EditText)findViewById(R.id.username);
        emailView = (EditText)findViewById(R.id.email);
        passwordView = (EditText)findViewById(R.id.password);
        progressDialog = new ProgressDialog(this);

        loginView = (TextView)findViewById(R.id.link_login);
        loginView.setPaintFlags(loginView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        profileImageView = (ImageView)findViewById(R.id.profile_image);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // allow to the user the choose his profile image
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_IMAGE_REQUEST);
            }
        });

        signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(emailView.getText().toString(),passwordView.getText().toString());
            }
        });

    }

    private void sendVerificationEmail() {
        Log.d(TAG, "Sending email verification...");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog.setMessage("Sending email verification, please wait...");
        progressDialog.show();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Log.d(TAG, "verification email successful sent");
                        }
                        else {
                            // delete the account and restart the activity
                            Log.d(TAG, "verification email not sent, exception: "+task.getException());
                        }
                        Intent intent = new Intent(getApplicationContext(), EmailVerificationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }


    private void createAccount(String email, String password){
        if(!validateForm())
            return;

        progressDialog.setMessage("Account creation, please wait...");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        Log.d(TAG,"createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, Log the message to the LogCat. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            if(task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException){
                                Log.d(TAG,"account creation failed. Email already in use!");
                                Toast.makeText(SignUpActivity.this, "This email is already registered...account not created",Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Log.d(TAG,"account creation succeded.");
                            sendVerificationEmail();
                        }
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    // check if both email and password form are filled
    private boolean validateForm() {
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

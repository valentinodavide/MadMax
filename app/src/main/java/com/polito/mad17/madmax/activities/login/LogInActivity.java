package com.polito.mad17.madmax.activities.login;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = LogInActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener; // to track whenever user signs in or out

    // UI references.
    private EditText emailView; // where the user inserts the email
    private EditText passwordView;  // where the user inserts the password
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        final String inviterUID, groupToBeAddedID;

        Button loginButton;     // login button
        TextView signupView;    // link to the sign up activity

        // getting Intent from invitation
        Intent intent = getIntent();

        String action = intent.getAction();
        Log.d(TAG, "action " + action);

        // retrieving data from the intent inviterUID & groupToBeAddedID as the group ID where to add the current user
        Uri data = intent.getData();
        if(data != null) {
            // to be used to set the current user as friend of the inviter
            inviterUID = data.getQueryParameter("inviterUID");
            //groupToBeAddedID = data.getQueryParameter("groupToBeAddedID");
        }
        else {
            inviterUID = null;
            //groupToBeAddedID = null;
            Log.e(TAG, "invitation failed?");
        }


        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

                String subTag = "onAuthStateChanged";

                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                // if the user is already logged and has already verified the mail skip the login phase and go to main page of app
                if(currentUser != null && currentUser.isEmailVerified())  {
                    Log.i(TAG, subTag+" user is logged, go to MainActivity");

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("UID", currentUser.getUid());

                    if(inviterUID != null) {
                        intent.putExtra("inviterUID", inviterUID);
                    }

                    /*
                    if (groupToBeAddedID != null) {
                        intent.putExtra("groupToBeAddedID", groupToBeAddedID);
                    }*/

                    startActivity(intent);
                    finish();
                }
                else {
                    // if the user is already logged but has not verified the mail redirect him to the email verification
                    if(currentUser != null && !currentUser.isEmailVerified()) {
                        Log.i(TAG, subTag+" user " + firebaseAuth.getCurrentUser().getEmail() + " is logged but should complete the registration");

                        Intent intent = new Intent(getApplicationContext(), EmailVerificationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        // if the user has done the logout
                        Log.i(TAG, subTag+" user has done the logout");
                    }
                }
            }
        };

        setContentView(R.layout.activity_log_in);

        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        signupView = (TextView) findViewById(R.id.link_signup);
        signupView.setPaintFlags(signupView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // to make the link underlined
        loginButton = (Button) findViewById(R.id.btn_login);
        progressDialog = new ProgressDialog(this);

        // allow to proceed in login using keyboard, without press Login button
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((actionId==R.id.login)||(actionId== EditorInfo.IME_NULL))
                    signIn(emailView.getText().toString(), passwordView.getText().toString());
                return true;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"login clicked");

                signIn(emailView.getText().toString(), passwordView.getText().toString());
            }
        });

        signupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"link to signup clicked");

                // todo aggiustare back button
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                PendingIntent pendingIntent = TaskStackBuilder.create(getApplicationContext())
                    .addNextIntentWithParentStack(intent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                builder.setContentIntent(pendingIntent);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        auth.addAuthStateListener(authListener); // attach the listener to the FirebaseAuth instance
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

        if (authListener != null)  // detach the listener to the FirebaseAuth instance
            auth.removeAuthStateListener(authListener);
    }


    private void signIn(String email, String password){
        Log.i(TAG, "signIn");

        if(!validateForm()) {
            Log.i(TAG, "submitted form is not valid");

            Toast.makeText(LogInActivity.this, "Invalid form!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Authentication, please wait...");
        progressDialog.show();

        // user authentication
        auth.signInWithEmailAndPassword(email, password).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

                Log.i(TAG, "authentication failed, exception: " + e.toString());
                Toast.makeText(LogInActivity.this, "Authentication failed.\nPlease insert a valid email/password",Toast.LENGTH_LONG).show();
            }
        });
    }

    // check if both email and password form are filled
    private boolean validateForm() {
        Log.i(TAG, "validateForm");

        boolean valid  = true;

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

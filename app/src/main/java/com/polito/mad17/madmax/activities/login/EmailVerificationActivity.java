package com.polito.mad17.madmax.activities.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;

public class EmailVerificationActivity extends AppCompatActivity {

    private static final String TAG = EmailVerificationActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener; // to track whenever user signs in or out

    private Button verifyEmailButton;
    private Button goToLoginButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_email_verification);

        progressDialog = new ProgressDialog(this);

        verifyEmailButton = (Button)findViewById(R.id.send_verification_button);
        verifyEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "verify email clicked");

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Log.e(TAG, "Error while retriving current user from db");

                    Toast.makeText(EmailVerificationActivity.this, "Error while retriving current user from db",Toast.LENGTH_LONG).show();
                    return;
                }

                progressDialog.setMessage("Sending email verification, please wait...");
                progressDialog.show();

                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Log.i(TAG, "verification email successful sent");
                        }
                        else {
                            // todo delete the account and restart the activity
                            Log.d(TAG, "verification email not sent, exception: "+task.getException());
                        }
                    }
                });
            }
        });

        goToLoginButton = (Button)findViewById(R.id.go_to_login_button);
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "login clicked");
                auth.signOut();
            }
        });

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if(currentUser!=null){
                    if(currentUser.isEmailVerified()){
                        Log.i(TAG, "email has been verified");

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("UID", currentUser.getUid());
                        startActivity(intent);
                        finish();
                    }
                }
                else{
                    Log.i(TAG, "user signed out");

                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener); // attach the listener to the FirebaseAuth instance
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null)  // detach the listener to the FirebaseAuth instance
            auth.removeAuthStateListener(authListener);
    }
}

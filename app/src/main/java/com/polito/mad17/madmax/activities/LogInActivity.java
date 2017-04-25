package com.polito.mad17.madmax.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.polito.mad17.madmax.R;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LogInActivity";
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener; // to track whenever user signs in or out

    // UI references.
    private EditText emailView; // where the user inserts the email
    private EditText passwordView;  // where the user inserts the password
    private ProgressDialog progressDialog;
    private Button loginButton; // login button
    private TextView signupView;    // link to the sign up activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

                String subTag = "onAuthStateChanged";
                if(firebaseAuth.getCurrentUser()!=null && auth.getCurrentUser().isEmailVerified()){
                    Log.d(TAG, subTag+" user is logged, go to GroupsActivity");
  //                  Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    finish();
                    startActivity(intent);

                }
                else {
                    if(firebaseAuth.getCurrentUser()!=null && !auth.getCurrentUser().isEmailVerified()) {
                        Log.d(TAG, subTag+" user " + firebaseAuth.getCurrentUser().getEmail() + " is logged but should complete the registration");
                        Intent intent = new Intent(getApplicationContext(), EmailVericationActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else{
                        Log.d(TAG, subTag+" user has done the logout");
                    }
                }

            }
        };

        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        signupView = (TextView) findViewById(R.id.link_signup);
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
                signIn(emailView.getText().toString(), passwordView.getText().toString());
            }
        });

        signupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"link to signup clicked");
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                finish();
                startActivity(intent);
            }
        });

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

    private void signIn(String email, String password){
        if (!validateForm()) {
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
                Log.d(TAG,e.toString());
                Toast.makeText(LogInActivity.this, "Authentication failed.\nPlease insert a valid email/password",Toast.LENGTH_LONG).show();
            }
        });
    }

    // check if both email and password form are filled
    private boolean validateForm() {
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

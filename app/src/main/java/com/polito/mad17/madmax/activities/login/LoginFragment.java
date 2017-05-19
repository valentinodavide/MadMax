package com.polito.mad17.madmax.activities.login;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupsFragment;

public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener; // to track whenever user signs in or out

    private OnItemClickInterface onClickLoginInterface;

    // UI references.
    private EditText emailView; // where the user inserts the email
    private EditText passwordView;  // where the user inserts the password
    private ProgressDialog progressDialog;
    private Button loginButton;     // login button
    private TextView signupView;    // link to the sign up activity


    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickLoginInterface = onItemClickInterface;
    }

    public LoginFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        setInterface((OnItemClickInterface) getActivity());

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailView = (EditText) view.findViewById(R.id.email);
        passwordView = (EditText) view.findViewById(R.id.password);
        signupView = (TextView) view.findViewById(R.id.link_signup);
        signupView.setPaintFlags(signupView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // to make the link underlined
        loginButton = (Button) view.findViewById(R.id.btn_login);
        progressDialog = new ProgressDialog(getContext());

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
                    Log.i(TAG, subTag + " user is logged, go to MainActivity");

                    onClickLoginInterface.itemClicked(LoginFragment.class.getSimpleName(), currentUser.getUid());
                }
                else {
                    // if the user is already logged but has not verified the mail redirect him to the email verification
                    if(currentUser != null && !currentUser.isEmailVerified()) {
                        Log.i(TAG, subTag+" user " + firebaseAuth.getCurrentUser().getEmail() + " is logged but should complete the registration");

                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            Log.e(TAG, "Error while retriving current user from db");

                            Toast.makeText(getContext(), "Error while retriving current user from db",Toast.LENGTH_LONG).show();
                            return;
                        }

                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if(task.isSuccessful()){

                                    Toast.makeText(getContext(), "Sent a new verification email",Toast.LENGTH_LONG).show();
                                    Log.i(TAG, "verification email successful sent");
                                }
                                else {
                                    Log.d(TAG, "verification email not sent, exception: "+task.getException());
                                }
                            }
                        });
                    }
                    else{
                        // if the user has done the logout
                        Log.i(TAG, subTag+" user has done the logout");
                    }
                }
            }
        };

        auth.addAuthStateListener(authListener); // attach the listener to the FirebaseAuth instance

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
                onClickLoginInterface.itemClicked(LoginFragment.class.getSimpleName(), "1");

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (authListener != null)  // detach the listener to the FirebaseAuth instance
            auth.removeAuthStateListener(authListener);
    }

    private void signIn(String email, String password){
        Log.i(TAG, "signIn");

        if(!validateForm()) {
            Log.i(TAG, "submitted form is not valid");

            Toast.makeText(getContext(), "Invalid form!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Authentication failed.\nPlease insert a valid email/password",Toast.LENGTH_LONG).show();
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

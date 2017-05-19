package com.polito.mad17.madmax.activities.login;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.entities.User;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class SignUpFragment extends Fragment {

    private static final String TAG = SignUpFragment.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    private FirebaseAuth auth;

    private OnItemClickInterface onClickSignUpInterface;

    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the image profile

    private EditText nameView;
    private EditText surnameView;
    private EditText usernameView;
    private EditText emailView;
    private EditText passwordView;
    private TextView loginView;
    private ImageView profileImageView;
    private ProgressDialog progressDialog;
    private Button signupButton;

    private String inviterID;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickSignUpInterface = onItemClickInterface;
    }

    public SignUpFragment() { }

    @Override
    @TargetApi(23) // used for letting AndroidStudio know that method requestPermissions() is called
    // in a controlled way: in particular it must be accessed only if API >= 23, and we guarantee it
    // via the static method MainActivity.shouldAskPermission()
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        inviterID = getArguments().getString("inviterID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setInterface((OnItemClickInterface) getActivity());
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        nameView = (EditText) view.findViewById(R.id.name);
        surnameView = (EditText) view.findViewById(R.id.surname);
        usernameView = (EditText) view.findViewById(R.id.username);
        emailView = (EditText) view.findViewById(R.id.email);
        passwordView = (EditText) view.findViewById(R.id.password);
        loginView = (TextView) view.findViewById(R.id.link_login);
        loginView.setPaintFlags(loginView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // to make the link underlined
        progressDialog = new ProgressDialog(getContext());

        profileImageView = (ImageView) view.findViewById(R.id.profile_image);
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

        signupButton = (Button) view.findViewById(R.id.btn_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "signup clicked");

                createAccount(emailView.getText().toString(), passwordView.getText().toString());
            }
        });

        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"link to login clicked");
                onClickSignUpInterface.itemClicked(SignUpFragment.class.getSimpleName(), "0");
            }
        });

        return view;
    }

    private void createAccount(String email, String password){
        Log.i(TAG, "createAccount");

        if(!validateForm()) {
            Log.i(TAG, "submitted form is not valid");

            Toast.makeText(getContext(), "Invalid form!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Account creation, please wait...");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                Log.i(TAG,"createUserWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, Log the message to the LogCat. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    try {
                        throw task.getException();
                    }
                    catch(FirebaseAuthWeakPasswordException e) {
                        passwordView.setError(e.getReason());
                        passwordView.requestFocus();
                    }
                    catch(FirebaseAuthUserCollisionException e) {
                        emailView.setError(e.getMessage());
                        emailView.requestFocus();
                    }
                    catch(Exception e) {
                        Log.e(TAG, e.getClass().toString() + ", message: " + e.getMessage());
                        Toast.makeText(getContext(), "An error occured", Toast.LENGTH_LONG).show();
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
            Log.e(TAG, "Error while retriving current user from db");

            Toast.makeText(getContext(), "Error while retriving current user from db",Toast.LENGTH_LONG).show();
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
        if(inviterID != null) {
            u.getUserFriends().put(inviterID, null);
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

                        onClickSignUpInterface.itemClicked(SignUpFragment.class.getSimpleName(), user.getUid());
                    }
                });

            }
        });

        progressDialog.setMessage("Sending email verification, please wait...");
        progressDialog.show();
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
        } else if (passwordView.length() < 6){
            passwordView.setError(getString(R.string.weak_password));
            valid = false;
        } else {
            passwordView.setError(null);
        }
        return valid;
    }

}

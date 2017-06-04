package com.polito.mad17.madmax.services;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

public class FirebaseServiceFCM extends FirebaseInstanceIdService {
    private String TAG = "FirebaseServiceFCM";
    private static FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (MainActivity.getCurrentUser() != null) {
            Log.d(TAG, "refreshedToken: "+refreshedToken);
            sendRegistrationToServer(refreshedToken);
        }
    }

    private void sendRegistrationToServer(String refreshedToken) {
        firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("token").setValue(refreshedToken);
    }
}

package com.polito.mad17.madmax.services;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.polito.mad17.madmax.activities.MainActivity;

public class FirebaseServiceFCM extends FirebaseInstanceIdService {
    private String TAG = "FirebaseServiceFCM";
    private static FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "refreshedToken: "+refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        MainActivity.getDatabase();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("token").setValue(refreshedToken);
    }
}

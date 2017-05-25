package com.polito.mad17.madmax.utilities;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.entities.User;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class UserValueEventListener implements ValueEventListener {

    private String userUID;
    private String inviterUID;
    private String groupToBeAddedID;

    public UserValueEventListener(String userUID, String inviterUID, String groupToBeAddedID) {
        this.userUID = userUID;
        this.inviterUID = inviterUID;
        this.groupToBeAddedID = groupToBeAddedID;
    }
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // TODO: come gestire?
        Log.d(TAG, "getting current user failed");
    }
}

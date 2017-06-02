package com.polito.mad17.madmax.service;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.polito.mad17.madmax.R;

import java.util.Map;


public class FirebaseServiceMessage extends FirebaseMessagingService {
    private String TAG = "FirebaseServiceMessage" ;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String body = remoteMessage.getNotification().getBody();
        Log.d(TAG, "notification body: "+body);
        Map<String, String> data = remoteMessage.getData();
        showNotification(remoteMessage.getNotification().getTitle(), body, data);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void showNotification(String title, String body, Map<String, String> data)
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.user_default)
                        .setContentTitle(title)
                        .setContentText("Hello World!");
    }
}

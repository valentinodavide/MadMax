package com.polito.mad17.madmax.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.expenses.ExpenseDetailActivity;
import com.polito.mad17.madmax.activities.expenses.PendingExpenseDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;

import java.util.Map;

import static com.polito.mad17.madmax.activities.MainActivity.getCurrentUID;


public class FirebaseServiceMessage extends FirebaseMessagingService {
    private String TAG = "FirebaseServiceMessage" ;
    private String currentUserID;

/*    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }*/

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String body = remoteMessage.getNotification().getBody();
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "notification body: "+body);
        String message[], messageContent = null;

        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = null;
        messageContent = body;
        switch (data.get("notificationTitle")){
            case "notification_invite":
                resultIntent = new Intent(getApplicationContext(), GroupDetailActivity.class);
                resultIntent.putExtra("groupID", data.get("groupID"));
                break;
            case "notification_expense_added":
                resultIntent = new Intent(getApplicationContext(), ExpenseDetailActivity.class);
                resultIntent.putExtra("groupID", data.get("groupID"));
                resultIntent.putExtra("expenseID", data.get("expenseID"));
                break;
            case "notification_expense_removed":
                resultIntent = new Intent(getApplicationContext(), GroupDetailActivity.class);
                resultIntent.putExtra("groupID", data.get("groupID"));
                resultIntent.putExtra("expenseID", data.get("expenseID"));
                break;
            case "notification_proposalExpense_added":
                resultIntent = new Intent(getApplicationContext(), PendingExpenseDetailActivity.class);
                resultIntent.putExtra("expenseID", data.get("expenseID"));
                break;
        }
       // User provaCurrent = MainActivity.getCurrentUser();
        resultIntent.putExtra("userID", getCurrentUID());

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(messageContent);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, notification);

        /*String body = remoteMessage.getNotification().getBody();
        Log.d(TAG, "notification body: "+body);
        Map<String, String> data = remoteMessage.getData();
        showNotification(remoteMessage.getNotification().getTitle(), body, data);*/
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void showNotification(String title, String body, Map<String, String> data){}
}

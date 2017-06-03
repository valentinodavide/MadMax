package com.polito.mad17.madmax.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/*Create a BroadcastReceiver that will receive the ACTION_BOOT_COMPLETED intent.
        When we boot up our device this class will “catch” the event and start the FirebaseService service*/
public class BroadcastReceiverOnBootComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"BOOT",Toast.LENGTH_LONG).show();
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("Receiver","ACTION_BOOT_COMPLETED");
            Intent serviceIntent = new Intent(context, FirebaseServiceMessage.class);
            context.startService(serviceIntent);
            /*File f = new File(
                    "/data/data/com.polito.mad17.madmax/shared_prefs/CurrentUser.xml");
            if (f.exists()) {
                Log.d("TAG", "SharedPreferences CurrentUser : exist");
                Intent serviceIntent = new Intent(context, FirebaseServiceMessage.class);
                context.startService(serviceIntent);
            }
            else
                Log.d("TAG", "Setup default preferences");*/
        }

    }
}
//context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE)
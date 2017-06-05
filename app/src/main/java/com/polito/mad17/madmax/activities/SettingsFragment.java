package com.polito.mad17.madmax.activities;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.google.firebase.database.DatabaseReference;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.utilities.FirebaseUtils;


public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static final String NOTIFICATION_INVITE = "notification_invite";
    public static final String NOTIFICATION_JOIN_GROUP = "notification_joinGroup";
    public static final String NOTIFICATION_LEAVE_GROUP = "notification_leaveGroup";
    public static final String NOTIFICATION_EXPENSE = "notification_expense";
    public static final String NOTIFICATION_PROPOSAL_EXPENSE = "notification_proposalExpense";
    public static final String NOTIFICATION_COMMENT = "notification_comment";
    public static final String NOTIFICATION_VOTE = "notification_vote";
    public static final String NOTIFICATION_PROPOSAL_APPROVED = "notification_proposalApproved";
    public static final String NOTIFICATION_PROPOSAL_NEGLECTED = "notification_proposalNeglected";
    public static final String NOTIFICATION_PAYMENT = "notification_payment";
    public static final String NOTIFICATION_CONFIRM_PAYMENT = "notification_confirmPayment";
    public static final String DEFAULT_CURRENCY = "defaultCurrency";

    private DatabaseReference databaseReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.preferences);
        databaseReference = FirebaseUtils.getDatabaseReference();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        CheckBoxPreference boxPref = (CheckBoxPreference)preference;

        if(boxPref.isChecked())
            databaseReference.child("users").child(MainActivity.getCurrentUID()).child("notifications").child(boxPref.getKey()).setValue(true);
        else
            databaseReference.child("users").child(MainActivity.getCurrentUID()).child("notifications").child(boxPref.getKey()).setValue(false);

        /*if(((CheckBoxPreference)preference).isChecked())
            Log.d(TAG, "preference "+preference.getKey()+" has been activated");
        else
            Log.d(TAG, "preference "+preference.getKey()+" has been deactivated");*/

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}

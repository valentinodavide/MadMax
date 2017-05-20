package com.polito.mad17.madmax.activities.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;

public class LoginSignUpActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = LoginSignUpActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener; // to track whenever user signs in or out

    private ViewPager viewPager;
    private PagerAdapter adapter;

    private String inviterUID;
    private String groupToBeAddedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // the first time user open the app the default preferences must be setted up
        PreferenceManager.setDefaultValues(this, R.layout.preferences, false);

        Log.i(TAG, "onCreate");

        // insert tabs and current fragment in the main layout
        setContentView(R.layout.activity_log_in_signup);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.login));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.signup));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setCurrentItem(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, tab.getPosition() + "");
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // getting Intent from invitation
        Intent intent = getIntent();

        String action = intent.getAction();
        Log.d(TAG, "action " + action);

        // retrieving data from the intent inviterUID & groupToBeAddedID as the group ID where to add the current user
        Uri data = intent.getData();
        if(data != null) {
            // to be used to set the current user as friend of the inviter
            Log.d(TAG, "there is an invite");
            inviterUID = data.getQueryParameter("inviterUID");
            groupToBeAddedID = data.getQueryParameter("groupToBeAddedID");
        }
        else {
            inviterUID = null;
            groupToBeAddedID = null;
            Log.e(TAG, "invitation failed?");
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        int numberOfTabs;

        LoginFragment loginFragment = null;
        SignUpFragment signUpFragment = null;

        public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {

            switch(position) {
                case 0:
                    Log.i(TAG, "here in case 0: LoginFragment");
                    loginFragment = new LoginFragment();
                    return loginFragment;
                case 1:
                    Log.i(TAG, "here in case 1: SignUpFragment");
                    signUpFragment = new SignUpFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("inviterID", inviterUID);
                    signUpFragment.setArguments(bundle);

                    return signUpFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return super.isViewFromObject(view, object);
        }
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "LoginFragment":

                if(itemID.equals("1"))
                {
                    viewPager.setCurrentItem(1);
                }
                else {
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("UID", itemID);

                    if (inviterUID != null) {
                        Log.i(TAG, "present inviterUID: " + inviterUID);
                        intent.putExtra("inviterUID", inviterUID);
                    }


                    if (groupToBeAddedID != null) {
                        Log.i(TAG, "present groupToBeAddedID: " + groupToBeAddedID);
                        intent.putExtra("groupToBeAddedID", groupToBeAddedID);
                    }

                    startActivity(intent);
                    finish();
                }

                break;

            case "SignUpFragment":

                if(itemID.equals("0"))
                {
                    viewPager.setCurrentItem(0);
                }
                else {
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("UID", itemID);

                    if (inviterUID != null) {
                        Log.i(TAG, "present inviterUID: " + inviterUID);
                        intent.putExtra("inviterUID", inviterUID);
                    }


                    if (groupToBeAddedID != null) {
                        Log.i(TAG, "present groupToBeAddedID: " + groupToBeAddedID);
                        intent.putExtra("groupToBeAddedID", groupToBeAddedID);
                    }

                    startActivity(intent);
                }

                break;
        }

    }
}

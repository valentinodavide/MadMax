package com.polito.mad17.madmax.activities.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.HashMap;

public class LoginSignUpActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = LoginSignUpActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener; // to track whenever user signs in or out

    private ViewPager viewPager;
    private PagerAdapter adapter;

    private String inviterUID;
    private String groupToBeAddedID;

    Bundle bundle = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // the first time user open the app the default preferences must be setted up
        PreferenceManager.setDefaultValues(this, R.layout.preferences, false);

        Log.i(TAG, "onCreate");

        // getting Intent from invitation
        Intent startingIntent = getIntent();

        String action = startingIntent.getAction();
        Log.d(TAG, "action " + action);

        // retrieving data from the intent inviterUID & groupToBeAddedID as the group ID where to add the current user
        Uri data = startingIntent.getData();
        if(data != null) {
            // to be used to set the current user as friend of the inviter
            Log.d(TAG, "there is an invite");
            bundle = new Bundle();
            inviterUID = data.getQueryParameter("inviterUID");
            bundle.putString("inviterUID", inviterUID);
            groupToBeAddedID = data.getQueryParameter("groupToBeAddedID");
            bundle.putString("groupToBeAddedID", groupToBeAddedID);
                startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtras(bundle));
                finish();
        }
        else {
            bundle = null;
            inviterUID = null;
            groupToBeAddedID = null;
            Log.d(TAG, "there is not an invite");
        }

        // insert tabs and current fragment in the main layout
        setContentView(R.layout.activity_log_in_signup);

        // for adding custom font to the title of the app
        TextView titleTextView = (TextView) findViewById(R.id.title);
        Typeface mycustomfont = Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf");
        titleTextView.setTypeface(mycustomfont);

        // for adding custom background image
        ImageView background = (ImageView) findViewById(R.id.background);
        Glide.with(this).load(R.drawable.background)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(background);

        //  findViewById(R.id.activity_log_in_signup_layout).setOnClickListener(this);
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
                Log.d(TAG, String.valueOf(tab.getPosition()));
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
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
                    bundle.putString("inviterUID", inviterUID);
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

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        switch(fragmentName) {
            case "LoginFragment":

                if(itemID.equals("1"))
                {
                    viewPager.setCurrentItem(1);
                }
                else {
                    if (bundle != null) {
                        // you arrive from a click of an invitate, so this avoid glitch
                        intent.putExtras(bundle);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0,0); //0 for no animation
                    }
                    else{
                        startActivity(intent);
                    }
                    finish();
                }

                break;

            case "SignUpFragment":

                if(itemID.equals("0"))
                {
                    viewPager.setCurrentItem(0);
                }
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}

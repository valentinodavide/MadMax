package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.login.LogInActivity;
import com.polito.mad17.madmax.entities.CircleTransform;
import com.polito.mad17.madmax.entities.CircleTransform;

import static com.polito.mad17.madmax.activities.MainActivity.auth;


public class BasicActivity extends AppCompatActivity {

    private static final String TAG = BasicActivity.class.getSimpleName();
    private static final int REQUEST_INVITE = 0;
    protected FrameLayout mainView;
    private String[] drawerOptions; // nav menu item
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View navHeader;
    private ImageView imgProfile;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    protected FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        mainView = (FrameLayout) findViewById(R.id.main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get the item shown in the navigation drawer
        drawerOptions = getResources().getStringArray(R.array.drawerItem);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        navHeader.setBackgroundColor(Color.BLUE);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // initializing navigation menu
        setUpNavigationView();
    }



    /* load navigation header menu info like profile image, name, email */
    protected void loadNavHeader() {
        // name and email
        txtName.setText(MainActivity.getCurrentUser().getName() + " " + MainActivity.getCurrentUser().getSurname());
        Log.d(TAG, "name: "+MainActivity.getCurrentUser().getName() + " - surname: " + MainActivity.getCurrentUser().getSurname());
        txtWebsite.setText(MainActivity.getCurrentUser().getEmail());
        Log.d(TAG, "email: "+MainActivity.getCurrentUser().getEmail());

        // Loading profile image
        Glide.with(this).load(MainActivity.getCurrentUser().getProfileImage())
                .centerCrop()
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);
        Log.d(TAG, "image url: "+MainActivity.getCurrentUser().getProfileImage());
    }

    public void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.home:
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.invite_friend:
                        Log.d(TAG, "my ID is " + MainActivity.getCurrentUser().getID());
                        String deepLink = getString(R.string.invitation_deep_link) + "?inviterUID=" + MainActivity.getCurrentUser().getID();

                        intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                                .setDeepLink(Uri.parse(deepLink))
                                .setMessage(getString(R.string.invitation_message))
                                //                     .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                                .setCallToActionText(getString(R.string.invitation_cta))
                                .build();

                        startActivityForResult(intent, REQUEST_INVITE);
                        break;
                    case R.id.settings:
                        break;
                    case R.id.logout:
                        Toast.makeText(getApplicationContext() , "Logout selected", Toast.LENGTH_SHORT).show();
                        auth.signOut();

                        intent = new Intent(getApplicationContext(), LogInActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        getSupportActionBar().setHomeButtonEnabled(true);
      //  getSupportActionBar().setBackgroundDrawable(getDrawable(R.drawable.calcetto));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == REQUEST_INVITE){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                Toast.makeText(getApplicationContext(), "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

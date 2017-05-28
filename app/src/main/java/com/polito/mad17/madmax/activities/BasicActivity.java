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

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.login.LoginSignUpActivity;

import static com.polito.mad17.madmax.activities.MainActivity.auth;
import static com.polito.mad17.madmax.activities.MainActivity.getCurrentUser;


public class BasicActivity extends AppCompatActivity {

    private static final String TAG = BasicActivity.class.getSimpleName();
    protected FrameLayout mainView;
    //private String[] drawerOptions; // nav menu item
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView imgProfile;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    protected FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        View navHeader;

        mainView = (FrameLayout) findViewById(R.id.main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get the item shown in the navigation drawer
        //drawerOptions = getResources().getStringArray(R.array.drawerItem);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        navHeader.setBackgroundColor(Color.BLUE);
        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BasicActivity.this, ProfileEdit.class);
                BasicActivity.this.startActivity(intent);
            }
        });
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // initializing navigation menu
        setUpNavigationView();
    }

    public void setUpNavigationView() {
        final ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;

                switch (item.getItemId()){
                    case R.id.home:
                        if(getLocalClassName().contains("MainActivity")) {
                            drawerLayout.closeDrawers();
                            break;
                        }
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.invite_friend:
                        Log.d(TAG, "my ID is " + getCurrentUser().getID());
                        String deepLink = getString(R.string.invitation_deep_link) + "?inviterUID=" + getCurrentUser().getID();

                        intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                                .setDeepLink(Uri.parse(deepLink))
                                .setMessage(getString(R.string.invitation_message))
                                //                     .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                                .setCallToActionText(getString(R.string.invitation_cta))
                                .build();

                        startActivityForResult(intent, MainActivity.REQUEST_INVITE);
                        break;
                    case R.id.settings:
                        intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.logout:
                        Toast.makeText(getApplicationContext() , "Logout selected", Toast.LENGTH_SHORT).show();
                        auth.signOut();

                        intent = new Intent(getApplicationContext(), LoginSignUpActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        getSupportActionBar().setHomeButtonEnabled(true);
      //  getSupportActionBar().setBackgroundDrawable(getDrawable(R.drawable.calcetto));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == MainActivity.REQUEST_INVITE){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }

                // add event for FRIEND_INVITE todo da modificare se abbiamo tempo di fare anche lo storico per utente
                /*User currentUser = MainActivity.getCurrentUser();
                Event event = new Event(
                        groupID,
                        Event.EventType.GROUP_MEMBER_ADD,
                        currentUser.getUsername(),
                        newMemeber.getUsername()
                );
                event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                FirebaseUtils.getInstance().addEvent(event);*/
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                Toast.makeText(getApplicationContext(), "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* load navigation header menu info like profile image, name, email */
    protected void loadNavHeader() {
        // name and email
        txtName.setText(getCurrentUser().getName() + " " + getCurrentUser().getSurname());
        Log.d(TAG, "name: "+ getCurrentUser().getName() + " - surname: " + getCurrentUser().getSurname());
        txtWebsite.setText(getCurrentUser().getEmail());
        Log.d(TAG, "email: "+ getCurrentUser().getEmail());

        // profile image
        getCurrentUser().loadImage(this, imgProfile);
    }
}

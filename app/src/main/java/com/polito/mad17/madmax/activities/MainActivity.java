package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.polito.mad17.madmax.R;

public class MainActivity extends AppCompatActivity {

    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        drawerOptions = getResources().getStringArray(R.array.drawerItem);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList = (ListView)findViewById(R.id.left_drawer);

        // set the adapter for the Listview
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerOptions));

        // set the click's listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    Toast.makeText(MainActivity.this, "Logout selected", Toast.LENGTH_SHORT).show();
                    auth.signOut();

                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }
}

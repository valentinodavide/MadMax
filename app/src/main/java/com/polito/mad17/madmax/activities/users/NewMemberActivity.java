package com.polito.mad17.madmax.activities.users;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.entities.User;

import java.util.HashMap;

public class NewMemberActivity extends AppCompatActivity {

    private static final String TAG = NewMemberActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private ListView lv;
    private HashMap<String, User> friends = new HashMap<>();
    //todo usare SharedPreferences invece della map globale alreadySelected
    public static HashMap<String, User> alreadySelected = new HashMap<>();
    private HashMapFriendsAdapter adapter;
    //private String myselfID;

    private Button buttonInvite;

    private static final int REQUEST_INVITE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonInvite = (Button) findViewById(R.id.btn_new_friend);

        Intent intent = getIntent();
        //myselfID = intent.getStringExtra("UID");


        lv = (ListView) findViewById(R.id.members);

        databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapshot: dataSnapshot.getChildren())
                {
                    getFriend(friendSnapshot.getKey());
                }

                adapter = new HashMapFriendsAdapter(friends);
                lv.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        //When i click on one friend of the list
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {


                    User item = adapter.getItem(position).getValue();
                    friends.remove(item.getID());
                    adapter.update(friends);
                    adapter.notifyDataSetChanged();

                    alreadySelected.put(item.getID(), item);

                    Context context = NewMemberActivity.this;
                    Class destinationActivity = NewGroupActivity.class;
                    Intent intent = new Intent(context, destinationActivity);
                    intent.putExtra("UID", MainActivity.getCurrentUser().getID());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("userAdded", item);
                    intent.putExtras(bundle);

                    startActivity(intent);

            }
        });

        buttonInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button clicked");
                String deepLink = R.string.invitation_deep_link + "?inviterUID=" + MainActivity.getCurrentUser().getID();

                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setDeepLink(Uri.parse(deepLink))
                        //                     .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                        .setCallToActionText(getString(R.string.invitation_cta))
                        .build();

                startActivityForResult(intent, REQUEST_INVITE);
            }
        });

    }

    public void getFriend(final String id)
    {
        databaseReference.child("users").child(id).addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User u = new User();
                u.setName(dataSnapshot.child("name").getValue(String.class));
                u.setSurname(dataSnapshot.child("surname").getValue(String.class));
                u.setID(dataSnapshot.getKey());

                //se l'amico letto da db non è già stato scelto, lo metto nella lista di quelli
                //che saranno stampati
                if (!alreadySelected.containsKey(u.getID()))
                {
                    friends.put(id, u);
                    adapter.update(friends);
                    adapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

}

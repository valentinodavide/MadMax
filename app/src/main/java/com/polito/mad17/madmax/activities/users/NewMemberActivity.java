package com.polito.mad17.madmax.activities.users;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;

public class NewMemberActivity extends AppCompatActivity {

    String groupID;
    private ListView lv;
    private DatabaseReference mDatabase;
    private boolean newGroupMode = false;
    ArrayList<User> potentialNewMembers = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        newGroupMode = false;

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        //se non c'è groupID allora voglio aggiungere un membro a un NUOVO gruppo che sto creando
        if (groupID == String.valueOf(-1))
            newGroupMode = true;
        else
            newGroupMode = false;

        lv = (ListView) findViewById(R.id.members);



       /* //read all friends
        Query query = mDatabase.child("users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                System.out.println("Dentro primo");
                //read all members that are already in the group
                Query query2 = mDatabase.child("groups").child("0").child("members");

                query2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot2) {

                        //amici che non sono ancora nel gruppo
                        //final HashMap<String, User> potentialNewMembers = new HashMap<>();

                        //per ogni friend, se è gia nel gruppo non lo stampo, altrimenti sì
                        for (DataSnapshot friendSnapshot: dataSnapshot.getChildren())
                        {
                            User u = friendSnapshot.getValue(User.class);
                            //potentialNewMembers.put(u.getID(), u);
                            potentialNewMembers.add(u);
                            //per ogni membro, guardo se il friend è il membro
                            for (DataSnapshot memberSnapshot: dataSnapshot2.getChildren())
                            {
                                if (memberSnapshot.getKey().equals(friendSnapshot.getKey()))
                                {
                                    //friend è già nel gruppo -> deve essere eliminato dai potential
                                    //potentialNewMembers.remove(friendSnapshot.getKey());
                                    potentialNewMembers.remove(u);
                                }
                            }

                        }

                        //stampo lista dei potential
                        BaseAdapter myadapter = new BaseAdapter() {
                            @Override
                            public int getCount() {
                                return potentialNewMembers.size();
                            }

                            @Override
                            public Object getItem(int position) {
                                return null;
                            }

                            @Override
                            public long getItemId(int position) {
                                return 0;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {

                                if (convertView==null) {
                                    convertView=getLayoutInflater().inflate(R.layout.item_friend,parent,false);
                                }

                                //collego le view agli elementi del layout
                                ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
                                TextView name=(TextView)convertView.findViewById(R.id.name);

                                User f = potentialNewMembers.get(position);



                                //setto le view con gli elementi estratti dal FriendItem
                                String p = f.getProfileImage();
                                int photoId;
                                if (p != null)
                                {
                                    photoId = Integer.parseInt(p);
                                    photo.setImageResource(photoId);
                                }

                                name.setText(f.getName() + " " + f.getSurname());


                                return convertView;
                            }

                        };

                        lv.setAdapter(myadapter);






                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });







            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


        //display list of all friends
        final FirebaseListAdapter<User> firebaseListAdapter = new FirebaseListAdapter<User>(
                this,   //activity contentente la ListView
                User.class,   //classe in cui viene messo il dato letto (?)
                R.layout.item_friend,   //layout del singolo item
                mDatabase.child("users") //nodo del db da cui leggo
        ) {
            @Override
            protected void populateView(View v, User model, int position) {

                Log.d("DEBUG", model.toString());
                TextView nametext = (TextView) v.findViewById(R.id.tv_friend_name);
                nametext.setText(model.getName() + " " + model.getSurname());

            }
        };

        lv.setAdapter(firebaseListAdapter);




        //When i click on one friend of the list
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {


                    User item = (User) firebaseListAdapter.getItem(position);
                    mDatabase.child("temporarygroups").child(groupID).child("members").push();
                    mDatabase.child("temporarygroups").child(groupID).child("members").child(item.getID()).setValue(item);

                    NewGroupActivity.newmembers.put(item.getID(), item);


                    Context context = NewMemberActivity.this;
                    Class destinationActivity = NewGroupActivity.class;
                    Intent intent = new Intent(context, destinationActivity);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("userAdded", item);
                    intent.putExtra("groupID", groupID);
                    intent.putExtra("newUserAdded", bundle);
                    startActivity(intent);



            }
        });







    }

}

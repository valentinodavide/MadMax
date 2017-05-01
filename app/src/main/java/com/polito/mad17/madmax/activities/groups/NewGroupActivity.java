package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.users.NewMemberActivity;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.polito.mad17.madmax.activities.MainActivity.myself;

public class NewGroupActivity extends AppCompatActivity {

    private EditText nameGroup;
    private EditText descriptionGroup;
    private ImageView imageGroup;
    private String imageString = null;
    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the group image
    private ListView lv;
    private DatabaseReference mDatabase;
    String tempGroupID;
    public static HashMap<String, User> newmembers = new HashMap<>();

    public static HashMap<String, Group> groups = myself.getUserGroups();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        nameGroup = (EditText) findViewById(R.id.et_name_group);
        descriptionGroup = (EditText) findViewById(R.id.et_description_group);
        imageGroup = (ImageView) findViewById(R.id.group_image);
//        imageGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // allow to choose the group image
//                Intent intent = new Intent();
//                // Show only images, no videos or anything else
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                // Always show the chooser (if there are multiple options available)
//                startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_IMAGE_REQUEST);
//            }
//        });

        Intent intent = getIntent();
        tempGroupID = intent.getStringExtra("groupID");




        //Button to add a new member
        Button newMemberButton = (Button) findViewById(R.id.addmember);
        newMemberButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Context context = NewGroupActivity.this;
                Class destinationActivity = NewMemberActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                intent.putExtra("groupID", tempGroupID);
                //startActivity(intent);
                startActivityForResult(intent, 1);


            }
        });


        lv = (ListView) findViewById(R.id.members);




        FirebaseListAdapter<User> firebaseListAdapter = new FirebaseListAdapter<User>(
                this,   //activity contentente la ListView
                User.class,   //classe in cui viene messo il dato letto (?)
                R.layout.item_friend,   //layout del singolo item
                mDatabase.child("temporarygroups").child(tempGroupID).child("members")  //nodo del db da cui leggo
        ) {
            @Override
            protected void populateView(View v, User model, int position) {

                Log.d("DEBUG", model.toString());
                TextView nametext = (TextView) v.findViewById(R.id.tv_friend_name);
                nametext.setText(model.getName() + " " + model.getSurname());

            }
        };


        lv.setAdapter(firebaseListAdapter);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }


    //When i click SAVE
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.action_save) {

            //display message if text field is empty
            if(TextUtils.isEmpty(nameGroup.getText().toString())){
                nameGroup.setError(getString(R.string.required));
                return false;
            }
            Integer newID = groups.size();
            String name = nameGroup.getText().toString();
            String description = descriptionGroup.getText().toString();

            Group newGroup = new Group(newID.toString(), name, "noImage", description);
            //Group newGroup = new Group(tempGroupID, name, "noImage", description);

            //add new group to database
            mDatabase.child("groups").child(tempGroupID).setValue(newGroup);

            //add users to new group
            for(Map.Entry<String, User> user : newmembers.entrySet())
            {
                joinGroupFirebase(user.getValue(), newGroup);
            }


            Intent intent = new Intent(NewGroupActivity.this, MainActivity.class);

//          Log.d("DEBUG", "groups.size() before " + groups.size());

            if(imageString!=null)
                newGroup = new Group(newID.toString(), name, imageString, description);
            else
                newGroup = new Group(newID.toString(), name, String.valueOf(R.mipmap.group), description);

            groups.put(newGroup.getID(), newGroup);


            //remove group from temporary
            mDatabase.child("temporarygroups").child(tempGroupID).removeValue();
            newmembers.clear();

            Toast.makeText(getBaseContext(), "Saved group", Toast.LENGTH_SHORT).show();

            NewGroupActivity.this.startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void joinGroupFirebase (final User u, Group g)
    {
        //Creo istanza del gruppo nella lista gruppi dello user
        mDatabase.child("users").child(u.getID()).child("groups").push();
        mDatabase.child("groups").child(g.getID()).child("members").push();

        Map <String, Object> groupValues = g.toMap();
        Map <String, Object> userValues = u.toMap();

        Map <String, Object> childUpdates = new HashMap<>();
        //metto nella map il gruppo a cui appartiene lo user
        childUpdates.put("/users/" + u.getID() + "/groups/" + g.getID(), groupValues);
        childUpdates.put("/groups/" + g.getID() + "/members/" + u.getID(), userValues);

        mDatabase.updateChildren(childUpdates);

        //creo un debito verso il gruppo
        mDatabase.child("users").child(u.getID()).child("groups").child(g.getID()).child("balanceWithGroup").setValue(0);

        Query query = mDatabase.child("groups").child(g.getID()).child("members").orderByKey();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    System.out.println("There is at least one member in this group");
                    for (DataSnapshot memberSnapshot: dataSnapshot.getChildren())
                    {
                        System.out.println(memberSnapshot.getKey());
                        //se il nuovo user non è già presente nel gruppo (teoricamente impossibile)
                        if (!memberSnapshot.getKey().equals(u.getID()))
                        {
                            //se il bilancio tra nuovo user e membro del gruppo non esiste già
                            if (!memberSnapshot.child("balancesWithUsers").hasChild(u.getID()))
                            {
                                //creo bilancio da membro a nuovo user
                                mDatabase.child("users").child(memberSnapshot.getKey()).child("balancesWithUsers").child(u.getID()).setValue(0);
                                //creo bilancio da nuovo user a membro
                                mDatabase.child("users").child(u.getID()).child("balancesWithUsers").child(memberSnapshot.getKey()).setValue(0);
                            }

                        }
                        else
                        {
                            System.out.println("User is already present in the group!");
                        }


                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // first of all control if is the requested result and if it return something
//        if(requestCode==PICK_IMAGE_REQUEST && data != null && data.getData()!=null){
//            Uri uri = data.getData();
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                imageGroup.setImageBitmap(bitmap);
//                imageString = BitMapToString(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}

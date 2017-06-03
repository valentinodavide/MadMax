package com.polito.mad17.madmax.activities.groups;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.users.FriendsViewAdapter;
import com.polito.mad17.madmax.activities.users.HashMapFriendsAdapter;
import com.polito.mad17.madmax.activities.users.NewMemberActivity;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TreeMap;


public class NewGroupActivity extends AppCompatActivity implements FriendsViewAdapter.ListItemClickListener {

    private static final String TAG = "NewGroupActivity";

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    private EditText nameGroup;
    private EditText descriptionGroup;
    private ImageView imageGroup;
    private Boolean imageSetted = false;
    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the group image
    //private int REQUEST_INVITE = 2; // to use for selecting a contact to invite
 //   private ListView lv;
    String tempGroupID;
    public static HashMap<String, User> newmembers = new HashMap<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FriendsViewAdapter friendsViewAdapter;
    private OnItemClickInterface onClickFriendInterface;
    private User userAdded;
    private HashMapFriendsAdapter adapter;
    //private String myselfID;

    public static TreeMap<String, Group> groups = MainActivity.getCurrentUser().getUserGroups();
    private Uri ImageUri;
    private String newgroup_id;

    @TargetApi(23) // used for letting AndroidStudio know that method requestPermissions() is called
    // in a controlled way: in particular it must be accessed only if API >= 23, and we guarantee it
    // via the static method MainActivity.shouldAskPermission()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

     //   lv = (ListView) findViewById(R.id.members);

        nameGroup = (EditText) findViewById(R.id.et_name_group);
        descriptionGroup = (EditText) findViewById(R.id.et_description_group);
        imageGroup = (ImageView) findViewById(R.id.group_image);
        Glide.with(this).load(R.drawable.group_default)
                .centerCrop()
                //.bitmapTransform(new CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageGroup);

        imageGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "image clicked");

                if (MainActivity.shouldAskPermission()) {
                    String[] perms = {"android.permission.READ_EXTERNAL_STORAGE"};

                    int permsRequestCode = 200;
                    requestPermissions(perms, permsRequestCode);
                }
                // allow to the user the choose his profile image
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_IMAGE_REQUEST);
                // now see onActivityResult
            }
        });
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

            Log.d(TAG, "Second step: invite members to group");
            //        String deepLink = getString(R.string.invitation_deep_link) + "?groupToBeAddedID=" + groupID+ "?inviterToGroupUID=" + MainActivity.getCurrentUser().getID();

            newgroup_id = databaseReference.child("groups").push().getKey();

            String name = nameGroup.getText().toString();
            String description = descriptionGroup.getText().toString();

            final Group newGroup = new Group("0", name, "noImage", description, 1);  //id is useless

            // for saving image
            if (imageSetted) {
                StorageReference uProfileImageFilenameRef = storageReference.child("groups").child(newgroup_id).child(newgroup_id + "_profileImage.jpg");

                // Get the data from an ImageView as bytes
                imageGroup.setDrawingCacheEnabled(true);
                imageGroup.buildDrawingCache();
                Bitmap bitmap = imageGroup.getDrawingCache();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                UploadTask uploadTask = uProfileImageFilenameRef.putBytes(imageData);

                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            newGroup.setImage(task.getResult().getDownloadUrl().toString());
                            Log.d(TAG, "group img url: " + newGroup.getImage());
                        }
                    }
                });
            }

            databaseReference.child("groups").child(newgroup_id).setValue(newGroup);
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            databaseReference.child("groups").child(newgroup_id).child("timestamp").setValue(timeStamp);
            databaseReference.child("groups").child(newgroup_id).child("numberMembers").setValue(1);
            FirebaseUtils.getInstance().joinGroupFirebase(MainActivity.getCurrentUser().getID(), newgroup_id);
            Log.d(TAG, "group " + newgroup_id + " created");

            // add event for GROUP_ADD
            User currentUser = MainActivity.getCurrentUser();
            Event event = new Event(
                    newgroup_id,
                    Event.EventType.GROUP_ADD,
                    currentUser.getName() + " " + currentUser.getSurname(),
                    newGroup.getName()
            );
            event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
            event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
            FirebaseUtils.getInstance().addEvent(event);

            Intent intent = new Intent(getApplicationContext(), NewMemberActivity.class);
            intent.putExtra("groupID", newgroup_id);
            intent.putExtra("groupName", name);
            startActivity(intent);

            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void onListItemClick(String friendID) {
        //Log.d("clickedItemIndex " + friendID);
        System.out.println("clickedItemIndex " + friendID);
        onClickFriendInterface.itemClicked(getClass().getSimpleName(), friendID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // control if is the requested result and if it return something
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult PICK_IMAGE_REQUEST");
            ImageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageGroup.setImageBitmap(bitmap);
            imageSetted = true;

            /*// Log.d(TAG, String.valueOf(bitmap));
            Glide.with(this).load(ImageUri)
                    .placeholder(R.drawable.group_default)
                    .centerCrop()
                    .into(imageGroup);*/
        }
        else {
            // control if is the requested result and if it return something
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult REQUEST_INVITE");
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "sent invitation " + id);
                }

                //Integer newID = groups.size();
                /*String name = nameGroup.getText().toString();
                String description = descriptionGroup.getText().toString();

                final Group newGroup = new Group("0", name, "noImage", description, 1);  //id is useless

                // for saving image
                StorageReference uProfileImageFilenameRef = storageReference.child("groups").child(newgroup_id).child(newgroup_id + "_profileImage.jpg");

                // Get the data from an ImageView as bytes
                imageGroup.setDrawingCacheEnabled(true);
                imageGroup.buildDrawingCache();
                Bitmap bitmap = imageGroup.getDrawingCache();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                UploadTask uploadTask = uProfileImageFilenameRef.putBytes(imageData);

                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            newGroup.setImage(task.getResult().getDownloadUrl().toString());
                            Log.d(TAG, "group img url: " + newGroup.getImage());
                        }
                        databaseReference.child("groups").child(newgroup_id).setValue(newGroup);
                        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                        databaseReference.child("groups").child(newgroup_id).child("timestamp").setValue(timeStamp);
                        databaseReference.child("groups").child(newgroup_id).child("numberMembers").setValue(1);
                        FirebaseUtils.getInstance().joinGroupFirebase(MainActivity.getCurrentUser().getID(), newgroup_id);


                        Log.d(TAG, "group " + newgroup_id + " created");
                        finish();
                    }
                });*/
            }
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
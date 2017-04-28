package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Group;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.polito.mad17.madmax.activities.MainActivity.groups;

public class NewGroupActivity extends AppCompatActivity {

    private EditText nameGroup;
    private EditText descriptionGroup;
    private ImageView imageGroup;
    private String imageString = null;
    private int PICK_IMAGE_REQUEST = 1; // to use for selecting the group image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        nameGroup = (EditText) findViewById(R.id.et_name_group);
        descriptionGroup = (EditText) findViewById(R.id.et_description_group);
        imageGroup = (ImageView) findViewById(R.id.group_image);
        imageGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // allow to choose the group image
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

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

            Intent intent = new Intent(NewGroupActivity.this, MainActivity.class);

//          Log.d("DEBUG", "groups.size() before " + groups.size());

            Group newGroup;
            if(imageString!=null)
                newGroup = new Group(newID.toString(), name, imageString, description);
            else
                newGroup = new Group(newID.toString(), name, String.valueOf(R.mipmap.group), description);

            groups.put(newGroup.getID(), newGroup);

//          Log.d("DEBUG", "groups.size() after " + groups.size());

            Toast.makeText(getBaseContext(), "Saved group", Toast.LENGTH_SHORT).show();

            NewGroupActivity.this.startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // first of all control if is the requested result and if it return something
        if(requestCode==PICK_IMAGE_REQUEST && data != null && data.getData()!=null){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageGroup.setImageBitmap(bitmap);
                imageString = BitMapToString(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}

package com.polito.mad17.madmax.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.R;

import static com.polito.mad17.madmax.activities.MainActivity.groups;

public class NewGroupActivity extends AppCompatActivity {

    private EditText nameGroup;
    private EditText descriptionGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        nameGroup = (EditText) findViewById(R.id.et_name_group);
        descriptionGroup = (EditText) findViewById(R.id.et_description_group);
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
            Toast.makeText(getBaseContext(), "Saved group", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(NewGroupActivity.this, MainActivity.class);

//          Log.d("DEBUG", "groups.size() before " + groups.size());
            Integer newID = groups.size();
            String name = nameGroup.getText().toString();
            String description = descriptionGroup.getText().toString();

            Group newGroup = new Group(newID.toString(), name, "noImage", description);
            groups.put(newGroup.getID(), newGroup);

//          Log.d("DEBUG", "groups.size() after " + groups.size());

            NewGroupActivity.this.startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

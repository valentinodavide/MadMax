package com.polito.mad17.madmax.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.polito.mad17.madmax.R;

public class GroupDetailsActivity extends AppCompatActivity {

    private TextView groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        groupName = (TextView) findViewById(R.id.tv_group_name);
        groupName.setText(intent.getStringExtra("groupName"));
    }

    public void onClickAddExpense(View view) {
        Intent myIntent = new Intent(GroupDetailsActivity.this, NewExpenseActivity.class);
        GroupDetailsActivity.this.startActivity(myIntent);
    }
}

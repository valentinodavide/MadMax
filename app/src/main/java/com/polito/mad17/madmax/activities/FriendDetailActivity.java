package com.polito.mad17.madmax.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;

public class FriendDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_friend_detail);

        View view = getLayoutInflater().inflate(R.layout.activity_friend_detail, null);

        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView surname=(TextView)view.findViewById(R.id.surname);
        Button balance=(Button) view.findViewById(R.id.balancebutton);

        //Extract data from bundle
        Bundle bundle = getIntent().getExtras();
        String n = bundle.getString("name");
        String s = bundle.getString("surname");
        Integer p = bundle.getInt("photoid");
        Integer b = bundle.getInt("balance");
        System.out.println(n + s);

        photo.setImageResource(p);
        name.setText(n);
        surname.setText(s);
        balance.setText("TOTAL BALANCE: " + b);


        setContentView(view);








    }
}

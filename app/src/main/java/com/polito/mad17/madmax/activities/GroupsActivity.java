package com.polito.mad17.madmax.activities;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.R;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupsActivity extends AppCompatActivity {

    public static HashMap<String, Group> groups = new HashMap<>();
    private ListView listView;
    public static ArrayList<User> users = new ArrayList<>();
    public static User myself;


    Integer[] imgid={
            R.drawable.obama,
            R.drawable.putin
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(groups.isEmpty()) {
            for (int i = 0; i <= 3; i++) {
                Group group = new Group(String.valueOf(i), "Group" + i, "imgGroup" + i, "description");

                for (int j = 0; j <= 4; j++) {
                    User user = new User(String.valueOf(j), "Name1", "Surname1", "ImageProfile1");
                    group.getMembers().put(user.getID(), user);
                }

                Log.d("DEBUG", group.toString());
                groups.put(group.getID(), group);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        Button friendsbutton = (Button) findViewById(R.id.friendsbutton);

        friendsbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Context context = GroupsActivity.this;
                Class destinationActivity = FriendsActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                startActivity(intent);

            }
        });

        listView = (ListView) findViewById(R.id.lv_list_groups);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(GroupsActivity.this, NewGroupActivity.class);
                GroupsActivity.this.startActivity(myIntent);

            }
        });

        //Create users
        User u1 = new User ("u01", "Alessandro", "Rota", null);
        User u2 = new User ("u02", "Barack", "Obama", String.valueOf(imgid[0]));
        User u3 = new User ("u03", "Vladimir", "Putin", String.valueOf(imgid[1]));
        //Add to users list (needed to share data with other activities)
        users.add(u2);
        users.add(u3);
        myself = u1;


        Group g1 = new Group("g01", "Vacanze", null, null);
        Group g2 = new Group("g02", "Calcetto", null, null);

        //Add users to group
        u1.joinGroup(g1);
        u2.joinGroup(g1);
        u3.joinGroup(g1);
        u1.joinGroup(g2);
        u2.joinGroup(g2);

        Expense e1 = new Expense("e01", "Pane", "Cibo", 30d, true, g1);
        Expense e2 = new Expense("e02", "Acqua", "Cibo", 20d, true, g1);
        Expense e3 = new Expense("e03", "Partita", "Sport", 5d, true, g2);

        u1.addExpense(e1);
        u2.addExpense(e2);
        u1.addExpense(e3);


        ListAdapter listAdapter = new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) { }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) { }

            @Override
            public int getCount() {
                return groups.size();
            }

            @Override
            public Object getItem(int position) {
                return groups.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if(convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_group, parent, false);
                }

//                Log.d("DEBUG", "group " + position groups.get(String.valueOf(position)).toString());
                Log.d("DEBUG", "position " + position);
                Group group = groups.get(String.valueOf(position));

                // ImageView groupImage = (ImageView) convertView.findViewById(R.id.img_group);
                // groupImage.setImageResource(group.getImage());

                TextView groupName = (TextView) convertView.findViewById(R.id.tv_group_name);
                groupName.setText(group.getName());
                groupName.setTag(group.getID());

                TextView numberNotifications = (TextView) convertView.findViewById(R.id.tv_group_debt);
                numberNotifications.setText(group.getNumberNotifications().toString());

                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };

        listView.setAdapter(listAdapter);
    }

    public void onClickOpenGroup(View view) {

        TextView groupName = (TextView) view;
        Log.d("DEBUG", groupName.getTag().toString());


        Intent myIntent = new Intent(GroupsActivity.this, GroupExpensesActivity.class);
        myIntent.putExtra("addExpense", false);
        myIntent.putExtra("groupName", groupName.getText().toString()); //Optional parameters
        GroupsActivity.this.startActivity(myIntent);
    }

//    private void scaleDownImage() {
//
//        Bitmap background = Bitmap.createBitmap((int)width, (int)height, Config.ARGB_8888);
//
//        float originalWidth = originalImage.getWidth();
//        float originalHeight = originalImage.getHeight();
//
//        Canvas canvas = new Canvas(background);
//
//        float scale = width / originalWidth;
//
//        float xTranslation = 0.0f;
//        float yTranslation = (height - originalHeight * scale) / 2.0f;
//
//        Matrix transformation = new Matrix();
//        transformation.postTranslate(xTranslation, yTranslation);
//        transformation.preScale(scale, scale);
//
//        Paint paint = new Paint();
//        paint.setFilterBitmap(true);
//
//        canvas.drawBitmap(originalImage, transformation, paint);
//
//        return background;
//    }

}

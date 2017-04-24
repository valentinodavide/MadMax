package com.polito.mad17.madmax.activities;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.R;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupsActivity extends AppCompatActivity {

    public static HashMap<String, Group> groups = new HashMap<>();
    private ListView listView;
    public static ArrayList<User> users = new ArrayList<>();
    public static User myself;


    Integer[] imgid={
            R.drawable.ale,
            R.drawable.davide,
            R.drawable.chiara,
            R.drawable.riki,
            R.drawable.rossella,
            R.drawable.vacanze,
            R.drawable.calcetto,
            R.drawable.casa,
            R.drawable.pasquetta,
            R.drawable.fantacalcio,
            R.drawable.alcolisti
    };

    Integer[] img_expense={
            R.drawable.expense1,
            R.drawable.expense2,
            R.drawable.expense3,
            R.drawable.expense4,
            R.drawable.expense5,
            R.drawable.expense6,
            R.drawable.expense7
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        JSONObject prova = new JSONObject();
        try {
            prova.put("nome", "chiara");
            prova.put("cognome", "nome");

            myRef.setValue(prova);
        }
        catch(org.json.JSONException exception)
        {
            exception.printStackTrace();
        }

        myRef.setValue("Hello, World!");
        */

//        scaleDownImage("E:\\Chiara\\Documents\\PoliTo\\MAD\\MadMax\\app\\src\\main\\res\\drawable\\obama.jpg");

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

        /*
        for(int i = 0; i <= 20; i++)
        {
            Group group = new Group(String.valueOf(i), "Group" + i, "imgGroup" + i);

            for(int j = 0; j <= 4; j++)
            {
                User user = new User(String.valueOf(j), "Name1", "Surname1", "ImageProfile1");
                group.getMembers().put(user.getID(), user);
            }

            Log.d("DEBUG", group.toString());
            groups.put(group.getID(), group);
        }
        */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(GroupsActivity.this, NewGroupActivity.class);
                GroupsActivity.this.startActivity(myIntent);

            }
        });

        if (users.isEmpty()) {
            //Create users
            User u0 = new User(String.valueOf(0), "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null);
            User u1 = new User(String.valueOf(1), "Alero3",         "Alessandro", "Rota",       "email1@email.it", "password1", String.valueOf(imgid[0]));
            User u2 = new User(String.valueOf(2), "deviz92",        "Davide", "Valentino",      "email2@email.it", "password2", String.valueOf(imgid[1]));
            User u3 = new User(String.valueOf(3), "missArmstrong",  "Chiara", "Di Nardo",       "email3@email.it", "password3", String.valueOf(imgid[2]));
            User u4 = new User(String.valueOf(4), "rickydivi",      "Riccardo", "Di Vittorio",  "email4@email.it", "password4", String.valueOf(imgid[3]));
            User u5 = new User(String.valueOf(5), "roxy",           "Rossella", "Mangiardi",    "email5@email.it", "password5", String.valueOf(imgid[4]));

            //Add to users list (needed to share data with other activities)
            users.add(u1);
            users.add(u2);
            users.add(u3);
            users.add(u4);
            users.add(u5);

            Group g1 = new Group(String.valueOf(0), "Vacanze",      String.valueOf(imgid[5]), "description0");
            Group g2 = new Group(String.valueOf(1), "Calcetto",     String.valueOf(imgid[6]), "description1");
            Group g3 = new Group(String.valueOf(2), "Spese Casa",   String.valueOf(imgid[7]), "description2");
            Group g4 = new Group(String.valueOf(3), "Pasquetta",   String.valueOf(imgid[8]), "description3");
            Group g5 = new Group(String.valueOf(4), "Fantacalcio",   String.valueOf(imgid[9]), "description4");
            Group g6 = new Group(String.valueOf(5), "Alcolisti Anonimi",   String.valueOf(imgid[10]), "description5");


            //Add users to group
            u0.joinGroup(g1);
            u1.joinGroup(g1);
            u2.joinGroup(g1);
            u3.joinGroup(g1);
            u4.joinGroup(g1);
            u5.joinGroup(g1);

            u0.joinGroup(g2);
            u1.joinGroup(g2);
            u2.joinGroup(g2);
            u4.joinGroup(g2);

            u0.joinGroup(g3);
            u4.joinGroup(g3);

            u0.joinGroup(g4);
            u2.joinGroup(g4);

            u0.joinGroup(g5);
            u2.joinGroup(g5);

            u0.joinGroup(g6);
            u2.joinGroup(g6);


            Expense e1 = new Expense(String.valueOf(0), "Nutella", "Cibo",          30d, "€",   String.valueOf(img_expense[0]), true, g1);
            Expense e2 = new Expense(String.valueOf(1), "Spese cucina", "Altro",    20d, "€",   String.valueOf(img_expense[1]), true, g1);
            u0.addExpense(e1);
            u3.addExpense(e2);

            Expense e3 = new Expense(String.valueOf(0), "Partita", "Sport",         5d, "€",    String.valueOf(img_expense[2]), true, g2);
            u0.addExpense(e3);

            Expense e4 = new Expense(String.valueOf(0), "Affitto", "Altro",         500d, "€",  String.valueOf(img_expense[3]), true, g3);
            u4.addExpense(e4);

            myself = u0;
            groups.put(g1.getID(), g1);
            groups.put(g2.getID(), g2);
            groups.put(g3.getID(), g3);
            groups.put(g4.getID(), g4);
            groups.put(g5.getID(), g5);
            groups.put(g6.getID(), g6);



        }

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

                //Log.d("DEBUG", groups.get(String.valueOf(position)).toString());
                Group group = groups.get(String.valueOf(position));

                ImageView groupImage = (ImageView) convertView.findViewById(R.id.img_group);
                String p = group.getImage();
                int photoId = Integer.parseInt(p);
                groupImage.setImageResource(photoId);

                TextView groupName = (TextView) convertView.findViewById(R.id.tv_group_name);
                groupName.setText(group.getName());
                groupName.setTag(group.getID());

                TextView balance = (TextView) convertView.findViewById(R.id.tv_group_debt);


                //mydebt = mio debito con il gruppo
                Double mygroupdebt = GroupsActivity.myself.getBalanceWithGroups().get(group.getID());

                DecimalFormat df = new DecimalFormat("#.##");


                if (mygroupdebt > 0)
                {
                    balance.setText("+ " + df.format(mygroupdebt) + " €");
                    balance.setBackgroundResource(R.color.greenBalance);

                }
                else if (mygroupdebt < 0)
                {
                    balance.setText("- " + df.format(Math.abs(mygroupdebt)) + " €");
                    balance.setBackgroundColor(Color.rgb(255,0,0));
                }
                else
                {
                    balance.setText("" + df.format(mygroupdebt) + " €");
                    balance.setBackgroundResource(R.color.greenBalance);
                }


                //numberNotifications.setText(group.getNumberNotifications().toString());

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

        String IDGroup = groupName.getTag().toString();

        Intent myIntent = new Intent(GroupsActivity.this, GroupExpensesActivity.class);
        myIntent.putExtra("addExpenseToGroup", false);
        myIntent.putExtra("IDGroup", IDGroup); //Optional parameters
        GroupsActivity.this.startActivity(myIntent);
    }

//    private void scaleDownImage(String imagePath) {
//
//        String outputFolder = "E:\\Chiara\\Documents\\PoliTo\\MAD\\MadMax\\app\\src\\main\\res\\drawable\\prova";
//
//        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//        BitmapFactory.decodeFile(imagePath, bitmapOptions);
//        Log.d("DEBUG", bitmapOptions.outWidth + " " + bitmapOptions.outHeight);
//
//        Integer imageWidth = bitmapOptions.outWidth;
//        Integer imageHeight = bitmapOptions.outHeight;
//
//        bitmapOptions.inJustDecodeBounds = true;
//        bitmapOptions.inScaled = true;
//        bitmapOptions.inSampleSize = 4;
//        bitmapOptions.inDensity = imageWidth;
//        bitmapOptions.inTargetDensity = (100) * bitmapOptions.inSampleSize;
//
//        Bitmap image = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
//
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = new FileOutputStream(outputFolder);
//            image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
//        }
//        catch(FileNotFoundException e)
//        {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileOutputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}

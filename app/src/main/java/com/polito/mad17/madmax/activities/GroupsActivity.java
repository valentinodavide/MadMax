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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.R;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    private DatabaseReference mDatabase;



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


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(GroupsActivity.this, NewGroupActivity.class);
                GroupsActivity.this.startActivity(myIntent);

            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (users.isEmpty()) {
            //Create users
            User u0 = new User(String.valueOf(0), "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null);
            User u1 = new User(String.valueOf(1), "Alero3",         "Alessandro", "Rota",       "email1@email.it", "password1", String.valueOf(imgid[0]));
            User u2 = new User(String.valueOf(2), "deviz92",        "Davide", "Valentino",      "email2@email.it", "password2", String.valueOf(imgid[1]));
            User u3 = new User(String.valueOf(3), "missArmstrong",  "Chiara", "Di Nardo",       "email3@email.it", "password3", String.valueOf(imgid[2]));
            User u4 = new User(String.valueOf(4), "rickydivi",      "Riccardo", "Di Vittorio",  "email4@email.it", "password4", String.valueOf(imgid[3]));
            User u5 = new User(String.valueOf(5), "roxy",           "Rossella", "Mangiardi",    "email5@email.it", "password5", String.valueOf(imgid[4]));

            //Add users to database
            mDatabase.child("users").child(u0.getID()).setValue(u0);
            mDatabase.child("users").child(u1.getID()).setValue(u1);
            mDatabase.child("users").child(u2.getID()).setValue(u2);
            mDatabase.child("users").child(u3.getID()).setValue(u3);
            mDatabase.child("users").child(u4.getID()).setValue(u4);
            mDatabase.child("users").child(u5.getID()).setValue(u5);



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

            //Add groups to database
            mDatabase.child("groups").child(g1.getID()).setValue(g1);
            mDatabase.child("groups").child(g2.getID()).setValue(g2);
            mDatabase.child("groups").child(g3.getID()).setValue(g3);
            mDatabase.child("groups").child(g4.getID()).setValue(g4);
            mDatabase.child("groups").child(g5.getID()).setValue(g5);
            mDatabase.child("groups").child(g6.getID()).setValue(g6);



            //Aggiungo utente a lista membri del gruppo e gruppo a lista gruppi nell'utente in Firebase
            joinGroupFirebase(u0,g1);
            joinGroupFirebase(u1,g1);
            joinGroupFirebase(u2,g1);
            joinGroupFirebase(u3,g1);
            joinGroupFirebase(u4,g1);
            joinGroupFirebase(u5,g1);


            joinGroupFirebase(u0,g2);
            joinGroupFirebase(u1,g2);
            joinGroupFirebase(u2,g2);
            joinGroupFirebase(u4,g2);

            joinGroupFirebase(u0,g3);
            joinGroupFirebase(u4,g3);

            joinGroupFirebase(u0,g4);
            joinGroupFirebase(u2,g4);

            joinGroupFirebase(u0,g5);
            joinGroupFirebase(u2,g5);

            joinGroupFirebase(u0,g6);
            joinGroupFirebase(u2,g6);




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


            groups.put(g1.getID(), g1);
            groups.put(g2.getID(), g2);
            groups.put(g3.getID(), g3);
            groups.put(g4.getID(), g4);
            groups.put(g5.getID(), g5);
            groups.put(g6.getID(), g6);

            //Spese in g1
            Expense e1 = new Expense(String.valueOf(0), "Nutella", "Cibo",          30d, "€",   String.valueOf(img_expense[0]), true, g1.getID());
            Expense e2 = new Expense(String.valueOf(1), "Spese cucina", "Altro",    20d, "€",   String.valueOf(img_expense[1]), true, g1.getID());
            //u0.addExpense(e1);
            //u3.addExpense(e2);
            addExpenseFirebase(u0,e1);
            addExpenseFirebase(u3,e2);

            //Spese in g2
            Expense e3 = new Expense(String.valueOf(2), "Partita", "Sport",         5d, "€",    String.valueOf(img_expense[2]), true, g2.getID());
            //u0.addExpense(e3);
            addExpenseFirebase(u0,e3);




            //Spese in g3
            Expense e4 = new Expense(String.valueOf(3), "Affitto", "Altro",         500d, "€",  String.valueOf(img_expense[3]), true, g3.getID());
            //u4.addExpense(e4);
            addExpenseFirebase(u4,e4);


            //Add expenses to Firebase

            mDatabase.child("expenses").child(e1.getID()).setValue(e1);
            mDatabase.child("expenses").child(e2.getID()).setValue(e2);
            mDatabase.child("expenses").child(e3.getID()).setValue(e3);
            mDatabase.child("expenses").child(e4.getID()).setValue(e4);



            myself = u0;




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
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void addExpenseFirebase(User u, Expense expense) {
        u.getAddedExpenses().put(expense.getID(), expense);  //spesa aggiunta alla lista spese utente
        //expense.getGroup().getExpenses().put(expense.getID(), expense);   //spesa aggiunta alla lista spese del gruppo
        String groupID = expense.getGroupID();
        Group g = groups.get(groupID);
        if ( g != null)
        {
            g.getExpenses().put(expense.getID(), expense);
        }

        //Creo istanza della spesa nella lista spese dello user e del gruppo
        mDatabase.child("users").child(u.getID()).child("expenses").push();
        mDatabase.child("groups").child(expense.getGroupID()).child("expenses").push();

        Map <String, Object> expensesValues = expense.toMap();

        Map <String, Object> childUpdates = new HashMap<>();
        //metto nella map la spesa
        childUpdates.put("/users/" + u.getID() + "/expenses/" + expense.getID(), expensesValues);
        childUpdates.put("/groups/" + expense.getGroupID() + "/expenses/" + expense.getID(), expensesValues);

        mDatabase.updateChildren(childUpdates);


        //updateBalance(expense);
    }




    /*

    // update balance among other users and among the group this user is part of
    private void updateBalanceFirebase (User u, Expense expense) {
        // todo per ora fa il calcolo come se le spese fossero sempre equamente divise fra tutti i
        // todo     membri del gruppo (cioè come se expense.equallyDivided fosse sempre = true


        Query query = mDatabase.child("expenses").child(expense.getID()).child("groupID");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupID = dataSnapshot.getValue(String.class); //id del guppo in cui è stata messa la spesa
                Double totalExpenseGroup = getTotalExpenseFirebase(groupID);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        Double total = g.getTotalExpense(); //spesa totale del gruppo aggiornata
        Double singlecredit = expense.getAmount() / g.getMembers().size();   //credito che io ho verso ogni singolo utente in virtù della spesa che ho fatto
        Double totalcredit = singlecredit * (g.getMembers().size() -1); //credito totale che io ho verso tutti gli altri membri del gruppo
        //es. se in un gruppo di 5 persone io ho pagato 10, ognuno mi deve 2
        //quindi totalcredit = 2*4 dove 4 è il n. di membri del gruppo diversi da me. In tutto devo ricevere 8.

        Double actualdebts = balanceWithGroups.get(g.getID());
        if (actualdebts != null) {
            //aggiorno il mio debito verso il gruppo
            balanceWithGroups.put(g.getID(), actualdebts + totalcredit);
        }
        else {
            System.out.println("Group not found");
        }

        //per ogni amico del gruppo in cui è stata aggiunta la spesa
        for (HashMap.Entry<String, User> friend : g.getMembers().entrySet()) {
            //se non sono io stesso
            if (!friend.getKey().equals(this.getID())) {
                //aggiorno mio credito verso di lui
                Double balance = balanceWithUsers.get(friend.getKey());
                if (balance != null) {
                    balanceWithUsers.put(friend.getKey(), balance+singlecredit);
                }
                else {
                    System.out.println("Friend not found");
                }

                //aggiorno debito dell'amico verso di me
                HashMap<String, Double> friendBalanceWithUsers = friend.getValue().getBalanceWithUsers();
                balance = friendBalanceWithUsers.get(this.getID());;

                if (balance != null) {
                    friend.getValue().getBalanceWithUsers().put(this.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Io non risulto tra i suoi debiti");
                    // => allora devo aggiungermi
                    friendBalanceWithUsers.put(this.getID(), -singlecredit);
                }

                //aggiorno debito dell'amico verso il gruppo
                HashMap<String, Double> friendBalanceWithGroups = friend.getValue().getBalanceWithGroups();
                balance = friendBalanceWithGroups.get(g.getID());
                if (balance != null) {
                    friend.getValue().getBalanceWithGroups().put(g.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Gruppo non risulta tra i suoi debiti");
                    // => allora lo devo aggiungere
                    friendBalanceWithGroups.put(g.getID(), -singlecredit);
                }
            }
        }

    }

    */
    /*
    //ritorna i soldi totali spesi dal gruppo (packake-private: visibilità di default)
    Double getTotalExpenseFirebase (String groupID) {

        Query query =  mDatabase.child("groups").child(groupID).child("expenses");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double total = 0d;

                for (DataSnapshot expenseSnapshot: dataSnapshot.getChildren())
                {
                    total += expenseSnapshot.child("amount").getValue(Double.class);
                }
                return total;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        for (HashMap.Entry<String, Expense> expense : expenses.entrySet()) {
            total += expense.getValue().getAmount();
        }


        return total;
    }



    }
    */

}

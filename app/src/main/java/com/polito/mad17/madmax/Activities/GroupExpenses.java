package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;

import java.util.ArrayList;

public class GroupExpenses extends AppCompatActivity {
    private ListView lv;
    private int IdGroup;

    //private TextView groupName; from Chiara

    public void onClickAddExpense(View view) {
        Intent myIntent = new Intent(GroupExpenses.this, NewExpenseActivity.class);
        GroupExpenses.this.startActivity(myIntent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // todo IdGroup must arrive from GroupList
        /*if (savedInstanceState == null) {
            return;
        }
        IdGroup = (int) savedInstanceState.get("IdGroup");*/

        setContentView(R.layout.activity_group_expenses);

        Intent intent = getIntent();
        /* from Chiara
        groupName = (TextView) findViewById(R.id.tv_group_name);
        groupName.setText(intent.getStringExtra("groupName"));
        */

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(GroupExpenses.this, NewExpenseActivity.class);
                GroupExpenses.this.startActivity(myIntent);

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });


        // todo getting expenses for that IdGroup and show them (now static values)
        final ArrayList<String> expenses = new ArrayList<>();

        String baseData[] = {
                "Spring", "Summer", "Autumn", "Winter",
                "Spring", "Summer", "Autumn", "Winter",
                "Spring", "Summer", "Autumn", "Winter",
                "Spring", "Summer", "Autumn", "Winter",
                "Spring", "Summer", "Autumn", "Winter",
                "Spring", "Summer", "Autumn", "Winter",
                "Spring", "Summer", "Autumn", "Winter",
                "Spring", "Summer", "Autumn", "Winter"
        };

        for (String s:baseData) {
            expenses.add(s);
        }


        lv = (ListView) findViewById(R.id.expenses);

        // for putting data inside the list view I need an adapter
        BaseAdapter a = new BaseAdapter() {

            @Override
            public int getCount() {
                return expenses.size();
            }

            @Override
            public Object getItem(int position) {
                return expenses.get(position);
            }

            @Override // not changed
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                // if it's the first time I create the view
                if (convertView == null) {
                    //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LayoutInflater inflater = LayoutInflater.from(GroupExpenses.this);

                    // last parameter is very important: for now we don't want to attach our view to the parent view
                    convertView = inflater.inflate(R.layout.item_expense, parent, false);
                }

                //ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
                TextView description = (TextView) convertView.findViewById(R.id.description);
                //TextView price = (TextView) convertView.findViewById(R.id.price);

                // todo foreach expenses retrieved from the db related to IdGroup =>

                    // todo set photo retrieved from database for IdGroup
                    description.setText(expenses.get(position));
                    //price.setText(expenses.get(position).price); ??

                /*b.setOnClickListener(new View.OnClickListener () {
                    @Override
                    public void onClick (View v) {
                        expenses.remove(position);
                        notifyDataSetChanged();
                    }
                });*/

                return convertView;
            }
        };

        lv.setAdapter(a);
    }
}

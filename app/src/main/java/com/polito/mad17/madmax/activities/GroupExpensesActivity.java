package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Expense;

import java.util.HashMap;

public class GroupExpensesActivity extends AppCompatActivity {
    public static HashMap<String, Expense> expenses = new HashMap<>();



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (savedInstanceState == null) {
            return;
        }
        IdGroup = (int) savedInstanceState.get("IdGroup");*/

        setContentView(R.layout.activity_group_expenses);



        // initialize statically a set of expenses for that group
        int i;
        for(i = 0; i < 10; i++)
        {
            //Expense expense = new Expense(String.valueOf(i), "Description expense " + i, (double) i+1);
            Expense expense = new Expense(String.valueOf(i), "Description expense " + i, null, null, null, null);


            Log.d("DEBUG", expense.toString());
            expenses.put(expense.getID(), expense);
        }



        Intent intent = getIntent();
        if (intent.getBooleanExtra("addExpense", true)) {
            // adding a new expense
            String description = intent.getStringExtra("description");
            String amount = intent.getStringExtra("amount");
            String currency = intent.getStringExtra("currency");
            //expenses.put(String.valueOf(i), new Expense(String.valueOf(i), description + " " + currency + " " + amount, (double) i+1));
            //expenses.put(String.valueOf(i), new Expense(String.valueOf(i), "Description expense " + i, null, null, null, null));
        }

        // then show group expenses list

        // todo getting IDGroup from the intent from GroupsActivity

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(GroupExpensesActivity.this, NewExpenseActivity.class);
                GroupExpensesActivity.this.startActivity(myIntent);

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        // todo when we'll use Firebase -> retrieve expenses HashMap with IDGroup received from GroupsActivity

        ListView lv = (ListView) findViewById(R.id.expenses);

        // for putting data inside the list view I need an adapter
        BaseAdapter a = new BaseAdapter() {

            @Override
            public int getCount() {
                return expenses.size();
            }

            @Override
            public Object getItem(int position) {
                return expenses.get(String.valueOf(position));
            }

            @Override // not changed
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // if it's the first time I create the view
                if (convertView == null) {
                    //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LayoutInflater inflater = LayoutInflater.from(GroupExpensesActivity.this);

                    // last parameter is very important: for now we don't want to attach our view to the parent view
                    convertView = inflater.inflate(R.layout.item_expense, parent, false);
                }

                Expense expense = expenses.get(String.valueOf(position));

                //ImageView category = (ImageView) convertView.findViewById(R.id.category);
                TextView description = (TextView) convertView.findViewById(R.id.description);
                TextView amount = (TextView) convertView.findViewById(R.id.amount);

                description.setText(expense.getDescription());
                String amountString = expense.getAmount() + " €";
                amount.setText(amountString);

                return convertView;
            }
        };

        lv.setAdapter(a);
    }
}

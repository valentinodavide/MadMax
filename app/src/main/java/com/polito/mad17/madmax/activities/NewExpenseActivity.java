package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.polito.mad17.madmax.R;

public class NewExpenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        EditText description;
        EditText amount;
        Spinner currency;
        Button saveExpense;

        description = (EditText) findViewById(R.id.edit_description);
        amount = (EditText) findViewById(R.id.edit_amount);
        currency = (Spinner) findViewById(R.id.currency);
        saveExpense = (Button) findViewById(R.id.save_expense);

        // showing values of the spinner for the currencies
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(adapter);

        saveExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText description = (EditText) findViewById(R.id.edit_description);
                EditText amount = (EditText) findViewById(R.id.edit_amount);
                Spinner currency = (Spinner) findViewById(R.id.currency);

                if (description == null || amount == null || currency == null) {
                    return;
                }

                Toast.makeText(getBaseContext(), R.string.expense_saved, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(NewExpenseActivity.this, GroupExpensesActivity.class);
                intent.putExtra("addExpense", true);
                intent.putExtra("description", description.getText());
                intent.putExtra("amount", amount.getText());
                intent.putExtra("currency", currency.getSelectedItem().toString());

                NewExpenseActivity.this.startActivity(intent);
                finish();
            }
        });
    }


    /*
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
            Toast.makeText(getBaseContext(), "Saved expense", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(NewExpenseActivity.this, GroupExpenses.class);
            intent.putExtra("nameExpense", nameExpense.getText());
            intent.putExtra("amountExpense", amountExpense.getText());
            intent.putExtra("categoryExpense", categoryExpense.getText());

            NewExpenseActivity.this.startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

//    public void onClickSaveExpense(View view) {
//
//        //display message if text field is empty
//        Toast.makeText(getBaseContext(), "Saved expense", Toast.LENGTH_SHORT).show();
//
//        Intent intent = new Intent(NewExpenseActivity.this, GroupExpenses.class);
//        intent.putExtra("nameExpense", nameExpense.getText());
//        intent.putExtra("amountExpense", amountExpense.getText());
//        intent.putExtra("categoryExpense", categoryExpense.getText());
//
//        NewExpenseActivity.this.startActivity(intent);
//    }
}

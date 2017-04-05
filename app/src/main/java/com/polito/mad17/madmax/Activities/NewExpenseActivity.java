package com.polito.mad17.madmax.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.polito.mad17.madmax.R;

import java.io.IOException;

import static com.polito.mad17.madmax.R.string.groups;

public class NewExpenseActivity extends AppCompatActivity {

    private EditText nameExpense;
    private EditText amountExpense;
    private EditText categoryExpense;
    private Button saveExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        nameExpense = (EditText) findViewById(R.id.et_name_expense);
        amountExpense = (EditText) findViewById(R.id.et_amount_expense);
        categoryExpense = (EditText) findViewById(R.id.et_category_expense);
        saveExpense = (Button) findViewById(R.id.bt_add_expense);
    }

    public void onClickSaveExpense(View view) {

        //display message if text field is empty
        Toast.makeText(getBaseContext(), "Saved expense", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(NewExpenseActivity.this, GroupExpenses.class);
        intent.putExtra("nameExpense", nameExpense.getText());
        intent.putExtra("amountExpense", amountExpense.getText());
        intent.putExtra("categoryExpense", categoryExpense.getText());

        NewExpenseActivity.this.startActivity(intent);
    }
}

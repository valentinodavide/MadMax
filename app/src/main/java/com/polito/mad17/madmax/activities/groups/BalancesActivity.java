package com.polito.mad17.madmax.activities.groups;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;

import java.util.HashMap;

public class BalancesActivity extends AppCompatActivity implements BalancesViewAdapter.ListItemClickListener{

    private HashMap<String, Double> balances;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private BalancesViewAdapter balancesViewAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skeleton_list);


        Intent intent = getIntent();
        balances = (HashMap<String, Double>) intent.getSerializableExtra("balances");


        RecyclerView.ItemDecoration divider = new InsetDivider.Builder(this)
                .orientation(InsetDivider.VERTICAL_LIST)
                .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                .color(getResources().getColor(R.color.colorDivider))
                .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                .overlay(true)
                .build();

        recyclerView = (RecyclerView) findViewById(R.id.rv_skeleton);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);

        //todo mettere a posto
        balancesViewAdapter = new BalancesViewAdapter(this, this, balances);
        recyclerView.setAdapter(balancesViewAdapter);




    }

    @Override
    public void onListItemClick(String groupID) {
    }
}

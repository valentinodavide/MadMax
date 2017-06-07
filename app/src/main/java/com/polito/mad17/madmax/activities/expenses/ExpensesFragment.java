package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.Collections;
import java.util.TreeMap;

public class ExpensesFragment extends Fragment implements ExpensesViewAdapter.ListItemClickListener, ExpensesViewAdapter.ListItemLongClickListener {

    private static final String TAG = ExpensesFragment.class.getSimpleName();

    private OnItemClickInterface onClickFriendInterface;
    private OnItemLongClickInterface onLongClickGroupInterface;
    private ValueEventListener groupListener;
    private String groupID;


    public void setInterface(OnItemClickInterface onItemClickInterface, OnItemLongClickInterface onItemLongClickInterface) {
        onClickFriendInterface = onItemClickInterface;
        onLongClickGroupInterface = onItemLongClickInterface;
    }

    private ExpensesViewAdapter expensesViewAdapter;

    private DatabaseReference databaseReference = FirebaseUtils.getDatabaseReference();
    private DatabaseReference groupRef;

    private TreeMap<String, Expense> expensesMap = new TreeMap<>(Collections.reverseOrder());


    public ExpensesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        setInterface((OnItemClickInterface) getActivity(), (OnItemLongClickInterface) getActivity());

        View view = inflater.inflate(R.layout.skeleton_list, container, false);

        Bundle bundle = getArguments();
        groupID = bundle.getString("groupID");

        RecyclerView.ItemDecoration divider = new InsetDivider.Builder(getContext())
                .orientation(InsetDivider.VERTICAL_LIST)
                .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                .color(ContextCompat.getColor(getContext(), R.color.colorDivider))
                .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                .overlay(true)
                .build();

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);

        expensesViewAdapter = new ExpensesViewAdapter(this.getContext(), this, this, expensesMap);
        recyclerView.setAdapter(expensesViewAdapter);

        groupRef = databaseReference.child("groups");

        Log.d(TAG, groupID);
        // retrieving group details for current group
        groupRef.child(groupID).child("expenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot expensesSnapshot) {

                for(DataSnapshot expense : expensesSnapshot.getChildren())
                {
                    FirebaseUtils.getInstance().getExpense(expense.getKey(), expensesMap, expensesViewAdapter);
                    Log.d(TAG, expense.getKey());
                }

                expensesViewAdapter.update(expensesMap);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(String expenseID) {
        Log.d(TAG, "clickedItemIndex " + expenseID);
        onClickFriendInterface.itemClicked(getClass().getSimpleName(), expenseID);
    }

    @Override
    public boolean onListItemLongClick (String friendID, View v) {
        Log.d(TAG, "longClickedItemIndex " + friendID);
        onLongClickGroupInterface.itemLongClicked(getClass().getSimpleName(), friendID, v);
        return true;
    }
}
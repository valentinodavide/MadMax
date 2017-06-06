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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.Collections;
import java.util.TreeMap;

public class PendingExpensesFragment extends Fragment implements PendingExpenseViewAdapter.ListItemClickListener, PendingExpenseViewAdapter.ListItemLongClickListener {

    private static final String TAG = PendingExpensesFragment.class.getSimpleName();
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private TreeMap<String, Expense> pendingExpensesMap = new TreeMap<>(Collections.reverseOrder());

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private PendingExpenseViewAdapter pendingExpenseViewAdapter;

    private OnItemClickInterface onClickGroupInterface;
    private OnItemLongClickInterface onLongClickGroupInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface, OnItemLongClickInterface onItemLongClickInterface) {
        onClickGroupInterface = onItemClickInterface;
        onLongClickGroupInterface = onItemLongClickInterface;
    }

    public PendingExpensesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d (TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d (TAG, "onCreateView");

        setInterface((OnItemClickInterface) getActivity(), (OnItemLongClickInterface) getActivity());

        View view = inflater.inflate(R.layout.skeleton_list, container, false);

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

        pendingExpenseViewAdapter = new PendingExpenseViewAdapter(this.getContext() ,this, this, pendingExpensesMap);
        recyclerView.setAdapter(pendingExpenseViewAdapter);


        //Ascolto le pending expenses dello user
        databaseReference.child("users").child(MainActivity.getCurrentUID()).child("proposedExpenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Per ogni pending expense dello user
                for (DataSnapshot pendingExpenseSnap : dataSnapshot.getChildren())
                {
                    //Se la pending expense non è stata eliminata (NELLO USER)
                    if (pendingExpenseSnap.getValue(Boolean.class))
                    {
                        FirebaseUtils.getInstance().getPendingExpense(pendingExpenseSnap.getKey(), pendingExpensesMap, pendingExpenseViewAdapter);
                        pendingExpenseViewAdapter.update(pendingExpensesMap);
                        pendingExpenseViewAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        //tolgo la spesa da quelle che verranno stampate, così la vedo sparire realtime
                        pendingExpensesMap.remove(pendingExpenseSnap.getKey());
                        pendingExpenseViewAdapter.update(pendingExpensesMap);
                        pendingExpenseViewAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(String groupID) {
        Log.d(TAG, "clickedItemIndex " + groupID);
        onClickGroupInterface.itemClicked(getClass().getSimpleName(), groupID);
    }

    @Override
    public boolean onListItemLongClick(String pendingID, View v) {
        Log.d(TAG, "longClickedItemIndex " + pendingID);
        onLongClickGroupInterface.itemLongClicked(getClass().getSimpleName(), pendingID, v);
        return true;
    }
}


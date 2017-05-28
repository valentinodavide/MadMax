package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.polito.mad17.madmax.activities.DetailFragment;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.entities.Comment;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.Collections;
import java.util.TreeMap;

public class ExpenseCommentsFragment extends Fragment implements ExpenseCommentsViewAdapter.ListItemClickListener {
    private static final String TAG = DetailFragment.class.getSimpleName();

    private OnItemClickInterface onClickGroupInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    private ExpenseCommentsViewAdapter expenseCommentsViewAdapter;
    private DatabaseReference databaseReference = FirebaseUtils.getDatabaseReference();
    private TreeMap<String, Comment> commentsMap = new TreeMap<>(Collections.<String>reverseOrder());

    public ExpenseCommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        String expenseID;

        View view = inflater.inflate(R.layout.skeleton_list, container, false);

        Bundle fragmentArguments = getArguments();
        expenseID = fragmentArguments.getString("expenseID");

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        expenseCommentsViewAdapter = new ExpenseCommentsViewAdapter(this.getContext(), this, commentsMap, getFragmentManager());
        recyclerView.setAdapter(expenseCommentsViewAdapter);

        DatabaseReference expenseRef = databaseReference.child("expenses");
        expenseRef.child(expenseID).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot commentSnapshot) {
                for(DataSnapshot comment : commentSnapshot.getChildren()) {
                    FirebaseUtils.getInstance().getComment(comment.getKey(), commentsMap, expenseCommentsViewAdapter);
                    Log.d(TAG, comment.getKey());
                }

                expenseCommentsViewAdapter.update(commentsMap);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(String itemID) { // the itemID will be the commentID
        Log.d(TAG, "clickedItemIndex " + itemID);
        onClickGroupInterface.itemClicked(getClass().getSimpleName(), itemID);
    }
}

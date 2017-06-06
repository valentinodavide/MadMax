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
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.Collections;
import java.util.TreeMap;

public class PendingExpenseDetailFragment extends Fragment {

    private static final String TAG = PendingExpenseDetailFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private VotersViewAdapter votersViewAdapter;

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private TreeMap<String, User> voters = new TreeMap<>(Collections.reverseOrder());
    private String expenseID;

    public PendingExpenseDetailFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.i(TAG, "onCreateView");

        //Read expenseID from ExpenseDetailPagerAdapter
        Bundle b = this.getArguments();
        expenseID = b.getString("expenseID");

        final View view = inflater.inflate(R.layout.skeleton_list, container, false);

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

        //todo mettere a posto
        votersViewAdapter = new VotersViewAdapter(voters, getContext());
        recyclerView.setAdapter(votersViewAdapter);

        //Retrieve data of this pending expense
        databaseReference.child("proposedExpenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Show list of voters for this pending expense
                for (DataSnapshot voterSnap : dataSnapshot.child("participants").getChildren())
                {
                    String vote = voterSnap.child("vote").getValue(String.class);
                    FirebaseUtils.getInstance().getVoter(voterSnap.getKey(), vote, voters, votersViewAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.entities.Expense;

import java.util.HashMap;

public class PendingExpensesFragment extends Fragment implements PendingExpenseViewAdapter.ListItemClickListener {

    private static final String TAG = PendingExpensesFragment.class.getSimpleName();
    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private HashMap<String, Expense> pendingExpensesMap = new HashMap<>();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private PendingExpenseViewAdapter pendingExpenseViewAdapter;

    private OnItemClickInterface onClickGroupInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    public PendingExpensesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setInterface((OnItemClickInterface) getActivity());

        View view = inflater.inflate(R.layout.skeleton_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        pendingExpenseViewAdapter = new PendingExpenseViewAdapter(this, pendingExpensesMap);
        recyclerView.setAdapter(pendingExpenseViewAdapter);


        //Ascolto le pending expenses dello user
        databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("proposedExpenses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Per ogni pending expense dello user
                for (DataSnapshot pendingExpenseSnap : dataSnapshot.getChildren())
                {
                    //Se la pending expense non è stata eliminata
                    if (pendingExpenseSnap.getValue(Boolean.class))
                    {
                        getPendingExpense(pendingExpenseSnap.getKey());
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

    void getPendingExpense (final String pendingID)
    {
        databaseReference.child("proposedExpenses").child(pendingID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Integer participantsCount = 0;
                Integer yes = 0;
                Integer no = 0;

                //Questo listener è chiamato ogni volta che questa spesa pending è modificata, quindi devo controllare
                //che io faccia ancora parte di questa spesa
                if (dataSnapshot.child("participants").hasChild(MainActivity.getCurrentUser().getID()) &&
                        !dataSnapshot.child("participants").child(MainActivity.getCurrentUser().getID()).child("deleted").getValue(Boolean.class) )
                {
                    for (DataSnapshot participantSnap : dataSnapshot.child("participants").getChildren())
                    {
                        //Se il partecipante alla spesa pending esiste ancora (nella spesa pending)
                        if (participantSnap.child("deleted").getValue(Boolean.class) == false)
                        {
                            participantsCount++;
                            if (participantSnap.child("vote").getValue(String.class).equals("yes"))
                                yes++;
                            if (participantSnap.child("vote").getValue(String.class).equals("no"))
                                no++;
                        }
                    }

                    //todo mettere foto

                    Expense pendingExpense = new Expense();
                    pendingExpense.setDescription(dataSnapshot.child("description").getValue(String.class));
                    pendingExpense.setGroupName(dataSnapshot.child("groupName").getValue(String.class));
                    pendingExpense.setAmount(dataSnapshot.child("amount").getValue(Double.class));
                    pendingExpense.setParticipantsCount(participantsCount);
                    pendingExpense.setYes(yes);
                    pendingExpense.setNo(no);
                    pendingExpense.setCurrency(dataSnapshot.child("currency").getValue(String .class));
                    pendingExpensesMap.put(pendingID, pendingExpense);
                    pendingExpenseViewAdapter.update(pendingExpensesMap);
                    pendingExpenseViewAdapter.notifyDataSetChanged();
                }

                //Se io non faccio più parte di questa spesa pending
                else
                {
                    pendingExpensesMap.remove(pendingID);
                    pendingExpenseViewAdapter.update(pendingExpensesMap);
                    pendingExpenseViewAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}


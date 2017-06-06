package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.net.Uri;
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
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.HashMap;

public class ExpenseDetailFragment extends Fragment implements ParticipantsViewAdapter.ListItemClickListener {

    private static final String TAG = ExpenseDetailFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ParticipantsViewAdapter participantsViewAdapter;
    private OnItemClickInterface onClickFriendInterface;
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private HashMap<String, User> participants = new HashMap<>();
    private String expenseID;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickFriendInterface = onItemClickInterface;
    }

    public ExpenseDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.i(TAG, "onCreateView");

        setInterface((OnItemClickInterface) getActivity());

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

        participantsViewAdapter = new ParticipantsViewAdapter(this.getContext(), this, participants);
        recyclerView.setAdapter(participantsViewAdapter);

        //Ascolto i participants alla spesa
        databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Per ogni participant
                for (DataSnapshot participantSnap : dataSnapshot.child("participants").getChildren())
                {
                    Double alreadyPaid = participantSnap.child("alreadyPaid").getValue(Double.class);
                    Double dueImport = alreadyPaid - participantSnap.child("fraction").getValue(Double.class) * dataSnapshot.child("amount").getValue(Double.class);
                    String currency = dataSnapshot.child("currency").getValue(String.class);
                    User u = new User();
                    u.setAlreadyPaid(alreadyPaid);
                    u.setDueImport(dueImport);
                    u.setExpenseCurrency(currency);
                    String participantID = participantSnap.getKey();
                    FirebaseUtils.getInstance().getParticipantName(participantID, participants, participantsViewAdapter, u);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.toException());
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(String friendID) {
        Log.d(TAG, "clickedItemIndex " + friendID);
        onClickFriendInterface.itemClicked(getClass().getSimpleName(), friendID);
    }
}

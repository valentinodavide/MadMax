package com.polito.mad17.madmax.activities.groups;

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
import com.polito.mad17.madmax.entities.Group;

import java.util.HashMap;

public class GroupsFragment extends Fragment implements GroupsViewAdapter.ListItemClickListener {

    private static final String TAG = GroupsFragment.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private HashMap<String, Group> groups = new HashMap<>();
    Double totBalance;

    private GroupsViewAdapter groupsViewAdapter;
    //private Double balance;

    private OnItemClickInterface onClickGroupInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    public GroupsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        setInterface((OnItemClickInterface) getActivity());

        //final View view = inflater.inflate(R.layout.skeleton_listview, container, false);
        final View view = inflater.inflate(R.layout.skeleton_list, container, false);

        //lv = (ListView) view.findViewById(R.id.rv_skeleton);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        groupsViewAdapter = new GroupsViewAdapter(this, groups);
        recyclerView.setAdapter(groupsViewAdapter);

        databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                {
                    //getGroup(groupSnapshot.getKey());
                    getGroupAndBalance(MainActivity.getCurrentUser().getID(), groupSnapshot.getKey());
                }

                //adapter = new HashMapGroupsAdapter(groups);
                //lv.setAdapter(adapter);


                //groupsViewAdapter.setGroupsData(groups, MainActivity.myself);
                groupsViewAdapter.update(groups);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.toException());
            }
        });


        /*
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        groupsViewAdapter = new GroupsViewAdapter(this);
        recyclerView.setAdapter(groupsViewAdapter);
        groupsViewAdapter.setGroupsData(MainActivity.myself.getUserGroups(), MainActivity.myself);
        Log.d(TAG, "dopo setAdapter");
        */

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

    public void getGroup(final String id)
    {
        databaseReference.child("groups").child(id).addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Group g = new Group();
                g.setName(dataSnapshot.child("name").getValue(String.class));
                //g.setBalance(getBalanceWithGroup(myselfID, id));
                groups.put(id, g);

                groupsViewAdapter.update(groups);
                groupsViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    //versione più complessa dell getGroup. Quest'ultima non verrà più usata.
    //oltre al nome gruppo, prende anche il bilancio dello user col gruppo
    void getGroupAndBalance (final String userID, final String groupID)
    {

        final HashMap <String, Double> totalBalance = new HashMap<>();
        totalBalance.put(userID,0d);
        totBalance = 0d;

        databaseReference.child("groups").child(groupID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                totalBalance.put(userID,0d);

                final String groupName = dataSnapshot.child("name").getValue(String.class);



                for (DataSnapshot groupExpenseSnapshot: dataSnapshot.child("expenses").getChildren())
                {
                    //Ascolto ogni singola spesa del gruppo
                    String expenseID = groupExpenseSnapshot.getKey();
                    databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Boolean involved = false; //dice se user contribuisce o no a quella spesa

                            for (DataSnapshot participantSnapshot: dataSnapshot.child("participants").getChildren())
                            {
                                if (participantSnapshot.getKey().equals(userID))
                                    involved = true;
                            }

                            //se user ha partecipato alla spesa
                            if (involved)
                            {
                                //alreadyPaid = soldi già messi dallo user per quella spesa
                                //dueImport = quota che user deve mettere per quella spesa
                                //balance = credito/debito dello user per quella spesa
                                Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);
                                Double dueImport = dataSnapshot.child("participants").child(userID).child("fraction").getValue(Double.class) * dataSnapshot.child("amount").getValue(Double.class);
                                Double balance = alreadyPaid - dueImport;
                                //se user per quella spesa ha già pagato più soldi della sua quota, il balance è positivo
                                Double currentBalance = totalBalance.get(userID);
                                totalBalance.put(userID, currentBalance+balance);
                                totBalance += balance;

                            }

                            Group g = new Group();
                            g.setName(groupName);
                            g.setBalance(totalBalance.get(userID));
                            //g.setBalance(totBalance);
                            groups.put(groupID, g);

                            groupsViewAdapter.update(groups);
                            groupsViewAdapter.notifyDataSetChanged();


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                Group g = new Group();
                g.setName(groupName);
                g.setBalance(0d);
                groups.put(groupID, g);

                groupsViewAdapter.update(groups);
                groupsViewAdapter.notifyDataSetChanged();
                totBalance = 0d;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return ;
    }



}
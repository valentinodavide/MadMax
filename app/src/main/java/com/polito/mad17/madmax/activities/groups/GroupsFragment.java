package com.polito.mad17.madmax.activities.groups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.users.HashMapFriendsAdapter;
import com.polito.mad17.madmax.entities.Group;

import java.util.HashMap;

public class GroupsFragment extends Fragment implements GroupsViewAdapter.ListItemClickListener {

    private static final String TAG = GroupsFragment.class.getSimpleName();
    private DatabaseReference mDatabase;
    private OnItemClickInterface onClickGroupInterface;
    private HashMap<String, Group> groups = new HashMap<>();
    private ListView lv;
    private HashMapGroupsAdapter adapter;


    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private GroupsViewAdapter groupsViewAdapter;

    public GroupsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        setInterface((OnItemClickInterface) getActivity());

        //final View view = inflater.inflate(R.layout.skeleton_listview, container, false);
        final View view = inflater.inflate(R.layout.skeleton_list, container, false);

        //lv = (ListView) view.findViewById(R.id.rv_skeleton);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //todo myselfID deve essere preso dalla MainActivty, non deve essere definito qui!!
        String myselfID = "-KjTCeDmpYY7gEOlYuSo";

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        groupsViewAdapter = new GroupsViewAdapter(this, groups);
        recyclerView.setAdapter(groupsViewAdapter);

        mDatabase.child("users").child(myselfID).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                {
                    getGroup(groupSnapshot.getKey());
                }

                //adapter = new HashMapGroupsAdapter(groups);
                //lv.setAdapter(adapter);


                //groupsViewAdapter.setGroupsData(groups, MainActivity.myself);
                groupsViewAdapter.update(groups);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        mDatabase.child("groups").child(id).addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Group g = new Group();
                g.setName(dataSnapshot.child("name").getValue(String.class));
                groups.put(id, g);
                //adapter.update(groups);
                //adapter.notifyDataSetChanged();

                //groupsViewAdapter.setGroupsData(groups, MainActivity.myself);
                groupsViewAdapter.update(groups);
                groupsViewAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


}

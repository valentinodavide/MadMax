package com.polito.mad17.madmax.activities.users;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.activities.OverlayDivider;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class FriendsFragment extends Fragment implements FriendsViewAdapter.ListItemClickListener, FriendsViewAdapter.ListItemLongClickListener {

    private static final String TAG = FriendsFragment.class.getSimpleName();
    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private TreeMap<String, User> friends = new TreeMap<>(Collections.reverseOrder());
    private Query query;
    private String groupID;

    private OnItemClickInterface onClickFriendInterface;
    private OnItemLongClickInterface onLongClickFriendInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface, OnItemLongClickInterface onItemLongClickInterface) {
        onClickFriendInterface = onItemClickInterface;
        onLongClickFriendInterface = onItemLongClickInterface;
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FriendsViewAdapter friendsViewAdapter;

    private ValueEventListener groupMembersListener;
    private ArrayList<String> listenedGroups = new ArrayList<>();
    private Boolean listenedFriends = false;

    public FriendsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d (TAG, "OnCreate from " + getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d (TAG, "OnCreateView from " + getActivity());

        final View view = inflater.inflate(R.layout.skeleton_list, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        setInterface((OnItemClickInterface) getActivity(), (OnItemLongClickInterface) getActivity());

        RecyclerView.ItemDecoration divider = new InsetDivider.Builder(getContext())
                .orientation(InsetDivider.VERTICAL_LIST)
                .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                .color(getResources().getColor(R.color.colorDivider))
                .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                .overlay(true)
                .build();

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);

        friendsViewAdapter = new FriendsViewAdapter(this, this, friends);
        recyclerView.setAdapter(friendsViewAdapter);

        final String activityName = getActivity().getClass().getSimpleName();
        Log.d (TAG, "Sono nella activity: " + activityName);

        //Se sono in MainActivity visualizzo lista degli amici
        if (activityName.equals("MainActivity"))
            query = databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("friends");

        //Se sono dentro un gruppo, visualizzo lista membri del gruppo
        else if (activityName.equals("GroupDetailActivity"))
        {
            Bundle b = this.getArguments();
            if (b != null)
            {
                groupID = b.getString("groupID");
                query = databaseReference.child("groups").child(groupID).child("members");
            }
        }

        Log.d(TAG, "query: "+ query);
        groupMembersListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot externalDataSnapshot) {

                //svuoto la map, così se viene eliminato uno user dal db, non viene tenuto nella map che si ricarica da zero
                //friends.clear();

                for (final DataSnapshot friendSnapshot: externalDataSnapshot.getChildren()) {
                        //getFriend(friendSnapshot.getKey());
                    Boolean deleted = true;
                    if(activityName.equals("MainActivity")){
                        Log.d(TAG, "key: "+friendSnapshot.getKey());
                        Log.d(TAG, "value: "+friendSnapshot.getValue());
                        if (!listenedFriends)
                            listenedFriends = true;
                        deleted  = friendSnapshot.child("deleted").getValue().equals(true);
                    }
                    else
                        if(activityName.equals("GroupDetailActivity"))
                        {
                            deleted  = friendSnapshot.child("deleted").getValue().equals(true);
                            //Se sono negli amici "generali" e non nei membri di un gruppo, non c'è il campo deleted, quindi sarà null
                            if (!listenedGroups.contains(groupID))
                                listenedGroups.add(groupID);
                        }



                    final String id = friendSnapshot.getKey();
                    final Boolean finalDeleted = deleted;
                    databaseReference.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User u = new User();
                            u.setName(dataSnapshot.child("name").getValue(String.class));
                            u.setSurname(dataSnapshot.child("surname").getValue(String.class));
                            u.setProfileImage(dataSnapshot.child("image").getValue(String.class));
                            if (!finalDeleted)
                                friends.put(id, u);
                            else
                                friends.remove(id);

                            friendsViewAdapter.update(friends);
                            friendsViewAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                friendsViewAdapter.update(friends);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });

        Log.d(TAG, "dopo setAdapter");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d (TAG, "OnStart from " + getActivity());

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
    public void onListItemClick(String friendID) {
        Log.d(TAG, "clickedItemIndex " + friendID);
        onClickFriendInterface.itemClicked(getClass().getSimpleName(), friendID);
    }

    @Override
    public boolean onListItemLongClick (String friendID, View v) {
        Log.d(TAG, "longClickedItemIndex " + friendID);
        onLongClickFriendInterface.itemLongClicked(getClass().getSimpleName(), friendID, v);

        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d (TAG, "OnStop from " + getActivity());
        //databaseReference.child("groups").child(groupID).removeEventListener(groupListener);
        //todo come gestire il fatto che ci sono più listener da eliminare?
        //Elimino una alla volta tutti i listener istanziati
        for (String groupID : listenedGroups)
        {
            databaseReference.child("groups").child(groupID).child("members").removeEventListener(groupMembersListener);
            Log.d(TAG, "Detached members listener on " + groupID);
        }

        if (listenedFriends)
        {
            databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("friends").removeEventListener(groupMembersListener);
            Log.d(TAG, "Detached friends listener");

        }
    }




    }
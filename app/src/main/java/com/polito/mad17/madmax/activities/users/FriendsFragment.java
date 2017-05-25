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
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.entities.User;

import java.util.HashMap;

public class FriendsFragment extends Fragment implements FriendsViewAdapter.ListItemClickListener, FriendsViewAdapter.ListItemLongClickListener {

    private static final String TAG = FriendsFragment.class.getSimpleName();
    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private HashMap<String, User> friends = new HashMap<>();
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

    public FriendsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.skeleton_list, container, false);
        //lv = (ListView) view.findViewById(R.id.rv_skeleton);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        //todo myselfID deve essere preso dalla MainActivty, non deve essere definito qui!!
        //String myselfID = "-KjTCeDmpYY7gEOlYuSo";

        setInterface((OnItemClickInterface) getActivity(), (OnItemLongClickInterface) getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

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

        Log.d(TAG, "query: "+query);
        query.addValueEventListener(new ValueEventListener() {
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
                        deleted  = friendSnapshot.getValue().equals("false");
                    }
                    else
                        if(activityName.equals("GroupDetailActivity"))
                            deleted  = friendSnapshot.child("deleted").getValue().equals("false");
                    //Se sono negli amici "generali" e non nei membri di un gruppo, non c'è il campo deleted, quindi sarà null


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
}
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private ValueEventListener groupListener;
    Boolean listenedGroup = false;
    private Double totBalance;




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

        friendsViewAdapter = new FriendsViewAdapter(this.getContext(), this, this, friends);
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

                            String name = dataSnapshot.child("name").getValue(String.class);
                            String surname = dataSnapshot.child("surname").getValue(String.class);
                            String profileImage = dataSnapshot.child("image").getValue(String.class);

                            if(activityName.equals("MainActivity"))
                            {
                                User u = new User();
                                u.setID(friendSnapshot.getKey());
                                u.setName(name);
                                u.setSurname(surname);
                                u.setProfileImage(profileImage);
                                if (!finalDeleted)
                                    friends.put(id, u);
                                else
                                    friends.remove(id);

                                friendsViewAdapter.update(friends);
                                friendsViewAdapter.notifyDataSetChanged();
                            }
                            else if (activityName.equals("GroupDetailActivity"))
                            {
                                getUserAndGroupBalance(id, name, surname, profileImage, groupID);
                            }




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

    //Retrieve balance of userID toward groupID
    void getUserAndGroupBalance (final String userID, final String name, final String surname, final String profileImage, final String groupID)
    {
        //retrieve data of group
        final HashMap<String, Double> totalBalance = new HashMap<>();
        totalBalance.put(userID,0d);
        totBalance = 0d;

        groupListener = databaseReference.child("groups").child(groupID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot groupDataSnapshot) {

                totalBalance.put(userID,0d);
                if (!listenedGroups.contains(groupID))
                    listenedGroups.add(groupID);

                final Boolean deleted = groupDataSnapshot.child("deleted").getValue(Boolean.class);

                if (deleted != null)
                {
                    for (DataSnapshot groupExpenseSnapshot: groupDataSnapshot.child("expenses").getChildren())
                    {
                        //Contribuiscono al bilancio solo le spese non eliminate dal gruppo
                        if (groupExpenseSnapshot.getValue(Boolean.class) == true)
                        {
                            //Adesso sono sicuro che la spesa non è stata eliminata
                            //Ascolto la singola spesa del gruppo
                            String expenseID = groupExpenseSnapshot.getKey();
                            databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Boolean involved = false; //dice se user contribuisce o no a quella spesa

                                    for (DataSnapshot participantSnapshot: dataSnapshot.child("participants").getChildren())
                                    {
                                        //todo poi gestire caso in cui utente viene tolto dai participant alla spesa
                                        if (participantSnapshot.getKey().equals(userID))
                                            involved = true;
                                    }

                                    //se user ha partecipato alla spesa
                                    if (involved)
                                    {
                                        //alreadyPaid = soldi già messi dallo user per quella spesa
                                        //dueImport = quota che user deve mettere per quella spesa
                                        //balance = credito/debito dello user per quella spesa
                                        for (DataSnapshot d : dataSnapshot.child("participants").getChildren())
                                        {
                                            Log.d (TAG, "PartCampo " + d.getKey() + ": " + d.getValue() );
                                        }
                                        Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);

                                        Log.d (TAG, "Fraction: " + Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())));

                                        Double dueImport = Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())) * dataSnapshot.child("amount").getValue(Double.class);
                                        Double balance = alreadyPaid - dueImport;
                                        //se user per quella spesa ha già pagato più soldi della sua quota, il balance è positivo
                                        Double currentBalance = totalBalance.get(userID);
                                        totalBalance.put(userID, currentBalance+balance);

                                    }

                                    User u = new User();
                                    u.setBalanceWithGroup(totalBalance.get(userID));
                                    u.setName(name);
                                    u.setSurname(surname);
                                    u.setProfileImage(profileImage);

                                    //u.setDeleted(deleted);
                                    //g.setBalance(totBalance);
                                    //se il gruppo non è deleted e io faccio ancora parte del gruppo
                                    if (!deleted &&
                                            groupDataSnapshot.child("members").hasChild(MainActivity.getCurrentUser().getID()) &&
                                            !groupDataSnapshot.child("members").child(MainActivity.getCurrentUser().getID()).child("deleted").getValue(Boolean.class)
                                            )
                                    {
                                        friends.put(userID, u);

                                    }
                                    else
                                    {
                                        friends.remove(userID);
                                    }

                                    friendsViewAdapter.update(friends);
                                    friendsViewAdapter.notifyDataSetChanged();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    /*
                    //Per gestire il caso di quando ho appena abbandonato o eliminato il gruppo
                    //if (dataSnapshot.child("members").hasChild(userID))
                    //{
                    Group g = new Group();
                    g.setName(groupName);
                    g.setBalance(0d);
                    g.setDeleted(deleted);
                    if (!deleted &&
                            groupDataSnapshot.child("members").hasChild(MainActivity.getCurrentUser().getID()) &&
                            !groupDataSnapshot.child("members").child(MainActivity.getCurrentUser().getID()).child("deleted").getValue(Boolean.class)) {
                        groups.put(groupID, g);
                    }
                    else
                    {
                        groups.remove(groupID);
                    }

//                    Group nullGroup = new Group();
//                    nullGroup.setID(groupID + 1);
//                    groups.put(nullGroup.getID(), nullGroup);

                    groupsViewAdapter.update(groups);
                    groupsViewAdapter.notifyDataSetChanged();
                    totBalance = 0d;
                    */
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
    }




    }
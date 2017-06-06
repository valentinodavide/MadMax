package com.polito.mad17.madmax.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.expenses.ExpensesFragment;
import com.polito.mad17.madmax.activities.expenses.NewExpenseActivity;
import com.polito.mad17.madmax.activities.groups.EventsFragment;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.activities.users.FriendsFragment;
import com.polito.mad17.madmax.activities.users.NewMemberActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.SimpleDateFormat;
import java.util.TreeMap;

import static android.app.Activity.RESULT_OK;

public class DetailFragment extends Fragment implements GroupsViewAdapter.ListItemClickListener {

    private OnItemClickInterface onClickGroupInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    private String activityName;
    private static final String TAG = DetailFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private GroupsViewAdapter groupsViewAdapter;

    private String friendID;
    private String groupID, groupName;
    private String userID;

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private TreeMap<String, Group> groups = new TreeMap<>();    //gruppi condivisi tra me e friend
    private FloatingActionButton fab;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create the view to return
        View mainView = null;

        //get the bundle
        Bundle bundle = this.getArguments();

        // the listener will be the GroupDetailActivity or the FriendDetailActivity
        setInterface((OnItemClickInterface) getActivity());

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // when an item in the list will be clicked the onListItemClicked will be called
        groupsViewAdapter = new GroupsViewAdapter(this.getContext(), this, groups, DetailFragment.TAG);

        if(activityName.equals("FriendDetailActivity")){

            Log.d(TAG, "FriendDetailActivity per RecylerView");

            // Inflate the layout for this fragment
            mainView = inflater.inflate(R.layout.skeleton_list, container, false);

            RecyclerView.ItemDecoration divider = new InsetDivider.Builder(getContext())
                    .orientation(InsetDivider.VERTICAL_LIST)
                    .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                    .color(ContextCompat.getColor(getContext(), R.color.colorDivider))
                    .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                    .overlay(true)
                    .build();

            recyclerView = (RecyclerView) mainView.findViewById(R.id.rv_skeleton);
            layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(divider);

            recyclerView.setAdapter(groupsViewAdapter);

            //Extract data from bundle
            friendID = bundle.getString("friendID");

            //Show shared groups
            databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("friends").child(friendID).child("sharedGroups").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot sharedGroupSnapshot: dataSnapshot.getChildren())
                    {
                        FirebaseUtils.getInstance().getGroup(sharedGroupSnapshot.getKey(), groups, groupsViewAdapter);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if(activityName.equals("GroupDetailActivity")){
            groupID = bundle.getString("groupID");

            mainView = inflater.inflate(R.layout.fragment_group_detail, container, false);

            fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            fab.setImageResource(android.R.drawable.ic_input_add);

            TabLayout tabLayout = (TabLayout) mainView.findViewById(R.id.tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText(R.string.expenses));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.members));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.activities));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            updateFab(0);

            final ViewPager viewPager = (ViewPager) mainView.findViewById(R.id.main_view_pager);
            final DetailFragment.PagerAdapter adapter = new DetailFragment.PagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    Log.d(TAG, "selected tab "+tab.getPosition());
                    updateFab(tab.getPosition());
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) { }

                @Override
                public void onTabReselected(TabLayout.Tab tab) { }
            });
        }

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // to know in which activity we are
        activityName = getActivity().getClass().getSimpleName();
        Log.d(TAG, TAG+" attached to "+activityName);

        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(String itemID) { // the itemID will be the friendID or the groupID
        Log.d(TAG, "clickedItemIndex " + itemID);
        onClickGroupInterface.itemClicked(getClass().getSimpleName(), itemID);
    }

    private void updateFab(int position){
        switch(position){
            case 0:
                // expenses fragment
                Log.d(TAG, "fab 0");
                fab.setImageResource(android.R.drawable.ic_input_add);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(getActivity(), NewExpenseActivity.class);
                        myIntent.putExtra("groupID", groupID);
                        myIntent.putExtra("userID", MainActivity.getCurrentUID());
                        myIntent.putExtra("callingActivity", "DetailFragment");
                        startActivity(myIntent);
                    }
                });
                break;
            case 1:
                // members fragment
                Log.d(TAG, "fab 1");
                fab.setImageResource(R.drawable.user_add);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), NewMemberActivity.class);
                        intent.putExtra("groupID", groupID);
                        intent.putExtra("groupName", groupName);
                        startActivity(intent);
                    }
                });
                break;
            case 2:
                // history fragment
                Log.d(TAG, "fab 2");
                fab.setVisibility(View.GONE); // lo storico non si tocca
                break;
        }
    }


    public class PagerAdapter extends FragmentPagerAdapter {

        int numberOfTabs;

        public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {

            Bundle bundle = new Bundle();
            switch (position) {
                case 0:
                    Log.d(TAG, "here in case 0");
                    ExpensesFragment expensesFragment = new ExpensesFragment();
                    //riga qui sotto aggiunta da Ale...prima il bundle veniva caricato nella OnDataChange quindi
                    //qui era ancora null e mi crashava, non so come potesse funzionare prima...
                    //in generale non usare i dati settati nella OnDataChange fuori dalla OnDataChange
                    bundle.putString("groupID", groupID);
                    expensesFragment.setArguments(bundle);
                    return expensesFragment;
                case 1:
                    Log.d(TAG, "here in case 1");
                    FriendsFragment membersFragment = new FriendsFragment();
                    Bundle b = new Bundle();
                    b.putString("groupID", groupID);
                    membersFragment.setArguments(b);
                    //setargument creo bundle bunfle.setstring
                    return membersFragment;
                case 2:
                    Log.d(TAG, "here in case 2");
                    EventsFragment eventsFragment = new EventsFragment();
                    bundle.putString("groupID", groupID);
                    eventsFragment.setArguments(bundle);
                    return eventsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return super.isViewFromObject(view, object);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == MainActivity.REQUEST_INVITE_GROUP){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);

                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }

                // add event for FRIEND_GROUP_INVITE
                User currentUser = MainActivity.getCurrentUser();
                Event event = new Event(
                        groupID,
                        Event.EventType.FRIEND_GROUP_INVITE,
                        currentUser.getName() + " " + currentUser.getSurname()
                );
                event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                FirebaseUtils.getInstance().addEvent(event);
            }
            else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                //Toast.makeText(getActivity(), "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class FriendDetailFragment extends Fragment implements GroupsViewAdapter.ListItemClickListener, GroupsViewAdapter.ListItemLongClickListener {

    private static final String TAG = FriendDetailFragment.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private OnItemClickInterface onClickGroupInterface;
    private OnItemLongClickInterface onLongClickGroupInterface;


    public void setInterface(OnItemClickInterface onItemClickInterface, OnItemLongClickInterface onItemLongClickInterface) {
        onClickGroupInterface = onItemClickInterface;
        onLongClickGroupInterface = onItemLongClickInterface;

    }

    private ImageView imageView;
    private TextView nameTextView;
    private TextView balanceTextView;
    private TextView balanceTextTextView;
    private Button payButton;
    private String friendID;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private GroupsViewAdapter groupsViewAdapter;
    private HashMap<String, Group> groups = new HashMap<>();    //gruppi condivisi tra me e friend

    public FriendDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setInterface((OnItemClickInterface) getActivity(),  (OnItemLongClickInterface) getActivity());

        databaseReference = FirebaseDatabase.getInstance().getReference();

        View view = inflater.inflate(R.layout.fragment_friend_detail, container, false);

        imageView = (ImageView) view.findViewById(R.id.img_photo);
        nameTextView = (TextView) view.findViewById(R.id.tv_friend_name);
        balanceTextView = (TextView) view.findViewById(R.id.tv_balance);
        balanceTextTextView = (TextView) view.findViewById(R.id.tv_balance_text);
        payButton = (Button) view.findViewById(R.id.btn_pay_debt);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        //todo mettere a posto
        groupsViewAdapter = new GroupsViewAdapter(this, this, groups);
        recyclerView.setAdapter(groupsViewAdapter);

        //Extract data from bundle
        Bundle bundle = this.getArguments();
        friendID = bundle.getString("friendID");


        //Show data of friend
        databaseReference.child("users").child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue(String.class);
                String surname = dataSnapshot.child("surname").getValue(String.class);
                nameTextView.setText(name + " " + surname);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Show shared groups
        databaseReference.child("users").child(MainActivity.getCurrentUser().getID()).child("friends").child(friendID).child("sharedGroups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot sharedGroupSnapshot: dataSnapshot.getChildren())
                {
                    getGroup(sharedGroupSnapshot.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


/*        User friendDetail = null;
        if(bundle != null) {
            friendDetail = bundle.getParcelable("friendDetails");

            String photo = friendDetail.getProfileImage();
            int photoUserId = Integer.parseInt(photo);
            imageView.setImageResource(photoUserId);

            nameTextView.setText(friendDetail.getName() + " " + friendDetail.getSurname());

            Double balance = 0.0;
            // todo controlla se giusto come dato
            if(friendDetail.getBalanceWithUsers() != null) {
                for (Map.Entry<String, Double> entry : friendDetail.getBalanceWithUsers().entrySet()) {
                    if (entry.getKey().equals(MainActivity.myself.getID())) {
                        balance = entry.getValue();
                    }
                }
            }

            DecimalFormat df = new DecimalFormat("#.##");
            balanceTextView.setText(df.format(Math.abs(balance)) + " €");

            if (balance > 0) {
                balanceTextTextView.setText(R.string.you_should_receive);
            } else if (balance < 0) {
                balanceTextTextView.setText(R.string.owes_you);
            } else {
                balanceTextTextView.setText(R.string.no_debts);
            }
        }*/

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
    @Override
    public boolean onListItemLongClick (String friendID, View v) {
        Log.d(TAG, "longClickedItemIndex " + friendID);
        onLongClickGroupInterface.itemLongClicked(getClass().getSimpleName(), friendID, v);



        return true;

    }

    //todo metodo ripetuto in diverse activity, correggere
    public void getGroup(final String id)
    {
        databaseReference.child("groups").child(id).addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Group g = new Group();
                g.setName(dataSnapshot.child("name").getValue(String.class));
                groups.put(id, g);
                groupsViewAdapter.update(groups);
                groupsViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });
    }

}

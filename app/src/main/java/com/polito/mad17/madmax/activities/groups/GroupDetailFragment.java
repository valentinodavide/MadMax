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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.expenses.ExpensesViewAdapter;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.Map;

public class GroupDetailFragment extends Fragment implements ExpensesViewAdapter.ListItemClickListener {

    private static final String TAG = GroupDetailFragment.class.getSimpleName();

    private OnItemClickInterface onClickGroupInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    private ImageView imageView;
    private TextView nameTextView;
    private TextView balanceTextView;
    private TextView balanceTextTextView;
    private Button payButton;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private GroupsViewAdapter groupsViewAdapter;

    public GroupDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_group_detail, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

//        groupsViewAdapter = new GroupsViewAdapter(this);
        recyclerView.setAdapter(groupsViewAdapter);

        //todo mettere a posto
        //groupsViewAdapter.setGroupsData(MainActivity.myself.getUserGroups(), MainActivity.myself);

        /* Preso dalla vecchia activity
        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView surname=(TextView)view.findViewById(R.id.surname);
        TextView balancetext=(TextView) view.findViewById(R.id.balancetext);
        TextView balance=(TextView) view.findViewById(R.id.balance);
        */



        //nameTextView.setText(friendDetail.getName() + " " + friendDetail.getSurname());
        nameTextView =(TextView)view.findViewById(R.id.tv_group_name);

        nameTextView.setText("Nome Gruppo");


        //Extract data from bundle
       /* Bundle bundle = this.getArguments();
        User friendDetail = null;
        if(bundle != null) {
            friendDetail = (User) bundle.getParcelable("friendDetails");

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

}

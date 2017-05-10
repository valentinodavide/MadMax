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

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.entities.Group;

public class ExpensesFragment extends Fragment implements ExpensesViewAdapter.ListItemClickListener {

    private static final String TAG = ExpensesFragment.class.getSimpleName();

    private OnItemClickInterface onClickFriendInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickFriendInterface = onItemClickInterface;
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ExpensesViewAdapter expensesViewAdapter;

    //private Group groupDetails = null;

    public ExpensesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setInterface((OnItemClickInterface) getActivity());

        View view = inflater.inflate(R.layout.skeleton_list, container, false);

        //Bundle bundle = getArguments();
        //groupDetails = bundle.getParcelable("groupDetails");

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        expensesViewAdapter = new ExpensesViewAdapter(this);
        recyclerView.setAdapter(expensesViewAdapter);

        //expensesViewAdapter.setExpensesData(groupDetails.getExpenses());

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
}

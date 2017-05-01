package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;

public class PendingExpensesFragment extends Fragment implements GroupsViewAdapter.ListItemClickListener {

    private static final String TAG = PendingExpensesFragment.class.getSimpleName();

    private OnItemClickInterface onClickGroupInterface;

    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
//    private PendingExpensesViewAdapter groupsViewAdapter;

    public PendingExpensesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setInterface((OnItemClickInterface) getActivity());

        View view = inflater.inflate(R.layout.fragment_pending_expenses, container, false);
//
//        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
//        recyclerView.setHasFixedSize(true);
//
//        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//        recyclerView.setLayoutManager(layoutManager);
//
//        groupsViewAdapter = new GroupsViewAdapter(this);
//        recyclerView.setAdapter(groupsViewAdapter);
//
//        groupsViewAdapter.setGroupsData(MainActivity.groups, MainActivity.myself);

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

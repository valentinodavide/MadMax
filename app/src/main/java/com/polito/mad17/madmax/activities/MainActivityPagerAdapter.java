package com.polito.mad17.madmax.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.polito.mad17.madmax.activities.expenses.PendingExpensesFragment;
import com.polito.mad17.madmax.activities.groups.GroupsFragment;
import com.polito.mad17.madmax.activities.users.FriendsFragment;


public class MainActivityPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = MainActivityPagerAdapter.class.getSimpleName();

    int numberOfTabs;

    FriendsFragment friendsFragment = null;
    GroupsFragment groupsFragment = null;
    PendingExpensesFragment pendingExpensesFragment = null;

    public MainActivityPagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                Log.i(TAG, "here in case 0: FriendsFragment");
                friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 1:
                Log.i(TAG, "here in case 1: GroupsFragment");
                groupsFragment = new GroupsFragment();
                return groupsFragment;
            case 2:
                Log.i(TAG, "here in case 2: PendingExpensesFragment");
                pendingExpensesFragment = new PendingExpensesFragment();
                return pendingExpensesFragment;
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

package com.polito.mad17.madmax.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.polito.mad17.madmax.activities.expenses.ExpenseCommentsFragment;
import com.polito.mad17.madmax.activities.expenses.ExpenseDetailFragment;

/**
 * Created by alessandro on 26/05/17.
 */

public class ExpenseDetailPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = ExpenseDetailPagerAdapter.class.getSimpleName();

    int numberOfTabs;
    String expenseID;

    ExpenseDetailFragment expenseDetailFragment = null;
    ExpenseCommentsFragment expenseCommentsFragment = null;

    public ExpenseDetailPagerAdapter(FragmentManager fragmentManager, int numberOfTabs, String expenseID) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
        this.expenseID = expenseID;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle b = new Bundle();
        b.putString("expenseID", expenseID);

        switch(position) {
            case 0:
                Log.i(TAG, "here in case 0: ExpenseDetailFragment");
                expenseDetailFragment = new ExpenseDetailFragment();
                expenseDetailFragment.setArguments(b);
                return expenseDetailFragment;
            case 1:
                Log.i(TAG, "here in case 1: ExpenseCommentsFragment");
                expenseCommentsFragment = new ExpenseCommentsFragment();
                expenseCommentsFragment.setArguments(b);
                return expenseCommentsFragment;
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

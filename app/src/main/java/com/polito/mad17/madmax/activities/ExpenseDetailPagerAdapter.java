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
import com.polito.mad17.madmax.activities.expenses.PendingExpenseDetailFragment;


public class ExpenseDetailPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = ExpenseDetailPagerAdapter.class.getSimpleName();

    private int numberOfTabs;
    private String expenseID;
    private String fragmentName;

    private ExpenseDetailFragment expenseDetailFragment = null;
    private PendingExpenseDetailFragment pendingExpenseDetailFragment = null;
    private ExpenseCommentsFragment expenseCommentsFragment = null;

    public ExpenseDetailPagerAdapter(FragmentManager fragmentManager, int numberOfTabs, String expenseID, String fragmentName) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
        this.expenseID = expenseID;
        this.fragmentName = fragmentName;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("expenseID", expenseID);

        switch(position) {
            case 0:
                if(fragmentName.equals("ExpenseDetailActivity")) {
                    Log.i(TAG, "here in case 0: ExpenseDetailFragment");
                    expenseDetailFragment = new ExpenseDetailFragment();
                    expenseDetailFragment.setArguments(bundle);
                    return expenseDetailFragment;
                }
                else
                {
                    Log.i(TAG, "here in case 0: PendingExpenseDetailFragment");
                    pendingExpenseDetailFragment = new PendingExpenseDetailFragment();
                    pendingExpenseDetailFragment.setArguments(bundle);
                    return pendingExpenseDetailFragment;
                }
            case 1:
                Log.i(TAG, "here in case 1: ExpenseCommentsFragment");
                expenseCommentsFragment = new ExpenseCommentsFragment();
                expenseCommentsFragment.setArguments(bundle);
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

package com.polito.mad17.madmax.activities.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.expenses.PendingExpensesFragment;
import com.polito.mad17.madmax.activities.expenses.ExpensesFragment;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.entities.Group;

public class GroupDetailActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = FriendDetailActivity.class.getSimpleName();

    private ImageView imageView;
    private TextView nameTextView;
    private TextView balanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_group_detail);

        Log.d(TAG, "onCreate di GroupDetailActivity");

        imageView = (ImageView) findViewById(R.id.img_photo);
        nameTextView = (TextView) findViewById(R.id.tv_group_name);
        balanceTextView = (TextView) findViewById(R.id.tv_balance);

        nameTextView.setText("Nome Gruppo");

        //Bundle bundle = getIntent().getExtras();
        /*
        Group groupDetail = null;
        if(bundle != null) {
            if(bundle.containsKey("groupDetails")) {
                Log.d(TAG, bundle.getParcelable("groupDetails").toString());

                groupDetail = bundle.getParcelable("groupDetails");

                String photo = groupDetail.getImage();
                int photoUserId = Integer.parseInt(photo);
                imageView.setImageResource(photoUserId);

                nameTextView.setText(groupDetail.getName());
                //todo populate with user balance with group
//                balanceTextView.setText();
            }
        }
        */

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.expenses));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.history));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.members));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        final GroupDetailActivity.PagerAdapter adapter = new GroupDetailActivity.PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        int numberOfTabs;

        public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Log.d(TAG, "here in case 0");
                    ExpensesFragment expensesFragment = new ExpensesFragment();
                    //expensesFragment.setArguments(details);
                    return expensesFragment;
                case 1:
                    Log.d(TAG, "here in case 1");
                    PendingExpensesFragment pendingExpensesFragment = new PendingExpensesFragment();
                    return pendingExpensesFragment;
                case 2:
                    Log.d(TAG, "here in case 2");
                    PendingExpensesFragment pendingExpensesFragment2 = new PendingExpensesFragment();
                    return pendingExpensesFragment2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }

    }
    
    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Log.d(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "FriendsFragment":
                break;

            case "GroupsFragment":
                break;
        }
    }
}

package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.expenses.VotersViewAdapter;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by alessandro on 02/06/17.
 */

public class BalancesViewAdapter extends  RecyclerView.Adapter<BalancesViewAdapter.ItemBalancesViewHolder> {

    private static final String TAG = BalancesViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private BalancesViewAdapter.ListItemClickListener itemClickListener;
    private BalancesViewAdapter.ListItemLongClickListener itemLongClickListener = null;
    private final ArrayList mData;
    private Context context;



    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }

    public BalancesViewAdapter(Context context, BalancesViewAdapter.ListItemClickListener listener, Map<String, Double> map) {
        this.context = context;
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public BalancesViewAdapter(Context context, BalancesViewAdapter.ListItemClickListener listener, BalancesViewAdapter.ListItemLongClickListener longListener, Map<String, Double> map) {
        this.context = context;
        itemClickListener = listener;
        itemLongClickListener = longListener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    class ItemBalancesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView balanceTextTextView;
        private TextView balanceTextView;

        public ItemBalancesViewHolder(View itemView) {
            super(itemView);
            balanceTextTextView = (TextView) itemView.findViewById(R.id.tv_balance_text);
            balanceTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, Double> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedVoter " + itemClicked.getKey());

            //itemClickListener.onListItemClick(itemClicked.getKey());

        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, Double> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedVoter " + itemClicked.getKey());
            //itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);
            return true;

        }
    }

    @Override
    public BalancesViewAdapter.ItemBalancesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.balance_item, parent, false);

        BalancesViewAdapter.ItemBalancesViewHolder itemVotersViewHolder = new BalancesViewAdapter.ItemBalancesViewHolder(view);

        return itemVotersViewHolder;
    }

    @Override
    public void onBindViewHolder(final BalancesViewAdapter.ItemBalancesViewHolder holder, int position) {


        DecimalFormat df = new DecimalFormat("#.##");

        Map.Entry<String, Double> item = getItem(position);

        String currency = item.getKey();
        Double balance = item.getValue();

        if (balance > 0)
        {
            holder.balanceTextTextView.setText(R.string.you_should_receive);
            holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

            String balanceText = df.format(Math.abs(balance)) + " " +  currency;
            Log.d(TAG, "balance "  + balance);

            holder.balanceTextView.setText(balanceText);
            holder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        }
        else
        {
            if (balance < 0)
            {
                holder.balanceTextTextView.setText(R.string.you_owe);
                holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

                String balanceText = df.format(Math.abs(balance)) + " " +  currency;
                Log.d(TAG, "balance "  + balance);

                holder.balanceTextView.setText(balanceText);
                holder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }
            //todo togliere?
            else
            {
                holder.balanceTextTextView.setText(R.string.no_debts);
                holder.balanceTextView.setText("0 " + currency);
                holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                holder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));

            }
        }


    }

    public Map.Entry<String, Double> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

}

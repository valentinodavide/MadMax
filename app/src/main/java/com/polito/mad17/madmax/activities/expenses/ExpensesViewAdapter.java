package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.SettingsFragment;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.Expense;

import java.util.ArrayList;
import java.util.Map;

public class ExpensesViewAdapter extends RecyclerView.Adapter<ExpensesViewAdapter.ItemExpensesViewHolder> {

    private static final String TAG = ExpensesViewAdapter.class.getSimpleName();

    private final ArrayList expenses;

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;
    private ListItemLongClickListener itemLongClickListener = null;
    private Context context;

    private LayoutInflater layoutInflater;

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }

    public ExpensesViewAdapter(Context context, ListItemClickListener listener, Map<String, Expense> expensesMap) {
        this.context = context;
        itemClickListener = listener;
        this.expenses = new ArrayList<>();
        expenses.addAll(expensesMap.entrySet());
        expenses.add("");
    }

    public ExpensesViewAdapter(Context context, ListItemClickListener listener, ListItemLongClickListener longListener, Map<String, Expense> expensesMap) {
        this.context = context;
        itemClickListener = listener;
        itemLongClickListener = longListener;
        this.expenses = new ArrayList<>();
        expenses.addAll(expensesMap.entrySet());
        expenses.add("");

    }

    class ItemExpensesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView balanceTextTextView;
        private TextView balanceTextView;
        //private String ID;

        ItemExpensesViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_sender);
            balanceTextTextView = (TextView) itemView.findViewById(R.id.tv_balance_text);
            balanceTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Log.d(TAG, "clickedExpense " + getItem(clickedPosition).getKey());

            itemClickListener.onListItemClick(getItem(clickedPosition).getKey());
        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, Expense> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedExpense " + itemClicked.getKey());
            itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);

            return true;

        }
    }

    @Override
    public ExpensesViewAdapter.ItemExpensesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.list_item, parent, false);

        ItemExpensesViewHolder itemExpensesViewHolder = new ItemExpensesViewHolder(view);

        Log.d(TAG, "dopo aver istanziato il view holder");
        return itemExpensesViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemExpensesViewHolder expensesViewHolder, int position) {

        if(position == (expenses.size() - 1))
        {
            Log.d(TAG, "item.getKey().equals(\"nullGroup\")");
            expensesViewHolder.nameTextView.setText("");
            expensesViewHolder.balanceTextTextView.setText("");
            expensesViewHolder.balanceTextView.setText("");
            //expensesViewHolder.itemView.setOnClickListener(null);
        }
        else {
            Expense expense = getItem(position).getValue();
            if (expense.getExpensePhoto() != null)
            {
                String photo = expense.getExpensePhoto();
                int photoUserId = Integer.parseInt(photo);
                expensesViewHolder.imageView.setImageResource(photoUserId);

                Glide.with(layoutInflater.getContext()).load(photo)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(expensesViewHolder.imageView);
            }
            else
            {
                Glide.with(layoutInflater.getContext()).load(R.drawable.ic_face)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(expensesViewHolder.imageView);
            }

            expensesViewHolder.nameTextView.setText(expense.getDescription());

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String defaultCurrency = sharedPref.getString(SettingsFragment.DEFAULT_CURRENCY, "");

            Double expenseBalance = expense.getBalance();
            String balance = Math.abs(expenseBalance) + " " + expense.getCurrency();

            if (expenseBalance > 0) {
                expensesViewHolder.balanceTextTextView.setText(R.string.credit_of);
                expensesViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                expensesViewHolder.balanceTextView.setText(balance);
                expensesViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

            }
            else
            {
                if (expenseBalance < 0)
                {
                    expensesViewHolder.balanceTextTextView.setText(R.string.debt_of);
                    expensesViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                    expensesViewHolder.balanceTextView.setText(balance);
                    expensesViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                }
                else
                {
                    expensesViewHolder.balanceTextTextView.setText(R.string.no_debts);
                    expensesViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    expensesViewHolder.balanceTextView.setText("0 " + defaultCurrency);
                }
            }

        }
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public Map.Entry<String, Expense> getItem(int position) {
        return (Map.Entry) expenses.get(position);
    }

    public void update(Map<String, Expense> map) {
        expenses.clear();
        expenses.addAll(map.entrySet());
        expenses.add("");
    }

}
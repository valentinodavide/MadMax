package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpensesViewAdapter extends RecyclerView.Adapter<ExpensesViewAdapter.ItemExpensesViewHolder> {

    private static final String TAG = ExpensesViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    private final ArrayList expenses;

    public ExpensesViewAdapter(ListItemClickListener listener, Map<String, Expense> expensesMap) {
        itemClickListener = listener;
        this.expenses = new ArrayList<>();
        expenses.addAll(expensesMap.entrySet());
    }

    class ItemExpensesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView smallTextView;
        private String ID;

        public ItemExpensesViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            smallTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Log.d(TAG, "clickedExpense " + getItem(clickedPosition).getValue().getID());

            itemClickListener.onListItemClick(getItem(clickedPosition).getValue().getID());
        }
    }

    @Override
    public ExpensesViewAdapter.ItemExpensesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.list_item, parent, false);

        ItemExpensesViewHolder itemExpensesViewHolder = new ItemExpensesViewHolder(view);

        Log.d(TAG, "dopo aver istanziato il view holder");
        return itemExpensesViewHolder;
    }

    @Override
    public void onBindViewHolder(final ExpensesViewAdapter.ItemExpensesViewHolder holder, int position) {

        Expense expense = getItem(position).getValue();

        if(expense.getExpensePhoto() != null) {
            String photo = expense.getExpensePhoto();
            int photoUserId = Integer.parseInt(photo);
            holder.imageView.setImageResource(photoUserId);
        }

        holder.nameTextView.setText(expense.getDescription());
        holder.ID = expense.getID();

        holder.smallTextView.setText(expense.getAmount() + " " + expense.getCurrency());

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
    }

}

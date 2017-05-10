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

import java.util.HashMap;

public class ExpensesViewAdapter extends RecyclerView.Adapter<ExpensesViewAdapter.ItemFriendsViewHolder> {

    private static final String TAG = ExpensesViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;

    private static HashMap<String, Expense> expenses = new HashMap<>();
    private int currentFriend = 0;

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    public ExpensesViewAdapter(ListItemClickListener listener) {
        itemClickListener = listener;
    }

    class ItemFriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView smallTextView;
        private String ID;

        public ItemFriendsViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            smallTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Log.d(TAG, "clickedFriend " + expenses.get(String.valueOf(clickedPosition+1)).getID());

            itemClickListener.onListItemClick(expenses.get(String.valueOf(clickedPosition+1)).getID());
        }
    }

    @Override
    public ExpensesViewAdapter.ItemFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.list_item, parent, false);

        ItemFriendsViewHolder itemFriendsViewHolder = new ItemFriendsViewHolder(view);

        return itemFriendsViewHolder;
    }

    @Override
    public void onBindViewHolder(final ExpensesViewAdapter.ItemFriendsViewHolder holder, int position) {

        Expense expense = expenses.get(position);

        Log.d(TAG, expense.toString());

        String photo = expense.getImage();
        int photoUserId = Integer.parseInt(photo);
        holder.imageView.setImageResource(photoUserId);

        holder.nameTextView.setText(expense.getDescription());
        holder.ID = expense.getID();

        holder.smallTextView.setText(expense.getAmount() + " €");

    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpensesData(HashMap<String, Expense> expenses) {
        this.expenses = expenses;
    }

}

package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.entities.Expense;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by alessandro on 19/05/17.
 */

public class PendingExpenseViewAdapter extends RecyclerView.Adapter<PendingExpenseViewAdapter.ItemExpensesViewHolder>  {

    private static final String TAG = PendingExpenseViewAdapter.class.getSimpleName();

    private final ArrayList pendingExpenses;

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private PendingExpenseViewAdapter.ListItemClickListener itemClickListener;
    private PendingExpenseViewAdapter.ListItemLongClickListener itemLongClickListener = null;

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();


    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }

    public PendingExpenseViewAdapter(PendingExpenseViewAdapter.ListItemClickListener listener, Map<String, Expense> pendingMap) {
        itemClickListener = listener;
        this.pendingExpenses = new ArrayList<>();
        pendingExpenses.addAll(pendingMap.entrySet());
    }

    public PendingExpenseViewAdapter(PendingExpenseViewAdapter.ListItemClickListener listener, PendingExpenseViewAdapter.ListItemLongClickListener longListener, Map<String, Expense> pendingMap) {
        itemClickListener = listener;
        itemLongClickListener = longListener;
        this.pendingExpenses = new ArrayList<>();
        pendingExpenses.addAll(pendingMap.entrySet());
    }

    class ItemExpensesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView groupTextView;
        private TextView amountTextView;
        private TextView yesTextView;
        private TextView noTextView;
        private TextView participantsCountTextView;
        private Button thumbUpButton;
        private Button thumbDownButton;


        ItemExpensesViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_pending);
            groupTextView = (TextView) itemView.findViewById(R.id.tv_group);
            amountTextView = (TextView) itemView.findViewById(R.id.tv_pricetext);
            yesTextView = (TextView) itemView.findViewById(R.id.tv_up_count);
            noTextView = (TextView) itemView.findViewById(R.id.tv_down_count);
            participantsCountTextView = (TextView) itemView.findViewById(R.id.tv_participants_count);

            thumbUpButton = (Button) itemView.findViewById(R.id.thumb_up_button);
            thumbDownButton = (Button) itemView.findViewById(R.id.thumb_down_button);


            itemView.setOnClickListener(this);
            thumbUpButton.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            final int clickedPosition = getAdapterPosition();
            int viewid = v.getId();
            Log.d (TAG, " ");
            Log.d (TAG, "clickedPosition " + clickedPosition);
            Log.d(TAG, "clickedExpense " + getItem(clickedPosition).getKey());

            if (v.getId() == thumbUpButton.getId())
            {
                Log.d (TAG, "clicked thumb up");

                //Guardo il mio voto attuale per questa spesa
                databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).child("participants")
                        .child(MainActivity.getCurrentUser().getID()).child("vote")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        if (dataSnapshot.getValue(String.class).equals("null") || dataSnapshot.getValue(String.class).equals("no"))
                        {
                            //up diventa blu, down diventa nero
                            databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).child("participants")
                                    .child(MainActivity.getCurrentUser().getID()).child("vote").setValue("yes");
                            thumbUpButton.setBackgroundResource(R.drawable.thumb_up_blue);
                            thumbDownButton.setBackgroundResource(R.drawable.thumb_down_black);

                        }
                        else if (dataSnapshot.getValue(String.class).equals("yes"))
                        {
                            //up diventa nero
                            databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).child("participants")
                                    .child(MainActivity.getCurrentUser().getID()).child("vote").setValue("null");
                            thumbUpButton.setBackgroundResource(R.drawable.thumb_up_black);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else if (v.getId() == thumbDownButton.getId())
            {
                Log.d (TAG, "clicked thumb down");

                //todo adattare
                //Guardo il mio voto attuale per questa spesa
                databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).child("participants")
                        .child(MainActivity.getCurrentUser().getID()).child("vote")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {


                                if (dataSnapshot.getValue(String.class).equals("null") || dataSnapshot.getValue(String.class).equals("no"))
                                {
                                    //up diventa blu, down diventa nero
                                    databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).child("participants")
                                            .child(MainActivity.getCurrentUser().getID()).child("vote").setValue("yes");
                                    thumbUpButton.setBackgroundResource(R.drawable.thumb_up_blue);
                                    thumbDownButton.setBackgroundResource(R.drawable.thumb_down_black);

                                }
                                else if (dataSnapshot.getValue(String.class).equals("yes"))
                                {
                                    //up diventa nero
                                    databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).child("participants")
                                            .child(MainActivity.getCurrentUser().getID()).child("vote").setValue("null");
                                    thumbUpButton.setBackgroundResource(R.drawable.thumb_up_black);

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            else
            {
                Log.d (TAG, "altro");

            }


            itemClickListener.onListItemClick(getItem(clickedPosition).getValue().getID());
        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, Expense> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedExpense " + itemClicked.getKey());
            //itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);

            return true;

        }
    }

    @Override
    public PendingExpenseViewAdapter.ItemExpensesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.pending_item, parent, false);

        PendingExpenseViewAdapter.ItemExpensesViewHolder itemExpensesViewHolder = new PendingExpenseViewAdapter.ItemExpensesViewHolder(view);

        Log.d(TAG, "dopo aver istanziato il view holder");


        return itemExpensesViewHolder;
    }

    @Override
    public void onBindViewHolder(final PendingExpenseViewAdapter.ItemExpensesViewHolder holder, int position) {

        Expense expense = getItem(position).getValue();

        if(expense.getExpensePhoto() != null) {
            String photo = expense.getExpensePhoto();
            int photoUserId = Integer.parseInt(photo);
            holder.imageView.setImageResource(photoUserId);
        }

        holder.nameTextView.setText(expense.getDescription());
        holder.groupTextView.setText(expense.getGroupName());
        holder.yesTextView.setText(expense.getYes().toString());
        holder.noTextView.setText(expense.getNo().toString());
        holder.participantsCountTextView.setText(expense.getParticipantsCount().toString());


        DecimalFormat df = new DecimalFormat("#.##");
        Double amount = expense.getAmount();
        holder.amountTextView.setText(df.format(amount) + " " + expense.getCurrency());



    }

    @Override
    public int getItemCount() {
        return pendingExpenses.size();
    }

    public Map.Entry<String, Expense> getItem(int position) {
        return (Map.Entry) pendingExpenses.get(position);
    }

    public void update(Map<String, Expense> map) {
        pendingExpenses.clear();
        pendingExpenses.addAll(map.entrySet());
    }




}

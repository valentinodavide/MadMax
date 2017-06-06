package com.polito.mad17.madmax.activities.expenses;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class PendingExpenseViewAdapter extends RecyclerView.Adapter<PendingExpenseViewAdapter.ItemExpensesViewHolder>  {

    private static final String TAG = PendingExpenseViewAdapter.class.getSimpleName();

    private final ArrayList pendingExpenses;

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private PendingExpenseViewAdapter.ListItemClickListener itemClickListener;
    private PendingExpenseViewAdapter.ListItemLongClickListener itemLongClickListener = null;

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private Context mContext;
    private Activity activity;
    private LayoutInflater layoutInflater;

    Map.Entry<String, Group> nullEntry = new Map.Entry<String, Group>() {
        @Override
        public String getKey() {
            return "0";
        }

        @Override
        public Group getValue() {
            return null;
        }

        @Override
        public Group setValue(Group value) {
            return null;
        }
    };

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }

    public PendingExpenseViewAdapter(PendingExpenseViewAdapter.ListItemClickListener listener, Map<String, Expense> pendingMap, Activity activity) {
        itemClickListener = listener;
        this.pendingExpenses = new ArrayList<>();
        this.activity = activity;
        this.mContext = activity.getApplicationContext();
        pendingExpenses.addAll(pendingMap.entrySet());
        pendingExpenses.add(nullEntry);
    }

    public PendingExpenseViewAdapter(Context context, PendingExpenseViewAdapter.ListItemClickListener listener, PendingExpenseViewAdapter.ListItemLongClickListener longListener, Map<String, Expense> pendingMap) {
        itemClickListener = listener;
        itemLongClickListener = longListener;
        this.pendingExpenses = new ArrayList<>();
        this.mContext = context;
        pendingExpenses.addAll(pendingMap.entrySet());
        pendingExpenses.add(nullEntry);
    }

    class ItemExpensesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private ImageView partecipantsImageView;
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
            groupTextView = (TextView) itemView.findViewById(R.id.tv_receiver);
            amountTextView = (TextView) itemView.findViewById(R.id.tv_pricetext);
            yesTextView = (TextView) itemView.findViewById(R.id.tv_up_count);
            noTextView = (TextView) itemView.findViewById(R.id.tv_down_count);
            participantsCountTextView = (TextView) itemView.findViewById(R.id.tv_participants_count);
            partecipantsImageView = (ImageView) itemView.findViewById(R.id.participants_icon);

            thumbUpButton = (Button) itemView.findViewById(R.id.thumb_up_button);
            thumbDownButton = (Button) itemView.findViewById(R.id.thumb_down_button);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            thumbUpButton.setOnClickListener(this);
            thumbDownButton.setOnClickListener(this);
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

                databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        String myVote = mutableData.child("participants").child(MainActivity.getCurrentUID()).child("vote").getValue(String.class);
                        final Event.EventType eventType;

                        if (myVote.equals("null") || myVote.equals("no"))
                        {
                            //up diventa blu, down diventa nero
                            mutableData.child("participants").child(MainActivity.getCurrentUID()).child("vote").setValue("yes");
                            eventType = Event.EventType.PENDING_EXPENSE_VOTE_UP;
                        }
                        else
                        {
                            //up diventa nero
                            mutableData.child("participants").child(MainActivity.getCurrentUID()).child("vote").setValue("null");
                            eventType = Event.EventType.PENDING_EXPENSE_VOTE_DOWN;
                        }

                        // add event for PENDING_EXPENSE_VOTE
                        databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User currentUser = MainActivity.getCurrentUser();
                                    Event event = new Event(
                                            dataSnapshot.child("groupID").getValue(String.class),
                                            eventType,
                                            currentUser.getName() + " " + currentUser.getSurname(),
                                            dataSnapshot.child("description").getValue(String.class)
                                    );
                                    event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                                    event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                                    FirebaseUtils.getInstance().addEvent(event);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, databaseError.toException());
                                }
                            }
                        );

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                        // Transaction completed
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                    }
                });
            }
            else if (v.getId() == thumbDownButton.getId())
            {
                Log.d (TAG, "clicked thumb down");

                databaseReference.child("proposedExpenses").child(getItem(clickedPosition).getKey()).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {

                        String myVote = mutableData.child("participants").child(MainActivity.getCurrentUID()).child("vote").getValue(String.class);

                        if (myVote.equals("null") || myVote.equals("yes"))
                        {
                            //up diventa blu, down diventa nero
                            mutableData.child("participants").child(MainActivity.getCurrentUID()).child("vote").setValue("no");
                        }
                        else if (myVote.equals("no")) {
                            //up diventa nero
                            mutableData.child("participants").child(MainActivity.getCurrentUID()).child("vote").setValue("null");
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                        // Transaction completed
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                    }
                });
            }
            else
            {
                Log.d (TAG, "cliccato l'intero item");

                if(!getItem(clickedPosition).getKey().equals("0")) {
                    itemClickListener.onListItemClick(getItem(clickedPosition).getKey());
                }
            }
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
    public PendingExpenseViewAdapter.ItemExpensesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.pending_item, parent, false);

        PendingExpenseViewAdapter.ItemExpensesViewHolder itemExpensesViewHolder = new PendingExpenseViewAdapter.ItemExpensesViewHolder(view);

        Log.d(TAG, "dopo aver istanziato il view holder");

        return itemExpensesViewHolder;
    }

    @Override
    public void onBindViewHolder(final PendingExpenseViewAdapter.ItemExpensesViewHolder holder, int position) {

        if(position == (pendingExpenses.size() - 1))
        {
            Log.d(TAG, "item.getKey().equals(\"nullGroup\")");
            holder.imageView.setVisibility(View.GONE);
            holder.nameTextView.setText("");
            holder.groupTextView.setText("");
            holder.yesTextView.setText("");
            holder.noTextView.setText("");
            holder.participantsCountTextView.setText("");
            holder.amountTextView.setText("");
            holder.partecipantsImageView.setVisibility(View.GONE);

            holder.thumbUpButton.setVisibility(View.GONE);
            holder.thumbDownButton.setVisibility(View.GONE);
        }
        else {
            Expense expense = getItem(position).getValue();

            String p = expense.getGroupImage();

            if (p != null) {
                //int photoUserId = Integer.parseInt(p);
                //holder.imageView.setImageResource(photoUserId);

                Glide.with(layoutInflater.getContext()).load(p)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);
            }
            else
            {
                Glide.with(layoutInflater.getContext()).load(R.drawable.group_default)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);

            }

            holder.nameTextView.setText(expense.getDescription());
            holder.groupTextView.setText(expense.getGroupName());
            holder.yesTextView.setText(expense.getYes().toString());
            holder.noTextView.setText(expense.getNo().toString());
            holder.participantsCountTextView.setText(expense.getParticipantsCount().toString());

            holder.imageView.setVisibility(View.VISIBLE);
            holder.partecipantsImageView.setVisibility(View.VISIBLE);
            holder.thumbUpButton.setVisibility(View.VISIBLE);
            holder.thumbDownButton.setVisibility(View.VISIBLE);

            DecimalFormat df = new DecimalFormat("#.##");
            Double amount = expense.getAmount();
            holder.amountTextView.setText(df.format(amount) + " " + expense.getCurrency());

            if (expense.getMyVote().equals("yes")) {
                holder.thumbUpButton.setBackgroundResource(R.drawable.thumb_up_teal);
                holder.thumbDownButton.setBackgroundResource(R.drawable.thumb_down_black);
            } else if (expense.getMyVote().equals("no")) {
                holder.thumbUpButton.setBackgroundResource(R.drawable.thumb_up_black);
                holder.thumbDownButton.setBackgroundResource(R.drawable.thumb_down_amber);
            } else if (expense.getMyVote().equals("null")) {
                holder.thumbUpButton.setBackgroundResource(R.drawable.thumb_up_black);
                holder.thumbDownButton.setBackgroundResource(R.drawable.thumb_down_black);
            }
        }
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
        pendingExpenses.add(nullEntry);
    }
}

package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
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
import com.polito.mad17.madmax.activities.users.FriendsViewAdapter;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.User;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by alessandro on 26/05/17.
 */

public class ParticipantsViewAdapter extends RecyclerView.Adapter<ParticipantsViewAdapter.ItemParticipantsViewHolder> {

    private static final String TAG = ParticipantsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ParticipantsViewAdapter.ListItemClickListener itemClickListener;
    private ParticipantsViewAdapter.ListItemLongClickListener itemLongClickListener = null;
    private Context context;

    private final ArrayList mData;

    private LayoutInflater layoutInflater;


    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }


    public ParticipantsViewAdapter(Context context, ParticipantsViewAdapter.ListItemClickListener listener, Map<String, User> map) {
        this.context = context;
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public ParticipantsViewAdapter(Context context, ParticipantsViewAdapter.ListItemClickListener listener, ParticipantsViewAdapter.ListItemLongClickListener longListener, Map<String, User> map) {
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

    class ItemParticipantsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView toPayTextView;
        private TextView alreadyPaidTextView;
        private TextView balanceTextTextView;

        public ItemParticipantsViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_participant_name);
            toPayTextView = (TextView) itemView.findViewById(R.id.tv_share);
            alreadyPaidTextView = (TextView) itemView.findViewById(R.id.tv_already_paid);
            balanceTextTextView = (TextView) itemView.findViewById(R.id.tv_balance_text);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            //Log.d(TAG, "clickedFriend " + friends.get(String.valueOf(clickedPosition+1)).getID());
            Map.Entry<String, User> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedParticipant " + itemClicked.getKey());

            //itemClickListener.onListItemClick(itemClicked.getKey());

        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, User> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedParticipant " + itemClicked.getKey());
            //itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);

            return true;
        }
    }

    @Override
    public ParticipantsViewAdapter.ItemParticipantsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.participant_item, parent, false);

        ParticipantsViewAdapter.ItemParticipantsViewHolder itemParticipantsViewHolder = new ParticipantsViewAdapter.ItemParticipantsViewHolder(view);

        return itemParticipantsViewHolder;
    }

    @Override
    public void onBindViewHolder(final ParticipantsViewAdapter.ItemParticipantsViewHolder holder, int position) {

        Map.Entry<String, User> item = getItem(position);


        String photo = item.getValue().getProfileImage();
        if (photo != null)
        {
            Glide.with(layoutInflater.getContext()).load(photo)
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
           /* int photoUserId = Integer.parseInt(photo);
            holder.imageView.setImageResource(photoUserId);*/
        }

        holder.nameTextView.setText(item.getValue().getName() + " " + item.getValue().getSurname());

        DecimalFormat df = new DecimalFormat("#.##");

        //todo mettere getCurrency
        //String share = df.format(Math.abs(item.getValue().getDueImport())) + " " + "€";
        //holder.shareTextView.setText(share);
        Double dueImport = item.getValue().getDueImport();

        if (dueImport > 0)
        {
            holder.balanceTextTextView.setText(R.string.credit_of);
            holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

            String balance = df.format(Math.abs(dueImport)) + " €";
            Log.d(TAG, "balance "  + balance);

            holder.toPayTextView.setText(balance);
            holder.toPayTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        }
        else
        {
            if (dueImport < 0)
            {
                holder.balanceTextTextView.setText(R.string.debt_of);
                holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                String balance = df.format(Math.abs(dueImport)) + " €";
                Log.d(TAG, "balance "  + balance + " " + R.color.colorAccent);
                holder.toPayTextView.setText(balance);
                holder.toPayTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            }
            else
            {
                holder.balanceTextTextView.setText(R.string.no_debts);
                holder.toPayTextView.setText("0 €");
                holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
            }
        }

        //todo mettere getCurrency
        String alreadyPaid = df.format(Math.abs(item.getValue().getAlreadyPaid())) + " " + "€";
        holder.alreadyPaidTextView.setText(alreadyPaid);

    }

    public Map.Entry<String, User> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


}

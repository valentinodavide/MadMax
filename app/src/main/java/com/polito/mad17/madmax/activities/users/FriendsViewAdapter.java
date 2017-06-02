package com.polito.mad17.madmax.activities.users;

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
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class FriendsViewAdapter extends RecyclerView.Adapter<FriendsViewAdapter.ItemFriendsViewHolder> {

    private static final String TAG = FriendsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;
    private ListItemLongClickListener itemLongClickListener = null;

    private final ArrayList mData;

    private LayoutInflater layoutInflater;
    private Context context;


    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }


    public FriendsViewAdapter(Context context, ListItemClickListener listener, Map<String, User> map) {
        this.context = context;
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        mData.add("");
    }

    public FriendsViewAdapter(Context context, ListItemClickListener listener, ListItemLongClickListener longListener, Map<String, User> map) {
        this.context = context;
        itemClickListener = listener;
        itemLongClickListener = longListener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        mData.add("");
    }

    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
        mData.add("");
    }

    class ItemFriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView balanceTextTextView;
        private TextView balanceTextView;
        private String ID;

        public ItemFriendsViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_sender);
            balanceTextTextView = (TextView) itemView.findViewById(R.id.tv_balance_text);
            //balanceTextTextView.setVisibility(View.INVISIBLE);
            balanceTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            //balanceTextView.setVisibility(View.INVISIBLE);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            //Log.d(TAG, "clickedFriend " + friends.get(String.valueOf(clickedPosition+1)).getID());
            Map.Entry<String, User> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedFriend " + itemClicked.getKey());

            itemClickListener.onListItemClick(itemClicked.getKey());
        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, User> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedFriend " + itemClicked.getKey());
            itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);

            return true;
        }
    }

    @Override
    public FriendsViewAdapter.ItemFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);

        ItemFriendsViewHolder itemFriendsViewHolder = new ItemFriendsViewHolder(view);

        return itemFriendsViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemFriendsViewHolder holder, int position) {


        if (position == (mData.size() - 1)) {
            Log.d(TAG, "item.getKey().equals(\"nullGroup\")");
            holder.nameTextView.setText("");
            holder.balanceTextView.setText("");
            holder.balanceTextTextView.setText("");
            holder.itemView.setOnClickListener(null);

        }
        else {
            Map.Entry<String, User> item = getItem(position);

            Log.d(TAG, item.getKey() + " " + item.getValue().getName() + " " + item.getValue().getProfileImage());

            String photo = item.getValue().getProfileImage();
            if (photo != null && !photo.equals(""))
            {
                Glide.with(layoutInflater.getContext()).load(photo)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);
            }
            else if(photo == null || photo.equals(""))
            {
                Glide.with(layoutInflater.getContext()).load(R.drawable.ic_face)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);
            }

            holder.nameTextView.setText(item.getValue().getName() + " " + item.getValue().getSurname());


            Double balance = item.getValue().getBalanceWithGroup();

            if (balance != null)
            {
                DecimalFormat df = new DecimalFormat("#.##");
                if (balance > 0) {
                    holder.balanceTextTextView.setText(R.string.should_receive_from_the_group);
                    holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                    //todo mettere valuta
                    String balanceText = df.format(Math.abs(balance)) + " €";
                    holder.balanceTextView.setText(balanceText);
                    holder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                }
                else
                {
                    if (balance < 0) {
                        holder.balanceTextTextView.setText(R.string.owes_to_the_group);
                        holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                        //todo mettere valuta
                        String balanceText = df.format(Math.abs(balance)) + " €";
                        holder.balanceTextView.setText(balanceText);
                        holder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                    } else {
                        holder.balanceTextTextView.setText(R.string.no_debts);
                        holder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                        //todo mettere valuta
                        holder.balanceTextView.setText("0 €");
                    }
                    //holder.balanceTextView.setVisibility(View.GONE);
                }
            }

            else if (balance == null)
            {
                holder.balanceTextView.setVisibility(View.GONE);
                holder.balanceTextTextView.setVisibility(View.GONE);

            }

        }
    }

    public Map.Entry<String, User> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
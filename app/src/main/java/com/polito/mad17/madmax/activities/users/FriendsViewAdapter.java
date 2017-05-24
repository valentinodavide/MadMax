package com.polito.mad17.madmax.activities.users;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsViewAdapter extends RecyclerView.Adapter<FriendsViewAdapter.ItemFriendsViewHolder> {

    private static final String TAG = FriendsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;
    private ListItemLongClickListener itemLongClickListener = null;

    private static HashMap<String, User> friends = new HashMap<>();
    private static User myself;
    private int currentFriend = 0;
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


    public FriendsViewAdapter(ListItemClickListener listener, Map<String, User> map) {
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public FriendsViewAdapter(ListItemClickListener listener, ListItemLongClickListener longListener, Map<String, User> map) {
        itemClickListener = listener;
        itemLongClickListener = longListener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    class ItemFriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView balanceTextView;
        private String ID;

        public ItemFriendsViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            balanceTextView = (TextView) itemView.findViewById(R.id.tv_balance);
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
    public void onBindViewHolder(final FriendsViewAdapter.ItemFriendsViewHolder holder, int position) {

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
        holder.ID = item.getValue().getID();
        holder.balanceTextView.setVisibility(View.GONE);


    }

    public Map.Entry<String, User> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
package com.polito.mad17.madmax.activities.users;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsViewAdapter extends RecyclerView.Adapter<FriendsViewAdapter.ItemFriendsViewHolder> {

    private static final String TAG = FriendsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;

    private static HashMap<String, User> friends = new HashMap<>();
    private static User myself;
    private int currentFriend = 0;
    private final ArrayList mData;


    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    public FriendsViewAdapter(ListItemClickListener listener, Map<String, User> map) {
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    class ItemFriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            //Log.d(TAG, "clickedFriend " + friends.get(String.valueOf(clickedPosition+1)).getID());
            Map.Entry<String, User> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedGroup " + itemClicked.getKey());

            itemClickListener.onListItemClick(itemClicked.getKey());

        }
    }

    @Override
    public FriendsViewAdapter.ItemFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
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
            int photoUserId = Integer.parseInt(photo);
            holder.imageView.setImageResource(photoUserId);
        }


        holder.nameTextView.setText(item.getValue().getName() + " " + item.getValue().getSurname());
        holder.ID = item.getValue().getID();
        holder.balanceTextView.setVisibility(View.GONE);

        //mydebt = mio debito con il membro f
        /*
        Double mydebt = myself.getBalanceWithUsers().get(friend.getID());

        DecimalFormat df = new DecimalFormat("#.##");

        if (mydebt != null)
        {
            if (mydebt > 0)
            {
                holder.smallTextView.setText("+ " + df.format(mydebt) + " €");
                holder.smallTextView.setBackgroundResource(R.color.greenBalance);

            }
            else if (mydebt < 0)
            {
                holder.smallTextView.setText("- " + df.format(Math.abs(mydebt)) + " €");
                holder.smallTextView.setBackgroundColor(Color.rgb(255,0,0));
            }
            else
            {
                holder.smallTextView.setText("" + df.format(mydebt) + " €");
                holder.smallTextView.setBackgroundResource(R.color.greenBalance);

            }
        }
        */



    }

    public Map.Entry<String, User> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }



}

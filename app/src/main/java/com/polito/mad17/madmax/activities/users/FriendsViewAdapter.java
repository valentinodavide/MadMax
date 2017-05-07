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
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.HashMap;

public class FriendsViewAdapter extends RecyclerView.Adapter<FriendsViewAdapter.ItemFriendsViewHolder> {

    private static final String TAG = FriendsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;

    private static HashMap<String, User> friends = new HashMap<>();
    private static User myself;
    private int currentFriend = 0;

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    public FriendsViewAdapter(ListItemClickListener listener) {
        itemClickListener = listener;
    }

    class ItemFriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView smallTextView;
        private String ID;

        public ItemFriendsViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_friend);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_friend_name);
            smallTextView = (TextView) itemView.findViewById(R.id.tv_friend_balance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Log.d(TAG, "clickedFriend " + friends.get(String.valueOf(clickedPosition+1)).getID());

            itemClickListener.onListItemClick(friends.get(String.valueOf(clickedPosition+1)).getID());
        }
    }

    @Override
    public FriendsViewAdapter.ItemFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_friend, parent, false);

        ItemFriendsViewHolder itemFriendsViewHolder = new ItemFriendsViewHolder(view);

        return itemFriendsViewHolder;
    }

    @Override
    public void onBindViewHolder(final FriendsViewAdapter.ItemFriendsViewHolder holder, int position) {


        User friend = null;

        while (friend == null) {
            friend = friends.get(String.valueOf(currentFriend));
            currentFriend++;
        }

        Log.d(TAG, friend.toString());

        String photo = friend.getProfileImage();
        if (photo != null)
        {
            int photoUserId = Integer.parseInt(photo);
            holder.imageView.setImageResource(photoUserId);
        }


        holder.nameTextView.setText(friend.getName() + " " + friend.getSurname());
        holder.ID = friend.getID();

        //mydebt = mio debito con il membro f
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



    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void setFriendsData(HashMap<String, User> users, User myself) {
        friends = users;
        friends.remove("0");
        this.myself = myself;
    }

    //serve per stampare la lista dei membri aggiunti a un nuovo gruppo che si sta creando
    public void setMembersData(HashMap<String, User> users, User myself) {
        friends = users;
        this.myself = myself;
    }

}

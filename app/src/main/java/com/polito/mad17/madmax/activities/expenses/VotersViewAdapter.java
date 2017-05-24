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
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by alessandro on 23/05/17.
 */

public class VotersViewAdapter extends RecyclerView.Adapter<VotersViewAdapter.ItemVotersViewHolder> {

    private static final String TAG = VotersViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private VotersViewAdapter.ListItemClickListener itemClickListener;
    private VotersViewAdapter.ListItemLongClickListener itemLongClickListener = null;
    private final ArrayList mData;


    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }


    public VotersViewAdapter(VotersViewAdapter.ListItemClickListener listener, Map<String, User> map) {
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public VotersViewAdapter(VotersViewAdapter.ListItemClickListener listener, VotersViewAdapter.ListItemLongClickListener longListener, Map<String, User> map) {
        itemClickListener = listener;
        itemLongClickListener = longListener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    class ItemVotersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView voteTextView;

        public ItemVotersViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_voter_name);
            voteTextView = (TextView) itemView.findViewById(R.id.tv_vote);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, User> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedVoter " + itemClicked.getKey());

            //itemClickListener.onListItemClick(itemClicked.getKey());

        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, User> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedVoter " + itemClicked.getKey());
            //itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);
            return true;

        }
    }

    @Override
    public VotersViewAdapter.ItemVotersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.voter_item, parent, false);

        VotersViewAdapter.ItemVotersViewHolder itemVotersViewHolder = new VotersViewAdapter.ItemVotersViewHolder(view);

        return itemVotersViewHolder;
    }

    @Override
    public void onBindViewHolder(final VotersViewAdapter.ItemVotersViewHolder holder, int position) {

        Map.Entry<String, User> item = getItem(position);


        String photo = item.getValue().getProfileImage();
        if (photo != null)
        {
            int photoUserId = Integer.parseInt(photo);
            holder.imageView.setImageResource(photoUserId);
        }


        holder.nameTextView.setText(item.getValue().getName() + " " + item.getValue().getSurname());

        if (item.getValue().getVote().equals("yes"))
        {
            holder.voteTextView.setBackgroundResource(R.drawable.thumb_up_black);
        }
        else if (item.getValue().getVote().equals("no"))
        {
            holder.voteTextView.setBackgroundResource(R.drawable.thumb_down_black);
        }
        else if (item.getValue().getVote().equals("null"))
        {
            holder.voteTextView.setVisibility(View.GONE);
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

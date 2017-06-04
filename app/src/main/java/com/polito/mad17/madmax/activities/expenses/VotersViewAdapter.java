package com.polito.mad17.madmax.activities.expenses;

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
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.Map;

public class VotersViewAdapter extends RecyclerView.Adapter<VotersViewAdapter.ItemVotersViewHolder> {

    private static final String TAG = VotersViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    private final ArrayList mData;

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

    public VotersViewAdapter(Map<String, User> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        mData.add(nullEntry);
    }

    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
        mData.add(nullEntry);
    }

    class ItemVotersViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView voteTextView;

        public ItemVotersViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_voter_name);
            voteTextView = (TextView) itemView.findViewById(R.id.tv_vote);
        }
    }

    @Override
    public VotersViewAdapter.ItemVotersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.voter_item, parent, false);

        VotersViewAdapter.ItemVotersViewHolder itemVotersViewHolder = new VotersViewAdapter.ItemVotersViewHolder(view);

        return itemVotersViewHolder;
    }

    @Override
    public void onBindViewHolder(final VotersViewAdapter.ItemVotersViewHolder holder, int position) {

        if(position == (mData.size() - 1))
        {
            Log.d(TAG, "item.getKey().equals(\"nullGroup\")");
            holder.nameTextView.setText("");
            holder.voteTextView.setVisibility(View.GONE);
        }
        else
        {
            Map.Entry<String, User> item = getItem(position);

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
                Glide.with(layoutInflater.getContext()).load(R.drawable.user_default)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);
            }

            holder.nameTextView.setText(item.getValue().getName() + " " + item.getValue().getSurname());

            Log.d(TAG, "vote " + item.getValue().getVote());

            if (item.getValue().getVote().equals("yes")) {
                holder.voteTextView.setVisibility(View.VISIBLE);
                holder.voteTextView.setBackgroundResource(R.drawable.thumb_up_black);
            } else if (item.getValue().getVote().equals("no")) {
                holder.voteTextView.setVisibility(View.VISIBLE);
                holder.voteTextView.setBackgroundResource(R.drawable.thumb_down_black);
            } else if (item.getValue().getVote().equals("null")) {
                holder.voteTextView.setVisibility(View.GONE);
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

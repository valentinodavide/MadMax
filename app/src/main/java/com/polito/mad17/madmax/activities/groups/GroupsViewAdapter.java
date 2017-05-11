package com.polito.mad17.madmax.activities.groups;

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

public class GroupsViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList mData;
    private static final String TAG = GroupsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;

    //public static HashMap<String, Group> groups = new HashMap<>();
    public static User myself;



    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    public GroupsViewAdapter(ListItemClickListener listener, Map<String, Group> map) {
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());


    }

    public void update(Map<String, Group> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    class ItemGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView smallTextView;
        private String ID;

        public ItemGroupViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            smallTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            //Log.d(TAG, "clickedGroup " + groups.get(String.valueOf(clickedPosition)).getID());
            //Map.Entry<String, Group> itemClicked = (Map.Entry) mData.get(clickedPosition);
            Map.Entry<String, Group> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedGroup " + itemClicked.getKey());


            //itemClickListener.onListItemClick(groups.get(String.valueOf(clickedPosition)).getID());
            itemClickListener.onListItemClick(itemClicked.getKey());


        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.list_item, parent, false);

        ItemGroupViewHolder itemGroupViewHolder = new ItemGroupViewHolder(view);
        Log.d(TAG, "created new itemGroupViewHolder, viewType " + viewType);

        return itemGroupViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        Log.d(TAG, "type " + getItemViewType(position));

        if(holder instanceof ItemGroupViewHolder)
            Log.d(TAG, "group " + holder.getItemViewType());

        final ItemGroupViewHolder groupViewHolder = (ItemGroupViewHolder) holder;

        Map.Entry<String, Group> item = getItem(position);


        //String p = groups.get(String.valueOf(position)).getImage();
        String p = item.getValue().getImage();
        if (p!= null)
        {
            int photoId = Integer.parseInt(p);
            groupViewHolder.imageView.setImageResource(photoId);
        }


        //groupViewHolder.nameTextView.setText(groups.get(String.valueOf(position)).getName());
        //groupViewHolder.ID = groups.get(String.valueOf(position)).getID();
        groupViewHolder.nameTextView.setText(item.getValue().getName());
        groupViewHolder.ID = item.getValue().getID();
        //groupViewHolder.smallTextView.setText(item.getValue().getBalance().toString());

        //todo mettere debito verso il gruppo
        //mydebt = mio debito con il gruppo

        Double mygroupdebt = item.getValue().getBalance();
        if (mygroupdebt == null) {
            groupViewHolder.smallTextView.setVisibility(View.GONE);
            return;
        }

        DecimalFormat df = new DecimalFormat("#.##");

        if (mygroupdebt > 0)
        {
            groupViewHolder.smallTextView.setText("+ " + df.format(mygroupdebt) + " €");
            groupViewHolder.smallTextView.setBackgroundResource(R.color.greenBalance);

        }
        else if (mygroupdebt < 0)
        {
            groupViewHolder.smallTextView.setText("- " + df.format(Math.abs(mygroupdebt)) + " €");
            groupViewHolder.smallTextView.setBackgroundColor(Color.rgb(255,0,0));
        }
        else
        {
            groupViewHolder.smallTextView.setText("" + df.format(mygroupdebt) + " €");
            groupViewHolder.smallTextView.setBackgroundResource(R.color.greenBalance);
        }

    }


    public Map.Entry<String, Group> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


}

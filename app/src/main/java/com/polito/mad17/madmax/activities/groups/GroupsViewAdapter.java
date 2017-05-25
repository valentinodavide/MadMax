package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import static com.polito.mad17.madmax.R.string.balance;

public class GroupsViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList mData;
    private static final String TAG = GroupsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;
    private ListItemLongClickListener itemLongClickListener = null;
    private Context context;

    //public static HashMap<String, Group> groups = new HashMap<>();
    public static User myself;

    private LayoutInflater layoutInflater;

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }
    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }

    public GroupsViewAdapter(Context context, ListItemClickListener listener, Map<String, Group> map) {
        this.context = context;
        itemClickListener = listener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public GroupsViewAdapter(Context context, ListItemClickListener listener, ListItemLongClickListener longListener, Map<String, Group> map) {
        this.context = context;
        itemClickListener = listener;
        itemLongClickListener = longListener;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    public void update(Map<String, Group> map) {
        mData.clear();
        mData.addAll(map.entrySet());
    }

    class ItemGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView balanceTextTextView;
        private TextView balanceTextView;
        //private String ID;

        public ItemGroupViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            balanceTextTextView = (TextView) itemView.findViewById(R.id.tv_balance_text);
            balanceTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();

            Map.Entry<String, Group> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedGroup " + itemClicked.getKey());

            itemClickListener.onListItemClick(itemClicked.getKey());
        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, Group> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedGroup " + itemClicked.getKey());
            itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);

            return true;

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);

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
            Glide.with(layoutInflater.getContext()).load(p)
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(groupViewHolder.imageView);
        }
        groupViewHolder.nameTextView.setText(item.getValue().getName());

        //todo mettere debito verso il gruppo
        //mydebt = mio debito con il gruppo

        Double mygroupdebt = item.getValue().getBalance();
        if (mygroupdebt == null) {
            groupViewHolder.balanceTextTextView.setVisibility(View.GONE);
            groupViewHolder.balanceTextView.setVisibility(View.GONE);
            return;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        if (mygroupdebt > 0)
        {
            groupViewHolder.balanceTextTextView.setText(R.string.credit_of);
            groupViewHolder.balanceTextTextView.setTextColor(context.getColor(R.color.colorPrimaryDark));

            String balance = df.format(Math.abs(mygroupdebt)) + " €";
            Log.d(TAG, "balance "  + balance);

            groupViewHolder.balanceTextView.setText(balance);
            groupViewHolder.balanceTextView.setTextColor(context.getColor(R.color.colorPrimaryDark));

        }
        else
        {
            if (mygroupdebt < 0)
            {
                groupViewHolder.balanceTextTextView.setText(R.string.debt_of);
                groupViewHolder.balanceTextTextView.setTextColor(context.getColor(R.color.colorAccent));

                String balance = df.format(Math.abs(mygroupdebt)) + " €";
                Log.d(TAG, "balance "  + balance + " " + R.color.colorAccent);
                groupViewHolder.balanceTextView.setText(balance);
                groupViewHolder.balanceTextView.setTextColor(context.getColor(R.color.colorAccent));
            }
            else
            {
                groupViewHolder.balanceTextTextView.setText(R.string.no_debts);
                groupViewHolder.balanceTextTextView.setTextColor(context.getColor(R.color.colorSecondaryText));
            }
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

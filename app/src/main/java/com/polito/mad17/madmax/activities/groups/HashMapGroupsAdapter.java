package com.polito.mad17.madmax.activities.groups;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.Group;

import java.util.ArrayList;
import java.util.Map;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by alessandro on 07/05/17.
 */

public class HashMapGroupsAdapter extends BaseAdapter {
    private final ArrayList mData;

    public HashMapGroupsAdapter(Map<String, Group> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        mData.add("");
    }


    public void update(Map<String, Group> map) {
        mData.clear();
        mData.addAll(map.entrySet());
        mData.add("");
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, Group> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        } else {
            result = convertView;
        }

        TextView name = (TextView) result.findViewById(R.id.tv_name);

        if(position == (mData.size() - 1))
        {
            Log.d(TAG, "item.getKey().equals(\"nullGroup\")");
//            groupViewHolder.imageView.
            name.setText("");
        }
        else
        {
            Map.Entry<String, Group> item = getItem(position);

            name.setText(item.getValue().getName());

            ImageView img_photo = (ImageView)result.findViewById(R.id.img_photo);
            Glide.with(parent.getContext()).load(item.getValue().getImage())
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(parent.getContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_photo);
            Log.d("Adapter image: ", item.getValue().getImage());

            result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    String groupID = getItem(position).getKey();
                }
            });
        }
        return result;
    }


}

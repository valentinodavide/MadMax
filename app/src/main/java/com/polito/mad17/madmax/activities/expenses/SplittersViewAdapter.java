package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.DecimalDigitsInputFilter;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alessandro on 05/06/17.
 */

public class SplittersViewAdapter extends RecyclerView.Adapter<SplittersViewAdapter.ItemSplittersViewAdapter> {

    private static final String TAG = SplittersViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    private final ArrayList mData;
    private Context context;
    private HashMap<String, Double> amounts = new HashMap<>();
    private LayoutInflater layoutInflater;
    private EditTextUpdateListener editTextUpdateListener = null;


    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }

    public interface EditTextUpdateListener {
        void onListItemEditTextUpdate(HashMap<String, Double> amounts);
    }

    public SplittersViewAdapter(Map<String, User> map, Context context, EditTextUpdateListener editTextUpdateListener) {
        this.context = context;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        amounts = new HashMap<>();
        for (Map.Entry<String, User> entry : map.entrySet())
        {
            amounts.put(entry.getKey(), entry.getValue().getSplitPart());
        }
        this.editTextUpdateListener = editTextUpdateListener;
    }

    public void update(Map<String, User> map) {
        mData.clear();
        mData.addAll(map.entrySet());
        for (Map.Entry<String, User> entry : map.entrySet())
        {
            amounts.put(entry.getKey(), entry.getValue().getSplitPart());
        }
        //mData.add(nullEntry);
    }

    class ItemSplittersViewAdapter extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView nameTextView;
        private EditText amountEditText;
        private TextView currencyTextView;
        public MyCustomEditTextListener myCustomEditTextListener;



        public ItemSplittersViewAdapter(View itemView, MyCustomEditTextListener myCustomEditTextListener) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_splitter_name);
            amountEditText = (EditText) itemView.findViewById(R.id.edit_amount);
            amountEditText.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});
            //Set listener on EditText
            this.myCustomEditTextListener = myCustomEditTextListener;
            this.amountEditText.addTextChangedListener(myCustomEditTextListener);
            currencyTextView = (TextView) itemView.findViewById(R.id.tv_currency);

        }
    }

    @Override
    public SplittersViewAdapter.ItemSplittersViewAdapter onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.split_item, parent, false);

        SplittersViewAdapter.ItemSplittersViewAdapter itemSplittersViewAdapter = new SplittersViewAdapter.ItemSplittersViewAdapter(view, new MyCustomEditTextListener());

        return itemSplittersViewAdapter;
    }



    @Override
    public void onBindViewHolder(final SplittersViewAdapter.ItemSplittersViewAdapter holder, final int position) {


        final Map.Entry<String, User> item = getItem(position);

        String photo = item.getValue().getProfileImage();

        if(photo != null) {
            Glide.with(context).load(photo)
                    .placeholder(R.drawable.user_default)
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        }
        else {
            Glide.with(context).load(R.drawable.user_default)
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        }

        Log.d (TAG, "Nome: " + item.getValue().getName());
        Log.d (TAG, "Cognome: " + item.getValue().getSurname());
        holder.nameTextView.setText(item.getValue().getName() + " " + item.getValue().getSurname());

        holder.currencyTextView.setText(item.getValue().getExpenseCurrency());

        holder.amountEditText.setText(item.getValue().getSplitPart().toString());

        // update MyCustomEditTextListener every time we bind a new item
        // so that it knows what item in mDataset to update
        holder.myCustomEditTextListener.updateUser(item);


    }

    public Map.Entry<String, User> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private class MyCustomEditTextListener implements TextWatcher {
        private Map.Entry<String, User> item;

        public void updateUser(Map.Entry<String, User> item) {
            this.item = item;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            double value;
            String c = charSequence.toString();
            if(!c.isEmpty())
                try
                {
                    Log.d (TAG, "Charsequence: " + c);
                    Log.d (TAG, "Changing " + item.getKey());
                    value= Double.parseDouble(c);
                    Log.d (TAG, "Put " + value + " in " + item.getValue().getName() + " " + item.getValue().getSurname());
                    amounts.put(item.getKey(), value);
                    Log.d (TAG, "Now amount contains: ");
                    for (Map.Entry<String, Double> entry : amounts.entrySet())
                    {
                        Log.d (TAG, entry.getKey() + " " + entry.getValue());
                    }

                        editTextUpdateListener.onListItemEditTextUpdate(amounts);
                    // it means it is double
                } catch (Exception e1) {
                    // this means it is not double
                    e1.printStackTrace();
                }        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }





}

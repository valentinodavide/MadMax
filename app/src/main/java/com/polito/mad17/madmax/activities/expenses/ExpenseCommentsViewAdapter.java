package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.support.v4.app.FragmentManager;
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
import com.polito.mad17.madmax.entities.Comment;
import com.polito.mad17.madmax.entities.CropCircleTransformation;

import java.util.ArrayList;
import java.util.Map;

import static com.polito.mad17.madmax.R.layout.comment;

/*
    NON CANCELLARE LE PARTI COMMENTATE: servono se avessimo tempo di implementare un'idea che mi è venuta in mente per visualizzare meglio i commenti molto lunghi
 */

public class ExpenseCommentsViewAdapter extends RecyclerView.Adapter<ExpenseCommentsViewAdapter.ExpenseCommentsViewHolder> {
    private static final String TAG = ExpenseCommentsViewAdapter.class.getSimpleName();

    private final ArrayList comments;
    //private FragmentManager fragmentManager;

    // OnClick handler to help the Activity easier to interface with RecyclerView
    //final private ExpenseCommentsViewAdapter.ListItemClickListener itemClickListener;
    private Context context;

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    public ExpenseCommentsViewAdapter(Context context, ListItemClickListener listener, Map<String, Comment> commentsMap, FragmentManager fragmentManager) {
        this.context = context;
        //this.itemClickListener = listener;
        //this.fragmentManager = fragmentManager;
        this.comments = new ArrayList<>();
        comments.addAll(commentsMap.entrySet());
    }

    class ExpenseCommentsViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {
        private ImageView authorPhoto;
        private TextView authorTextView;
        private TextView commentTextView;
        private TextView timestampTextView;

        ExpenseCommentsViewHolder (View itemView) {
            super(itemView);
            authorPhoto = (ImageView) itemView.findViewById(R.id.author_photo);
            authorTextView = (TextView) itemView.findViewById(R.id.author);
            commentTextView = (TextView) itemView.findViewById(R.id.comment);
            timestampTextView = (TextView) itemView.findViewById(R.id.comment_timestamp);
            //itemView.setOnClickListener(this);
        }

        /*@Override
        public void onClick(View v) {
            DetailCommentDialogFragment detailCommentDialogFragment = new DetailCommentDialogFragment();
            detailCommentDialogFragment.show(fragmentManager, "CommentDetail");
        }*/
    }

    @Override
    public ExpenseCommentsViewAdapter.ExpenseCommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(comment, parent, false);

        ExpenseCommentsViewHolder expenseCommentsViewHolder = new ExpenseCommentsViewHolder(view);

        Log.d(TAG, "dopo aver istanziato il view holder");
        return expenseCommentsViewHolder;
    }

    @Override
    public void onBindViewHolder(final ExpenseCommentsViewHolder expenseCommentsViewHolder, int position) {
        Comment comment = getItem(position).getValue();

        // loading author profile photo
        Glide.with(context).load(comment.getAuthorPhoto())
                .centerCrop()
                .bitmapTransform(new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(expenseCommentsViewHolder.authorPhoto);

        expenseCommentsViewHolder.authorTextView.setText(comment.getAuthor());
        expenseCommentsViewHolder.commentTextView.setText(comment.getMessage());
        expenseCommentsViewHolder.timestampTextView.setText(
                context.getString(R.string.day) + " " + comment.getDate() + " " +
                context.getString(R.string.at) + " " + comment.getTime());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public Map.Entry<String, Comment> getItem(int position) {
        return (Map.Entry) comments.get(position);
    }

    public void update(Map<String, Comment> map) {
        comments.clear();
        comments.addAll(map.entrySet());
    }
}
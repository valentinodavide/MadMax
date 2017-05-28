package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.User;

import java.util.ArrayList;
import java.util.Map;

import static com.polito.mad17.madmax.R.layout.event;

public class EventsViewAdapter extends RecyclerView.Adapter<EventsViewAdapter.EventViewHolder> {

    private static final String TAG = EventsViewAdapter.class.getSimpleName();

    private final ArrayList events;

    // OnClick handler to help the Activity easier to interface with RecyclerView
    //final private ListItemClickListener itemClickListener;
    private Context context;


    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    public EventsViewAdapter(Context context, /*ListItemClickListener listener, */ Map<String, Event> eventsMap) {
        this.context = context;
        //itemClickListener = listener;
        this.events = new ArrayList<>();
        events.addAll(eventsMap.entrySet());
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView descriptionTextView;
        private TextView timestampTextView;

        EventViewHolder (View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.event_photo);
            descriptionTextView = (TextView) itemView.findViewById(R.id.event_description);
            timestampTextView = (TextView) itemView.findViewById(R.id.event_timestamp);
        }
    }

    @Override
    public EventsViewAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(event, parent, false);

        EventViewHolder eventViewHolder = new EventViewHolder(view);

        Log.d(TAG, "dopo aver istanziato il view holder");
        return eventViewHolder;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder eventViewHolder, int position) {
        final Event event = getItem(position).getValue();
        User currentUser = MainActivity.getCurrentUser();
        DatabaseReference databaseReference = MainActivity.getDatabase().getReference();

        switch(event.getEventType()) {
            case GROUP_ADD:
                eventViewHolder.imageView.setImageResource(R.drawable.group_add);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.GROUP_ADD) + " \"" + event.getObject() + "\"");
                break;
            /*case GROUP_REMOVE:
                eventViewHolder.imageView.setImageResource(R.drawable.group_remove);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.GROUP_REMOVE) + " \"" + event.getObject() + "\"");
                break;*/
            case GROUP_EDIT:
                eventViewHolder.imageView.setImageResource(R.drawable.group_edit);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.GROUP_EDIT) + " \"" + event.getObject() + "\"");
                break;
            case GROUP_MEMBER_ADD:
                eventViewHolder.imageView.setImageResource(R.drawable.member_add);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.GROUP_MEMBER_ADD) + " \"" + event.getObject() + "\"");
                break;
            case GROUP_MEMBER_REMOVE:
                eventViewHolder.imageView.setImageResource(R.drawable.member_remove);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.GROUP_MEMBER_REMOVE) + " \"" + event.getObject() + "\"");
                break;
            case EXPENSE_ADD:
                eventViewHolder.imageView.setImageResource(R.drawable.expense_add);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.EXPENSE_ADD) + " \"" + event.getObject() + "\"");
                break;
            case EXPENSE_REMOVE:
                eventViewHolder.imageView.setImageResource(R.drawable.expense_remove);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.EXPENSE_REMOVE) + " \"" + event.getObject() + "\"");
                break;
            case EXPENSE_EDIT:
                eventViewHolder.imageView.setImageResource(R.drawable.expense_edit);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.EXPENSE_EDIT) + " \"" + event.getObject() + "\"");
                break;
            case PENDING_EXPENSE_ADD:
                eventViewHolder.imageView.setImageResource(R.drawable.expense_add);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.PENDING_EXPENSE_ADD) + " \"" + event.getObject() + "\"");
                break;
            case PENDING_EXPENSE_REMOVE:
                eventViewHolder.imageView.setImageResource(R.drawable.expense_remove);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.PENDING_EXPENSE_REMOVE) + " \"" + event.getObject() + "\"");
                break;
            case PENDING_EXPENSE_EDIT:
                eventViewHolder.imageView.setImageResource(R.drawable.expense_edit);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.PENDING_EXPENSE_EDIT) + " \"" + event.getObject() + "\"");
                break;
            case PENDING_EXPENSE_VOTE_UP:
                eventViewHolder.imageView.setImageResource(R.drawable.vote_up);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.PENDING_EXPENSE_VOTE) + " \"" + event.getObject() + "\"");
                break;
            case PENDING_EXPENSE_VOTE_DOWN:
                eventViewHolder.imageView.setImageResource(R.drawable.vote_down);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.PENDING_EXPENSE_VOTE) + " \"" + event.getObject() + "\"");
                break;
            case PENDING_EXPENSE_APPROVED:
                eventViewHolder.imageView.setImageResource(R.drawable.pending_expense_approved);
                event.setDescription(context.getString(R.string.PENDING_EXPENSE) + " \"" + event.getObject() + "\" " + context.getString(R.string.PENDING_EXPENSE_APPROVED));
                break;
            case PENDING_EXPENSE_NEGLECTED:
                eventViewHolder.imageView.setImageResource(R.drawable.pending_expense_neglected);
                event.setDescription(context.getString(R.string.PENDING_EXPENSE) + " \"" + event.getObject() + "\" " + context.getString(R.string.PENDING_EXPENSE_NEGLECTED));
                break;
            /*case FRIEND_INVITE:
                eventViewHolder.imageView.setImageResource(R.drawable.member_add);
                event.setDescription(context.getString(R.string.FRIEND_INVITE) + " \"" + event.getObject() + "\"");
                break;*/
            case FRIEND_GROUP_INVITE:
                eventViewHolder.imageView.setImageResource(R.drawable.member_add);
                event.setDescription(context.getString(R.string.FRIEND_GROUP_INVITE) + " \"" + event.getObject() + "\"");
                break;
            case USER_PAY: // instead of the username the subject must be the ID of the user
                eventViewHolder.imageView.setImageResource(R.drawable.user_pay);
                if (currentUser.getID().equals(event.getSubject())) {
                    event.setDescription(context.getString(R.string.ME_PAY) + " " + event.getAmount());
                }
                else {
                    databaseReference.child("users").child(event.getSubject()).child("username")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                event.setDescription("\"" + dataSnapshot.getValue(String.class) + "\" " + context.getString(R.string.FRIEND_PAY) + " " + event.getAmount());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, databaseError.toException());
                            }
                        });
                }
                break;
            case USER_COMMENT_ADD: // instead of the username the subject must be the ID of the user
                eventViewHolder.imageView.setImageResource(R.drawable.user_comment);
                if (currentUser.getID().equals(event.getSubject())) {
                    event.setDescription(context.getString(R.string.ME_COMMENT_ADD) + " " + "\"" + event.getObject() + "\"");
                }
                else {
                    databaseReference.child("users").child(event.getSubject()).child("username")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    event.setDescription("\"" + dataSnapshot.getValue(String.class) + "\" " + context.getString(R.string.FRIEND_COMMENT_ADD) + " " + "\"" + event.getObject() + "\"");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, databaseError.toException());
                                }
                            });
                }
                break;
            default:
                eventViewHolder.imageView.setImageResource(R.drawable.default_event);
                event.setDescription("\"" + event.getSubject() + "\" " + context.getString(R.string.did) + " \"" + event.getObject() + "\"");
        }

        eventViewHolder.descriptionTextView.setText(event.getDescription());
        eventViewHolder.timestampTextView.setText(
                context.getString(R.string.day) + " " + event.getDate() + " " +
                context.getString(R.string.at) + " " + event.getTime());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public Map.Entry<String, Event> getItem(int position) {
        return (Map.Entry) events.get(position);
    }

    public void update(Map<String, Event> map) {
        events.clear();
        events.addAll(map.entrySet());
    }
}
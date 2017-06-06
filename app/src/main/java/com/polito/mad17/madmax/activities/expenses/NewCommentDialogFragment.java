package com.polito.mad17.madmax.activities.expenses;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.entities.Comment;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.SimpleDateFormat;

public class NewCommentDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NewCommentDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        //void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NewCommentDialogListener newCommentDialogListener;

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        final String groupID = arguments.getString("groupID");
        final String expenseID = arguments.getString("expenseID");
        final String expenseName = arguments.getString("expenseName");
        final Boolean isExpense = arguments.getBoolean("isExpense");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(R.string.prompt_comment)
            .setView(R.layout.new_comment_dialog)
            .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    User currentUser = MainActivity.getCurrentUser();
                    EditText message = (EditText) getDialog().findViewById(R.id.comment_message);

                    // add comment
                    Comment comment = new Comment(
                            expenseID,
                            currentUser.getName() + " " + currentUser.getSurname(),
                            currentUser.getProfileImage(),
                            message.getText().toString()
                    );
                    comment.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                    comment.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));

                    FirebaseUtils.getInstance().addComment(comment, isExpense);

                    // add event for USER_COMMENT_ADD
                    Event event = new Event(
                            groupID,
                            Event.EventType.USER_COMMENT_ADD,
                            currentUser.getID(), // instead of the username the subject must be the ID of the user
                            expenseName
                    );
                    event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                    event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                    FirebaseUtils.getInstance().addEvent(event);

                    // Send the positive button event back to the host activity
                    newCommentDialogListener.onDialogPositiveClick(NewCommentDialogFragment.this);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog

                    // Send the negative button event back to the host activity
                    //newCommentDialogListener.onDialogNegativeClick(NewCommentDialogFragment.this);
                }
            });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NewCommentDialogListener so we can send events to the host
            newCommentDialogListener = (NewCommentDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}

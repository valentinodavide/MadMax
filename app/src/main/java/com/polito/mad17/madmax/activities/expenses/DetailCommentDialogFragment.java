package com.polito.mad17.madmax.activities.expenses;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.polito.mad17.madmax.R;

/*
    ANCHE SE NON UTILIZZATO NON CANCELLARE:
    se ci fosse tempo finisco un'idea che ho avuto per visualizzare meglio commenti lunghi -->
 */

public class DetailCommentDialogFragment extends DialogFragment {
    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setTitle("Autore ha commentato:")
            .setView(inflater.inflate(R.layout.detail_comment, null))
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}

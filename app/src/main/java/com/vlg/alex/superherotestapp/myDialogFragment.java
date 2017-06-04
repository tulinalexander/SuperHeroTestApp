package com.vlg.alex.superherotestapp;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Alex on 28.02.2017.
 */

public class myDialogFragment extends DialogFragment {

    //newInstance method for call dialog from another fragment
    public static myDialogFragment newInstance(String title) {
        myDialogFragment frag = new myDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DetailFragment.DetailFragmentListener listener;
        String title = getString(R.string.fragment_dialog_title);
        String message = getString(R.string.fragment_dialog_message);;
        String button1String = getString(R.string.fragment_dialog_positive);
        String button2String = getString(R.string.fragment_dialog_cancel);

        listener = (DetailFragment.DetailFragmentListener) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);  // title
        builder.setMessage(message); // message
        builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Uri uri = Uri.parse(getArguments().getString("title"));
                getActivity().getContentResolver().delete(uri, null, null);
                Intent updateWidgetIntent = new Intent(getContext(), myWidget.class);
                updateWidgetIntent.setAction("update_widget");
                getContext().sendBroadcast(updateWidgetIntent);
                listener.onHeroDeleted();
            }
        });
        builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {


            }
        });
        builder.setCancelable(false);

        return builder.create();
    }



}
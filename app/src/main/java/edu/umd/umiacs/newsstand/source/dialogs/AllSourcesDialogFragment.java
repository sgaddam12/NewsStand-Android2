package edu.umd.umiacs.newsstand.source.dialogs;

import java.util.ArrayList;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.layers.LayersAlertDialogFragment.LayersAlertPositiveListener;
import edu.umd.umiacs.newsstand.source.Source;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class AllSourcesDialogFragment extends DialogFragment {
    private AllSourcesAlertPositiveListener alertPositiveListener;
    private AllSourcesAlertDismissListener alertDismissListener;
    private ArrayList<Source> mAllSources;
    private int position;

    /**
     * An interface implemented in the hosting activity for "OK" button click listener
     */
    public interface AllSourcesAlertPositiveListener {
        public void onAllSourcesPositiveClick(int position);
    }

    public interface AllSourcesAlertDismissListener {
        public void onAllSourcesDismiss();
    }

    /**
     * This is a callback method executed when this fragment is attached to an activity.
     * This function ensures that, the hosting activity implements the interface AlertPositiveListener
     */
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        try {
            alertPositiveListener = (AllSourcesAlertPositiveListener) activity;
            alertDismissListener = (AllSourcesAlertDismissListener) activity;
        } catch (ClassCastException e) {
            // The hosting activity does not implement the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() +
                    " must implement AllSourcesAlertPositiveListener or AllSourcesAlertDismissListener");
        }
    }

    /**
     * This is the OK button listener for the alert dialog,
     * which in turn invokes the method onPositiveClick(position)
     * of the hosting activity which is supposed to implement it
     */
    OnClickListener positiveListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            AlertDialog alert = (AlertDialog) dialog;
            int position = alert.getListView().getCheckedItemPosition();
            alertPositiveListener.onAllSourcesPositiveClick(position);
        }
    };

    /**
     * This is the Cancel button listener for the alert dialog
     */
    OnClickListener negativeListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            alertDismissListener.onAllSourcesDismiss();
        }
    };

    /**
     * This is the back button listener for the alert dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        alertDismissListener.onAllSourcesDismiss();
    }

    private int getSelectedPosition() {
        for (int i = 0; i < mAllSources.size(); i++) {
            if (mAllSources.get(i).isSelected())
                return i;
        }

        return mAllSources.size();
    }

    private String[] getSourceNames() {
        int numElements = 3;
        if (position == mAllSources.size())
            numElements++;
        String[] sourceNames = new String[numElements];
        for (int i = 0; i < mAllSources.size(); i++)
            sourceNames[i] = mAllSources.get(i).getName();
        if (position == mAllSources.size())
            sourceNames[mAllSources.size()] = "Keep Source Filters";
        return sourceNames;
    }



    /**
     * This is a callback method which will be executed
     * on creating this fragment
     */
    @SuppressWarnings("unchecked")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** Getting the arguments passed to this fragment */
        Bundle bundle = getArguments();
        mAllSources = (ArrayList<Source>) bundle.getSerializable("allSources");

        position = getSelectedPosition();

        /** Creating a builder for the alert dialog window */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /** Setting a title for the window */
        builder.setTitle("All Sources");

        /** Setting items to the alert dialog */
        builder.setSingleChoiceItems(getSourceNames(), position, null);

        /** Setting a positive button and its listener */
        builder.setPositiveButton("OK", positiveListener);

        /** Setting a positive button and its listener */
        builder.setNegativeButton("Cancel", negativeListener);

        /** Creating the alert dialog window using the builder class */
        AlertDialog dialog = builder.create();

        /** Return the alert dialog window */
        return dialog;
    }
}

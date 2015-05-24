package edu.umd.umiacs.newsstand.source.dialogs;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.SparseBooleanArray;

import edu.umd.umiacs.newsstand.source.Source;


public class LanguageSourcesDialogFragment extends DialogFragment {
    private LanguageSourcesAlertPositiveListener alertPositiveListener;
    private LanguageSourcesAlertDismissListener alertDismissListener;
    private ArrayList<Source> mLanguageSources;

    /**
     * An interface implemented in the hosting activity for "OK" button click listener
     */
    public interface LanguageSourcesAlertPositiveListener {
        public void onLanguageSourcesPositiveClick(SparseBooleanArray selectedPositions);
    }

    /**
     * An interface implemented in the hosting activity for "Cancel" button and back click listener
     */
    public interface LanguageSourcesAlertDismissListener {
        public void onLanguageSourcesDismiss();
    }

    /**
     * This is a callback method executed when this fragment is attached to an activity.
     * This function ensures that, the hosting activity implements the interface AlertPositiveListener
     */
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        try {
            alertPositiveListener = (LanguageSourcesAlertPositiveListener) activity;
            alertDismissListener = (LanguageSourcesAlertDismissListener) activity;
        } catch (ClassCastException e) {
            // The hosting activity does not implement the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() + " must implement LanguageSourcesAlertPositiveListener");
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
            SparseBooleanArray sparseSelectedPositions = alert.getListView().getCheckedItemPositions();
            alertPositiveListener.onLanguageSourcesPositiveClick(sparseSelectedPositions);
        }
    };

    /**
     * This is the Cancel button listener for the alert dialog
     */
    OnClickListener negativeListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            alertDismissListener.onLanguageSourcesDismiss();
        }
    };

    /**
     * This is the back button listener for the alert dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        alertDismissListener.onLanguageSourcesDismiss();
    }

    private boolean[] getSelectedIndexes() {
        ArrayList<Boolean> selectedIndexesList = new ArrayList<Boolean>();
        for (int i = 0; i < mLanguageSources.size(); i++) {
            if (mLanguageSources.get(i).isSelected())
                selectedIndexesList.add(true);
            else
                selectedIndexesList.add(false);
        }

        boolean[] selectedIndexes = new boolean[selectedIndexesList.size()];
        for (int i = 0; i < selectedIndexesList.size(); i++) {
            selectedIndexes[i] = selectedIndexesList.get(i);
        }

        return selectedIndexes;
    }

    private String[] getSourceNames() {
        String[] sourceNames = new String[mLanguageSources.size()];
        for (int i = 0; i < mLanguageSources.size(); i++)
            sourceNames[i] = "\u200e" + mLanguageSources.get(i).getName() +
                    "\n\u200e" + mLanguageSources.get(i).getNumDocs() + "\u200e articles\u200e";

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
        mLanguageSources = (ArrayList<Source>) bundle.getSerializable("languageSources");

        /** Creating a builder for the alert dialog window */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMultiChoiceItems(getSourceNames(), getSelectedIndexes(), null);
        // builder.setSingleChoiceItems(getSourceNames(), 1, null);
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

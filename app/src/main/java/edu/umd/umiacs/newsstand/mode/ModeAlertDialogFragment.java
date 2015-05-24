/* Code modified from http://wptrafficanalyzer.in/blog/alert-dialog-window-with-radio-buttons-in-android/ */

package edu.umd.umiacs.newsstand.mode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import edu.umd.umiacs.newsstand.MainActivity;

public class ModeAlertDialogFragment extends DialogFragment {
    private ModeAlertPositiveListener alertPositiveListener;

    /**
     * An interface implemented in the hosting activity for "OK" button click listener
     */
    public interface ModeAlertPositiveListener {
        public void onModePositiveClick(int position);
    }

    /**
     * This is a callback method executed when this fragment is attached to an activity.
     * This function ensures that, the hosting activity implements the interface AlertPositiveListener
     */
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        try {
            alertPositiveListener = (ModeAlertPositiveListener) activity;
        } catch (ClassCastException e) {
            // The hosting activity does not implement the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() + " must implement AlertPositiveListener");
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
            alertPositiveListener.onModePositiveClick(position);
        }
    };

    /**
     * This is a callback method which will be executed
     * on creating this fragment
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** Getting the arguments passed to this fragment */
        Bundle bundle = getArguments();
        int position = bundle.getInt("position");

        /** Creating a builder for the alert dialog window */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /** Setting a title for the window */
        builder.setTitle("Select Mode");

        /** Setting items to the alert dialog */
        builder.setSingleChoiceItems(MainActivity.modes, position, null);

        /** Setting a positive button and its listener */
        builder.setPositiveButton("OK", positiveListener);

        /** Setting a positive button and its listener */
        builder.setNegativeButton("Cancel", null);

        /** Creating the alert dialog window using the builder class */
        AlertDialog dialog = builder.create();

        /** Return the alert dialog window */
        return dialog;
    }
}

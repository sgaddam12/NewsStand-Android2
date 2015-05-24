package edu.umd.umiacs.newsstand.source.dialogs;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.source.Source;
import edu.umd.umiacs.newsstand.source.SourcesActivity;

public class FeedSourcesDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "edu.umd.umiacs.newsstand.source.dialogs.FeedSourcesDialogFragment";

    private FeedSourcesAlertPositiveListener alertPositiveListener;
    private FeedSourcesAlertDismissListener alertDismissListener;
    private ArrayList<Source> mFeedSources;

    private ListView mListView;
    private FeedSourcesListAdapter mListAdapter;

    /**
     * An interface implemented in the hosting activity for "OK" button click listener
     */
    public interface FeedSourcesAlertPositiveListener {
        public void onFeedSourcesPositiveClick(ArrayList<Source> updatedSources);
    }

    public interface FeedSourcesAlertDismissListener {
        public void onFeedSourcesDismiss();
    }

    /**
     * This is a callback method executed when this fragment is attached to an activity.
     * This function ensures that, the hosting activity implements the interface AlertPositiveListener
     */
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        try {
            alertPositiveListener = (FeedSourcesAlertPositiveListener) activity;
            alertDismissListener = (FeedSourcesAlertDismissListener) activity;
        } catch (ClassCastException e) {
            // The hosting activity does not implement the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() + " must implement FeedSourcesAlertPositiveListener");
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
            ArrayList<Source> updatedSources = mListAdapter.getSources();
            alertPositiveListener.onFeedSourcesPositiveClick(updatedSources);
        }
    };

    /**
     * This is the Cancel button listener for the alert dialog
     */
    OnClickListener negativeListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            alertDismissListener.onFeedSourcesDismiss();
        }
    };

    /**
     * This is the back button listener for the alert dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        alertDismissListener.onFeedSourcesDismiss();
    }

    private boolean[] getSelectedIndexes() {
        ArrayList<Boolean> selectedIndexesList = new ArrayList<Boolean>();
        for (int i = 0; i < mFeedSources.size(); i++) {
            if (mFeedSources.get(i).isSelected())
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
        String[] sourceNames = new String[mFeedSources.size()];
        int i = 0;
        for (Source currentSource : mFeedSources) {
            sourceNames[i] = "\u200e" + currentSource.getName() + "\u200e (" + currentSource.getCountryName() + ")" +
                    "\n\u200e" + mFeedSources.get(i).getNumDocs() + "\u200e articles\u200e\u200e " +
                    "(" + currentSource.getLangCode() + ")";
            i++;
        }
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
        mFeedSources = (ArrayList<Source>) bundle.getSerializable("feedSources");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_location, null);

        /** Creating a builder for the alert dialog window */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Feeds");
        builder.setView(view);

        mListView = (ListView) view.findViewById(R.id.locationListView);
        mListAdapter = new FeedSourcesListAdapter(getActivity(), mFeedSources);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(this);

        //builder.setMultiChoiceItems(getSourceNames(), getSelectedIndexes(), null);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long duration) {
        Log.i(TAG, "On item click");
        mListAdapter.setSelectedPosition(position, true);
        final int finalPosition = position;


        Timer timer = new Timer();

        try {
            //final SourcesActivity sourcesActivity = (SourcesActivity) getActivity();
        timer.schedule(new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mListAdapter.setSelectedPosition(finalPosition, false);
                    }
                });
            }
        }, 150);
        } catch (Exception e) {
            e.printStackTrace();
            mListAdapter.setSelectedPosition(finalPosition, false);
        }
    }
}



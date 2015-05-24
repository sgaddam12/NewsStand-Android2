package edu.umd.umiacs.newsstand.filters;

import edu.umd.umiacs.newsstand.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerDialogFragment extends DialogFragment {
    private final static String TAG = "Number Picker Dialog Fragment";

    private Context context;
    private NumberPicker mNumberPicker;

    private int filterMode;
    private int initialValue;

    private NumberPickerPositiveListener alertPositiveListener;

    /**
     * An interface implemented in the hosting activity for "OK" button click listener
     */
    public interface NumberPickerPositiveListener {
        public void onNumberPickerPositiveClick(int type, int value);
    }

    /**
     * This is a callback method executed when this fragment is attached to an activity.
     * This function ensures that, the hosting activity implements the interface AlertPositiveListener
     */
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        try {
            alertPositiveListener = (NumberPickerPositiveListener) activity;
        } catch (ClassCastException e) {
            // The hosting activity does not implement the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() + " must implement AlertPositiveListener");
        }
    }

    OnClickListener positiveListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String[] displayedValues = mNumberPicker.getDisplayedValues();
            int value = Integer.parseInt(displayedValues[mNumberPicker.getValue()]);
            alertPositiveListener.onNumberPickerPositiveClick(filterMode, value);
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "On Create Dialog");

        // Filter Images 0, Filter Video 1
        Bundle bundle = getArguments();
        filterMode = bundle.getInt("mode");
        initialValue = bundle.getInt("initial");

        context = getActivity().getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_number_picker, null);
        builder.setView(view);

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        setupNumberPicker();

        if (filterMode == 0)
            builder.setTitle("Minimum Images");
        else
            builder.setTitle("Minimum Videos");

        builder.setPositiveButton("OK", positiveListener);
        builder.setNegativeButton("Cancel", null);

        return builder.create();
    }

    private void setupNumberPicker() {
        String[] nums = new String[21];

        if (filterMode == 0) {
            for (int i = 0; i < nums.length; i++)
                nums[i] = Integer.toString(i * 5);
            initialValue = initialValue / 5;
        } else
            for (int i = 0; i < nums.length; i++)
                nums[i] = Integer.toString(i);


        mNumberPicker.setMaxValue(nums.length - 1);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setValue(initialValue);
        mNumberPicker.setDisplayedValues(nums);
        mNumberPicker.setWrapSelectorWheel(false);
        mNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }


}

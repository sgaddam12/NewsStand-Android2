package edu.umd.umiacs.newsstand.filters;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.layers.LayersAlertDialogFragment;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;

public class FiltersPreferenceFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        OnPreferenceClickListener {

    private final static String TAG = "Filters Preference Fragment";

    private CheckBoxPreference mAllTopicsCheckBox;
    private CheckBoxPreference mGeneralCheckBox;
    private CheckBoxPreference mBusinessCheckBox;
    private CheckBoxPreference mEntertainmentCheckBox;
    private CheckBoxPreference mHealthCheckBox;
    private CheckBoxPreference mSciTechCheckBox;
    private CheckBoxPreference mSportsCheckBox;

    private Preference mNumImagesPreference;
    private Preference mNumVideosPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.filters);
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        setCheckBoxes();

        mNumImagesPreference = (Preference) findPreference(FiltersActivity.NUM_IMAGES);
        mNumImagesPreference.setOnPreferenceClickListener(this);

        mNumVideosPreference = (Preference) findPreference(FiltersActivity.NUM_VIDEOS);
        mNumVideosPreference.setOnPreferenceClickListener(this);

        setPreferenceTitles(getActivity().getSharedPreferences(MainActivity.GENERAL_PREFS, 0));
    }

    private void setCheckBoxes() {
        mAllTopicsCheckBox = (CheckBoxPreference) findPreference(FiltersActivity.ALL_TOPICS);
        mGeneralCheckBox = (CheckBoxPreference) findPreference(FiltersActivity.TOPIC_GENERAL);
        mBusinessCheckBox = (CheckBoxPreference) findPreference(FiltersActivity.TOPIC_BUSINESS);
        mEntertainmentCheckBox = (CheckBoxPreference) findPreference(FiltersActivity.TOPIC_ENTERTAINMENT);
        mHealthCheckBox = (CheckBoxPreference) findPreference(FiltersActivity.TOPIC_HEALTH);
        mSciTechCheckBox = (CheckBoxPreference) findPreference(FiltersActivity.TOPIC_SCITECH);
        mSportsCheckBox = (CheckBoxPreference) findPreference(FiltersActivity.TOPIC_SPORTS);
    }

    private void setTopicsCheckedState(SharedPreferences sharedPreferences, boolean checked) {
        mGeneralCheckBox.setChecked(checked);
        mBusinessCheckBox.setChecked(checked);
        mEntertainmentCheckBox.setChecked(checked);
        mHealthCheckBox.setChecked(checked);
        mSciTechCheckBox.setChecked(checked);
        mSportsCheckBox.setChecked(checked);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        if (getActivity() != null) {
            SharedPreferences generalPreferences = getActivity()
                    .getSharedPreferences(MainActivity.GENERAL_PREFS, 0);
            SharedPreferences.Editor editor = generalPreferences.edit();

            if (key.equals(FiltersActivity.ALL_TOPICS)) {
                setTopicsCheckedState(sharedPreferences, false);
                editor.putBoolean(FiltersActivity.ALL_TOPICS, mAllTopicsCheckBox.isChecked());
                editor.commit();
            } else {
                if (key.equals(FiltersActivity.TOPIC_GENERAL)) {
                    editor.putBoolean(FiltersActivity.TOPIC_GENERAL, mGeneralCheckBox.isChecked());
                } else if (key.equals(FiltersActivity.TOPIC_BUSINESS)) {
                    editor.putBoolean(FiltersActivity.TOPIC_BUSINESS, mBusinessCheckBox.isChecked());
                } else if (key.equals(FiltersActivity.TOPIC_ENTERTAINMENT)) {
                    editor.putBoolean(FiltersActivity.TOPIC_ENTERTAINMENT, mEntertainmentCheckBox.isChecked());
                } else if (key.equals(FiltersActivity.TOPIC_HEALTH)) {
                    editor.putBoolean(FiltersActivity.TOPIC_HEALTH, mHealthCheckBox.isChecked());
                } else if (key.equals(FiltersActivity.TOPIC_SCITECH)) {
                    editor.putBoolean(FiltersActivity.TOPIC_SCITECH, mSciTechCheckBox.isChecked());
                } else if (key.equals(FiltersActivity.TOPIC_SPORTS)) {
                    editor.putBoolean(FiltersActivity.TOPIC_SPORTS, mSportsCheckBox.isChecked());
                }
                editor.commit();
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String preferenceKey = preference.getKey();

        FragmentManager manager = getFragmentManager();
        NumberPickerDialogFragment numberPickerAlert = new NumberPickerDialogFragment();

        Bundle bundle = new Bundle();
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(MainActivity.GENERAL_PREFS, 0);

        if (preferenceKey.equals(FiltersActivity.NUM_IMAGES)) {
            bundle.putInt("mode", 0);
            bundle.putInt("initial",
                    sharedPreferences.getInt(MainActivity.FILTER_NUM_IMAGES, 0));
        } else {
            bundle.putInt("mode", 1);
            bundle.putInt("initial",
                    sharedPreferences.getInt(MainActivity.FILTER_NUM_VIDEOS, 0));
        }

        numberPickerAlert.setArguments(bundle);
        numberPickerAlert.show(manager, "number_picker_alert_fragment");

        return false;
    }

    private void setPreferenceTitles(SharedPreferences sharedPreferences) {
        int num_images = sharedPreferences.getInt(
                MainActivity.FILTER_NUM_IMAGES, 0);
        int num_videos = sharedPreferences.getInt(
                MainActivity.FILTER_NUM_VIDEOS, 0);

        if (num_images > 0)
            mNumImagesPreference.setTitle("At least " + num_images + " images");
        else
            mNumImagesPreference.setTitle("No filter");

        if (num_videos > 0)
            mNumVideosPreference.setTitle("At least " + num_videos + " videos");
        else
            mNumVideosPreference.setTitle("No filter");
    }

    public void onNumberPickerUpdate(int mode, int value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                MainActivity.GENERAL_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (mode == 0) {
            editor.putInt(MainActivity.FILTER_NUM_IMAGES, value);
        } else {
            editor.putInt(MainActivity.FILTER_NUM_VIDEOS, value);
        }
        editor.commit();

        setPreferenceTitles(sharedPreferences);
    }
}

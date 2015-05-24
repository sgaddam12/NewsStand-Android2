package edu.umd.umiacs.newsstand.settings;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsPreferenceFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "edu.umd.umiacs.newsstand.settings.SettingsPreferenceFragment";

    private static final String CURRENT_MAP_HOME_KEY = "pref_setCurrentMapHome";
    private static final String SIDE_SWIPE_KEY = "pref_sideSwipe";
    private static final String ONE_HAND_MODE_KEY = "pref_oneHandMode";

    private double mMapLatBot;
    private double mMapLatTop;
    private double mMapLonLeft;
    private double mMapLonRight;

    private int oneHandMode;

    private boolean mTopStories;

    private boolean setCurrentMapToHome;
    private CheckBoxPreference setCurrentMapToHomeCheckBoxPreference;

    public SettingsPreferenceFragment() {
        Log.i(TAG, "SettingsPrefFragment");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        mMapLatBot = getArguments().getDouble("mapLatBot");
        mMapLatTop = getArguments().getDouble("mapLatTop");
        mMapLonLeft = getArguments().getDouble("mapLonLeft");
        mMapLonRight = getArguments().getDouble("mapLonRight");

        mTopStories = getArguments().getBoolean("topStories");

        setCurrentMapToHomeCheckBoxPreference = (CheckBoxPreference) findPreference(CURRENT_MAP_HOME_KEY);

        setPreferenceDefaults();
    }

    private void setPreferenceDefaults() {
        SharedPreferences sharedPreferences = getPreferenceManager()
                .getSharedPreferences();

        if (mTopStories) {
            setCurrentMapToHomeCheckBoxPreference.setEnabled(false);
            setCurrentMapToHomeCheckBoxPreference.setSelectable(false);
        }

        ListPreference oneHandModeListPreference = (ListPreference) findPreference(ONE_HAND_MODE_KEY);
        oneHandMode = Integer.parseInt(sharedPreferences.getString(
                ONE_HAND_MODE_KEY, "0"));
        final String[] oneHandOptionsArray = getResources().getStringArray(
                R.array.oneHandedModeOptionsArray);
        oneHandModeListPreference.setTitle(oneHandOptionsArray[oneHandMode]);

        /* BCF 8/13/13 - removed for now
        ListPreference sideSwipeListPreference = (ListPreference) findPreference(SIDE_SWIPE_KEY);
        int sideSwipeValue = Integer.parseInt(sharedPreferences.getString(
                SIDE_SWIPE_KEY, "1"));
        final String[] sideSwipeOptionsArray = getResources().getStringArray(
                R.array.sideSwipeOptionsArray);
        sideSwipeListPreference.setTitle(sideSwipeOptionsArray[sideSwipeValue]);*/
    }

    // ================================================================================
    // OnSharedPreferenceChange Listener
    // ================================================================================
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(CURRENT_MAP_HOME_KEY)) {
            setCurrentMapToHome = sharedPreferences.getBoolean(key, false);
        } else {
            String newValue = sharedPreferences.getString(key, "");
            int intNewValue = Integer.parseInt(newValue);
            Log.i(TAG, "onSharedPreferenceChanged: " + key + " value: "
                    + newValue);

            ListPreference listPreference = (ListPreference) findPreference(key);

            if (key.equals(ONE_HAND_MODE_KEY)) {
                final String[] optionsArray = getResources().getStringArray(
                        R.array.oneHandedModeOptionsArray);
                Log.i(TAG, optionsArray[intNewValue]);
                listPreference.setTitle(optionsArray[intNewValue]);
                SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.GENERAL_PREFS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(MainActivity.ONE_HAND_MODE_KEY, intNewValue);
                editor.commit();
                Log.i(TAG, "put " + intNewValue);
            }
        }

    }

    // ================================================================================
    // Application State
    // ================================================================================
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onDestroy() {
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.GENERAL_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (setCurrentMapToHomeCheckBoxPreference.isChecked()) {
            editor.putFloat(MainActivity.HOME_LAT_BOT, (float) mMapLatBot);
            editor.putFloat(MainActivity.HOME_LAT_TOP, (float) mMapLatTop);
            editor.putFloat(MainActivity.HOME_LON_LEFT, (float) mMapLonLeft);
            editor.putFloat(MainActivity.HOME_LON_RIGHT, (float) mMapLonRight);
        }
        editor.commit();

        setCurrentMapToHomeCheckBoxPreference.setChecked(false);
        super.onDestroy();
    }

}

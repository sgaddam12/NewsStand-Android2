package edu.umd.umiacs.newsstand.source;

import java.util.ArrayList;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SourcesPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private static String ALL_SOURCES = "pref_allSources";
    private static String FEED_SOURCES = "pref_feedSources";

    private ArrayList<Source> mFeedSources;

    private ListPreference mAllSourcesListPreference;
    private Preference mFeedSourcesPreference;

    public SourcesPreferenceFragment() {
    }

    public SourcesPreferenceFragment(ArrayList<Source> feedSources) {
        mFeedSources = feedSources;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.sources);
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        setSourcesDefault();
    }

    private void setSourcesDefault() {
        SharedPreferences sharedPreferences = getPreferenceManager()
                .getSharedPreferences();

        mAllSourcesListPreference = (ListPreference) findPreference(ALL_SOURCES);
        int allSourcesValue = Integer.parseInt(sharedPreferences.getString(
                ALL_SOURCES, "0"));
        final String[] oneHandOptionsArray = getResources().getStringArray(
                R.array.allSourcesOptionsArray);
        mAllSourcesListPreference.setTitle(oneHandOptionsArray[allSourcesValue]);

        mFeedSourcesPreference = (Preference) findPreference(FEED_SOURCES);

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {

    }
}
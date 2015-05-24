package edu.umd.umiacs.newsstand.filters;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.filters.FiltersPreferenceFragment;
import edu.umd.umiacs.newsstand.filters.NumberPickerDialogFragment.NumberPickerPositiveListener;

public class FiltersActivity extends Activity implements NumberPickerPositiveListener, View.OnClickListener {
    private final static String TAG = "Filters Activity";

    public final static String ALL_TOPICS = "filter_all_topics";
    public final static String TOPIC_GENERAL = "filter_topic_general";
    public final static String TOPIC_BUSINESS = "filter_topic_business";
    public final static String TOPIC_ENTERTAINMENT = "filter_topic_entertainment";
    public final static String TOPIC_HEALTH = "filter_topic_health";
    public final static String TOPIC_SCITECH = "filter_topic_scitech";
    public final static String TOPIC_SPORTS = "filter_topic_sports";

    public final static String NUM_IMAGES = "filter_num_images";
    public final static String NUM_VIDEOS = "filter_num_videos";

    private ActionBar mActionBar;
    private Button mBackButton;
    private Button mTitleButton;

    private String mTitle;

    private FiltersPreferenceFragment filtersPreferenceFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getIntent().getStringExtra(MainActivity.TITLE);
        setupActionBar();

        filtersPreferenceFragment = new FiltersPreferenceFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, filtersPreferenceFragment)
                .commit();
    }

    private void setupActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.action_general);

            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);

            mBackButton = (Button) findViewById(R.id.generalBackButton);
            mBackButton.setText(mTitle);
            mBackButton.setOnClickListener(this);

            mTitleButton = (Button) findViewById(R.id.generalTitleButton);
            mTitleButton.setText("Filters");
        }
    }

    // ================================================================================
    // NumberPickerDialogFragment.NumberPickerPositiveListener
    // ================================================================================
    @Override
    public void onNumberPickerPositiveClick(int mode, int value) {
        filtersPreferenceFragment.onNumberPickerUpdate(mode, value);

    }


    // ================================================================================
    // On Click Listener
    // ================================================================================
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.generalBackButton:
                finish();
        }
    }
}

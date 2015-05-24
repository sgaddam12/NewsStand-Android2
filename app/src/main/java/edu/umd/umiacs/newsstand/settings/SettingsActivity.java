package edu.umd.umiacs.newsstand.settings;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "Settings Activity";

    private ActionBar mActionBar;
    private Button mBackButton;
    private Button mTitleButton;

    private String mTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        mTitle = intent.getStringExtra(MainActivity.TITLE);

        double mapLatBot = intent.getDoubleExtra(MainActivity.MAP_LAT_BOT, 0.0);
        double mapLatTop = intent.getDoubleExtra(MainActivity.MAP_LAT_TOP, 0.0);
        double mapLonLeft = intent.getDoubleExtra(MainActivity.MAP_LON_LEFT, 0.0);
        double mapLonRight = intent.getDoubleExtra(MainActivity.MAP_LON_RIGHT, 0.0);

        boolean isTopStories = intent.getBooleanExtra("topStories", false);

        setupActionBar();

        Bundle bundle = new Bundle();
        bundle.putDouble("mapLatBot", mapLatBot);
        bundle.putDouble("mapLatTop", mapLatTop);
        bundle.putDouble("mapLonLeft", mapLonLeft);
        bundle.putDouble("mapLonRight", mapLonRight);
        bundle.putBoolean("topStories", isTopStories);
        Log.i(TAG, "after bundle");
        SettingsPreferenceFragment settingsPreferenceFragment =
                new SettingsPreferenceFragment();
        settingsPreferenceFragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsPreferenceFragment)
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
            mTitleButton.setText("Settings");
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.generalBackButton:
                finish();
        }
    }
}

package edu.umd.umiacs.newsstand.source;

import java.util.ArrayList;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.NewsStandApplication;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.source.Source.SourceType;
import edu.umd.umiacs.newsstand.source.dialogs.AllSourcesDialogFragment;
import edu.umd.umiacs.newsstand.source.dialogs.AllSourcesDialogFragment.AllSourcesAlertPositiveListener;
import edu.umd.umiacs.newsstand.source.dialogs.FeedSourcesDialogFragment;
import edu.umd.umiacs.newsstand.source.dialogs.FeedSourcesDialogFragment.FeedSourcesAlertPositiveListener;
import edu.umd.umiacs.newsstand.source.dialogs.LanguageSourcesDialogFragment;
import edu.umd.umiacs.newsstand.source.dialogs.LanguageSourcesDialogFragment.LanguageSourcesAlertPositiveListener;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import android.widget.AdapterView.OnItemClickListener;

public class SourcesActivity extends Activity implements OnClickListener, OnItemClickListener, AllSourcesAlertPositiveListener,
        AllSourcesDialogFragment.AllSourcesAlertDismissListener,
        LanguageSourcesDialogFragment.LanguageSourcesAlertDismissListener,
        LanguageSourcesAlertPositiveListener, FeedSourcesAlertPositiveListener,
        FeedSourcesDialogFragment.FeedSourcesAlertDismissListener {
    private static String TAG = "SourcesActivity";

    public final static String ITEM_TITLE = "title";
    public final static String ITEM_CAPTION = "caption";

    private ListView mSourcesListView;

    private ActionBar mActionBar;
    private Button mBackButton;
    private Button mTitleButton;

    private String mTitle;

    private ArrayList<Source> mAllSources;
    private ArrayList<Source> mLanguageSources;
    private ArrayList<Source> mCountrySources;
    private ArrayList<Source> mFeedSources;

    private String mAllSourcesSelectedText;
    private int mNumLanguageSelected;
    private int mNumCountrySelected;
    private int mNumFeedSelected;

    private SourcesListAdapter mSourcesListAdapter;

    // SectionHeaders
    private final static String[] sectionHeaderTitles = new String[]{"ALL SOURCES", "LANGUAGE SOURCES", "COUNTRY SOURCES",
            "FEED SOURCES"};

    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sources);

        Intent intent = getIntent();

        mTitle = intent.getStringExtra(MainActivity.TITLE);

        mAllSources = (ArrayList<Source>) intent.getSerializableExtra("allSources");
        mLanguageSources = (ArrayList<Source>) intent.getSerializableExtra("languageSources");
        mCountrySources = (ArrayList<Source>) intent.getSerializableExtra("countrySources");
        mFeedSources = (ArrayList<Source>) intent.getSerializableExtra("feedSources");

        setupActionBar();

        mAllSourcesSelectedText = getAllSourcesText();
        setupListFromSources(false);
    }

    private void setupListFromSources(boolean useGlobalVariables) {
        if (!useGlobalVariables) {
            NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
            mAllSources = applicationState.getAllSources();
            mLanguageSources = applicationState.getLanguageSources();
            mFeedSources = applicationState.getFeedSources();
        }

        mSourcesListAdapter = new SourcesListAdapter(this, mAllSourcesSelectedText,
                getSelectedSources(mLanguageSources), getSelectedSources(mCountrySources), getSelectedSources(mFeedSources));

        ArrayList<String> allSourceNames = new ArrayList<String>();
        for (int i = 0; i < mAllSources.size(); i++) {
            if (mAllSources.get(i).isSelected()) {
                allSourceNames.add(mAllSources.get(i).getName());
                break;
            }

            if (i == mAllSources.size() - 1) {
                allSourceNames.add("Filters Added");
            }
        }

        ArrayAdapter<String> allSourcesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allSourceNames);
        mSourcesListAdapter.addSection(sectionHeaderTitles[0], allSourcesListAdapter);

        ArrayList<String> languageSourceNames = new ArrayList<String>();
        languageSourceNames.add("Add Language Filter");
        languageSourceNames.addAll(getSelectedSourceNames(mLanguageSources));

        mNumLanguageSelected = languageSourceNames.size() - 1;
        ArrayAdapter<String> languageSourcesListAdapter = new ArrayAdapter<String>(this, R.layout.item_source, R.id.langSourcesRowTitle,
                languageSourceNames);
        mSourcesListAdapter.addSection(sectionHeaderTitles[1], languageSourcesListAdapter);

        mNumCountrySelected = 0;

        ArrayList<String> feedSourceNames = new ArrayList<String>();
        feedSourceNames.add("Add Feed Filter");
        feedSourceNames.addAll(getSelectedSourceNames(mFeedSources));

        mNumFeedSelected = feedSourceNames.size() - 1;
        ArrayAdapter<String> feedSourcesListAdapter = new ArrayAdapter<String>(this, R.layout.item_source, R.id.sourcesRowTitle,
                feedSourceNames);
        mSourcesListAdapter.addSection(sectionHeaderTitles[3], feedSourcesListAdapter);

        mSourcesListView = (ListView) findViewById(R.id.sourcesListView);
        mSourcesListView.setAdapter(mSourcesListAdapter);
        mSourcesListView.setOnItemClickListener(this);
    }


    //================================================================================
    // ActionBar Calls
    //================================================================================

    private void setupActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.action_sources);

            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);

            mBackButton = (Button) findViewById(R.id.sourcesBackButton);
            mBackButton.setText(mTitle);
            mBackButton.setOnClickListener(this);

            mTitleButton = (Button) findViewById(R.id.sourcesTitleButton);
            mTitleButton.setText("News Sources");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //================================================================================
    // Get Sources
    //================================================================================

    private ArrayList<String> getSelectedSourceNames(ArrayList<Source> sourceArray) {
        ArrayList<String> selectedSourceNames = new ArrayList<String>();
        if (sourceArray != null)
            for (Source currentSource : sourceArray)
                if (currentSource.isSelected())
                    selectedSourceNames.add(currentSource.getName());

        return selectedSourceNames;
    }

    private ArrayList<Source> getSelectedSources(ArrayList<Source> sourceArray) {
        ArrayList<Source> selectedSources = new ArrayList<Source>();
        if (sourceArray != null)
            for (Source currentSource : sourceArray)
                if (currentSource.isSelected())
                    selectedSources.add(currentSource);
        return selectedSources;
    }

    private String getAllSourcesText() {
        String allSourcesText = "";

        for (Source currentSource : mAllSources) {
            if (currentSource.isSelected())
                allSourcesText = currentSource.getName();
        }

        if (allSourcesText.equalsIgnoreCase("")) {
            allSourcesText = "Filtering Sources";
        }

        return allSourcesText;
    }

    private void unselectAllSources(SourceType sourceType) {
        ArrayList<Source> sourcesList = new ArrayList<Source>();

        switch (sourceType) {
            case ALL_SOURCE:
                sourcesList = mAllSources;
                break;
            case LANGUAGE_SOURCE:
                sourcesList = mLanguageSources;
                break;
            case COUNTRY_SOURCE:
                sourcesList = mCountrySources;
                break;
            case FEED_SOURCE:
                sourcesList = mFeedSources;
                break;
            default:
                break;
        }

        for (Source currentSource : sourcesList) {
            currentSource.setSelected(false);
        }
    }

    //Called whenever at least one language source is selected
    private void languageSourcesSelected() {
        // First update all sources since we are now filtering by at least one language
        unselectAllSources(SourceType.ALL_SOURCE);
        mAllSourcesSelectedText = "Filtering Sources";

        // Make sure only feed sources selected match filters
        ArrayList<String> filteredLanguages = new ArrayList<String>();
        for (Source currentSource : mLanguageSources)
            if (currentSource.isSelected())
                filteredLanguages.add(currentSource.getLangCode());

        for (int i = 0; i < mFeedSources.size(); i++)
            if (!filteredLanguages.contains(mFeedSources.get(i).getLangCode())) {
                mFeedSources.get(i).setSelected(false);
            }
    }

    private void feedSourcesSelected() {
        // First update all sources since we are now filtering by at least one language
        unselectAllSources(SourceType.ALL_SOURCE);
        mAllSourcesSelectedText = "Filtering Sources";
    }

    private ArrayList<Source> getAvailableFeedSources() {
        ArrayList<Source> feedSources = new ArrayList<Source>();

        if (mNumLanguageSelected == 0) {
            return mFeedSources;
        } else {
            ArrayList<String> filteredLanguages = new ArrayList<String>();
            for (Source currentSource : mLanguageSources)
                if (currentSource.isSelected())
                    filteredLanguages.add(currentSource.getLangCode());

            for (Source currentSource : mFeedSources)
                if (filteredLanguages.contains(currentSource.getLangCode())) {
                    feedSources.add(currentSource);
                }
        }
        return feedSources;
    }

    //================================================================================
    // On Click Listener
    //================================================================================

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sourcesBackButton:
                finish();
                return;
        }

        int tag = (Integer) view.getTag();
        int cancelSection = tag / 1000;
        int cancelRow = tag % 1000;
        ArrayList<Source> sourceList = null;
        SourceType sourceType = SourceType.ALL_SOURCE;

        switch (cancelSection) {
            case 0:
                sourceList = mLanguageSources;
                sourceType = SourceType.LANGUAGE_SOURCE;
                break;
            case 1:
                sourceList = mCountrySources;
                sourceType = SourceType.COUNTRY_SOURCE;
                break;
            case 2:
                sourceList = mFeedSources;
                sourceType = SourceType.FEED_SOURCE;
                break;
        }

        int selectedSeen = 0;
        for (Source currentSource : sourceList) {
            if (currentSource.isSelected()) {
                if (cancelRow == selectedSeen) {
                    currentSource.setSelected(false);
                }
                selectedSeen++;
            }
        }

        switch (sourceType) {
            case LANGUAGE_SOURCE:
                mNumLanguageSelected--;
                if (mNumLanguageSelected > 0)
                    languageSourcesSelected();
                break;
            case COUNTRY_SOURCE:
                mNumCountrySelected--;
                break;
            case FEED_SOURCE:
                mNumFeedSelected--;
                break;
            default:
                break;
        }

        if (mNumLanguageSelected + mNumCountrySelected + mNumFeedSelected == 0)
            setAllSourcesMostRecent();

        setupListFromSources(false);
    }

    //================================================================================
    // OnItem Click Listener
    //================================================================================

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
        FragmentManager manager = getFragmentManager();
        Bundle bundle = new Bundle();
        if (position == 1) { // All Sources Selection
            mSourcesListAdapter.setSelectedPosition(SourceType.ALL_SOURCE);
            bundle.putSerializable("allSources", mAllSources);
            AllSourcesDialogFragment dialogAlert = new AllSourcesDialogFragment();
            dialogAlert.setArguments(bundle);
            dialogAlert.show(manager, "all_sources_alert_fragment");
        } else if (position == 3) { // Language Sources Selection
            mSourcesListAdapter.setSelectedPosition(SourceType.LANGUAGE_SOURCE);
            bundle.putSerializable("languageSources", mLanguageSources);
            LanguageSourcesDialogFragment dialogAlert = new LanguageSourcesDialogFragment();
            dialogAlert.setArguments(bundle);
            dialogAlert.show(manager, "language_sources_alert_fragment");
        } else if (position == 3 + mNumLanguageSelected + 2) {
            mSourcesListAdapter.setSelectedPosition(SourceType.FEED_SOURCE);
            ArrayList<Source> feedSources = getAvailableFeedSources();
            bundle.putSerializable("feedSources", feedSources);
            FeedSourcesDialogFragment dialogAlert = new FeedSourcesDialogFragment();
            dialogAlert.setArguments(bundle);
            dialogAlert.show(manager, "feed_sources_alert_fragment");
        }
    }

    //================================================================================
    // All Sources Dialog Listeners
    //================================================================================

    @Override
    public void onAllSourcesPositiveClick(int position) {
        mSourcesListAdapter.setSelectedPosition(null);
        for (int i = 0; i < mAllSources.size(); i++) {
            if (i == position)
                mAllSources.get(i).setSelected(true);
            else
                mAllSources.get(i).setSelected(false);
        }
        mAllSourcesSelectedText = getAllSourcesText();
        setupListFromSources(false);
    }

    @Override
    public void onAllSourcesDismiss() {
        mSourcesListAdapter.setSelectedPosition(null);
    }

    private void setAllSourcesMostRecent() {
        unselectAllSources(SourceType.ALL_SOURCE);
        mAllSources.get(0).setSelected(true);
        mAllSourcesSelectedText = getAllSourcesText();
    }

    //================================================================================
    // Language Sources Dialog Listeners
    //================================================================================

    @Override
    public void onLanguageSourcesPositiveClick(SparseBooleanArray sparseSelectedPositions) {
        mSourcesListAdapter.setSelectedPosition(null);
        mNumLanguageSelected = 0;
        boolean currentLanguageSelected;

        for (int i = 0; i < mLanguageSources.size(); i++) {
            currentLanguageSelected = sparseSelectedPositions.get(i);

            mLanguageSources.get(i).setSelected(currentLanguageSelected);
            if (currentLanguageSelected) {
                mNumLanguageSelected++;
            }
        }
        if (mNumLanguageSelected > 0)
            languageSourcesSelected();
        else if (mNumLanguageSelected + mNumCountrySelected + mNumFeedSelected == 0) {
            setAllSourcesMostRecent();
        }
        setupListFromSources(false);
    }

    @Override
    public void onLanguageSourcesDismiss () {
        mSourcesListAdapter.setSelectedPosition(null);
    }

    //================================================================================
    // Feed Sources Dialog Listeners
    //================================================================================
    @Override
    public void onFeedSourcesPositiveClick(ArrayList<Source> updatedFeedSources) {
        mSourcesListAdapter.setSelectedPosition(null);
        mNumFeedSelected = 0;
        boolean feedSelected;

        if (mNumLanguageSelected == 0) {
            mFeedSources = updatedFeedSources;
            for (Source currentSource : mFeedSources) {
                if (currentSource.isSelected())
                    mNumFeedSelected++;
            }
        } else {
            ArrayList<String> filteredLanguages = new ArrayList<String>();
            for (Source currentSource : mLanguageSources)
                if (currentSource.isSelected())
                    filteredLanguages.add(currentSource.getLangCode());

            int filteredIndex = 0;
            for (int i = 0; i < mFeedSources.size(); i++)
                if (filteredLanguages.contains(mFeedSources.get(i).getLangCode())) {
                    feedSelected = updatedFeedSources.get(filteredIndex).isSelected();
                    mFeedSources.get(i).setSelected(feedSelected);
                    if (feedSelected) {
                        mNumFeedSelected++;
                    }
                    filteredIndex++;
                }
        }

        if (mNumFeedSelected > 0)
            feedSourcesSelected();
        else if (mNumLanguageSelected + mNumCountrySelected + mNumFeedSelected == 0) {
            setAllSourcesMostRecent();
        }
        setupListFromSources(false);
    }

    @Override
    public void onFeedSourcesDismiss () {
        mSourcesListAdapter.setSelectedPosition(null);
    }

    //================================================================================
    // Life Cycle
    //================================================================================

    @Override
    protected void onPause() {
        super.onPause();

        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        applicationState.setAllSources(mAllSources);
        applicationState.setFeedSources(mFeedSources);
        applicationState.setLanguageSources(mLanguageSources);
    }
}

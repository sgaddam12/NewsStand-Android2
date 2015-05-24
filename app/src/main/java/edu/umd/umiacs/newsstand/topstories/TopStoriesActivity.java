package edu.umd.umiacs.newsstand.topstories;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.NewsStandApplication;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.filters.FiltersActivity;
import edu.umd.umiacs.newsstand.imageview.ImageGridActivity;
import edu.umd.umiacs.newsstand.location.Article;
import edu.umd.umiacs.newsstand.map.MapMarker;
import edu.umd.umiacs.newsstand.map.MapUpdateRequest;
import edu.umd.umiacs.newsstand.settings.SettingsActivity;
import edu.umd.umiacs.newsstand.source.Source;
import edu.umd.umiacs.newsstand.source.Source.SourceType;
import edu.umd.umiacs.newsstand.source.SourcesActivity;
import edu.umd.umiacs.newsstand.videoview.VideoViewActivity;
import edu.umd.umiacs.newsstand.webview.WebViewActivity;
//import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class TopStoriesActivity extends Activity implements
        OnItemClickListener, OnClickListener, OnInfoWindowClickListener, OnMarkerClickListener,
        OnScrollListener, SeekBar.OnSeekBarChangeListener, //PullToRefreshAttacher.OnRefreshListener,
        SensorEventListener {
    private final String TAG = "edu.umd.umiacs.newsstand.topstories.TopStoriesActivity";

    private ActionBar mActionBar;
    private Button mBackButton;
    private Button mTitleButton;
    private ImageButton mTranslateButton;

    //private PullToRefreshAttacher mPullToRefreshHelper;
    private boolean mRefreshDownloading;
    private boolean mFirstLoad = true;

    private ListView mTopStoriesListView;
    private ProgressBar mProgressBar;
    private TopStoriesListAdapter mTopStoriesListAdapter;
    private int mSelectedIndex;
    private int mSelectedPosition;

    private ArrayList<Article> mTopStories;

    private GoogleMap mTopStoriesMap;
    private ArrayList<MapMarker> mMapMarkers;
    private ArrayList<Marker> mMarkers;
    private ArrayList<Marker> mCurrentMarkers;

    private SeekBar mTopStoriesSeekBar;
    private double mLastSeekValue;

    private ImageButton mTopStoriesZoomInButton;
    private ImageButton mTopStoriesZoomOutButton;

    private String mSearchKey;
    private TextView mSearchKeyTextView;
    private ImageButton mSearchKeyCancelButton;
    private ImageButton mSearchKeyCancelClickableArea;

    private String mSourceText;
    private TextView mSourceTextView;
    private ImageButton mSourceCancelButton;
    private ImageButton mSourceCancelClickableArea;
    private SourceType mSelectedSourceType;
    private int mSelectedSourceIndex = 0;

    private AsyncTask<String, Void, String> mapUpdateAsyncTask;

    private int mHandMode;
    private int mLastHandMode;

    //Accelerometer
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    boolean mSensorInitialized;

    private BitmapDescriptor generalBitmap;
    private BitmapDescriptor businessBitmap;
    private BitmapDescriptor entertainmentBitmap;
    private BitmapDescriptor healthBitmap;
    private BitmapDescriptor sciTechBitmap;
    private BitmapDescriptor sportsBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topstories);

        refreshTopStories();

        setUpListView();
        setUpMapIfNeeded();
        setupAccelerometer();
        setIconBitmaps();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate options menu from XML
        getMenuInflater().inflate(R.menu.activity_top_stories, menu);

        setupTopBarItems();
        return true;
    }

    private void setIconBitmaps() {
        generalBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_general_original);
        businessBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_business_original);
        entertainmentBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_entertainment_original);
        healthBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_health_original);
        sciTechBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_scitech_original);
        sportsBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_sports_original);
    }

    // ================================================================================
    // ActionBar Initialization/Controls
    // ================================================================================

    private void setupTopBarItems() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.action_top_stories);

            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);

            mBackButton = (Button) findViewById(R.id.topStoriesBackButton);
            mBackButton.setText("Map");
            mBackButton.setOnClickListener(this);

            MainActivity mainActivity = (MainActivity) getParent();
            String standText = "NewsStand";
            if (mainActivity != null && mainActivity.getStandMode() == MainActivity.StandMode.TWITTERSTAND) {
                standText = "TwitterStand";
            }
            mTitleButton = (Button) findViewById(R.id.topStoriesTitleButton);
            mTitleButton.setText(standText);

            mTranslateButton = (ImageButton) findViewById(R.id.topStoriesTranslateImageButton);
            mTranslateButton.setOnClickListener(this);
        }
    }

    // ================================================================================
    // ListView Initialization
    // ================================================================================

    private void setUpListView() {
        ArrayList<String> articleTitles = new ArrayList<String>();
        mTopStoriesListView = (ListView) findViewById(R.id.topStoriesListView);
        mTopStoriesListView.setSelected(false);
        mTopStoriesListView.setOnItemClickListener(this);
        mTopStoriesListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.item_topstories, R.id.topStoriesTitleText,
                articleTitles));
        mTopStoriesListAdapter = new TopStoriesListAdapter(this, mTopStories);
        mTopStoriesListView.setBackgroundColor(getResources().getColor(
                android.R.color.white));
        mTopStoriesListView.setAdapter(mTopStoriesListAdapter);
        mTopStoriesListView.setOnScrollListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.topStoriesProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        //mPullToRefreshHelper = new PullToRefreshAttacher(this);
        //mPullToRefreshHelper.setRefreshableView(mTopStoriesListView, this);
    }

    // ================================================================================
    // Map Initialization
    // ================================================================================

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mTopStoriesMap == null) {
            //Log.i(TAG, "Setup Map - map is null");
            // Try to obtain the map from the SupportMapFragment.
            mTopStoriesMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.topStoriesMap)).getMap();
            // Check if we were successful in obtaining the map.
            if (mTopStoriesMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mTopStoriesMap.setOnInfoWindowClickListener(this);
        mTopStoriesMap.setOnMarkerClickListener(this);
        mTopStoriesMap.getUiSettings().setZoomControlsEnabled(false); // Will use custom
        setupMapOverlays();
    }

    private void setupMapOverlays() {
        mTopStoriesZoomInButton = (ImageButton) findViewById(R.id.topStoriesZoomInButton);
        mTopStoriesZoomOutButton = (ImageButton) findViewById(R.id.topStoriesZoomOutButton);
        mTopStoriesSeekBar = (SeekBar) findViewById(R.id.topStoriesSeekBar);

        mTopStoriesZoomInButton.setOnClickListener(this);
        mTopStoriesZoomOutButton.setOnClickListener(this);
        mTopStoriesSeekBar.setOnSeekBarChangeListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.GENERAL_PREFS, 0);
        mSearchKey = sharedPreferences.getString(MainActivity.SEARCH_KEY, "");

        mSearchKeyTextView = (TextView) findViewById(R.id.topStoriesSearchKeyText);
        mSearchKeyCancelButton = (ImageButton) findViewById(R.id.topStoriesSearchKeyCancelButton);
        mSearchKeyCancelClickableArea = (ImageButton) findViewById(R.id.topStoriesSearchKeyCancelClickableArea);
        mSearchKeyCancelButton.setOnClickListener(this);
        mSearchKeyCancelClickableArea.setOnClickListener(this);

        mSourceTextView = (TextView) findViewById(R.id.topStoriesSourceText);
        mSourceCancelButton = (ImageButton) findViewById(R.id.topStoriesSourceCancelButton);
        mSourceCancelClickableArea = (ImageButton) findViewById(R.id.topStoriesSourceCancelClickableArea);
        mSourceCancelButton.setOnClickListener(this);
        mSourceCancelClickableArea.setOnClickListener(this);

        mLastSeekValue = (mTopStoriesSeekBar.getProgress() * 1.0) / 100.0;
    }

    // ================================================================================
    // Variable Initialization
    // ================================================================================

    private void setupAccelerometer() {
        mSensorInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mHandMode == 0) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorInitialized = true;
        }

    }

    // ================================================================================
    // ActionBar
    // ================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_search_key:
                //Log.i(TAG, "Search Key Item Clicked");
                searchKeyItemClicked();
                return true;
            case R.id.menu_sources:
                //Log.i(TAG, "Sources Item Clicked");
                sourcesItemClicked();
                return true;
            case R.id.menu_map:
                //Log.i(TAG, "Top Stories Item Clicked");
                mapItemClicked();
                return true;
            case R.id.menu_filters:
                //Log.i(TAG, "Filters Item Clicked");
                filtersItemClicked();
                return true;
            case R.id.menu_settings:
                //Log.i(TAG, "Settings Item Clicked");
                settingsItemClicked();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    // ================================================================================
    // ActionBar Items
    // ================================================================================
    private void searchKeyItemClicked() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Keyword Search");
        alert.setMessage("Set keyword to filter results.");

        final EditText textInput = new EditText(this);
        textInput.setHint("Enter keyword");
        alert.setView(textInput);

        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mSearchKey = textInput.getText().toString();
                updateSearchKeyword();
                refreshTopStories();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void sourcesItemClicked() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        ArrayList<Source> allSources = applicationState.getAllSources();
        ArrayList<Source> languageSources = applicationState.getLanguageSources();
        ArrayList<Source> feedSources = applicationState.getFeedSources();
        Intent intent = new Intent(this, SourcesActivity.class);
        //Log.i(TAG, Integer.toString(languageSources.size()));
        intent.putExtra(MainActivity.TITLE, "Top\nStories");
        intent.putExtra("allSources", allSources);
        intent.putExtra("languageSources", languageSources);
        intent.putExtra("feedSources", feedSources);
        startActivity(intent);
    }

    private void mapItemClicked() {
        finish();
    }

    private void filtersItemClicked() {
        Intent intent = new Intent(this, FiltersActivity.class);
        startActivity(intent);
    }

    private void settingsItemClicked() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("topStories", true);
        startActivity(intent);
    }

    // ================================================================================
    // Map Overlays
    // ================================================================================

    private void zoomInButtonClicked() {
        float zoomTo = mTopStoriesMap.getCameraPosition().zoom + 2;
        mTopStoriesMap.animateCamera(CameraUpdateFactory.zoomTo(zoomTo));
    }

    private void zoomOutButtonClicked() {
        float zoomTo = mTopStoriesMap.getCameraPosition().zoom - 2;
        if (zoomTo < 2)
            zoomTo = 2.0f;
        mTopStoriesMap.animateCamera(CameraUpdateFactory.zoomTo(zoomTo));
    }

    private void searchKeyCancelButtonClicked() {
        mSearchKeyTextView.setVisibility(View.GONE);
        mSearchKeyCancelButton.setVisibility(View.GONE);
        mSearchKeyCancelClickableArea.setVisibility(View.GONE);
        mSearchKeyTextView.setText("");
        mSearchKey = "";
        refreshTopStories();
    }

    private void updateSearchKeyword() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.GENERAL_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (mSearchKey == null || mSearchKey.equals("")) {
            mSearchKeyTextView.setText("");
            mSearchKeyTextView.setVisibility(View.GONE);
            mSearchKeyCancelButton.setVisibility(View.GONE);
            mSearchKeyCancelClickableArea.setVisibility(View.GONE);
        } else {
            mSearchKeyTextView.setText("Search Keyword: " + mSearchKey);
            mSearchKeyTextView.setVisibility(View.VISIBLE);
            mSearchKeyCancelButton.setVisibility(View.VISIBLE);
            mSearchKeyCancelClickableArea.setVisibility(View.VISIBLE);
        }

        editor.putString(MainActivity.SEARCH_KEY, mSearchKeyTextView.getText().toString());
    }

    private void updateSourceText() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        ArrayList<Source> allSources = applicationState.getAllSources();
        ArrayList<Source> languageSources = applicationState.getLanguageSources();
        ArrayList<Source> feedSources = applicationState.getFeedSources();

        // Make sure sources have been set
        if (allSources == null || languageSources == null || feedSources == null)
            return;

        boolean foundSourceType = false;

        String firstNameFound = "";
        int numSelected = 0;

        int i = 0;

        // Check if a feed source is selected
        for (Source feedSource : feedSources) {
            if (feedSource.isSelected()) {
                if (!foundSourceType) {
                    firstNameFound = feedSource.getName();
                    mSelectedSourceIndex = i;
                }
                numSelected++;
                foundSourceType = true;
                mSelectedSourceType = SourceType.FEED_SOURCE;
            }
            i++;
        }
        if (numSelected > 1) {
            mSourceText = "Sources: " + firstNameFound + "...";
        } else if (numSelected == 1) {
            mSourceText = "Source: " + firstNameFound;
        }

        // Check if a language source is selected if no feed
        if (!foundSourceType) {
            i = 0;
            for (Source languageSource : languageSources) {
                if (languageSource.isSelected()) {
                    if (!foundSourceType) {
                        firstNameFound = languageSource.getName();
                        mSelectedSourceIndex = i;
                    }
                    numSelected++;
                    foundSourceType = true;
                    mSelectedSourceType = SourceType.LANGUAGE_SOURCE;
                }
                i++;
            }

            if (numSelected > 1) {
                mSourceText = "Languages: " + firstNameFound + "...";
            } else if (numSelected == 1) {
                mSourceText = "Language: " + firstNameFound;
            }
        }

        // Check which all sources selected if no feed or language
        if (!foundSourceType) {
            i = 0;
            for (Source allSource : allSources) {
                if (allSource.isSelected()) {
                    mSourceText = "Rep. Source: " + allSource.getName();
                    mSelectedSourceType = SourceType.ALL_SOURCE;
                    mSelectedSourceIndex = i;
                    foundSourceType = true;
                    break;
                }
                i++;
            }
            if (!foundSourceType) {
                mSourceText = "Rep. Source: Most Recent";
                allSources.get(0).setSelected(true);
                applicationState.setAllSources(allSources);
                mSelectedSourceType = SourceType.ALL_SOURCE;
                mSelectedSourceIndex = 0;
            }
        }

        mSourceText = "\u200e" + mSourceText;
        mSourceTextView.setText(mSourceText);
        if (numSelected > 1)
            mSourceTextView.setTextIsSelectable(true);
        else
            mSourceTextView.setTextIsSelectable(false);

        if (mSelectedSourceType != SourceType.ALL_SOURCE || mSelectedSourceIndex != 0) {
            mSourceCancelButton.setVisibility(View.VISIBLE);
            mSourceCancelClickableArea.setVisibility(View.VISIBLE);
        } else {
            mSourceCancelButton.setVisibility(View.GONE);
            mSourceCancelClickableArea.setVisibility(View.GONE);
        }
    }

    private void sourceCancelButtonClicked() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        if (mSelectedSourceType == SourceType.FEED_SOURCE) {
            ArrayList<Source> feedSources = applicationState.getFeedSources();
            feedSources.get(mSelectedSourceIndex).setSelected(false);
            applicationState.setFeedSources(feedSources);
        } else if (mSelectedSourceType == SourceType.LANGUAGE_SOURCE) {
            ArrayList<Source> languageSources = applicationState.getLanguageSources();
            languageSources.get(mSelectedSourceIndex).setSelected(false);
            applicationState.setLanguageSources(languageSources);
        } else if (mSelectedSourceType == SourceType.ALL_SOURCE) {
            ArrayList<Source> allSources = applicationState.getAllSources();
            allSources.get(mSelectedSourceIndex).setSelected(false);
            allSources.get(0).setSelected(true);
            applicationState.setAllSources(allSources);
        }

        refreshTopStories();
        updateSourceText();
    }

    // ================================================================================
    // OnScrollListener
    // ================================================================================

    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int firstVisibleIndex = mTopStoriesListView.getFirstVisiblePosition();
        int lastVisibleIndex = mTopStoriesListView.getLastVisiblePosition();
        mTopStoriesListView.setItemChecked(mSelectedIndex, false);
        if (firstVisibleIndex + mSelectedPosition > lastVisibleIndex) {
            mSelectedIndex = lastVisibleIndex;
        } else {
            mSelectedIndex = firstVisibleIndex + mSelectedPosition;
        }
        mTopStoriesListAdapter.setSelectedPosition(mSelectedIndex);
        updateMapMarkers();
    }

    // ================================================================================
    // Marker Click Listener
    // ================================================================================
    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    private MapMarker getMapMarkerForMarker(Marker currentMarker) {
        MapMarker selectedMapMarker = null;
        return selectedMapMarker;
    }

    // ================================================================================
    // Info Window Click Listener
    // ================================================================================
    @Override
    public void onInfoWindowClick(Marker marker) {
        // MapMarker selectedMapMarker = getMapMarkerForMarker(marker);
        // if (selectedMapMarker != null) {
        // Intent intent = new Intent(this, LocationActivity.class);
        // intent.putExtra(MainActivity.GAZ_ID, selectedMapMarker.getGaz_id());
        // startActivity(intent);
        // }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long duration) {
        int firstVisibleIndex = mTopStoriesListView.getFirstVisiblePosition();

        mSelectedPosition = position - firstVisibleIndex;
        mSelectedIndex = position;
        mTopStoriesListAdapter.setSelectedPosition(mSelectedIndex);
        updateMapMarkers();
    }

    private void updateMapMarkers() {
        if (mTopStories == null)
            return;

        if (mSelectedIndex < 0 || mSelectedIndex >= mTopStories.size())
            mSelectedIndex = 0;

        Article currentArticle = mTopStories.get(mSelectedIndex);

        String mapUpdateURL = "http://newsstand.umiacs.umd.edu/news/xml_map?cluster_id="
                + currentArticle.getCluster_id();
        //Log.i(TAG, "mapUpdateURL");

        if (mapUpdateAsyncTask == null) {
            mapUpdateAsyncTask = new DownloadMapUpdateXmlTask()
                    .execute(mapUpdateURL);
        } else {
            if (mapUpdateAsyncTask.getStatus() == AsyncTask.Status.RUNNING
                    || mapUpdateAsyncTask.getStatus() == AsyncTask.Status.PENDING)
                mapUpdateAsyncTask.cancel(true);

            mapUpdateAsyncTask = null;
            mapUpdateAsyncTask = new DownloadMapUpdateXmlTask()
                    .execute(mapUpdateURL);
        }
    }

    // ================================================================================
    // Activity Lifecycle
    // ================================================================================
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.GENERAL_PREFS, 0);
        int handMode = sharedPreferences.getInt(MainActivity.ONE_HAND_MODE_KEY, 0);

        if (handMode == 0) {
            if (mSensorManager != null)
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
            }
            mHandMode = handMode;
            updateHandMode();
        }

        if (!mFirstLoad) {
            updateSearchKeyword();
            updateSourceText();
            refreshTopStories();
        } else {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            updateSearchKeyword();
                            updateSourceText();
                            ObjectAnimator searchTextAnimation
                                    = ObjectAnimator.ofFloat(mSearchKeyTextView, "alpha", 0.0f, 1.0f);
                            searchTextAnimation.setDuration(200);
                            searchTextAnimation.start();

                            ObjectAnimator sourceTextAnimation
                                    = ObjectAnimator.ofFloat(mSourceTextView, "alpha", 0.0f, 1.0f);
                            sourceTextAnimation.setDuration(200);
                            sourceTextAnimation.start();
                        }
                    });
                }
            }, 300);
        }

        mFirstLoad = false;
    }

    // ================================================================================
    // Update Top Stories
    // ================================================================================

    private void refreshTopStories() {
        String topStoriesURL = "http://newsstand.umiacs.umd.edu/news/iphone_results?" + getPreferencesString()
                + getSourcesString();
        //Log.i(TAG, "topStoriesURL");
        new DownloadXmlTask().execute(topStoriesURL);
    }

    private void updateTopStories(ArrayList<Article> topStories) {
        mTopStories = topStories;
        boolean foundTranslated = false;

        if (mTopStories != null) {
            for (Article currentArticle : mTopStories) {
                String translatedTitle = currentArticle.getTranslate_title().trim();
                if (translatedTitle != null && !translatedTitle.equals("")) {
                    foundTranslated = true;
                    break;
                }
            }

            if (foundTranslated) mTranslateButton.setVisibility(View.VISIBLE);
            else mTranslateButton.setVisibility(View.GONE);
        }

        mProgressBar.setVisibility(View.INVISIBLE);
        mTopStoriesListAdapter = new TopStoriesListAdapter(this, mTopStories);
        mTopStoriesListView.setAdapter(mTopStoriesListAdapter);

        if (mRefreshDownloading) {
            //mPullToRefreshHelper.setRefreshComplete();
            mRefreshDownloading = false;
        }
    }

    // ================================================================================
    // ListView Button Selection
    // ================================================================================


    @Override
    public void onClick(View view) {
        //First check if button press not in list
        boolean buttonClicked = false;

        switch (view.getId()) {
            case R.id.topStoriesTranslateImageButton:
                mTopStoriesListAdapter.toggleTranslateTitles();
                buttonClicked = true;
                break;
            case R.id.topStoriesZoomInButton:
                zoomInButtonClicked();
                buttonClicked = true;
                break;
            case R.id.topStoriesZoomOutButton:
                zoomOutButtonClicked();
                buttonClicked = true;
                break;
            case R.id.topStoriesSearchKeyCancelClickableArea:
            case R.id.topStoriesSearchKeyCancelButton:
                //Log.i(TAG, "Search Key Cancel Button Clicked");
                searchKeyCancelButtonClicked();
                buttonClicked = true;
                break;
            case R.id.topStoriesSourceCancelButton:
            case R.id.topStoriesSourceCancelClickableArea:
                //Log.i(TAG, "Source Cancel Button Clicked");
                sourceCancelButtonClicked();
                buttonClicked = true;
                break;
            case R.id.topStoriesBackButton:
                finish();
                buttonClicked = true;
                break;
        }

        // If not button clicked then a button was clicked on the list
        if (!buttonClicked) {
            int tag = (Integer) view.getTag();
            int selectionType = tag / 1000;
            int articleRow = tag % 1000;

            switch (selectionType) {
                case 0: // Article Title Selected
                    //Log.i(TAG, "Article at row " + articleRow + " selected");
                    articleSelected(articleRow);
                    break;
                case 1: // Image Button Selected
                    //Log.i(TAG, "Image at row " + articleRow + " selected");
                    imageSelected(articleRow);
                    break;
                case 2: // Video Button Selected
                    //Log.i(TAG, "Video at row " + articleRow + " selected");
                    videoSelected(articleRow);
                    break;
                case 3: // Related Button Selected
                    //Log.i(TAG, "Related at row " + articleRow + " selected");
                    relatedSelected(articleRow);
                    break;
                default:
                    break;
            }
        }
    }

    private void articleSelected(int row) {
        if (mTopStories != null && mTopStories.size() > 0) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra("articleURL", mTopStories.get(row).getUrl());
            intent.putExtra(MainActivity.TITLE, "Top\nStories");
            //Log.i(TAG, mTopStories.get(row).getUrl());
            startActivity(intent);
        }
    }

    private void imageSelected(int row) {
        if (mTopStories != null && row <= mTopStories.size()) {
            Intent intent = new Intent(this, ImageGridActivity.class);
            intent.putExtra(MainActivity.CLUSTER_ID, mTopStories.get(row).getCluster_id());
            intent.putExtra(MainActivity.TITLE, "Top\nStories");
            startActivity(intent);
        }
    }

    private void videoSelected(int row) {
        if (mTopStories != null && row <= mTopStories.size()) {
            Intent intent = new Intent(this, VideoViewActivity.class);
            intent.putExtra(MainActivity.CLUSTER_ID, mTopStories.get(row).getCluster_id());
            intent.putExtra(MainActivity.TITLE, "Top\nStories");
            startActivity(intent);
        }
    }

    private void relatedSelected(int row) {
        if (mTopStories != null && row <= mTopStories.size()) {
            Intent intent = new Intent(this, WebViewActivity.class);

            String relatedURL = "http://newsstand.umiacs.umd.edu/news/story_light?cluster_id=" +
                    mTopStories.get(row).getCluster_id()+ "&limit=30&page=1";
            intent.putExtra("articleURL",relatedURL);
            intent.putExtra(MainActivity.TITLE, "Top\nStories");

            startActivity(intent);
        }
    }

    // ================================================================================
    // SeekBar.OnSeekBarChangeListener
    // ================================================================================

    private void updateMarkersFromSeekBar(int progress) {
        int i = 0;

        // Progress from 0 to 100, make it 0 to 1
        double progressPercent = progress / 100.0;

        if (mMarkers != null) {
            int max = (int) (progressPercent * mMarkers.size() + 1);
            int lastMax = (int) (mLastSeekValue * mMarkers.size() + 1);

            mLastSeekValue = progressPercent;

            while (i < mMarkers.size()) {
                if (i <= lastMax) mMarkers.get(i).setVisible(true);
                else if (i > max) mMarkers.get(i).setVisible(false);

                i++;
            }
        }

        setMapView();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateMarkersFromSeekBar(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    // ================================================================================
    // Update Map Markers
    // ================================================================================
    private void updateMapMarkers(ArrayList<MapMarker> updatedMapMarkers) {
        mMapMarkers = updatedMapMarkers;

        if (mMarkers != null) {
            for (Marker currentMarker : mMarkers) {
                currentMarker.remove();
            }
            mMarkers.clear();
        }

        mMarkers = null;
        mMarkers = new ArrayList<Marker>();

        //Log.i(TAG, "Update Map Markers");

        Marker currentMarker = null;

        for (MapMarker currentMapMarker : mMapMarkers) {
            BitmapDescriptor currentBitmapDescriptor = bitmapDescriptorForTopic(currentMapMarker
                    .getTopic());
            currentMarker = mTopStoriesMap.addMarker(new MarkerOptions()
                    .icon(currentBitmapDescriptor)
                    .position(
                            new LatLng(currentMapMarker.getLatitude(),
                                    currentMapMarker.getLongitude()))
                    .title(currentMapMarker.getName())
                    .snippet(currentMapMarker.getTitle()));
            mMarkers.add(currentMarker);
        }

        updateMarkersFromSeekBar((int) (mLastSeekValue * 100));
    }

    private List<LatLng> latLngForMarkers() {
        List<LatLng> points = new ArrayList<LatLng>();

        if (mMarkers != null)
            for (Marker currentMarker : mMarkers) {
                if (currentMarker.isVisible())
                    points.add(currentMarker.getPosition());
            }
        return points;
    }

    private void setMapView() {
        List<LatLng> points = latLngForMarkers();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (points == null || points.size() < 1) {
            //Log.i(TAG, "No points found");
            return;
        }

        for (LatLng point : points) {
            builder.include(point);
        }

        mTopStoriesMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                builder.build(), 50));
    }

    private BitmapDescriptor bitmapDescriptorForTopic(String topic) {
        BitmapDescriptor topicBitmapDescriptor = null;

        if (topic.equals("General")) {
            topicBitmapDescriptor = generalBitmap;
        } else if (topic.equals("Business")) {
            topicBitmapDescriptor = businessBitmap;
        } else if (topic.equals("Entertainment")) {
            topicBitmapDescriptor = entertainmentBitmap;
        } else if (topic.equals("Health")) {
            topicBitmapDescriptor = healthBitmap;
        } else if (topic.equals("SciTech")) {
            topicBitmapDescriptor = sciTechBitmap;
        } else if (topic.equals("Sports")) {
            topicBitmapDescriptor = sportsBitmap;
        }

        if (topicBitmapDescriptor == null)
            topicBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker_general_original));
        return topicBitmapDescriptor;
    }

    // ================================================================================
    // Pull to Refresh Delegate
    // ================================================================================
    //@Override
    public void onRefreshStarted(View view) {
        mRefreshDownloading = true;
        refreshTopStories();
    }

    // ================================================================================
    // Get Filter Parameters
    // ================================================================================

    private String getTopicString() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.GENERAL_PREFS, 0);
        String topicString = "";

        if (sharedPreferences.getBoolean(FiltersActivity.ALL_TOPICS, false))
            return topicString;

        topicString = "&cat=";

        if (sharedPreferences.getBoolean(FiltersActivity.TOPIC_BUSINESS, false)) {
            topicString += "Business,";
        }
        if (sharedPreferences.getBoolean(FiltersActivity.TOPIC_ENTERTAINMENT, false)) {
            topicString += "Entertainment,";
        }
        if (sharedPreferences.getBoolean(FiltersActivity.TOPIC_GENERAL, false)) {
            topicString += "General,";
        }
        if (sharedPreferences.getBoolean(FiltersActivity.TOPIC_HEALTH, false)) {
            topicString += "Health,";
        }
        if (sharedPreferences.getBoolean(FiltersActivity.TOPIC_SCITECH, false)) {
            topicString += "SciTech,";
        }
        if (sharedPreferences.getBoolean(FiltersActivity.TOPIC_SPORTS, false)) {
            topicString += "Sports,";
        }

        if (topicString.length() > 0 && topicString.charAt(topicString.length() - 1) == ',') {
            topicString = topicString.substring(0, topicString.length() - 1);
        }

        return topicString;
    }

    private String getPreferencesString() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.GENERAL_PREFS, 0);

        String paramString = "";
        //Log.i(TAG, getTopicString());
        paramString += getTopicString();

        int numImages = sharedPreferences.getInt(FiltersActivity.NUM_IMAGES, 0);
        if (numImages > 0)
            paramString += "&num_images=" + Integer.toString(numImages + 1);
        int numVideos = sharedPreferences.getInt(FiltersActivity.NUM_VIDEOS, 0);
        if (numVideos > 0)
            paramString += "&num_videos=" + Integer.toString(numVideos + 1);

        if (mSearchKey != null && !mSearchKey.equals("")) {
            paramString += "&search=" + mSearchKey;
        }

        return paramString;
    }

    private String getSourcesString() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        ArrayList<Source> allSources = applicationState.getAllSources();
        ArrayList<Source> languageSources = applicationState.getLanguageSources();
        ArrayList<Source> feedSources = applicationState.getFeedSources();

        // Check if any all sources are selected
        for (int i = 0; i < allSources.size(); i++) {
            if (allSources.get(i).isSelected()) {
                if (i == 0) {
                    return "&rank=time";
                } else if (i == 2) {
                    return "&rank=newest";
                } else {
                    return "";
                }
            }
        }

        String sourceParamString = "";
        boolean first = true;

        // Check if any feeds are selected
        for (Source feedSource : feedSources) {
            if (feedSource.isSelected()) {
                if (first) {
                    sourceParamString = "&feedlinks=" + feedSource.getFeedLink();
                    first = false;
                } else
                    sourceParamString += "," + feedSource.getFeedLink();
            }
        }

        if (!sourceParamString.equals(""))
            return sourceParamString;

        for (Source languageSource : languageSources) {
            if (languageSource.isSelected()) {
                if (first) {
                    sourceParamString = "&lang=" + languageSource.getLangCode();
                    first = false;
                } else
                    sourceParamString += "," + languageSource.getLangCode();
            }
        }

        return sourceParamString;
    }

    // ================================================================================
    // Sensor Interface -- One Hand Mode
    // ================================================================================
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];

        if (x > 2.0) {
            mHandMode = 1;
        } else if (x < -2.0) {
            mHandMode = 2;
        }

        if (mLastHandMode != mHandMode) updateHandMode();
    }

    private void updateHandMode() {
        mLastHandMode = mHandMode;

        RelativeLayout.LayoutParams zoomInParams = (RelativeLayout.LayoutParams) mTopStoriesZoomInButton.getLayoutParams();
        RelativeLayout.LayoutParams zoomOutParams = (RelativeLayout.LayoutParams) mTopStoriesZoomOutButton.getLayoutParams();

        //Log.i(TAG, "Hand Mode: " + mHandMode);

        if (mHandMode == 2) { // Right Hand
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            zoomInParams.leftMargin = (int) getResources().getDimension(R.dimen.neutral_zoom_right);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            zoomOutParams.leftMargin = (int) getResources().getDimension(R.dimen.neutral_zoom_right);
        } else { // Neutral & Left Hand
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            zoomInParams.rightMargin = (int) getResources().getDimension(R.dimen.neutral_zoom_right);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            zoomOutParams.rightMargin = (int) getResources().getDimension(R.dimen.neutral_zoom_right);
        }

        RelativeLayout topStoriesRelativeLayout = (RelativeLayout) findViewById(R.id.topStoriesRelativeLayout);

        topStoriesRelativeLayout.removeView(mTopStoriesZoomInButton);
        topStoriesRelativeLayout.removeView(mTopStoriesZoomOutButton);

        mTopStoriesZoomInButton.setLayoutParams(zoomInParams);
        mTopStoriesZoomOutButton.setLayoutParams(zoomOutParams);

        topStoriesRelativeLayout.addView(mTopStoriesZoomInButton);
        topStoriesRelativeLayout.addView(mTopStoriesZoomOutButton);
    }

    // ================================================================================
    // Network Calls for Articles
    // ================================================================================
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {
        ArrayList<Article> updatedArticles;

        @Override
        protected String doInBackground(String... urls) {
            try {
                updatedArticles = loadXmlFromNetwork(urls[0]);
                return "finished";
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        protected void onPostExecute(String result) {
            updateTopStories(updatedArticles);
        }
    }

    private ArrayList<Article> loadXmlFromNetwork(String urlString)
            throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        TopStoriesUpdateRequest topStoriesUpdateRequest = new TopStoriesUpdateRequest();
        ArrayList<Article> articlesFound = null;

        try {
            stream = downloadUrl(urlString);
            articlesFound = topStoriesUpdateRequest.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        //Log.i(TAG, "Found " + articlesFound.size() + " articles");
        return articlesFound;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        //Log.i(TAG, "Download URL " + urlString);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    // ================================================================================
    // Network Calls for Markers
    // ================================================================================
    private class DownloadMapUpdateXmlTask extends
            AsyncTask<String, Void, String> {
        ArrayList<MapMarker> updatedMarkers;

        @Override
        protected String doInBackground(String... urls) {
            try {
                updatedMarkers = loadMapUpdateXmlFromNetwork(urls[0]);
                return "finished";
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        protected void onPostExecute(String result) {
            updateMapMarkers(updatedMarkers);
        }
    }

    private ArrayList<MapMarker> loadMapUpdateXmlFromNetwork(String urlString)
            throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        MapUpdateRequest mapUpdateRequest = new MapUpdateRequest();
        ArrayList<MapMarker> markersFound = null;

        try {
            stream = downloadUrl(urlString);
            markersFound = mapUpdateRequest.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        //Log.i(TAG, "Found " + markersFound.size() + " articles");
        return markersFound;
    }


}

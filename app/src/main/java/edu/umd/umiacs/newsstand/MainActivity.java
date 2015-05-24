package edu.umd.umiacs.newsstand;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import edu.umd.umiacs.newsstand.about.AboutDialogFragment;
import edu.umd.umiacs.newsstand.cache.DeviceGrid;
import edu.umd.umiacs.newsstand.cache.DeviceGridCell;
import edu.umd.umiacs.newsstand.cache.Prediction;
import edu.umd.umiacs.newsstand.cache.QueryRecord;
import edu.umd.umiacs.newsstand.filters.FiltersActivity;
import edu.umd.umiacs.newsstand.general.NoResultsRequest;
import edu.umd.umiacs.newsstand.imageview.ImageGridActivity;
import edu.umd.umiacs.newsstand.layers.LayersAlertDialogFragment;
import edu.umd.umiacs.newsstand.layers.LayersAlertDialogFragment.LayersAlertPositiveListener;
import edu.umd.umiacs.newsstand.location.LocationActivity;
import edu.umd.umiacs.newsstand.map.MapInfoWindowAdapter;
import edu.umd.umiacs.newsstand.map.MapMarker;
import edu.umd.umiacs.newsstand.map.MapUpdateRequest;
import edu.umd.umiacs.newsstand.mode.ModeAlertDialogFragment;
import edu.umd.umiacs.newsstand.search.GeocoderPlusAddress;
import edu.umd.umiacs.newsstand.search.LocationSearchActivity;
import edu.umd.umiacs.newsstand.settings.SettingsActivity;
import edu.umd.umiacs.newsstand.source.Source;
import edu.umd.umiacs.newsstand.source.Source.SourceType;
import edu.umd.umiacs.newsstand.source.SourcesActivity;
import edu.umd.umiacs.newsstand.source.update.SourceUpdateRequest;
import edu.umd.umiacs.newsstand.topstories.TopStoriesActivity;
import io.keen.client.android.KeenClient;
import io.keen.client.android.UploadFinishedCallback;
import io.keen.client.android.exceptions.KeenException;


public class MainActivity extends Activity implements OnCameraChangeListener, OnClickListener,
        View.OnLongClickListener,
        OnInfoWindowClickListener, OnMarkerClickListener, SeekBar.OnSeekBarChangeListener,
        LayersAlertPositiveListener, LocationListener, ModeAlertDialogFragment.ModeAlertPositiveListener,
        SensorEventListener {

    private final String TAG = "edu.umd.umiacs.newsstand.MainActivitiy";

    public static final String GENERAL_PREFS = "GeneralPrefsFile";
    public static final String ALL_SOURCES_FILENAME = "all_sources_file";
    public static final String LANGUAGE_SOURCES_FILENAME = "language_sources_file";
    public static final String COUNTRY_SOURCES_FILENAME = "country_sources_file";
    public static final String FEED_SOURCES_FILENAME = "feed_sources_file";

    public static final String QUERY_RECORDS_FILENAME = "query_records_file";
    public static final String TRAINING_SESSION_FILENAME = "training_session_file";
    public static final String SAMPLE_SESSION_FILENAME = "sample_session_file3";

    public static final String ONE_HAND_MODE_KEY = "ONE_HAND_MODE";

    public static final String CONSTRAINTS = "CONSTRAINTS";
    public static final String GAZ_ID = "GAZ_ID";
    public static final String CLUSTER_ID = "CLUSTER_ID";
    public static final String TITLE = "TITLE";
    public static final String LOCATION_NAME = "LOCATION_NAME";

    public static final String MAP_LAT_BOT = "MAP_LAT_BOT";
    public static final String MAP_LAT_TOP = "MAP_LAT_TOP";
    public static final String MAP_LON_LEFT = "MAP_LON_LEFT";
    public static final String MAP_LON_RIGHT = "MAP_LON_RIGHT";

    public static final String HOME_LAT_BOT = "HOME_LAT_BOT";
    public static final String HOME_LAT_TOP = "HOME_LAT_TOP";
    public static final String HOME_LON_LEFT = "HOME_LAT_LEFT";
    public static final String HOME_LON_RIGHT = "HOME_LON_RIGHT";

    public static final String FILTER_NUM_IMAGES = "FILTER_NUM_IMAGES";
    public static final String FILTER_NUM_VIDEOS = "FILTER_NUM_VIDEOS";

    public static final String SEARCH_KEY = "SEARCH_KEY";
    public static final String TWITTERSTAND = "TWITTERSTAND";

    public static final int MAX_CACHE_SECONDS = 120;
    public static final float TEXT_MARKER_SIZE = 20.0f; // in dip
    public static final double IMAGE_MARKER_SIZE = 35.0; // in dip

    public static final int TIMEOUT_MILLISECONDS = 4000;

    public enum DeviceGridCellMarkerState {
        DOWNLOADING,
        COMPLETED
    }

    public enum StandMode {
        NEWSSTAND(0), TWITTERSTAND(1), PHOTOSTAND(2);
        private final int value;

        private StandMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options;

    public static String[] modes = new String[]{
            "NewsStand",
            "TwitterStand",
            "PhotoStand"
    };
    private StandMode mStandMode;
    private StandMode mPreviousStandMode;
    private StandMode[] mStandModeValues;

    // PhotoStand specific
    private ArrayList<String> mImageURLs;
    private int mImageMarkerPixels;

    private ArrayList<DeviceGridCell> mWindowGridCells;
    private ArrayList<DeviceGridCell> mPredictions;
    private ArrayList<DeviceGridCellMarker> mGridCellMarkers;

    private boolean retrievedCurrentWindow;

    private ArrayList<QueryRecord> mQueryRecords;
    private ArrayList<QueryRecord> mTrainingSession;
    private ArrayList<QueryRecord> mSampleSession;
    private int sampleSessionIndex;
    private String sampleSessionString = "";
    private long sessionStartTime = 0;

    private int K = 5;
    private Prediction mPrediction;

    private boolean firstLoad = true;
    private boolean firstCameraChange = true;

    private float mDensityMultiplier; // Scale pixels according to device

    private ActionBar mActionBar;
    private Menu mMenu;

    private LocationManager mLocationManager;

    private MenuItem mLocateMenuItem;
    private SearchManager mSearchManager;
    private SearchView mLocationSearchView;

    private TextView mModeText;
    private SeekBar mSeekBar;
    private double mLastSeekValue;

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private double mMapLatBot;
    private double mMapLatTop;
    private double mMapLonLeft;
    private double mMapLonRight;

    private ImageButton mZoomInButton;
    private ImageButton mZoomOutButton;
    private ImageButton mRefreshButton;

    private AsyncTask<String, Void, String> mapUpdateAsyncTask;
    private AsyncTask<String, Void, String> languageSourceAsyncTask;
    private AsyncTask<String, Void, String> feedSourceAsyncTask;

    private BitmapDescriptor generalBitmap;
    private BitmapDescriptor businessBitmap;
    private BitmapDescriptor entertainmentBitmap;
    private BitmapDescriptor healthBitmap;
    private BitmapDescriptor sciTechBitmap;
    private BitmapDescriptor sportsBitmap;


    private ArrayList<MapMarker> mMapMarkers;
    private ArrayList<Marker> mMarkers;
    private ArrayList<Rect> mBoundingRects;
    private int mLabelColor;
    private int mShadowColor;

    private boolean mZoomToArea; // Must find markers if set (zoom out until)


    private LocationSearchActivity mLocationSearchActivity;

    public static String[] layers = new String[]{
            "Icon",
            "Disease",
            "Keyword",
            "Location",
            "People",
            "Brands"
    };
    private int mPreviousLayerSelected = 0;
    private int mLayerSelected = 0;

    private String mSearchKey;
    private TextView mSearchKeyTextView;
    private ImageButton mSearchKeyCancelButton;
    private ImageButton mSearchKeyCancelClickableArea;
    private SourceType mSelectedSourceType;
    private int mSelectedSourceIndex = 0;
    private int mCacheSize;

    private String mSourceText;
    private TextView mSourceTextView;
    private ImageButton mSourceCancelButton;
    private ImageButton mSourceCancelClickableArea;

    private String mConstraints;

    private int mHandMode;
    private int mLastHandMode;

    //Accelerometer
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    boolean mSensorInitialized;

    //Grid
    private DeviceGrid portraitGrid;
    private DeviceGrid landscapeGrid;

    // Keen Analytics
    private int mWindowsSinceUpdate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate options menu from XML
        getMenuInflater().inflate(R.menu.activity_main, menu);

        mMenu = menu;

        setupTopBarItems();
        setupMenuItemsAndActionViews(menu);
        setupLocationSearchView();

        return true;
    }

    // ================================================================================
// Cache stuff
// ================================================================================
    private void initializeCache() {
        mQueryRecords = loadQueryRecordsFromFile();
        if (mQueryRecords == null) {
            mQueryRecords = new ArrayList<QueryRecord>();
        }

        mCacheSize = 10;
        mPrediction = new Prediction();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeSources();
        initializeCache();

        setupImageMarkers();
        setupAccelerometer();
        setupLocationManager();

        setupActionBar();
        setupTopBarItems();
        setupKeen();
    }

    @Override
    protected void onPause() {
        //Log.i(TAG, "ON PAUSE CALLED!");
        saveGlobalsToFiles();
        mSensorManager.unregisterListener(this);

        uploadKeen();

        super.onPause();
    }

    public void saveGlobalsToFiles() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        FileOutputStream fosAll = null, fosLang = null, fosFeed = null;
        ObjectOutputStream oosAll = null, oosLang = null, oosFeed = null;

        try {
            fosAll = openFileOutput(ALL_SOURCES_FILENAME, Context.MODE_PRIVATE);
            oosAll = new ObjectOutputStream(fosAll);
            oosAll.writeObject(applicationState != null ? applicationState.getAllSources() : null);
            fosLang = openFileOutput(LANGUAGE_SOURCES_FILENAME, Context.MODE_PRIVATE);
            oosLang = new ObjectOutputStream(fosLang);
            oosLang.writeObject(applicationState.getLanguageSources());

            fosFeed = openFileOutput(FEED_SOURCES_FILENAME, Context.MODE_PRIVATE);
            oosFeed = new ObjectOutputStream(fosFeed);
            oosFeed.writeObject(applicationState.getFeedSources());
        } catch (Exception e) {
            Log.e("NewsStand", "failed to write", e);
        } finally {
            try {
                if (oosAll != null) oosAll.close();
                if (fosAll != null) fosAll.close();
                if (oosLang != null) oosLang.close();
                if (fosLang != null) fosLang.close();
                if (oosFeed != null) oosFeed.close();
                if (fosFeed != null) fosFeed.close();
            } catch (Exception e) { /* do nothing */ }
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Source> loadSourcesFromFile(String filename) {
        ArrayList<Source> sources = new ArrayList<Source>();
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = openFileInput(filename);
            ois = new ObjectInputStream(fis);
            sources = (ArrayList<Source>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (ois != null) ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sources;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<QueryRecord> loadQueryRecordsFromFile() {
        ArrayList<QueryRecord> queryRecords = new ArrayList<QueryRecord>();
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = openFileInput(QUERY_RECORDS_FILENAME);
            ois = new ObjectInputStream(fis);
            queryRecords = (ArrayList<QueryRecord>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (ois != null) ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return queryRecords;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<QueryRecord> loadSampleSessionFromFile() {
        ArrayList<QueryRecord> sampleSession = new ArrayList<QueryRecord>();
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = openFileInput(SAMPLE_SESSION_FILENAME);
            ois = new ObjectInputStream(fis);
            sampleSession = (ArrayList<QueryRecord>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (ois != null) ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sampleSession;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<QueryRecord> loadTrainingSessionFromFile() {
        ArrayList<QueryRecord> sampleSession = new ArrayList<QueryRecord>();
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = openFileInput(TRAINING_SESSION_FILENAME);
            ois = new ObjectInputStream(fis);
            sampleSession = (ArrayList<QueryRecord>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (ois != null) ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sampleSession;
    }

    // ================================================================================
    // Variable Initialization
    // ================================================================================

    private void setupActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            mActionBar.setCustomView(R.layout.action_main);
        }
    }

    private void setupMenuItemsAndActionViews(Menu menu) {
        mLocateMenuItem = menu.findItem(R.id.locateMenuItem);
        mSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mLocationSearchView = (SearchView) menu.findItem(R.id.locateMenuItem).getActionView();
    }

    private void setupLocationManager() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                100000,          // 1000-second interval.
                100,             // 100 meters.
                this);

        mPrediction = new Prediction();
        // mPrediction.learnModel();
    }

    private void setupImageMarkers() {
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.color.white)
                .cacheInMemory()
                .cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        mImageURLs = new ArrayList<String>();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mImageMarkerPixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) IMAGE_MARKER_SIZE, metrics));

        mDensityMultiplier = getBaseContext().getResources().getDisplayMetrics().density;
    }

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
    // Custom ActionBar
    // ================================================================================

    private void setupTopBarItems() {
        LinearLayout homeLinearLayout = (LinearLayout) findViewById(R.id.homeLinearLayout);
        homeLinearLayout.setOnClickListener(this);

        LinearLayout localLinearLayout = (LinearLayout) findViewById(R.id.localLinearLayout);
        localLinearLayout.setOnClickListener(this);

        LinearLayout worldLinearLayout = (LinearLayout) findViewById(R.id.worldLinearLayout);
        worldLinearLayout.setOnClickListener(this);

        LinearLayout locateLinearLayout = (LinearLayout) findViewById(R.id.locateLinearLayout);
        locateLinearLayout.setOnClickListener(this);

        mModeText = (TextView) findViewById(R.id.mapModeText);
        mSeekBar = (SeekBar) findViewById(R.id.mapSeekBar);
        mLastSeekValue = (mSeekBar.getProgress() * 1.0) / 100.0;

        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View clickedView) {
        switch (clickedView.getId()) {
            case R.id.homeLinearLayout:
                //Log.i(TAG, "Home Item Clicked");
                homeItemClicked();
                return;
            case R.id.localLinearLayout:
                //Log.i(TAG, "Local Item Clicked");
                localItemClicked();
                return;
            case R.id.worldLinearLayout:
                //Log.i(TAG, "World Item Clicked");
                worldItemClicked();
                return;
            case R.id.locateLinearLayout:
                //Log.i(TAG, "Locate linear layout");
                locateItemClicked();
                return;
            case R.id.zoomInButton:
                //Log.i(TAG, "Zoom In Button Clicked");
                zoomInButtonClicked();
                return;
            case R.id.zoomOutButton:
                //Log.i(TAG, "Zoom Out Button Clicked");
                zoomOutButtonClicked();
                return;
            case R.id.refreshButton:
                //Log.i(TAG, "Refresh Button Clicked");
                refreshButtonClicked();
                return;
            case R.id.searchKeyCancelClickableArea:
            case R.id.searchKeyCancelButton:
                //Log.i(TAG, "Search Key Cancel Button Clicked");
                searchKeyCancelButtonClicked();
                return;
            case R.id.sourceCancelButton:
            case R.id.sourceCancelClickableArea:
                //Log.i(TAG, "Source Cancel Button Clicked");
                sourceCancelButtonClicked();
                return;
        }
    }

    private void homeItemClicked() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                GENERAL_PREFS, 0);
        float homeLatBot = sharedPreferences
                .getFloat(HOME_LAT_BOT, (float) 0.0);
        float homeLatTop = sharedPreferences
                .getFloat(HOME_LAT_TOP, (float) 0.0);
        float homeLonLeft = sharedPreferences.getFloat(HOME_LON_LEFT,
                (float) 0.0);
        float homeLonRight = sharedPreferences.getFloat(HOME_LON_RIGHT,
                (float) 0.0);

        if (Math.abs(homeLatBot + homeLatTop + homeLonLeft + homeLonRight) < .001)
            return;

        try {
            LatLng addressSouthWest = new LatLng(homeLatBot, homeLonLeft);
            LatLng addressNorthEast = new LatLng(homeLatTop, homeLonRight);

            LatLngBounds addressBounds = new LatLngBounds(addressSouthWest,
                    addressNorthEast);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                    addressBounds, 0);


            if (!firstCameraChange)
                mMap.animateCamera(cameraUpdate);
            else
                mMap.moveCamera(cameraUpdate);
        } catch (IllegalArgumentException e) {
            // invalid
        }
    }

    private void localItemClicked() {
        Location localLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (localLocation == null) {
            localLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (localLocation == null)
                return; //Should put some type of can not determine location message
        }

        LatLng localLatLng = new LatLng(localLocation.getLatitude(), localLocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(localLatLng, 13);
        mZoomToArea = true;
        mMap.animateCamera(cameraUpdate);
    }

    private void worldItemClicked() {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
    }

    private void locateItemClicked() {
        mLocateMenuItem.expandActionView();
    }


    // ================================================================================
    //  On Long Click Listener
    // ================================================================================
    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.sourceCancelClickableArea:
            case R.id.sourceCancelButton:
                setAllSourcesMostRecent();
                return true;

        }
        return false;
    }

    private void setAllSourcesMostRecent() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());

        ArrayList<Source> allSources = applicationState.getAllSources();
        for (Source currentSource : allSources)
            currentSource.setSelected(false);

        ArrayList<Source> feedSources = applicationState.getFeedSources();
        for (Source currentSource : feedSources)
            currentSource.setSelected(false);

        ArrayList<Source> languageSources = applicationState.getLanguageSources();
        for (Source currentSource : languageSources)
            currentSource.setSelected(false);

        mSelectedSourceIndex = 0;
        mSelectedSourceType = SourceType.ALL_SOURCE;
        allSources.get(0).setSelected(true);

        applicationState.setAllSources(allSources);
        applicationState.setFeedSources(feedSources);
        applicationState.setLanguageSources(languageSources);

        updateSourceText();
    }

    // ================================================================================
    // ActionBar
    // ================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        dismissLocationSearchView();

        switch (item.getItemId()) {
            case R.id.menu_layers:
                //Log.i(TAG, "Layers Item Clicked");
                layersItemClicked();
                return true;
            case R.id.menu_search_key:
                //Log.i(TAG, "Search Key Item Clicked");
                searchKeyItemClicked();
                return true;
            case R.id.menu_mode:
                //Log.i(TAG, "Mode Item Clicked");
                modeItemClicked();
                return true;
            case R.id.menu_sources:
                //Log.i(TAG, "Sources Item Clicked");
                sourcesItemClicked();
                return true;
            case R.id.menu_top_stories:
                //Log.i(TAG, "Top Stories Item Clicked");
                topStoriesItemClicked();
                return true;
            case R.id.menu_about:
                //Log.i(TAG, "About Item Clicked");
                aboutItemClicked();
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

    private void aboutItemClicked() {
        FragmentManager manager = getFragmentManager();
        AboutDialogFragment aboutFragment = new AboutDialogFragment();
        aboutFragment.show(manager, "about_fragment");
    }

    private void filtersItemClicked() {
        Intent intent = new Intent(this, FiltersActivity.class);
        intent.putExtra(TITLE, "Map");
        startActivity(intent);
    }

    private void layersItemClicked() {
        FragmentManager manager = getFragmentManager();
        LayersAlertDialogFragment layersAlert = new LayersAlertDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("position", mLayerSelected);
        layersAlert.setArguments(bundle);

        layersAlert.show(manager, "layers_alert_fragment");
    }

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
                refreshMap();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void modeItemClicked() {
        FragmentManager manager = getFragmentManager();
        ModeAlertDialogFragment modeAlert = new ModeAlertDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("position", mStandMode.getValue());
        modeAlert.setArguments(bundle);

        modeAlert.show(manager, "mode_alert_fragment");
    }

    private void sourcesItemClicked() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        ArrayList<Source> allSources = applicationState.getAllSources();
        ArrayList<Source> languageSources = applicationState.getLanguageSources();
        ArrayList<Source> feedSources = applicationState.getFeedSources();
        Intent intent = new Intent(this, SourcesActivity.class);
        //Log.i(TAG, Integer.toString(languageSources.size()));
        intent.putExtra(TITLE, "Map");
        intent.putExtra("allSources", allSources);
        intent.putExtra("languageSources", languageSources);
        intent.putExtra("feedSources", feedSources);
        startActivity(intent);
    }

    private void topStoriesItemClicked() {
        Intent intent = new Intent(this, TopStoriesActivity.class);
        startActivity(intent);
    }

    private void settingsItemClicked() {
        Intent intent = new Intent(this, SettingsActivity.class);
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        intent.putExtra(TITLE, "Map");
        intent.putExtra(MAP_LAT_BOT, bounds.southwest.latitude);
        intent.putExtra(MAP_LAT_TOP, bounds.northeast.latitude);
        intent.putExtra(MAP_LON_LEFT, bounds.southwest.longitude);
        intent.putExtra(MAP_LON_RIGHT, bounds.northeast.longitude);

        startActivity(intent);
    }

    private void setupLocationSearchView() {
        mLocationSearchView.setSearchableInfo(mSearchManager
                .getSearchableInfo(getComponentName()));
        mLocationSearchView.setIconifiedByDefault(false);
        mLocationSearchView.setSubmitButtonEnabled(true);
        mLocationSearchActivity = new LocationSearchActivity(this);
        mLocationSearchView.setOnQueryTextListener(mLocationSearchActivity);
    }

    public void dismissLocationSearchView() {
        mLocateMenuItem.collapseActionView();
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

    // ================================================================================
    // Layers Interface
    // ================================================================================
    @Override
    public void onLayersPositiveClick(int position) {
        if (mLayerSelected != position) {
            mPreviousLayerSelected = mLayerSelected;
            mLayerSelected = position;
            refreshMap();
        }
    }

    // ================================================================================
    // Mode Interface
    // ================================================================================

    private void updateButtonsForMode() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());
        ArrayList<Source> allSources = applicationState.getAllSources();

        if (mStandMode.equals(StandMode.NEWSSTAND)) {
            mSourceTextView.setVisibility(View.VISIBLE);
            if (!allSources.get(0).isSelected()) {
                mSourceCancelButton.setVisibility(View.VISIBLE);
                mSourceCancelClickableArea.setVisibility(View.VISIBLE);
            }

            mMenu.findItem(R.id.menu_layers).setEnabled(true);
            mMenu.findItem(R.id.menu_sources).setEnabled(true);
            mMenu.findItem(R.id.menu_top_stories).setEnabled(true);

        } else if (mStandMode.equals(StandMode.TWITTERSTAND)) {
            mSourceTextView.setVisibility(View.GONE);
            mSourceCancelButton.setVisibility(View.GONE);
            mSourceCancelClickableArea.setVisibility(View.GONE);

            mMenu.findItem(R.id.menu_layers).setEnabled(true);
            mMenu.findItem(R.id.menu_sources).setEnabled(false);
            mMenu.findItem(R.id.menu_top_stories).setEnabled(true);
        } else if (mStandMode.equals(StandMode.PHOTOSTAND)) {
            mSourceTextView.setVisibility(View.VISIBLE);
            if (!allSources.get(0).isSelected()) {
                mSourceCancelButton.setVisibility(View.VISIBLE);
                mSourceCancelClickableArea.setVisibility(View.VISIBLE);
            }

            mMenu.findItem(R.id.menu_layers).setEnabled(false);
            mMenu.findItem(R.id.menu_sources).setEnabled(true);
            mMenu.findItem(R.id.menu_top_stories).setEnabled(false);
        }
    }

    @Override
    public void onModePositiveClick(int position) {
        if (mStandMode.getValue() != position) {
            mPreviousStandMode = mStandMode;
            mStandMode = mStandModeValues[position];
            mModeText.setText(modes[position]);

            if (mGridCellMarkers != null)
                mGridCellMarkers.clear();

            updateButtonsForMode();
            refreshMap();
        }
    }

    // ================================================================================
    // Map Initialization
    // ================================================================================

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            //Log.i(TAG, "Setup Map - map is null");
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();

                Location localLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (localLocation == null) {
                    localLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (localLocation == null)
                        return; //Should put some type of can not determine location message
                }

                LatLng localLatLng = new LatLng(localLocation.getLatitude(), localLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(localLatLng, 1);
                mMap.animateCamera(cameraUpdate);
            }
        }

        mLabelColor = getResources().getColor(R.color.green_label);
        mShadowColor = getResources().getColor(R.color.black);
    }

    private void setIconBitmaps() {
        generalBitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_general_original);
        businessBitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_business_original);
        entertainmentBitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_entertainment_original);
        healthBitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_health_original);
        sciTechBitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_scitech_original);
        sportsBitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_sports_original);
    }

    private void setUpMap() {
        mMap.setOnCameraChangeListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        mMap.getUiSettings().setZoomControlsEnabled(false); // Will use custom

        setIconBitmaps();
        setupMapOverlays();
    }

    private void setupMapOverlays() {
        mZoomInButton = (ImageButton) findViewById(R.id.zoomInButton);
        mZoomOutButton = (ImageButton) findViewById(R.id.zoomOutButton);
        mRefreshButton = (ImageButton) findViewById(R.id.refreshButton);

        mZoomInButton.setOnClickListener(this);
        mZoomOutButton.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);

        mSearchKeyTextView = (TextView) findViewById(R.id.searchKeyText);
        mSearchKeyCancelButton = (ImageButton) findViewById(R.id.searchKeyCancelButton);
        mSearchKeyCancelClickableArea = (ImageButton) findViewById(R.id.searchKeyCancelClickableArea);
        mSearchKeyCancelButton.setOnClickListener(this);
        mSearchKeyCancelClickableArea.setOnClickListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.GENERAL_PREFS, 0);
        mSearchKey = sharedPreferences.getString(SEARCH_KEY, "");

        mSourceTextView = (TextView) findViewById(R.id.sourceText);
        mSourceCancelButton = (ImageButton) findViewById(R.id.sourceCancelButton);
        mSourceCancelClickableArea = (ImageButton) findViewById(R.id.sourceCancelClickableArea);
        mSourceCancelButton.setOnClickListener(this);
        mSourceCancelClickableArea.setOnClickListener(this);
        mSourceCancelButton.setOnLongClickListener(this);
        mSourceCancelClickableArea.setOnLongClickListener(this);

        mMap.setInfoWindowAdapter(new MapInfoWindowAdapter(getLayoutInflater()));
    }

    // ================================================================================
    // Window Grid
    // ================================================================================

    private double lat2y(double lat) {
        return 180 / Math.PI * Math.log(Math.tan(Math.PI / 4 + lat * (Math.PI / 180) / 2));
    }

    private double y2lat(double y) {
        return 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
    }

    private void setDeviceXYBounds() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng southWest = bounds.southwest;
        LatLng northEast = bounds.northeast;

        double lowY = lat2y(southWest.latitude);
        double highY = lat2y(northEast.latitude);

        double lowX = southWest.longitude;
        double highX = northEast.longitude;

        if (highY < lowY) {
            double temp = lowY;
            lowY = highY;
            highY = temp;
        }

        float xDif = (float) Math.abs(highX - lowX);
        float yDif = (float) Math.abs(highY - lowY);

        if (xDif < yDif) { // Device is in Portrait
            portraitGrid = new DeviceGrid(new PointF(xDif, yDif));
            landscapeGrid = new DeviceGrid(new PointF(yDif, xDif));
        } else { // Device is in Landscape
            portraitGrid = new DeviceGrid(new PointF(yDif, xDif));
            landscapeGrid = new DeviceGrid(new PointF(xDif, yDif));
        }

        //Log.i(TAG, "set device xy bounds");
    }

    private PointF convertLatLngToPoint(LatLng latLngPoint) {
        float x = (float) latLngPoint.longitude + 180.0f;
        float y = (float) lat2y(latLngPoint.latitude) + 180.0f;
        return new PointF(x, y);
    }

    private PointF getCurrentXYBounds() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng southwest = bounds.southwest;
        LatLng northeast = bounds.northeast;

        if (southwest.latitude > 85.0511)
            southwest = new LatLng(85.0511, southwest.longitude);
        if (southwest.latitude < -85.0511)
            southwest = new LatLng(-85.0511, southwest.longitude);

        if (northeast.latitude > 85.0511)
            northeast = new LatLng(85.0511, northeast.longitude);
        if (northeast.latitude < -85.0511)
            northeast = new LatLng(-85.0511, northeast.longitude);

        double lowY = lat2y(southwest.latitude);
        double highY = lat2y(northeast.latitude);

        double lowX = southwest.longitude;
        double highX = northeast.longitude;

        if (highY < lowY) {
            double temp = lowY;
            lowY = highY;
            highY = temp;
        }

        float xDif = (float) Math.abs(highX - lowX);
        float yDif = (float) Math.abs(highY - lowY);

        return new PointF(xDif, yDif);
    }

    // ================================================================================
    // Map Overlays
    // ================================================================================

    private void zoomInButtonClicked() {
        float zoomTo = mMap.getCameraPosition().zoom + 2;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomTo));
    }

    private void zoomOutButtonClicked() {
        float zoomTo = mMap.getCameraPosition().zoom - 2;
        if (zoomTo < 2)
            zoomTo = 2.0f;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomTo));
    }

    private void refreshButtonClicked() {
        refreshMap();
    }

    private void searchKeyCancelButtonClicked() {
        mSearchKeyTextView.setVisibility(View.GONE);
        mSearchKeyCancelButton.setVisibility(View.GONE);
        mSearchKeyCancelClickableArea.setVisibility(View.GONE);
        mSearchKeyTextView.setText("");
        mSearchKey = "";
        refreshMap();
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

        editor.putString(SEARCH_KEY, mSearchKeyTextView.getText().toString());
    }

    private void updateHandMode() {
        mLastHandMode = mHandMode;

        RelativeLayout.LayoutParams zoomInParams = (RelativeLayout.LayoutParams) mZoomInButton.getLayoutParams();
        RelativeLayout.LayoutParams zoomOutParams = (RelativeLayout.LayoutParams) mZoomOutButton.getLayoutParams();
        RelativeLayout.LayoutParams refreshParams = (RelativeLayout.LayoutParams) mRefreshButton.getLayoutParams();

        if (mHandMode == 3) { // Neutral
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            zoomInParams.rightMargin = (int) getResources().getDimension(R.dimen.neutral_zoom_right);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            zoomOutParams.rightMargin = (int) getResources().getDimension(R.dimen.neutral_zoom_right);
            refreshParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            refreshParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            refreshParams.leftMargin = (int) getResources().getDimension(R.dimen.neutral_refresh_left);
        } else if (mHandMode == 1) { // Left Hand
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            zoomInParams.rightMargin = (int) getResources().getDimension(R.dimen.leftHand_zoomIn_right);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            zoomOutParams.rightMargin = (int) getResources().getDimension(R.dimen.leftHand_zoomOut_right);
            refreshParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            refreshParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            refreshParams.leftMargin = (int) getResources().getDimension(R.dimen.leftHand_refresh_left);
        } else if (mHandMode == 2) { // Right Hand
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            zoomInParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            zoomInParams.leftMargin = (int) getResources().getDimension(R.dimen.rightHand_zoomIn_left);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            zoomOutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            zoomOutParams.leftMargin = (int) getResources().getDimension(R.dimen.rightHand_zoomOut_left);
            refreshParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            refreshParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            refreshParams.rightMargin = (int) getResources().getDimension(R.dimen.rightHand_refresh_right);
        }

        RelativeLayout mainRelativeLayout = (RelativeLayout) findViewById(R.id.mainRelativeLayout);

        mainRelativeLayout.removeView(mZoomInButton);
        mainRelativeLayout.removeView(mZoomOutButton);
        mainRelativeLayout.removeView(mRefreshButton);

        mZoomInButton.setLayoutParams(zoomInParams);
        mZoomOutButton.setLayoutParams(zoomOutParams);
        mRefreshButton.setLayoutParams(refreshParams);

        mainRelativeLayout.addView(mZoomInButton);
        mainRelativeLayout.addView(mZoomOutButton);
        mainRelativeLayout.addView(mRefreshButton);

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

        refreshMap();
        updateSourceText();
    }

    // ================================================================================
    // Map Updates
    // ================================================================================

    // Called when the camera changes
    @Override
    public void onCameraChange(CameraPosition position) {
        if (firstCameraChange) {
            //Log.i(TAG, "first camera change");
            setDeviceXYBounds();
            homeItemClicked();
            firstCameraChange = false;
            refreshMap();
        } else
            refreshMap();
    }

    public void updateMapToAddress(GeocoderPlusAddress foundAddress) {
        LatLng addressSouthWest = new LatLng(foundAddress.getViewPort()
                .getSouthWest().getLatitude(), foundAddress.getViewPort()
                .getSouthWest().getLongitude());
        LatLng addressNorthEast = new LatLng(foundAddress.getViewPort()
                .getNorthEast().getLatitude(), foundAddress.getViewPort()
                .getNorthEast().getLongitude());

        LatLngBounds addressBounds = new LatLngBounds(addressSouthWest,
                addressNorthEast);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                addressBounds, 0);

        mZoomToArea = true;
        mMap.animateCamera(cameraUpdate);
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

        if (mLayerSelected > 0) {
            if (mLayerSelected == 1)  // Disease
                paramString += "&layer=2";
            else if (mLayerSelected == 2) // Keyword
                paramString += "&layer=0";
            else if (mLayerSelected == 4)   // People
                paramString += "&layer=1";
            else if (mLayerSelected == 5)
                paramString += "&layer=4";
            // paramString += "&morton=1&zoom=" + mMap.getCameraPosition().zoom;
        }

        if (mStandMode == StandMode.PHOTOSTAND) {
            paramString += "&stand=2";
            paramString += "&zoom=" + (int) (mMap.getCameraPosition().zoom + 1);
        }

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
    // Marker Calls
    // ================================================================================

    private void updatePredictionMarkers() {
        if (mPredictions != null) {
            for (DeviceGridCell predictionGridCell : mPredictions) {
                boolean foundMarkers = false;
                for (DeviceGridCellMarker deviceGridCellMarker : mGridCellMarkers) {
                    if (deviceGridCellMarker.equals(predictionGridCell)) {
                        foundMarkers = true;
                        break;
                    }
                }

                if (!foundMarkers) {
                    DeviceGridCellMarker gridCellMarker = new DeviceGridCellMarker(predictionGridCell, mConstraints);
                    mGridCellMarkers.add(gridCellMarker);
                }
            }
        }
    }


    private void refreshMap() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        mMapLonLeft = bounds.southwest.longitude;
        mMapLatBot = bounds.southwest.latitude;
        mMapLonRight = bounds.northeast.longitude;
        mMapLatTop = bounds.northeast.latitude;

        String filtersString = getPreferencesString();
        String sourcesString = getSourcesString();

        if (mMapLonLeft > mMapLonRight) {
            double temp = mMapLonLeft;
            mMapLonLeft = mMapLonRight;
            mMapLonRight = temp;
        }

        String constraints = filtersString + sourcesString;

        DeviceGrid deviceGrid;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            deviceGrid = landscapeGrid;
        } else {
            deviceGrid = portraitGrid;
        }

        LatLng southwest = bounds.southwest;
        LatLng northeast = bounds.northeast;

        if (southwest.latitude > 85.0511)
            southwest = new LatLng(85.0511, southwest.longitude);
        if (southwest.latitude < -85.0511)
            southwest = new LatLng(-85.0511, southwest.longitude);

        if (northeast.latitude > 85.0511)
            northeast = new LatLng(85.0511, northeast.longitude);
        if (northeast.latitude < -85.0511)
            northeast = new LatLng(-85.0511, northeast.longitude);

        mWindowGridCells = deviceGrid.getGridCellsForWindowAndZoom(convertLatLngToPoint(southwest),
                convertLatLngToPoint(northeast), getCurrentXYBounds());

        DeviceGridCell[] predictionsAndWindow = mPrediction.makePrediction(mWindowGridCells, K, getCurrentXYBounds(), mCacheSize);

        mPredictions = new ArrayList<DeviceGridCell>();
        for (DeviceGridCell predictionGridCell : predictionsAndWindow) {
            boolean found = false;
            for (DeviceGridCell windowGridCell : mWindowGridCells) {
                if (windowGridCell != null && predictionGridCell != null && predictionGridCell.equals(windowGridCell))
                    found = true;
            }
            if (!found)
                mPredictions.add(predictionGridCell);
        }


        if (mGridCellMarkers != null) {
            // Clear cache if new constraints
            if ((mConstraints != null && !constraints.equals(mConstraints))
                    || mPreviousLayerSelected != mLayerSelected) {
                mGridCellMarkers.clear();
                //Log.i(TAG, "Clear grid cell markers");
            }

            // First remove all grid cell markers that have a time over MAX_CACHE_SECONDS
            ArrayList<DeviceGridCellMarker> recentGridCellMarkers = new ArrayList<DeviceGridCellMarker>();
            long currentTimeSeconds = System.currentTimeMillis() / 1000;

            for (DeviceGridCellMarker gridCellMarker : mGridCellMarkers) {
                if (currentTimeSeconds - gridCellMarker.getDownloadTime() < MAX_CACHE_SECONDS ||
                        gridCellMarker.getState() == DeviceGridCellMarkerState.DOWNLOADING)
                    recentGridCellMarkers.add(gridCellMarker);
            }

            mGridCellMarkers = recentGridCellMarkers;

            ArrayList<MapMarker> cachedMarkers = new ArrayList<MapMarker>();
            boolean foundAllMarkers = true;

            ArrayList<DeviceGridCellMarker> currentWindowGridCellMarkers = new ArrayList<DeviceGridCellMarker>();
            // Check if each grid cell has markers already in cache
            for (DeviceGridCell gridCell : mWindowGridCells) {
                boolean foundMarkers = false;
                boolean isDownloading = false;
                for (DeviceGridCellMarker deviceGridCellMarker : mGridCellMarkers) {
                    if (deviceGridCellMarker.equals(gridCell)) {
                        if (deviceGridCellMarker.getState() == DeviceGridCellMarkerState.DOWNLOADING) {
                            isDownloading = true;
                            //sendKeenMapQueryRecord(new QueryRecord(deviceGridCellMarker), "downloading", null);
                        } else {
                            if (deviceGridCellMarker != null && deviceGridCellMarker.getMarkers() != null) {
                                cachedMarkers.addAll(deviceGridCellMarker.getMarkers());
                                foundMarkers = true;
                                //sendKeenMapQueryRecord(new QueryRecord(deviceGridCellMarker), "cached", deviceGridCellMarker.getMarkers());
                            }
                        }
                        deviceGridCellMarker.setCurrentWindow(true);
                        break;
                    }
                }
                if (!foundMarkers) {
                    if (!isDownloading) {
                        DeviceGridCellMarker gridCellMarker = new DeviceGridCellMarker(gridCell, constraints, true);
                        mGridCellMarkers.add(gridCellMarker);
                        currentWindowGridCellMarkers.add(gridCellMarker);
                    }
                    foundAllMarkers = false;
                    retrievedCurrentWindow = false;
                }
            }

            if (foundAllMarkers) {
                cachedMarkers = markersInWindow(cachedMarkers);
                Collections.sort(cachedMarkers, new Comparator<MapMarker>() {
                    @Override
                    public int compare(MapMarker lhs, MapMarker rhs) {
                        return Float.valueOf(lhs.getCluster_score()).compareTo(Float.valueOf(rhs.getCluster_score()));
                    }
                });
                retrievedCurrentWindow = true;
                if (K > 0)
                    updatePredictionMarkers();
                updateMarkers(cachedMarkers);
            }
        } else {
            retrievedCurrentWindow = false;
            mGridCellMarkers = new ArrayList<DeviceGridCellMarker>();
            for (DeviceGridCell gridCell : mWindowGridCells) {
                DeviceGridCellMarker gridCellMarker = new DeviceGridCellMarker(gridCell, constraints, true);
                mGridCellMarkers.add(gridCellMarker);
            }
        }

        mConstraints = constraints;
    }

    // TODO: Fix to check for PhotoStand

    private boolean mapUpdateHasMarker(Marker currentMarker,
                                       ArrayList<MapMarker> updatedMapMarkers) {
        if (updatedMapMarkers == null)
            return false;
        if (mStandMode != StandMode.PHOTOSTAND) {
            for (MapMarker currentMapMarker : updatedMapMarkers) {
                if (currentMapMarker.getName().equalsIgnoreCase(
                        currentMarker.getTitle())
                        && currentMapMarker.getTitle().equalsIgnoreCase(
                        currentMarker.getSnippet())
                        && Math.abs(currentMapMarker.getLatitude()
                        - currentMarker.getPosition().latitude) < .001
                        && Math.abs(currentMapMarker.getLongitude()
                        - currentMarker.getPosition().longitude) < .001)
                    return true;
            }
        } else { // Check for PhotoStand Marker
            for (MapMarker currentMapMarker : updatedMapMarkers) {
                if (currentMapMarker.getName().equalsIgnoreCase(
                        currentMarker.getTitle())
                        && currentMapMarker.getCaption().equalsIgnoreCase(
                        currentMarker.getSnippet())
                        && Math.abs(currentMapMarker.getLatitude()
                        - currentMarker.getPosition().latitude) < .001
                        && Math.abs(currentMapMarker.getLongitude()
                        - currentMarker.getPosition().longitude) < .001)

                    return true;
            }
        }
        return false;
    }

    private boolean mapUpdateHasMarker(MapMarker currentMapMarker) {
        for (Marker currentMarker : mMarkers) {
            if (currentMapMarker.getName().equalsIgnoreCase(
                    currentMarker.getTitle())
                    && currentMapMarker.getTitle().equalsIgnoreCase(
                    currentMarker.getSnippet())
                    && Math.abs(currentMapMarker.getLatitude()
                    - currentMarker.getPosition().latitude) < .001
                    && Math.abs(currentMapMarker.getLongitude()
                    - currentMarker.getPosition().longitude) < .001)

                return true;
        }

        return false;
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

        return topicBitmapDescriptor;
    }

    public void gridMarkersDidComplete(DeviceGridCellMarker deviceGridCellMarker) {
        if (retrievedCurrentWindow)
            return;

        ArrayList<MapMarker> updatedMapMarkers = new ArrayList<MapMarker>();

        for (DeviceGridCell gridCell : mWindowGridCells) {
            ArrayList<MapMarker> foundMarkers = null;
            for (DeviceGridCellMarker gridCellMarker : mGridCellMarkers) {
                if (gridCellMarker.equals(gridCell) && gridCellMarker.getState() == DeviceGridCellMarkerState.COMPLETED) {
                    foundMarkers = gridCellMarker.getMarkers();
                    break;
                }
            }
            if (foundMarkers == null) {
                return; // Did not get all grid cell markers yet
            }

            updatedMapMarkers.addAll(foundMarkers);
        }

        retrievedCurrentWindow = true;

        updatedMapMarkers = markersInWindow(updatedMapMarkers);
        Collections.sort(updatedMapMarkers, new Comparator<MapMarker>() {
            @Override
            public int compare(MapMarker lhs, MapMarker rhs) {
                return Float.valueOf(lhs.getCluster_score()).compareTo(Float.valueOf(rhs.getCluster_score()));
            }
        });

        updateMarkers(updatedMapMarkers);
        if (K > 0)
            updatePredictionMarkers();
    }

    private ArrayList<MapMarker> markersInWindow(ArrayList<MapMarker> mapMarkers) {
        ArrayList<MapMarker> containedMarkers = new ArrayList<MapMarker>();

        LatLng southwest = mMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northeast = mMap.getProjection().getVisibleRegion().latLngBounds.northeast;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(southwest);
        builder.include(northeast);
        LatLngBounds windowBounds = builder.build();

        for (MapMarker currentMarker : mapMarkers) {
            if (windowBounds.contains(new LatLng(currentMarker.getLatitude(), currentMarker.getLongitude())))
                containedMarkers.add(currentMarker);
        }

        return containedMarkers;
    }

    private Rect getRectForMarker(String markerName, LatLng latLng) {
        Paint paint = new Paint();
        float scaledPixels = TEXT_MARKER_SIZE * mDensityMultiplier;
        paint.setTextSize(scaledPixels);
        paint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        paint.setColor(mLabelColor);
        paint.setShadowLayer(2, 2, 2, mShadowColor);
        Rect textRect = new Rect();
        paint.getTextBounds(markerName, 0, markerName.length(), textRect);

        /*
           The following is taken from
           http://stackoverflow.com/questions/7549182/android-paint-measuretext-vs-gettextbounds
        */

        float measuredText = paint.measureText(markerName);
        int bw = textRect.width();
        textRect.offset(0, -textRect.top);
        paint.setStyle(Paint.Style.STROKE);

        /*
           Determine marker bounds from text and the location of the lat, lon in relation
           to the screen. Note that top must be <= bottom for Rect function
        */
        Rect markerBoundingRegion = new Rect();
        Point screenPoint = mMap.getProjection().toScreenLocation(latLng);
        markerBoundingRegion.set((int) (screenPoint.x - measuredText / 2.0), screenPoint.y - textRect.height(),
                (int) (screenPoint.x + measuredText / 2.0), screenPoint.y + textRect.height());

        return markerBoundingRegion;
    }

    private void zoomOutOneLevel() {
        float zoomTo = mMap.getCameraPosition().zoom - 2;
        if (zoomTo > 2)
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomTo));
        else
            mZoomToArea = false;
    }

    private void updateMarkers(ArrayList<MapMarker> updatedMapMarkers) {
        if (mZoomToArea) {
            if (updatedMapMarkers == null || updatedMapMarkers.size() < 1)
                zoomOutOneLevel();
            else
                mZoomToArea = false;
        }

        // If layer or mode changed then remove all markers
        boolean layerOrModeChanged = (mPreviousLayerSelected != mLayerSelected)
                || (mPreviousStandMode != mStandMode);

        mPreviousLayerSelected = mLayerSelected;
        mPreviousStandMode = mStandMode;

        if (mMarkers != null && mMarkers.size() > 0) {
            ArrayList<Marker> markersToRemove = new ArrayList<Marker>();
            for (Marker currentMarker : mMarkers) {
                if (layerOrModeChanged || !mapUpdateHasMarker(currentMarker, updatedMapMarkers)) {
                    currentMarker.remove();
                    markersToRemove.add(currentMarker);
                }
            }
            for (Marker currentRemoveMarker : markersToRemove)
                mMarkers.remove(currentRemoveMarker);
        } else {
            mMarkers = new ArrayList<Marker>();
        }

        ArrayList<Rect> boundingRects = new ArrayList<Rect>();

        if (mStandMode != StandMode.PHOTOSTAND) {
            for (MapMarker currentMapMarker : updatedMapMarkers) {
                if (layerOrModeChanged || !mapUpdateHasMarker(currentMapMarker)) {
                    Marker currentMarker = null;
                    if (mLayerSelected == 0) {
                        BitmapDescriptor currentBitmapDescriptor =
                                bitmapDescriptorForTopic(currentMapMarker.getTopic());
                        currentMarker = mMap.addMarker(new MarkerOptions()
                                .icon(currentBitmapDescriptor)
                                .position(
                                        new LatLng(currentMapMarker.getLatitude(),
                                                currentMapMarker.getLongitude()))
                                .title(currentMapMarker.getName())
                                .snippet(currentMapMarker.getTitle())
                                .visible(false));
                    } else {
                        String markerName = currentMapMarker.getKeyword();

                        if (mLayerSelected == 3) {
                            markerName = currentMapMarker.getName();
                        }

                        if (markerName == null)
                            continue;

                        Paint paint = new Paint();
                        float scaledPixels = TEXT_MARKER_SIZE * mDensityMultiplier;
                        paint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
                        paint.setTextSize(scaledPixels);
                        paint.setColor(mLabelColor);
                        paint.setShadowLayer(2, 2, 2, mShadowColor);
                        Rect textRect = new Rect();
                        paint.getTextBounds(markerName, 0, markerName.length(), textRect);

                        // The following is taken from
                        // http://stackoverflow.com/questions/7549182/android-paint-measuretext-vs-gettextbounds

                        float measuredText = paint.measureText(markerName);
                        int bw = textRect.width();
                        textRect.offset(0, -textRect.top);
                        paint.setStyle(Paint.Style.STROKE);

                        // Determine marker bounds from text and the location of the lat, lon in relation
                        // to the screen. Note that top must be <= bottom for Rect function

                        Rect markerBoundingRegion = new Rect();
                        Point screenPoint = mMap.getProjection().toScreenLocation(new LatLng(currentMapMarker.getLatitude(),
                                currentMapMarker.getLongitude()));
                        markerBoundingRegion.set((int) (screenPoint.x - measuredText / 2.0), screenPoint.y - textRect.height(),
                                (int) (screenPoint.x + measuredText / 2.0), screenPoint.y + textRect.height());

                        boolean intersects = false;

                        for (Rect currentBoundingRect : boundingRects)
                            if (Rect.intersects(markerBoundingRegion, currentBoundingRect)) {
                                intersects = true;
                                break;
                            }

                        if (!intersects) {
                            Bitmap.Config conf = Bitmap.Config.ARGB_8888;

                            int textHeight = (int) Math.round(2.0 * (textRect.height()));
                            if (textHeight < 50)
                                textHeight = 50;
                            Bitmap bmp = Bitmap.createBitmap((int) measuredText,
                                    textHeight, conf);
                            Canvas canvas = new Canvas(bmp);

                            // second was at 50
                            canvas.drawText(markerName, 0, 50, paint);
                            currentMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(currentMapMarker.getLatitude(),
                                            currentMapMarker.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                                    .anchor(0.5f, 1)
                                    .title(currentMapMarker.getName())
                                    .snippet(currentMapMarker.getTitle())
                                    .visible(false));

                            boundingRects.add(markerBoundingRegion);
                        }
                    }

                    if (currentMarker != null) {
                        mMarkers.add(currentMarker);
                    }
                } else if (mLayerSelected != 0 && mapUpdateHasMarker(currentMapMarker)) {
                    String markerName = currentMapMarker.getKeyword();

                    if (mLayerSelected == 3) {
                        markerName = currentMapMarker.getName();
                    }

                    boundingRects.add(getRectForMarker(markerName, new LatLng(currentMapMarker.getLatitude(),
                            currentMapMarker.getLongitude())));

                }
            }
            mMapMarkers = updatedMapMarkers;
            updateMarkersFromSeekBar(mSeekBar.getProgress());
        } else {
            mImageURLs.clear();
            //mImageViews.clear();
            ArrayList<MapMarker> distinctiveMapMarkers = new ArrayList<MapMarker>();
            for (MapMarker currentMapMarker : updatedMapMarkers) {
                if (currentMapMarker.getDistinctiveness() >= 6) {
                    mImageURLs.add(currentMapMarker.getImg_url());
                    distinctiveMapMarkers.add(currentMapMarker);
                }
            }
            mMapMarkers = distinctiveMapMarkers;

            mImageURLs = new ArrayList<String>(new LinkedHashSet<String>(mImageURLs)); // remove duplicates
            for (String imageURL : mImageURLs) {
                imageLoader.loadImage(imageURL, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageLoadingCompleted(imageUri, loadedImage);
                    }
                });

            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int distinctiveness) {
        int pixels = mImageMarkerPixels;
        //if (distinctiveness == 9)
        //    pixels *= 2;

        return Bitmap.createScaledBitmap(bitmap, pixels, pixels, false);
    }

    private void imageLoadingCompleted(String url, Bitmap bitmap) {
        for (MapMarker currentMapMarker : mMapMarkers) {
            if (url.equals(currentMapMarker.getImg_url())) {
                Marker currentMarker;
                Bitmap markerBitmap = resizeBitmap(bitmap, currentMapMarker.getDistinctiveness());
                currentMarker = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                        .position(
                                new LatLng(currentMapMarker.getLatitude(),
                                        currentMapMarker.getLongitude()))
                        .title(currentMapMarker.getName())
                        .snippet(currentMapMarker.getCaption())
                        .visible(true));
                currentMapMarker.setVisible(true);

                if (currentMarker != null)
                    mMarkers.add(currentMarker);
            }
        }
    }

    // ================================================================================
    // Keen Analytics
    // ================================================================================
    private void setupKeen() {
        // initialize the Keen Client with your Project ID.
        KeenClient.initialize(getApplicationContext(), getResources().getString(R.string.keen_project_id),
                getResources().getString(R.string.keen_write_key), getResources().getString(R.string.keen_read_key));

        // Send device information
        Map<String, Object> event = new HashMap<String, Object>();
        event.put("os_version", System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")");
        event.put("api_level", android.os.Build.VERSION.SDK_INT);
        event.put("device", android.os.Build.DEVICE);
        event.put("model_product", android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")");

        // override the Keen timestamp
        Map<String, Object> keenProperties = new HashMap<String, Object>();
        keenProperties.put("timestamp", Calendar.getInstance());

        // add it to the "purchases" collection in your Keen Project
        try {
            KeenClient.client().addEvent("device_info", event, keenProperties);
            //Log.i(TAG, "Map window added to keen client");
        } catch (KeenException e) {
            // do nothing for now
            //Log.e(TAG, "KeenException thrown");
        } catch (IllegalStateException e) {
            //Log.e(TAG, "Keen IllegalStateException thrown");
        }

        uploadKeen();
    }

    private void uploadKeen() {
        // upload all captured events to Keen
        try {
            KeenClient.client().upload(new UploadFinishedCallback() {
                public void callback() {
                    // use this to notify yourself when the upload finishes, if you wish. we'll just log for now.
                    //Log.i(TAG, "Keen client has finished uploading!");
                }
            });
        } catch (Exception e) {
            //Log.e(TAG, "Keen Client upload exception");
        }
    }

    private Map<String, Object> topicRatio (ArrayList<MapMarker> markers) {
        Map<String, Object> ratiosMap = new HashMap<String, Object>();
        double numGeneral=0, numBusiness=0, numEntertainment=0, numHealth=0, numSciTech=0, numSports = 0, numInvalid=0;
        double numElms = markers.size();

        for (MapMarker mapMarker : markers) {
            String topic = "" + mapMarker.getTopic();

            if (topic.equals("General"))
                numGeneral++;
            else if (topic.equals("Sports"))
                numSports++;
            else if (topic.equals("Business"))
                numBusiness++;
            else if (topic.equals("Entertainment"))
                numEntertainment++;
            else if (topic.equals("SciTech"))
                numSciTech++;
            else if (topic.equals("Health"))
                numHealth++;
            else
                numInvalid++;
        }

        ratiosMap.put("%general", numGeneral/numElms);
        ratiosMap.put("%business", numBusiness/numElms);
        ratiosMap.put("%entertainment", numEntertainment/numElms);
        ratiosMap.put("%health", numHealth/numElms);
        ratiosMap.put("%scitech", numSciTech/numElms);
        ratiosMap.put("%sports", numSports/numElms);
        ratiosMap.put("%invalid", numInvalid/numElms);

        return ratiosMap;
    }

    private void sendKeenMapQueryRecord(QueryRecord mapQueryRecord, String roundTripTime, ArrayList<MapMarker> markers) {
        // create an event to eventually upload to Keen
        Map<String, Object> event = new HashMap<String, Object>();
        event.put("window", mapQueryRecord.toString());
        event.put("constraints", mConstraints);
        event.put("rtt", roundTripTime);

        Map<String, Object> topicRatio = null;
        if (markers != null && markers.size() > 0) {
            event.put("count", markers.size());
            topicRatio = topicRatio(markers);
            topicRatio.put("window", mapQueryRecord.toString());
        } else {
            event.put("count", 0);
        }
        // override the Keen timestamp
        Map<String, Object> keenProperties = new HashMap<String, Object>();
        keenProperties.put("timestamp", Calendar.getInstance());

        // add it to the map_window collection in your Keen Project
        try {
            KeenClient.client().addEvent("map_windows", event, keenProperties);
            if (topicRatio != null)
                KeenClient.client().addEvent("map_topic_ratio", topicRatio, keenProperties);
        } catch (KeenException e) {
            // do nothing for now
            //Log.e(TAG, "KeenException thrown");
        } catch (IllegalStateException e) {
            //Log.e(TAG, "Keen IllegalStateException thrown");
        }

        if (mWindowsSinceUpdate > 100) {
            uploadKeen();
            mWindowsSinceUpdate = 0;
        } else {
            mWindowsSinceUpdate++;
        }
    }

    // ================================================================================
    // Marker Click Listener
    // ================================================================================
    @Override
    public boolean onMarkerClick(Marker marker) {
        // Send Request to DB to improve response time
        MapMarker selectedMapMarker = getMapMarkerForMarker(marker);
        if (selectedMapMarker != null) {
            int gaz_id = selectedMapMarker.getGaz_id();
            String constraints = getSourcesString() + getPreferencesString();
            String locationURL = "";
            if (mStandMode == StandMode.NEWSSTAND) {
                locationURL = "http://newsstand.umiacs.umd.edu/news/xml_top_locations?gaz_id=";
            } else if (mStandMode == StandMode.TWITTERSTAND) {
                locationURL = "http://twitterstand.umiacs.umd.edu/news/xml_top_locations?gaz_id=";
            } else {
                locationURL = "http://newsstand.umiacs.umd.edu/news/xml_gaz_images?gaz_id=";
            }

            locationURL += gaz_id + constraints;
            NoResultsRequest locationRequest = new NoResultsRequest();
            locationRequest.execute(locationURL);
        }
        return false;
    }

    private MapMarker getMapMarkerForMarker(Marker currentMarker) {
        MapMarker selectedMapMarker = null;
        if (mMapMarkers != null) {
            if (mStandMode != StandMode.PHOTOSTAND) {
                for (MapMarker currentMapMarker : mMapMarkers) {
                    if (currentMapMarker != null && currentMapMarker.getName() != null
                            && currentMarker.getTitle() != null
                            && currentMapMarker.getName().equalsIgnoreCase(
                            currentMarker.getTitle())
                            && currentMarker.getTitle() != null
                            && currentMapMarker.getTitle().equalsIgnoreCase(
                            currentMarker.getSnippet())
                            && Math.abs(currentMapMarker.getLatitude()
                            - currentMarker.getPosition().latitude) < .001
                            && Math.abs(currentMapMarker.getLongitude()
                            - currentMarker.getPosition().longitude) < .001) {
                        selectedMapMarker = currentMapMarker;
                        break;
                    }
                }
            } else {
                for (MapMarker currentMapMarker : mMapMarkers) {
                    if (currentMapMarker != null
                            && currentMapMarker.getName() != null
                            && currentMarker.getTitle() != null
                            && currentMapMarker.getName().equalsIgnoreCase(
                            currentMarker.getTitle())
                            && currentMapMarker.getCaption().equalsIgnoreCase(
                            currentMarker.getSnippet())
                            && Math.abs(currentMapMarker.getLatitude()
                            - currentMarker.getPosition().latitude) < .001
                            && Math.abs(currentMapMarker.getLongitude()
                            - currentMarker.getPosition().longitude) < .001) {
                        selectedMapMarker = currentMapMarker;
                        break;
                    }
                }
            }
        }

        return selectedMapMarker;
    }

    // ================================================================================
    // Info Window Click Listener
    // ================================================================================
    // Open Location Activity for NewsStand & TwitterStand...Image Viewer for PhotoStand and TweetPhoto
    @Override
    public void onInfoWindowClick(Marker marker) {
        MapMarker selectedMapMarker = getMapMarkerForMarker(marker);
        if (selectedMapMarker != null) {
            if (mStandMode != StandMode.PHOTOSTAND) {
                Intent intent = new Intent(this, LocationActivity.class);
                intent.putExtra(GAZ_ID, selectedMapMarker.getGaz_id());
                intent.putExtra(CONSTRAINTS, mConstraints);
                intent.putExtra(TITLE, "Map");
                intent.putExtra(LOCATION_NAME, selectedMapMarker.getName());
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ImageGridActivity.class);
                intent.putExtra(MainActivity.GAZ_ID, selectedMapMarker.getGaz_id());
                intent.putExtra(MainActivity.TITLE, "Map");
                boolean isTwitterstand = (mStandMode == StandMode.TWITTERSTAND);
                intent.putExtra(MainActivity.TWITTERSTAND, isTwitterstand);
                intent.putExtra(MainActivity.CONSTRAINTS, mConstraints);
                intent.putExtra(MainActivity.LOCATION_NAME, selectedMapMarker.getName());
                startActivity(intent);
            }
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
    // Location Listener
    // ================================================================================
    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    // ================================================================================
    // Source Initialization
    // ================================================================================

    private void initializeSources() {
        NewsStandApplication applicationState = ((NewsStandApplication) getApplicationContext());

        ArrayList<Source> allSources = applicationState.getAllSources();
        ArrayList<Source> languageSources = applicationState.getLanguageSources();
        ArrayList<Source> feedSources = applicationState.getFeedSources();

        allSources = loadSourcesFromFile(ALL_SOURCES_FILENAME);
        if (allSources == null || allSources.size() == 0) {
            allSources = new ArrayList<Source>();
            allSources.add(new Source("Most Recent", Source.AllSourceType.MOST_RECENT));
            allSources.get(0).setSelected(true);
            allSources.add(new Source("Most Reputable", Source.AllSourceType.MOST_REPUTABLE));
            allSources.add(new Source("Real Time", Source.AllSourceType.REAL_TIME));
        }
        applicationState.setAllSources(allSources);

        languageSources = loadSourcesFromFile(LANGUAGE_SOURCES_FILENAME);
        if (languageSources == null || languageSources.size() == 0) {
            String languageSourcesURL = "http://newsstand.umiacs.umd.edu/news/get_language_sources_xml";
            DownloadSourcesXmlTask languageSourcesXmlTask = new DownloadSourcesXmlTask();
            languageSourcesXmlTask.setSourceType(SourceType.LANGUAGE_SOURCE);
            languageSourceAsyncTask = languageSourcesXmlTask.execute(languageSourcesURL);
        } else {
            applicationState.setLanguageSources(languageSources);
        }

        feedSources = loadSourcesFromFile(FEED_SOURCES_FILENAME);
        if (feedSources == null || feedSources.size() == 0) {
            String feedSourcesURL = "http://newsstand.umiacs.umd.edu/news/get_featured_sources_xml";
            DownloadSourcesXmlTask feedSourcesXmlTask = new DownloadSourcesXmlTask();
            feedSourcesXmlTask.setSourceType(SourceType.FEED_SOURCE);
            feedSourceAsyncTask = feedSourcesXmlTask.execute(feedSourcesURL);
        } else {
            applicationState.setFeedSources(feedSources);
        }

        mStandMode = StandMode.NEWSSTAND;
        mStandModeValues = StandMode.values();
    }

    private void setLanguageSourcesFound(ArrayList<Source> languageSourcesFound) {
        NewsStandApplication appState = ((NewsStandApplication) getApplicationContext());
        appState.setLanguageSources(languageSourcesFound);

        languageSourceAsyncTask.cancel(true);
        languageSourceAsyncTask = null;
    }

    private void setFeedSourcesFound(ArrayList<Source> feedSourcesFound) {
        NewsStandApplication appState = ((NewsStandApplication) getApplicationContext());
        appState.setFeedSources(feedSourcesFound);

        feedSourceAsyncTask.cancel(true);
        feedSourceAsyncTask = null;
    }


    // ================================================================================
    // Activity Lifecycle
    // ================================================================================
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        SharedPreferences sharedPreferences = getSharedPreferences(
                GENERAL_PREFS, 0);
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

        if (!firstLoad) {
            refreshMap();
            updateSearchKeyword();
            updateSourceText();
        } else {
/*            mSearchKeyTextView.setAlpha(0.0f);
            mSourceTextView.setAlpha(0.0f);
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
            }, 1000);*/
        }
        firstLoad = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Log.i(TAG, "onSaveInstanceState");

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    // ================================================================================
    // Network Calls for Markers
    // ================================================================================


    private class DownloadMapUpdateXmlTask extends AsyncTask<String, Void, String> {
        ArrayList<MapMarker> updatedMarkers;
        long numMillis;

        @Override
        protected String doInBackground(String... urls) {
            try {
                numMillis = System.currentTimeMillis();
                updatedMarkers = loadMapUpdateXmlFromNetwork(urls[0]);
                return "finished";
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        protected void onPostExecute(String result) {
            updateMarkers(updatedMarkers);
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
        //   //Log.i(TAG, "Found " + markersFound.size() + " markers");
        return markersFound;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        //    //Log.i(TAG, "Download URL " + urlString);
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
// Network Call for Sources
// ================================================================================
    private class DownloadSourcesXmlTask extends AsyncTask<String, Void, String> {
        ArrayList<Source> sourcesFound;
        SourceType sourceType;

        @Override
        protected String doInBackground(String... urls) {
            try {
                //         //Log.i("DOWNLOAD FEED", "1");
                sourcesFound = loadFeedSourcesXmlFromNetwork(urls[0], sourceType);
                return "finished";
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        public void setSourceType(SourceType sourceType) {
            this.sourceType = sourceType;
        }

        protected void onPostExecute(String result) {
            if (sourceType == SourceType.LANGUAGE_SOURCE) {
                setLanguageSourcesFound(sourcesFound);
            } else if (sourceType == SourceType.FEED_SOURCE) {
                setFeedSourcesFound(sourcesFound);
            }
        }
    }

    private ArrayList<Source> loadFeedSourcesXmlFromNetwork(String urlString, SourceType sourceType)
            throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        SourceUpdateRequest sourceUpdateRequest = new SourceUpdateRequest(sourceType);
        ArrayList<Source> sourcesFound = null;

        try {
            stream = downloadUrl(urlString);
            sourcesFound = sourceUpdateRequest.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        if (sourceType == SourceType.LANGUAGE_SOURCE) {
//            //Log.i(TAG, "Found " + sourcesFound.size() + " language sources");
        } else {
            //           //Log.i(TAG, "Found " + sourcesFound.size() + " feed sources");
        }
        return sourcesFound;
    }

// ================================================================================
// Network Call for Sources
// ================================================================================

    private class DeviceGridCellMarker extends DeviceGridCell {
        private String TAG = "edu.umd.umiacs.newsstand.DeviceGridCellMarkers";

        private ArrayList<MapMarker> markers;
        private long downloadTime;
        private String constraints;

        private DownloadMarkersGridCellXmlTask task;
        private DeviceGridCellMarkerState state;

        boolean isCurrentWindow = false;
        long numMillis;

        public DeviceGridCellMarker(DeviceGridCell deviceGridCell, String constraints) {
            super(deviceGridCell);
            this.constraints = constraints;
            downloadMarkers();
        }

        public DeviceGridCellMarker(DeviceGridCell deviceGridCell, String constraints, boolean isCurrentWindow) {
            super(deviceGridCell);
            this.constraints = constraints;
            this.isCurrentWindow = isCurrentWindow;
            downloadMarkers();
        }

        private void downloadMarkers() {
            numMillis = System.currentTimeMillis();
            state = DeviceGridCellMarkerState.DOWNLOADING;
            task = new DownloadMarkersGridCellXmlTask();
            String query = getQuery();
            //Log.i(TAG, "start " + query);

            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        }

        private String getQuery() {
            double lat_low = y2lat(lowerLeftY - 180.0);
            double lat_high = y2lat(upperRightY - 180.0);
            double lon_low = lowerLeftX - 180.0;
            double lon_high = upperRightX - 180.0;

            String markersURL = "";
            if (mStandMode == StandMode.NEWSSTAND || mStandMode == StandMode.PHOTOSTAND) {
                markersURL = "http://newsstand.umiacs.umd.edu/news/xml_map?lat_low=";
            } else if (mStandMode == StandMode.TWITTERSTAND) {
                markersURL = "http://twitterstand.umiacs.umd.edu/news/xml_map?lat_low=";
            }
            markersURL += lat_low + "&lat_high=" + lat_high + "&lon_low="
                    + lon_low + "&lon_high=" + lon_high + constraints;

            ////Log.i(TAG, markersURL);

            return markersURL;
        }

        private double y2lat(double y) {
            return 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
        }

        private void updateGridCellMarkers(ArrayList<MapMarker> downloadedMarkers) {
            long res = System.currentTimeMillis() - numMillis;
            markers = downloadedMarkers;
            downloadTime = System.currentTimeMillis() / 1000;
            state = DeviceGridCellMarkerState.COMPLETED;
            gridMarkersDidComplete(this);
            sendKeenMapQueryRecord(new QueryRecord(this), String.valueOf(res), markers);
        }

        public DeviceGridCellMarkerState getState() {
            return state;
        }

        public ArrayList<MapMarker> getMarkers() {
            return markers;
        }

        public long getDownloadTime() {
            return downloadTime;
        }

        public void setCurrentWindow(boolean value) {
            isCurrentWindow = value;
        }

        public void cancelTask() {
            task.cancel(true);
        }

        // ================================================================================
        // Network Calls for Markers
        // ================================================================================
        private class DownloadMarkersGridCellXmlTask extends AsyncTask<String, Void, String> {
            ArrayList<MapMarker> updatedMarkers;
            int attempts = 0; // Attempts to retrieve markers

            @Override
            protected String doInBackground(String... urls) {
                try {
                    updatedMarkers = loadMapUpdateXmlFromNetwork(urls[0]);
                    return "finished";
                } catch (IOException e) {
                    attempts++;
                    if (attempts < 3)
                        doInBackground(urls);
                    return "Connection error";
                } catch (XmlPullParserException e) {
                    attempts++;
                    if (attempts < 3)
                        doInBackground(urls);
                    return "XML error";
                }
            }

            protected void onPostExecute(String result) {
                updateGridCellMarkers(updatedMarkers);
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
            ////Log.i(TAG, "Found " + markersFound.size() + " markers");
            return markersFound;
        }

        // Given a string representation of a URL, sets up a connection and gets
        // an input stream.
        private InputStream downloadUrl(String urlString) throws IOException {
            ////Log.i(TAG, "Download URL " + urlString);
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
    }

// ================================================================================
// Getters & Setters
// ================================================================================

    public StandMode getStandMode() {
        return mStandMode;
    }

    public void setStandMode(StandMode standMode) {
        mStandMode = standMode;
    }

}
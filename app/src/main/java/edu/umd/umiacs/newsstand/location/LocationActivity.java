package edu.umd.umiacs.newsstand.location;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import android.app.ActionBar;
import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.*;

import edu.umd.umiacs.newsstand.topstories.TopStoriesListAdapter;

import io.keen.client.android.KeenClient;
import io.keen.client.android.UploadFinishedCallback;
import io.keen.client.android.exceptions.KeenException;
import org.xmlpull.v1.XmlPullParserException;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.snippet.SnippetActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

public class LocationActivity extends Activity implements OnItemClickListener, View.OnClickListener {
    private final String TAG = "edu.umd.umiacs.newsstand.Location.LocationActivity";

    public final static String ARTICLES = "articles";
    public final static String SELECTED = "selected";

    private ArrayList<Article> mArticles;
    private LocationListAdapter mLocationListAdapter;

    private ActionBar mActionBar;
    private String mTitle;

    private Button mBackButton;
    private Button mLocationTextButton;
    private ImageButton mTranslateButton;

    private String mLocation;
    private int gaz_id;
    private String mConstraints;

    private ListView mListView;
    private ProgressBar mProgressBar;

    private boolean mFirst = true;
    private boolean isTwitterstand;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Intent intent = getIntent();
        gaz_id = intent.getIntExtra(MainActivity.GAZ_ID, -1);
        mConstraints = intent.getStringExtra(MainActivity.CONSTRAINTS);
        mTitle = intent.getStringExtra(MainActivity.TITLE);
        mLocation = intent.getStringExtra(MainActivity.LOCATION_NAME);
        isTwitterstand = intent.getBooleanExtra(MainActivity.TWITTERSTAND, false);

        setupActionBar();

        mListView = (ListView) findViewById(R.id.locationListView);
        mProgressBar = (ProgressBar) findViewById(R.id.locationProgressBar);

        if (gaz_id > 0) {
            mProgressBar.setVisibility(View.VISIBLE);
            String locationURL = "";
            if (!isTwitterstand)
                locationURL = "http://newsstand.umiacs.umd.edu/news/xml_top_locations?gaz_id=" + gaz_id + mConstraints;
            else
                locationURL = "http://twitterstand.umiacs.umd.edu/news/xml_top_locations?gaz_id=" + gaz_id + mConstraints;


            Log.i(TAG, "Map Update: " + locationURL);
            new DownloadXmlTask().execute(locationURL);
        }

        mListView.setOnItemClickListener(this);
        mListView.setBackgroundColor(getResources().getColor(R.color.white));
        mListView.setDivider(null);
        mListView.setDividerHeight(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (!mFirst && mLocationListAdapter != null)
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mLocationListAdapter.setSelectedPosition(-1);
                        }
                    });
            }
        }, 300);
        mFirst = false;
    }

    //================================================================================
    // ActionBar Calls
    //================================================================================

    private void setupActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.action_location);

            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);

            mBackButton = (Button) findViewById(R.id.locationBackButton);
            mBackButton.setText(mTitle);
            mBackButton.setOnClickListener(this);

            mLocationTextButton = (Button) findViewById(R.id.locationLocationName);
            mLocationTextButton.setText(mLocation);

            mTranslateButton = (ImageButton) findViewById(R.id.locationTranslateButton);
            mTranslateButton.setOnClickListener(this);
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
    // OnItemClick Listener
    //================================================================================
    @Override
    public void onItemClick(AdapterView<?> arg0, View viewSelected, int selectedIndex, long arg3) {
        mLocationListAdapter.setSelectedPosition(selectedIndex);

        Intent intent = new Intent(this, SnippetActivity.class);
        intent.putExtra(ARTICLES, mArticles);
        intent.putExtra(SELECTED, selectedIndex);
        intent.putExtra(MainActivity.LOCATION_NAME, mLocation);
        startActivity(intent);
    }


    //================================================================================
    // Article Calls
    //================================================================================
    private void updateArticles(ArrayList<Article> articlesFound) {
        mArticles = articlesFound;
        boolean foundTranslated = false;
        if (mArticles != null) {
            for (Article currentArticle : mArticles) {
                String translatedTitle = currentArticle.getTranslate_title().trim();
                if (!translatedTitle.equals("")) {
                    foundTranslated = true;
                }
            }

            if (mArticles.size() == 1) { // If only one article then open snippet
                Intent intent = new Intent(this, SnippetActivity.class);
                intent.putExtra(ARTICLES, mArticles);
                intent.putExtra(SELECTED, 0);
                intent.putExtra(MainActivity.LOCATION_NAME, mTitle);
                startActivity(intent);
                finish();
            } else {
                if (foundTranslated) mTranslateButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mLocationListAdapter = new LocationListAdapter(this, mArticles);
                mListView.setAdapter(mLocationListAdapter);
            }
        }
    }

    //================================================================================
    // Keen Analytics
    //================================================================================

    private Map<String, Object> topicRatio (ArrayList<Article> articles) {
        Map<String, Object> ratiosMap = new HashMap<String, Object>();
        double numGeneral=0, numBusiness=0, numEntertainment=0, numHealth=0, numSciTech=0, numSports = 0, numInvalid=0;
        double numElms = articles.size();

        for (Article article : articles) {
            String topic = "" + article.getTopic();

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

    private void sendKeenLocationQueryRecord (long roundTripTime) {
        Map<String, Object> event = new HashMap<String, Object>();
        event.put("gaz_id", gaz_id);
        event.put("location", mLocation);
        event.put("constraints", mConstraints);

        Map<String, Object> topicRatio = null;
        if (mArticles != null && mArticles.size() > 0) {
            event.put("count", mArticles.size());

            int num_images = 0;
            int num_videos = 0;

            for (Article article : mArticles ) {
                num_images += article.getNum_images();
                num_videos += article.getNum_videos();
            }

            event.put("num_images", num_images);
            event.put("num_videos", num_videos);

            topicRatio = topicRatio(mArticles);
            topicRatio.put("gaz_id", gaz_id);
        } else {
            event.put("count", 0);
            event.put("num_images", 0);
            event.put("num_videos", 0);
        }

        String mode = "newsstand";
        if (isTwitterstand)
            mode = "twitterstand";
        event.put("mode", mode);

        event.put("rtt", roundTripTime);

        // override the Keen timestamp
        Map<String, Object> keenProperties = new HashMap<String, Object>();
        keenProperties.put("timestamp", Calendar.getInstance());

        try {
            KeenClient.client().addEvent("location", event, keenProperties);
            if (topicRatio != null)
                KeenClient.client().addEvent("location_topic_ratio", topicRatio, keenProperties);
        } catch (KeenException e) {
            // do nothing for now
            Log.e(TAG, "KeenException thrown");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Keen IllegalStateException thrown");
        }

        uploadKeen();
    }

    private void uploadKeen() {
        // upload all captured events to Keen
        try {
            KeenClient.client().upload(new UploadFinishedCallback() {
                public void callback() {
                    // use this to notify yourself when the upload finishes, if you wish. we'll just log for now.
                    Log.i(TAG, "Keen client has finished uploading!");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Keen Client upload exception");
        }
    }

    //================================================================================
    // OnClick Listener
    //================================================================================
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.locationBackButton:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.locationTranslateButton:
                mLocationListAdapter.toggleTranslateTitles();
                break;
        }
    }


    //================================================================================
    // Network Calls for Articles
    //================================================================================
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {
        ArrayList<Article> updatedArticles;
        long startTime;

        @Override
        protected String doInBackground(String... urls) {
            try {
                startTime = System.currentTimeMillis();
                updatedArticles = loadXmlFromNetwork(urls[0]);
                return "finished";
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        protected void onPostExecute(String result) {
            updateArticles(updatedArticles);
            sendKeenLocationQueryRecord(System.currentTimeMillis()-startTime);
        }
    }

    private ArrayList<Article> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        LocationRequest locationRequest = new LocationRequest();
        ArrayList<Article> articlesFound = null;

        try {
            stream = downloadUrl(urlString);
            articlesFound = locationRequest.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        Log.i(TAG, "Found " + articlesFound.size() + " articles");
        return articlesFound;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.

    private InputStream downloadUrl(String urlString) throws IOException {
        Log.i(TAG, "Download URL " + urlString);
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

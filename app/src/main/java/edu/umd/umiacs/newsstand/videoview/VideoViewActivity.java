package edu.umd.umiacs.newsstand.videoview;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.webview.WebViewActivity;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

public class VideoViewActivity extends ListActivity implements
        OnItemClickListener, View.OnClickListener {
    private final static String TAG = "edu.umd.uumiacs.newsstand.videoview.VideoViewActivity";
    private final static String DEVELOPER_KEY = "AIzaSyBl-z03zoQDROGCLmOrgJtFhfIaaYHaT5E";

    private final static boolean AUTOPLAY = true;
    private final static boolean LIGHTBOX_MODE = false;

    private ActionBar mActionBar;
    private Button mBackButton;
    private Button mTitleButton;

    private ArrayList<Video> mVideos;
    private VideoListAdapter mVideoListAdapter;

    private String mTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int cluster_id = intent.getIntExtra(MainActivity.CLUSTER_ID, -1);
        mTitle = intent.getStringExtra(MainActivity.TITLE);

        setupActionBar();

        mVideos = null;

        if (cluster_id > 0) {
            String videosURL = "http://newsstand.umiacs.umd.edu/news/xml_videos?cluster_id="
                    + cluster_id;

            Log.i(TAG, "Videos Update: " + videosURL);
            new DownloadXmlTask().execute(videosURL);
        }

        getListView().setOnItemClickListener(this);
    }

    // ================================================================================
    // Action Bar
    // ================================================================================

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
            mTitleButton.setText("Videos");
        }
    }

    // ================================================================================
    // OnItemClick Listener
    // ================================================================================
    @Override
    public void onItemClick(AdapterView<?> arg0, View viewSelected,
                            int selectedIndex, long arg3) {
        if (mVideos != null && mVideos.size() >= selectedIndex) {
            Video selectedVideo = mVideos.get(selectedIndex);
            String videoURL = selectedVideo.getUrl();
            String videoID;

            if (videoURL.contains("youtube.com")) {
                int vIndex = videoURL.indexOf("v=");
                int ampIndex = videoURL.indexOf("&", vIndex);

                if (vIndex > 0) {
                    if (ampIndex < 0) {
                        videoID = videoURL.substring(vIndex + 2);
                    } else {
                        videoID = videoURL.substring(vIndex + 2, ampIndex);
                    }
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                            this, DEVELOPER_KEY, videoID, 0, AUTOPLAY, LIGHTBOX_MODE);
                    if (canResolveIntent(intent)) {
                        startActivityForResult(intent, 1);
                    } else {
                        intent = new Intent(this, WebViewActivity.class);
                        intent.putExtra("articleURL", videoURL);
                        intent.putExtra("title", "Videos");
                        startActivity(intent);
                    }

                } else { // Did not find v= .... Open in WebViewActivity
                    Intent intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra("articleURL", videoURL);
                    intent.putExtra("title", "Videos");
                    startActivity(intent);
                }

            } else {
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("articleURL", videoURL);
                intent.putExtra("title", "Videos");
                startActivity(intent);
            }
        }
    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }

    private void updateVideos(ArrayList<Video> updatedVideos) {
        mVideos = updatedVideos;
        mVideoListAdapter = new VideoListAdapter(this, mVideos);
        getListView().setAdapter(mVideoListAdapter);
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

    // ================================================================================
    // Network Calls for Videos
    // ================================================================================
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {
        ArrayList<Video> updatedVideos;

        @Override
        protected String doInBackground(String... urls) {
            try {
                updatedVideos = loadXmlFromNetwork(urls[0]);
                return "finished";
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        protected void onPostExecute(String result) {
            updateVideos(updatedVideos);
        }
    }

    private ArrayList<Video> loadXmlFromNetwork(String urlString)
            throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        VideoRequest videoRequest = new VideoRequest();
        ArrayList<Video> videosFound = null;

        try {
            stream = downloadUrl(urlString);
            videosFound = videoRequest.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        Log.i(TAG, "Found " + videosFound.size() + " Videos");
        return videosFound;
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

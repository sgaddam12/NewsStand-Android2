package edu.umd.umiacs.newsstand.imageview;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


/**
 * Created by Brendan Fruin on 5/30/13.
 * Modified version of UniversalImageLoader
 */

public class ImageGridActivity extends Activity implements ActionBar.TabListener, View.OnClickListener {
    private final static String TAG = "edu.umd.umiacs.newsstand.imageview.ImageGridActivity";

    private final static String CLUSTER_IMAGES = "CLUSTEER_IMAGES";

    public enum ImageDisplayType {
        ALL_IMAGES,
        MARK_DUPS,
        HIDE_DUPS,
        TOPICS
    }

    private String mTitle;
    private String mLocationName;

    private int mCluster_id;
    private int mGaz_id;
    private String mConstraints;

    private ActionBar mActionBar;
    private Menu mMenu;
    private Button mBackButton;
    private Button mTitleButton;
    private ImageButton mEnlargedGridButton;

    private GridFragment mCurrentFragment;

    private ProgressBar mProgressBar;

    private ShareActionProvider mShareActionProvider;

    private boolean mIsPhotoStand;
    private boolean mIsShowMoreImages;

    private ArrayList<Image> mImages;
    private ArrayList<Image> mImagesNoDups;
    private ArrayList<Image> mTopicImages;
    private ArrayList<Boolean> mMultipleImagesInCluster;

    // More in Topic
    private ArrayList<Image> mClusterImages;
    private ArrayList<Image> mClusterImagesNoDups;

    private Tab mAllImageTab;
    private Tab mMarkImageTab;
    private Tab mHideImageTab;
    private Tab mTopicsTab;

    private ImageDownloadXmlTask imageDownloadXmlTask;

    private int mAttempts = 0;

    // ================================================================================
    // Activity Lifecycle
    // ================================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Intent intent = getIntent();

        mTitle = intent.getStringExtra(MainActivity.TITLE);
        mLocationName = intent.getStringExtra(MainActivity.LOCATION_NAME);
        mConstraints = intent.getStringExtra(MainActivity.CONSTRAINTS);

        mImages = null;

        mProgressBar = (ProgressBar) findViewById(R.id.imageProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mCluster_id = intent.getIntExtra(MainActivity.CLUSTER_ID, -1);
        mGaz_id = intent.getIntExtra(MainActivity.GAZ_ID, -1);

        if (mTitle.equals("Map"))
            mIsPhotoStand = true;

        downloadTask();
    }

    private void downloadTask () {
        if (!mIsPhotoStand) {
            String imagesURL = "http://newsstand.umiacs.umd.edu/news/xml_images?cluster_id="
                    + mCluster_id;
            imageDownloadXmlTask = new ImageDownloadXmlTask();
            imageDownloadXmlTask.execute(imagesURL);

        } else {
            if (mGaz_id > 0) {
                String imagesURL = "http://newsstand.umiacs.umd.edu/news/xml_gaz_images?gaz_id="
                        + mGaz_id + mConstraints;
                imageDownloadXmlTask = new ImageDownloadXmlTask();
                imageDownloadXmlTask.execute(imagesURL);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate options menu from XML
        getMenuInflater().inflate(R.menu.activity_image_view, menu);
        mMenu = menu;
        setupActionBar();
        return true;
    }

    @Override
    public void onDestroy() {
        if (imageDownloadXmlTask != null)
            imageDownloadXmlTask.cancel(true);
        super.onDestroy();
    }

    // ================================================================================
    // Action Bar
    // ================================================================================

    private void setupActionBar() {
        if (mActionBar == null)
            mActionBar = getActionBar();

        if (mActionBar != null) {

            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(true);

            mActionBar.setCustomView(R.layout.action_image_view);
            mActionBar.setDisplayShowCustomEnabled(true);

            mTitleButton = (Button) findViewById(R.id.imageTitleButton);

            if (!mIsPhotoStand) {
                mTitleButton.setText("Images");
                if (mTitle.equals("Headline")) {
                   mActionBar.setIcon(R.drawable.back_headline);
                } else {
                   mActionBar.setIcon(R.drawable.back_topstories);
                }
            } else {
                mTitleButton.setText(mLocationName);
                mActionBar.setIcon(R.drawable.back_map);
            }

            mEnlargedGridButton = (ImageButton) findViewById(R.id.imageEnlargeGrid);
            mEnlargedGridButton.setOnClickListener(this);

            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.image_moreTopic:
                showMoreInTopic();
                return true;
            case R.id.image_enlargeImage:
                showImagePager();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        Log.i(TAG, "Share intent");
        if (mShareActionProvider != null) {
            shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, mImages.get(0).getImageURL());
            shareIntent.setType("image/jpeg");
            startActivity(Intent.createChooser(shareIntent, "Share with"));
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    // ================================================================================
    // Action Bar Actions
    // ================================================================================

    private void showMoreInTopic() {
        Log.i(TAG, "Show more in topic");
        int clusterID = mCurrentFragment.getCurrentClusterID();
        mClusterImages = new ArrayList<Image>();
        mClusterImagesNoDups = new ArrayList<Image>();
        mActionBar.setIcon(R.drawable.back_topics);
        mTitleButton.setText("Images");

        for (Image currentImage : mImages) {
            if (currentImage.getClusterID() == clusterID) {
                mClusterImages.add(currentImage);
                if (!currentImage.isDuplicate())
                    mClusterImagesNoDups.add(currentImage);
            }
        }

        mIsShowMoreImages = true;
        refreshTabMoreInTopic();
    }

    private void refreshTabMoreInTopic() {
        mActionBar.removeAllTabs();

        mAllImageTab = mActionBar.newTab().setTabListener(this);
        mAllImageTab.setText("All");
        mActionBar.addTab(mAllImageTab);

        mMarkImageTab = mActionBar.newTab().setTabListener(this);
        mMarkImageTab.setText("Mark Dups");
        mActionBar.addTab(mMarkImageTab);

        mHideImageTab = mActionBar.newTab().setTabListener(this);
        mHideImageTab.setText("Hide Dups");
        mActionBar.addTab(mHideImageTab);

        if (!mIsShowMoreImages) {
            mTopicsTab = mActionBar.newTab().setTabListener(this);
            mTopicsTab.setText("Topics");
            mActionBar.addTab(mTopicsTab);

            mActionBar.selectTab(mTopicsTab);
        } else {
            mActionBar.selectTab(mAllImageTab);
        }
    }

    private void showImagePager() {
        ImageDisplayType imageDisplayType = mCurrentFragment.getImageDisplayType();
        ArrayList<Image> images;

        if (!mIsShowMoreImages) {
            if (imageDisplayType == ImageDisplayType.ALL_IMAGES
                    || imageDisplayType == ImageDisplayType.MARK_DUPS) {
                images = mImages;
            } else if (imageDisplayType == ImageDisplayType.HIDE_DUPS) {
                images = mImagesNoDups;
            } else {
                images = mTopicImages;
            }
        } else {
            if (imageDisplayType == ImageDisplayType.ALL_IMAGES
                    || imageDisplayType == ImageDisplayType.MARK_DUPS) {
                images = mClusterImages;
            } else {
                images = mClusterImagesNoDups;
            }
        }

        String[] imageURLs = new String[images.size()];
        int i = 0;
        for (Image currentImage : images) {
            imageURLs[i] = currentImage.getImageURL();
            i++;
        }

        int selectedIndex = mCurrentFragment.getSelectedIndex();
        Intent intent = new Intent(this, ImagePagerActivity.class);

        String title = "Images";
        if (mLocationName != null && !mLocationName.equals(""))
            title = mLocationName;

        intent.putExtra(MainActivity.TITLE, title);
        intent.putExtra(ImagePagerActivity.IMAGES, images);
        intent.putExtra(ImagePagerActivity.IMAGE_URLS, imageURLs);
        intent.putExtra(ImagePagerActivity.IMAGE_POSITION, selectedIndex);
        intent.putExtra("title", mLocationName);
        startActivity(intent);
    }

    // ================================================================================
    // Back Button
    // ================================================================================
    @Override
    public void onBackPressed() {
        Log.i(TAG, "On back");
        if (mIsShowMoreImages) {
            mIsShowMoreImages = false;
            mActionBar.setIcon(R.drawable.back_map);
            mTitleButton.setText(mLocationName);
            refreshTabMoreInTopic();
        } else {
            super.onBackPressed();
        }
    }

    // ================================================================================
    // OnClick Listener
    // ================================================================================
    @Override
    public void onClick(View clickedView) {
        Log.i(TAG, "Click detected");
        switch (clickedView.getId()) {
            case R.id.imageEnlargeGrid:
                if (mCurrentFragment != null)
                    mCurrentFragment.actionGridImageClicked();
                break;
        }
    }

    public void changeImageEnlargedGridButtonState(boolean isImageEnlarged) {
        if (isImageEnlarged)
            mEnlargedGridButton.setImageResource(R.drawable.ic_action_grid);
        else
            mEnlargedGridButton.setImageResource(R.drawable.ic_action_enlarged_grid);
    }

    // ================================================================================
    // Orientation Changes
    // ================================================================================
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
    }

    // ================================================================================
    // Network Callback
    // ================================================================================

    private void updateImages(ArrayList<Image> updatedImages) {
        if (updatedImages == null || updatedImages.size() == 0) {
            if (mAttempts < 2) {
                Log.i(TAG, "Retrying download");
                downloadTask();
                mAttempts++;
                return;
            } else {
                finish();
                return;
            }
        }

        mImages = updatedImages;

        mProgressBar.setVisibility(View.GONE);

        mImagesNoDups = new ArrayList<Image>();
        for (Image currentImage : updatedImages)
            if (!currentImage.isDuplicate())
                mImagesNoDups.add(currentImage);

        if (mActionBar == null)
            mActionBar = getActionBar();

        mAllImageTab = mActionBar.newTab().setTabListener(this);
        mAllImageTab.setText("All");
        mActionBar.addTab(mAllImageTab);

        mMarkImageTab = mActionBar.newTab().setTabListener(this);
        mMarkImageTab.setText("Mark Dups");
        mActionBar.addTab(mMarkImageTab);

        mHideImageTab = mActionBar.newTab().setTabListener(this);
        mHideImageTab.setText("Hide Dups");
        mActionBar.addTab(mHideImageTab);

        if (mIsPhotoStand) {
            mTopicsTab = mActionBar.newTab().setTabListener(this);
            mTopicsTab.setText("Topics");
            mActionBar.addTab(mTopicsTab);

            HashSet<Integer> clustersSeen = new HashSet<Integer>();
            mTopicImages = new ArrayList<Image>();

            HashMap<Integer, Integer> numImagesInCluster = new HashMap<Integer, Integer>();

            for (Image currentImage : updatedImages) {
                Integer currentClusterID = currentImage.getClusterID();
                if (!clustersSeen.contains(currentClusterID)) {
                    mTopicImages.add(currentImage);
                    clustersSeen.add(currentClusterID);

                    numImagesInCluster.put(currentClusterID, 1);
                } else {
                    Integer numImages = numImagesInCluster.get(currentClusterID);
                    if (numImages != null) {
                        numImages++;
                        numImagesInCluster.put(currentClusterID, numImages);
                    }
                }
            }

            Log.i(TAG, " " + clustersSeen);

            // Order by cluster score
            Collections.sort(mTopicImages, new Comparator<Image>() {
                @Override
                public int compare(Image lhs, Image rhs) {
                    return Float.valueOf(lhs.getClusterScore()).compareTo(Float.valueOf(rhs.getClusterScore()));
                }
            });

            mMultipleImagesInCluster = new ArrayList<Boolean>();
            for (Image currentImage : mTopicImages) {
                Integer numImages = numImagesInCluster.get(currentImage.getClusterID());
                if (numImages != null && numImages > 1)
                    mMultipleImagesInCluster.add(Boolean.TRUE);
                else
                    mMultipleImagesInCluster.add(Boolean.FALSE);
            }
        }
       // setShareIntent(new Intent());
    }

    // ================================================================================
    // Tab Listener
    // ================================================================================

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        String tabText = tab.getText().toString();

        setShowMoreInTopic(false);

        boolean setOffset = false;
        int offset = 0;
        int index = 0;
        if (mCurrentFragment != null
                && mCurrentFragment.getImageDisplayType() != ImageDisplayType.TOPICS
                && !tabText.equals("Topics")) {
            setOffset = true;
            offset = mCurrentFragment.getGridOffset();
            index = mCurrentFragment.getFirstVisibleIndex();
            Log.i(TAG, "index " + index + " offset " + offset );
        }

        setOffset = false;

        if (tabText.equals("All")) {
            if (!mIsShowMoreImages)
                if (!setOffset)
                    mCurrentFragment = new GridFragment(ImageDisplayType.ALL_IMAGES, mImages);
                else
                    mCurrentFragment = new GridFragment(ImageDisplayType.ALL_IMAGES, mImages,
                            index, offset);
            else
                mCurrentFragment = new GridFragment(ImageDisplayType.ALL_IMAGES, mClusterImages);
        } else if (tabText.equals("Mark Dups")) {
            if (!mIsShowMoreImages)
                if (!setOffset)
                    mCurrentFragment = new GridFragment(ImageDisplayType.MARK_DUPS, mImages);
                else
                    mCurrentFragment = new GridFragment(ImageDisplayType.MARK_DUPS, mImages,
                            index, offset);
            else
                mCurrentFragment = new GridFragment(ImageDisplayType.MARK_DUPS, mClusterImages);
        } else if (tabText.equals("Hide Dups")) {
            if (!mIsShowMoreImages)
                if (!setOffset)
                    mCurrentFragment = new GridFragment(ImageDisplayType.HIDE_DUPS, mImagesNoDups);
                else
                    mCurrentFragment = new GridFragment(ImageDisplayType.HIDE_DUPS, mImagesNoDups,
                            index, offset);
            else
                mCurrentFragment = new GridFragment(ImageDisplayType.HIDE_DUPS, mClusterImagesNoDups);
        } else {
            mCurrentFragment = new GridFragment(ImageDisplayType.TOPICS, mTopicImages, mMultipleImagesInCluster);
        }

        // Attach fragment1.xml layout
        ft.add(android.R.id.content, mCurrentFragment);
        ft.attach(mCurrentFragment);
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        ft.remove(mCurrentFragment);
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    // ================================================================================
    // Network Calls
    // ================================================================================

    private class ImageDownloadXmlTask extends AsyncTask<String, Void, String> {
        ArrayList<Image> updatedImages;

        @Override
        protected String doInBackground(String... urls) {
            try {
                updatedImages = loadXmlFromNetwork(urls[0]);
                return "finished";
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return getResources().getString(R.string.xml_error);
            }
        }

        protected void onPostExecute(String result) {
            Log.i(TAG, "Calling update Images");
            updateImages(updatedImages);
        }
    }

    private ArrayList<Image> loadXmlFromNetwork(String urlString)
            throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        ImageRequest imageRequest = new ImageRequest();
        ArrayList<Image> imagesFound = null;

        try {
            stream = downloadUrl(urlString);
            imagesFound = imageRequest.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        Log.i(TAG, "Found " + imagesFound.size() + " images");
        return imagesFound;
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

    // ================================================================================
    // Setters
    // ================================================================================

    public void setEnlargedImageGrid (boolean isImageEnlarged) {
        if (isImageEnlarged) {
            mEnlargedGridButton.setImageResource(R.drawable.ic_action_grid);
        } else {
            mEnlargedGridButton.setImageResource(R.drawable.ic_action_grid);
        }
    }

    public void setShowMoreInTopic(boolean showMoreInTopic) {
        mMenu.getItem(1).setVisible(showMoreInTopic);
    }
}

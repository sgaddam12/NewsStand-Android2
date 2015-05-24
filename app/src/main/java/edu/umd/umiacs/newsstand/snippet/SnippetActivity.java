package edu.umd.umiacs.newsstand.snippet;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.VideoView;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.general.NoResultsRequest;
import edu.umd.umiacs.newsstand.imageview.ImageGridActivity;
import edu.umd.umiacs.newsstand.location.Article;
import edu.umd.umiacs.newsstand.location.LocationActivity;
import edu.umd.umiacs.newsstand.videoview.VideoViewActivity;
import edu.umd.umiacs.newsstand.webview.WebViewActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;

public class SnippetActivity extends Activity implements View.OnClickListener {
    final private String TAG = "edu.umd.umiacs.newsstand.snippet.SnippetActivity";

    private Context context;

    private WebView mWebView;
    private ArrayList<Article> mArticles;
    private int mSelectedIndex;

    private String mTitle;

    private ActionBar mActionBar;

    private Button mBackButton;
    private Button mTitleButton;
    private ImageButton mUpImageButton;
    private ImageButton mDownImageButton;

    private boolean mShowingTranslated;

    private enum ErrorType {
        NOT_LOCATION,
        WRONG_LOCATION,
        CORRECT
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet);

        context = this;

        Intent intent = getIntent();
        mArticles = (ArrayList<Article>) intent.getSerializableExtra(LocationActivity.ARTICLES);
        mTitle = intent.getStringExtra(MainActivity.LOCATION_NAME);
        mSelectedIndex = intent.getIntExtra(LocationActivity.SELECTED, 0);
        String htmlMarkup = mArticles.get(mSelectedIndex).getMarkup();

        setupActionBar();
        mWebView = (WebView) findViewById(R.id.snippet_webview);

        final Activity activity = this;

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadDataWithBaseURL("", htmlMarkup, "text/html", "UTF-8", "");
        Log.i(TAG, htmlMarkup);
        updateSnippetDisplayed(mSelectedIndex);
    }

    //================================================================================
    // ActionBar Calls
    //================================================================================

    private void setupActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.action_snippet);

            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);

            mBackButton = (Button) findViewById(R.id.snippetBackButton);
            mBackButton.setText(mTitle);
            mBackButton.setOnClickListener(this);

            mTitleButton = (Button) findViewById(R.id.snippetTitleName);
            mTitleButton.setText("Headline");

            mUpImageButton = (ImageButton) findViewById(R.id.snippetUpButton);
            mUpImageButton.setOnClickListener(this);

            mDownImageButton = (ImageButton) findViewById(R.id.snippetDownButton);
            mDownImageButton.setOnClickListener(this);

            if (mArticles.size() > 1) {
                mUpImageButton.setVisibility(View.VISIBLE);
                mDownImageButton.setVisibility(View.VISIBLE);

                if (mSelectedIndex > 0) {
                    mUpImageButton.setVisibility(View.VISIBLE);
                } else {
                    mUpImageButton.setVisibility(View.GONE);
                }

                if (mSelectedIndex < mArticles.size() - 1) {
                    mDownImageButton.setVisibility(View.VISIBLE);
                } else {
                    mDownImageButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private void updateSnippetDisplayed(int updatedIndex) {
        mSelectedIndex = updatedIndex;
        mShowingTranslated = false;

        final String htmlMarkup = mArticles.get(mSelectedIndex).getMarkup();
        final String htmlMarkupTranslated = mArticles.get(mSelectedIndex).getTranslate_markup();

        mWebView = (WebView) findViewById(R.id.snippet_webview);

        final Activity activity = this;

        mWebView.loadDataWithBaseURL("", htmlMarkup, "text/html", "UTF-8", "");

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                Log.i(TAG, url);
                if (url.contains("cluster_videos")) {
                    Intent intent = new Intent(activity, VideoViewActivity.class);
                    intent.putExtra(MainActivity.CLUSTER_ID, mArticles.get(mSelectedIndex).getCluster_id());
                    intent.putExtra(MainActivity.TITLE, "Headline");
                    startActivity(intent);
                    return true;
                } else if (url.contains("cluster_images")) {
                    Intent intent = new Intent(activity, ImageGridActivity.class);
                    intent.putExtra(MainActivity.CLUSTER_ID, mArticles.get(mSelectedIndex).getCluster_id());
                    intent.putExtra(MainActivity.TITLE, "Headline");
                    startActivity(intent);
                    return true;
                } else if (url.contains("bteitler_error_report_not_loc")) {
                    displayAlertDialogForErrorType(ErrorType.NOT_LOCATION);
                    return true;
                } else if (url.contains("bteitler_error_report_loc")) {
                    displayAlertDialogForErrorType(ErrorType.WRONG_LOCATION);
                    return true;
                } else if (url.contains("bfruin_report_correct")) {
                    displayAlertDialogForErrorType(ErrorType.CORRECT);
                    return true;
                } else if (url.contains("bfruin_show_translated")) {
                    if (!mShowingTranslated && htmlMarkupTranslated != null) {
                        mWebView.loadDataWithBaseURL("", htmlMarkupTranslated, "text/html", "UTF-8", "");
                    } else {
                        mWebView.loadDataWithBaseURL("", htmlMarkup, "text/html", "UTF-8", "");
                    }
                    mShowingTranslated = !mShowingTranslated;
                } else if (!url.contains("umiacs.umd")) {
                    Intent intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra("articleURL", url);
                    intent.putExtra(MainActivity.TITLE, "Headline");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        if (mArticles.size() > 1) {
            mUpImageButton.setVisibility(View.VISIBLE);
            mDownImageButton.setVisibility(View.VISIBLE);

            if (mSelectedIndex > 0) {
                mUpImageButton.setVisibility(View.VISIBLE);
            } else {
                mUpImageButton.setVisibility(View.GONE);
            }

            if (mSelectedIndex < mArticles.size() - 1) {
                mDownImageButton.setVisibility(View.VISIBLE);
            } else {
                mDownImageButton.setVisibility(View.GONE);
            }
        }

        NoResultsRequest imageRequest = new NoResultsRequest();
        imageRequest.execute("http://newsstand.umiacs.umd.edu/news/xml_images?cluster_id="
                + mArticles.get(mSelectedIndex).getCluster_id());
        NoResultsRequest videoRequest = new NoResultsRequest();
        videoRequest.execute("http://newsstand.umiacs.umd.edu/news/xml_videos?cluster_id="
                + mArticles.get(mSelectedIndex).getCluster_id());
    }

    private void displayAlertDialogForErrorType (ErrorType errorType) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        String errType = "";

        if (errorType == ErrorType.NOT_LOCATION) {
            errType = "NOT_LOC";
            alert.setTitle("Not a Location");
            alert.setMessage("Report " + mTitle + " is not a location");
        } else if (errorType == ErrorType.WRONG_LOCATION) {
            errType = "WRONG_LOC";
            alert.setTitle("Wrong Location");
            alert.setMessage("Report " + mTitle + " is at the wrong location");
        } else {
            errType = "CORRECT_LOC";
            alert.setTitle("Correct Location");
            alert.setMessage("Report " + mTitle + " is at the correct location");
        }

        final EditText textInput = new EditText(context);
        textInput.setHint("Enter description");
        alert.setView(textInput);

        final String type = errType;
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String comment = textInput.getText().toString();
                String errorString =
                        "http://newsstand.umiacs.umd.edu/mike/feedback?gaztagid=" +
                                mArticles.get(mSelectedIndex).getGazTag_id() +
                                "&errtype=" + type + "&comment=" + comment;
                NoResultsRequest errorReport = new NoResultsRequest();
                errorReport.execute(errorString);
                Log.i(TAG, errorString);
            }
        });

        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    //================================================================================
    // OnClick Listener
    //================================================================================

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case (R.id.snippetBackButton):
                NavUtils.navigateUpFromSameTask(this);
                break;
            case (R.id.snippetUpButton):
                updateSnippetDisplayed(mSelectedIndex - 1);
                break;
            case (R.id.snippetDownButton):
                updateSnippetDisplayed(mSelectedIndex + 1);
                break;
        }
    }
}

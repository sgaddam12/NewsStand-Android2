package edu.umd.umiacs.newsstand.webview;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.location.LocationActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class WebViewActivity extends Activity implements View.OnClickListener {
    private final String TAG = "Web View";

    private String mTitle;
    private String mWebViewTitle;

    private ActionBar mActionBar;
    private Menu mMenu;

    private Button mBackButton;
    private Button mTitleButton;
    private ImageButton mBackHistoryButton;
    private ImageButton mForwardHistoryButton;

    private WebView mWebView;
    private WebSettings mWebSettings;
    private ProgressBar mProgressBar;

    private int mBackHistorySize;
    private int mForwardHistorySize;

    private boolean firstLoad = true;
    private boolean mButtonPress;

    // ================================================================================
    // Initialization
    // ================================================================================
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Intent intent = getIntent();
        mTitle = intent.getStringExtra(MainActivity.TITLE);
        mWebViewTitle = intent.getStringExtra("webViewTitle");
        if (mWebViewTitle == null || mWebViewTitle.length() < 3)
            mWebViewTitle = "News Story";

        String articleURL = intent.getStringExtra("articleURL");

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebSettings = mWebView.getSettings();

        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setUseWideViewPort(true);

        // Enable JavaScript and Flash
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setPluginState(PluginState.ON);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(progress);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

            }

            public void onPageStarted(WebView webView, String url, Bitmap favicon) {
                if (!mButtonPress && !firstLoad) {
                    if (mWebView.canGoBack()) {
                        mBackHistorySize++;
                        mBackHistoryButton.setVisibility(View.VISIBLE);
                    }
                }

                firstLoad = false;
                mButtonPress = false;
            }

            public void onPageFinished(WebView view, String url) {
                if (mBackHistoryButton != null) {
                    if (!mWebView.canGoBack()) {
                        mBackHistoryButton.setVisibility(View.GONE);
                    } else {
                        mBackHistoryButton.setVisibility(View.VISIBLE);
                    }
                }

                if (mForwardHistoryButton != null) {
                    if (!mWebView.canGoForward()) {
                        mForwardHistoryButton.setVisibility(View.GONE);
                    } else {
                        mForwardHistoryButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mWebView.loadUrl(articleURL);
    }

    // ================================================================================
    // Action Bar
    // ================================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate options menu from XML
        getMenuInflater().inflate(R.menu.activity_web_view, menu);
        mMenu = menu;

        setupActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.webTextPlus:
                increaseTextSize();
                return true;
            case R.id.webTextMinus:
                decreaseTextSize();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ================================================================================
    // Life Cycle
    // ================================================================================

    private void setupActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.action_web_view);

            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);

            mBackButton = (Button) findViewById(R.id.webBackButton);
            mBackButton.setText(mTitle);
            mBackButton.setOnClickListener(this);

            mTitleButton = (Button) findViewById(R.id.webTitleButton);
            mTitleButton.setText(mWebViewTitle);

            mBackHistoryButton = (ImageButton) findViewById(R.id.webBackHistoryButton);
            mBackHistoryButton.setOnClickListener(this);

            mForwardHistoryButton = (ImageButton) findViewById(R.id.webForwardHistoryButton);
            mForwardHistoryButton.setOnClickListener(this);
        }
    }

    // ================================================================================
    // Text Sizing
    // ================================================================================

    private void increaseTextSize() {
        mWebSettings.setTextZoom(mWebSettings.getTextZoom() + 50);
        mMenu.getItem(3).setEnabled(true);
    }

    private void decreaseTextSize() {
        int currentTextSize = mWebSettings.getTextZoom();
        mWebSettings.setTextZoom(currentTextSize - 25);

        if (currentTextSize - 25 <= 0) {
            mMenu.getItem(3).setEnabled(false);
        }
    }

    // ================================================================================
    // Life Cycle
    // ================================================================================
    @Override
    public void onPause() {
        mWebView.setVisibility(View.GONE);
        mWebView.destroy();
        super.onPause();
    }


    // ================================================================================
    // Web View History Controls
    // ================================================================================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void webBackHistoryButtonPressed() {
        if (mWebView.canGoBack()) {
            mBackHistorySize--;
            mForwardHistorySize++;
            mWebView.goBack();

            mForwardHistoryButton.setVisibility(View.VISIBLE);
        } else {
            mBackHistorySize = 0;
        }

        if (mBackHistorySize == 0)
            mBackHistoryButton.setVisibility(View.GONE);
    }

    private void webForwardHistoryButtonPressed() {
        if (mWebView.canGoForward()) {
            mForwardHistorySize--;
            mBackHistorySize++;
            mWebView.goForward();

            mBackHistoryButton.setVisibility(View.VISIBLE);
        } else {
            mForwardHistorySize = 0;
        }

        if (mForwardHistorySize == 0)
            mForwardHistoryButton.setVisibility(View.GONE);
    }

    // ================================================================================
    // OnClick Listener
    // ================================================================================

    @Override
    public void onClick(View view) {
        mButtonPress = true;

        switch (view.getId()) {
            case (R.id.webBackButton):
                finish();
                break;
            case (R.id.webBackHistoryButton):
                webBackHistoryButtonPressed();
                break;
            case (R.id.webForwardHistoryButton):
                webForwardHistoryButtonPressed();
                break;
        }
    }
}

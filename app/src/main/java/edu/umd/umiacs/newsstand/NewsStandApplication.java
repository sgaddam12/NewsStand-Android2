package edu.umd.umiacs.newsstand;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import edu.umd.umiacs.newsstand.source.Source;

import android.app.Application;

public class NewsStandApplication extends Application {
    private ArrayList<Source> mAllSources;
    private ArrayList<Source> mLanguageSources;
    private ArrayList<Source> mFeedSources;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this
        // configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory()
                .cacheOnDisc()
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public ArrayList<Source> getAllSources() {
        return mAllSources;
    }

    public void setAllSources(ArrayList<Source> allSources) {
        this.mAllSources = allSources;
    }

    public ArrayList<Source> getFeedSources() {
        return mFeedSources;
    }

    public void setFeedSources(ArrayList<Source> feedSources) {
        this.mFeedSources = feedSources;
    }

    public ArrayList<Source> getLanguageSources() {
        return mLanguageSources;
    }

    public void setLanguageSources(ArrayList<Source> languageSources) {
        this.mLanguageSources = languageSources;
    }

}

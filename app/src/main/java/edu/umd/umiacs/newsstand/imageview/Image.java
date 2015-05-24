package edu.umd.umiacs.newsstand.imageview;

import java.io.Serializable;

/**
 * Created by Brendan on 5/30/13.
 */
public class Image implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mImageURL;
    private String mArticleURL;
    private String mCaption;

    private int mHeight;
    private int mWidth;

    private int mClusterID;
    private int mImageClusterID;

    float mClusterScore;

    boolean mIsDuplicate;

    public Image(String imageURL, String articleURL, String caption, int height, int width, int clusterID,
                 int imageClusterID, float clusterScore, boolean isDuplicate) {
        mImageURL = imageURL;
        mArticleURL = articleURL;
        mCaption = caption;
        mHeight = height;
        mWidth = width;
        mClusterID = clusterID;
        mImageClusterID = imageClusterID;
        mClusterScore = clusterScore;
        mIsDuplicate = isDuplicate;
    }

    public String getImageURL() {
        return mImageURL;
    }

    public String getArticleURL() {
        return mArticleURL;
    }

    public String getCaption() {
        return mCaption;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getClusterID() {
        return mClusterID;
    }

    public int getmImageClusterID() {
        return mImageClusterID;
    }

    public float getClusterScore() {
        return mClusterScore;
    }

    public boolean isDuplicate() {
        return mIsDuplicate;
    }
}

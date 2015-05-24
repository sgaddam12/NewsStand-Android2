package edu.umd.umiacs.newsstand.videoview;

import android.graphics.Bitmap;

public class Video {
    private String url;
    private String title;
    private String sourceName;
    private String sourceDomain;
    private String pubDate;
    private String imgPreview;
    private String duration;

    private Bitmap storedImage;

    public Video() {
    }

    public Video(String url, String title, String sourceName, String sourceDomain,
                 String pubDate, String imgPreview, String duration) {
        this.url = url;
        this.title = title;
        this.sourceName = sourceName;
        this.sourceDomain = sourceDomain;
        this.pubDate = pubDate;
        this.imgPreview = imgPreview;
        this.duration = duration;

        this.setStoredImage(null);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getImgPreview() {
        return imgPreview;
    }

    public void setImgPreview(String imgPreview) {
        this.imgPreview = imgPreview;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSourceDomain() {
        return sourceDomain;
    }

    public void setSourceDomain(String sourceDomain) {
        this.sourceDomain = sourceDomain;
    }

    public Bitmap getStoredImage() {
        return storedImage;
    }

    public void setStoredImage(Bitmap storedImage) {
        this.storedImage = storedImage;
    }
}

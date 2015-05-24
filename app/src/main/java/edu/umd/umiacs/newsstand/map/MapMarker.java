package edu.umd.umiacs.newsstand.map;

public class MapMarker {
    private String name;
    private String title;
    private String description;
    private String topic;
    private String keyword;
    private String img_url;
    private String caption;

    private float latitude;
    private float longitude;

    private float cluster_score;

    private int cluster_id;
    private int gaz_id;

    private int img_height;
    private int img_width;
    private int distinctiveness;

    private boolean isVisible;

    public MapMarker() {

    }

    public MapMarker(String name, String title, String description, String topic, String keyword,
                     String img_url, String caption, float latitude, float longitude, float cluster_score,
                     int cluster_id, int gaz_id, int height, int width, int distinctiveness) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.topic = topic;
        this.keyword = keyword;
        this.img_url = img_url;
        this.caption = caption;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cluster_id = cluster_id;
        this.cluster_score = cluster_score;
        this.gaz_id = gaz_id;
        this.img_height = height;
        this.img_width = width;
        this.distinctiveness = distinctiveness;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public int getCluster_id() {
        return cluster_id;
    }

    public void setCluster_id(int cluster_id) {
        this.cluster_id = cluster_id;
    }

    public int getGaz_id() {
        return gaz_id;
    }

    public void setGaz_id(int gaz_id) {
        this.gaz_id = gaz_id;
    }


    public float getCluster_score() {
        return cluster_score;
    }

    public void setCluster_score(float cluster_score) {
        this.cluster_score = cluster_score;
    }

    public String getCaption () {
        return caption;
    }

    public int getImgHeight() {
        return img_height;
    }

    public int getImgWidth() {
        return img_width;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible (boolean visible) {
        this.isVisible = visible;
    }

    public int getDistinctiveness () {
        return distinctiveness;
    }
}

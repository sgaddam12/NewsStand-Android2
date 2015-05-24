package edu.umd.umiacs.newsstand.location;

import java.io.Serializable;

public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String topic;
    private String title;
    private String translate_title;
    private String description;
    private String url;
    private String domain;
    private String time;
    private String markup;
    private String translate_markup;
    private String snippet;
    private String keyword;

    private int cluster_id;
    private int gaztag_id;
    private int num_images;
    private int num_videos;
    private int num_docs;

    public Article() {
    }

    public Article(String name, String title, String translate_title, String description, String topic,
                   String markup, String translate_markup, String snippet, String keyword, int cluster_id, int gaztag_id,
                   int num_images, int num_videos) {
        this.name = name;
        this.title = title;
        this.translate_title = translate_title;
        this.description = description;
        this.topic = topic;
        this.markup = markup;
        this.translate_markup = translate_markup;
        this.snippet = snippet;
        this.keyword = keyword;
        this.cluster_id = cluster_id;
        this.gaztag_id = gaztag_id;
        this.num_images = num_images;
        this.num_videos = num_videos;
    }

    public Article(String title, String translate_title, String url, String description, String domain,
                   String topic, String time, int cluster_id, int num_images, int num_videos, int num_docs) {
        this.title = title;
        this.translate_title = translate_title;
        this.url = url;
        this.description = description;
        this.domain = domain;
        this.topic = topic;
        this.setTime(time);
        this.cluster_id = cluster_id;
        this.num_images = num_images;
        this.num_videos = num_videos;
        this.num_docs = num_docs;
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

    public String getTranslate_title() {
        return translate_title;
    }

    public void setTranslate_title(String translate_title) {
        this.translate_title = translate_title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMarkup() {
        return markup;
    }

    public void setMarkup(String markup) {
        this.markup = markup;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getCluster_id() {
        return cluster_id;
    }

    public void setCluster_id(int cluster_id) {
        this.cluster_id = cluster_id;
    }

    public int getGazTag_id() {
        return gaztag_id;
    }

    public void setGaz_id(int gaztag_id) {
        this.gaztag_id = gaztag_id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getNum_images() {
        return num_images;
    }

    public void setNum_images(int num_images) {
        this.num_images = num_images;
    }

    public int getNum_videos() {
        return num_videos;
    }

    public void setNum_videos(int num_videos) {
        this.num_videos = num_videos;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getNum_docs() {
        return num_docs;
    }

    public void setNum_docs(int num_docs) {
        this.num_docs = num_docs;
    }

    public String getTranslate_markup() {
        return translate_markup;
    }

    public void setTranslate_markup(String translate_markup) {
        this.translate_markup = translate_markup;
    }
}

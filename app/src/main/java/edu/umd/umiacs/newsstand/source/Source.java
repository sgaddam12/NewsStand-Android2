package edu.umd.umiacs.newsstand.source;

import java.io.Serializable;

public class Source implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public enum AllSourceType {MOST_RECENT, MOST_REPUTABLE, REAL_TIME}

    public enum SourceType {
        ALL_SOURCE, FEED_SOURCE,
        COUNTRY_SOURCE, LANGUAGE_SOURCE, BOUND_SOURCE
    }

    ;

    private String name;
    private String englishName;
    private String langCode;
    private String countryCode;
    private String countryName;

    private AllSourceType allSourceType;

    private int feedLink;
    private SourceType sourceType;

    private boolean selected;
    private boolean flag_selected;
    private boolean highlight;

    // Need Bounding Region vars

    private int numDocs;
    private int numHybrid2Docs;

    //Constructor for all sources
    public Source(String name, AllSourceType allSourceType) {
        this.name = name;
        this.setAllSourceType(allSourceType);
    }

    // Constructor for languages
    public Source(String langCode, String nativeLangName, String langName, int numDocs) {
        this.langCode = langCode;
        this.name = nativeLangName;
        this.englishName = langName;
        this.numDocs = numDocs;

        sourceType = SourceType.LANGUAGE_SOURCE;
    }

    // Constructor for feeds
    public Source(String name, String langCode, String countryCode, String countryName,
                  int feedLink, int numDocs) {
        this.name = name;
        this.langCode = langCode;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.feedLink = feedLink;
        this.numDocs = numDocs;

        sourceType = SourceType.FEED_SOURCE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public int getFeedLink() {
        return feedLink;
    }

    public void setFeedLink(int feedLink) {
        this.feedLink = feedLink;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isFlag_selected() {
        return flag_selected;
    }

    public void setFlag_selected(boolean flag_selected) {
        this.flag_selected = flag_selected;
    }

    public int getNumDocs() {
        return numDocs;
    }

    public void setNumDocs(int numDocs) {
        this.numDocs = numDocs;
    }

    public int getNumHybrid2Docs() {
        return numHybrid2Docs;
    }

    public void setNumHybrid2Docs(int numHybrid2Docs) {
        this.numHybrid2Docs = numHybrid2Docs;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public AllSourceType getAllSourceType() {
        return allSourceType;
    }

    public void setAllSourceType(AllSourceType allSourceType) {
        this.allSourceType = allSourceType;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}

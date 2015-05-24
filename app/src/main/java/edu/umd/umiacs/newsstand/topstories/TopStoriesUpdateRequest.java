package edu.umd.umiacs.newsstand.topstories;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import edu.umd.umiacs.newsstand.location.Article;

public class TopStoriesUpdateRequest {
    private final String TAG = "TopStoriesUpdateRequest";
    private static final String ns = null;

    private final String ITEM = "item";
    private final String TITLE = "title";
    private final String TRANSLATE_TITLE = "translate_title";
    private final String CLUSTER_ID = "cluster_id";
    private final String URL = "url";
    private final String DESCRIPTION = "description";
    private final String DOMAIN = "domain";
    private final String TOPIC = "topic";
    private final String NUM_DOCS = "num_docs";
    private final String NUM_IMAGES = "num_images";
    private final String NUM_VIDEOS = "num_videos";
    private final String TIME = "time";

    public ArrayList<Article> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            Log.i(TAG, "PARSE");
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList<Article> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Article> articles = new ArrayList<Article>();
        Log.i(TAG, "Read Feed");
        //parser.require(XmlPullParser.START_TAG, ns, "channel");
        //Log.i(TAG, parser.getText());
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the item tag.
            if (name.equals("channel")) {
            } else if (name.equals(ITEM)) {
                articles.add(readArticle(parser));
            } else {
                skip(parser);
            }
        }
        Log.i(TAG, "Found " + articles.size() + " markers");
        return articles;
    }

    private Article readArticle(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, ITEM);
        String title = null;
        String translate_title = null;
        String url = null;
        String description = null;
        String domain = null;
        String topic = null;
        String time = null;

        int cluster_id = 0;
        int num_images = 0;
        int num_videos = 0;
        int num_docs = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String currentElement = parser.getName();
            if (currentElement.equals(TITLE)) {
                title = read(parser, TITLE);
            } else if (currentElement.equals(TRANSLATE_TITLE)) {
                translate_title = read(parser, TRANSLATE_TITLE);
            } else if (currentElement.equals(URL)) {
                url = read(parser, URL);
            } else if (currentElement.equals(DESCRIPTION)) {
                description = read(parser, DESCRIPTION);
            } else if (currentElement.equals(DOMAIN)) {
                domain = read(parser, DOMAIN);
            } else if (currentElement.equals(TOPIC)) {
                topic = read(parser, TOPIC);
            } else if (currentElement.equals(TIME)) {
                time = read(parser, TIME);
            } else if (currentElement.equals(CLUSTER_ID)) {
                cluster_id = Integer.parseInt(read(parser, CLUSTER_ID));
            } else if (currentElement.equals(NUM_IMAGES)) {
                num_images = Integer.parseInt(read(parser, NUM_IMAGES));
            } else if (currentElement.equals(NUM_VIDEOS)) {
                num_videos = Integer.parseInt(read(parser, NUM_VIDEOS));
            } else if (currentElement.equals(NUM_DOCS)) {
                num_docs = Integer.parseInt(read(parser, NUM_DOCS));
            } else {
                skip(parser);
            }
        }
        return new Article(title, translate_title, url, description, domain, topic, time,
                cluster_id, num_images, num_videos, num_docs);
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String read(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return name;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


}

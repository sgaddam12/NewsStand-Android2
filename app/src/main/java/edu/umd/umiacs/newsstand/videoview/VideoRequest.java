package edu.umd.umiacs.newsstand.videoview;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import edu.umd.umiacs.newsstand.location.Article;

import android.util.Log;
import android.util.Xml;

public class VideoRequest {
    private final String TAG = "VideoRequest";
    private static final String ns = null;

    private final String ITEM = "item";
    private final String URL = "url";
    private final String TITLE = "title";
    private final String SOURCE_NAME = "name";
    private final String SOURCE_DOMAIN = "domain";
    private final String PUB_DATE = "pub_date";
    private final String IMG_PREVIEW = "preview";
    private final String DURATION = "length";

    public ArrayList<Video> parse(InputStream in)
            throws XmlPullParserException, IOException {
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

    private ArrayList<Video> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ArrayList<Video> videos = new ArrayList<Video>();
        Log.i(TAG, "Read Feed");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("channel")) {
            } else if (name.equals(ITEM)) {
                videos.add(readVideo(parser));
            } else {
                skip(parser);
            }
        }
        Log.i(TAG, "Found " + videos.size() + " videos");
        return videos;
    }

    private Video readVideo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, ITEM);
        String url = null;
        String title = null;
        String source_name = null;
        String source_domain = null;
        String pub_date = null;
        String img_preview = null;
        String duration = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String currentElement = parser.getName();
            if (currentElement.equals(URL)) {
                url = read(parser, URL);
            } else if (currentElement.equals(TITLE)) {
                title = read(parser, TITLE);
            } else if (currentElement.equals(SOURCE_NAME)) {
                source_name = read(parser, SOURCE_NAME);
            } else if (currentElement.equals(SOURCE_DOMAIN)) {
                source_domain = read(parser, SOURCE_DOMAIN);
            } else if (currentElement.equals(PUB_DATE)) {
                pub_date = read(parser, PUB_DATE);
            } else if (currentElement.equals(IMG_PREVIEW)) {
                img_preview = read(parser, IMG_PREVIEW);
            } else if (currentElement.equals(DURATION)) {
                duration = read(parser, DURATION);
            } else {
                skip(parser);
            }
        }
        return new Video(url, title, source_name, source_domain, pub_date, img_preview, duration);
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

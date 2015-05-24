package edu.umd.umiacs.newsstand.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class MapUpdateRequest {
    private final String TAG = "MapUpdateRequest";
    private static final String ns = null;

    private final String ITEM = "item";
    private final String NAME = "name";
    private final String TITLE = "title";
    private final String DESCRIPTION = "description";
    private final String TOPIC = "topic";
    private final String KEYWORD = "keyword";
    private final String IMG_URL = "img_url";
    private final String CAPTION = "caption";
    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";
    private final String CLUSTER_ID = "cluster_id";
    private final String CLUSTER_SCORE = "score";
    private final String GAZ_ID = "gaz_id";
    private final String HEIGHT = "height";
    private final String WIDTH = "width";
    private final String DISTINCTIVENESS = "distinctiveness";

    public ArrayList<MapMarker> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList<MapMarker> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<MapMarker> markers = new ArrayList<MapMarker>();
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
                markers.add(readMarker(parser));
            } else {
                skip(parser);
            }
        }
        //  Log.i(TAG, "Found " + markers.size() + " markers");
        return markers;
    }

    private MapMarker readMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, ITEM);
        String name = null;
        String title = null;
        String description = null;
        String topic = null;
        String keyword = null;
        String img_url = null;
        String caption = null;

        float latitude = 0;
        float longitude = 0;
        int cluster_id = 0;
        float cluster_score = 0;
        int gaz_id = 0;
        int height = 0;
        int width = 0;
        int distinctiveness = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String currentElement = parser.getName();
            if (currentElement.equals(NAME)) {
                name = read(parser, NAME);
            } else if (currentElement.equals(TITLE)) {
                title = read(parser, TITLE);
            } else if (currentElement.equals(DESCRIPTION)) {
                description = read(parser, DESCRIPTION);
            } else if (currentElement.equals(TOPIC)) {
                topic = read(parser, TOPIC);
            } else if (currentElement.equals(KEYWORD)) {
                keyword = read(parser, KEYWORD);
            } else if (currentElement.equals(IMG_URL)) {
                img_url = read(parser, IMG_URL);
            } else if (currentElement.equals(CAPTION)) {
                caption = read(parser, CAPTION);
            } else if (currentElement.equals(LATITUDE)) {
                latitude = Float.parseFloat(read(parser, LATITUDE));
            } else if (currentElement.equals(LONGITUDE)) {
                longitude = Float.parseFloat(read(parser, LONGITUDE));
            } else if (currentElement.equals(CLUSTER_ID)) {
                cluster_id = Integer.parseInt(read(parser, CLUSTER_ID));
            } else if (currentElement.equals(CLUSTER_SCORE)) {
                cluster_score = Float.parseFloat(read(parser, CLUSTER_SCORE));
            } else if (currentElement.equals(GAZ_ID)) {
                gaz_id = Integer.parseInt(read(parser, GAZ_ID));
            } else if (currentElement.equals(HEIGHT)) {
                height = Integer.parseInt(read(parser, HEIGHT));
            } else if (currentElement.equals(WIDTH)) {
                width = Integer.parseInt(read(parser, WIDTH));
            } else if (currentElement.equals(DISTINCTIVENESS)) {
                distinctiveness = Integer.parseInt(read(parser, DISTINCTIVENESS));
            } else {
                skip(parser);
            }
        }
        return new MapMarker(name, title, description, topic, keyword, img_url, caption, latitude,
                longitude, cluster_score, cluster_id, gaz_id, height, width, distinctiveness);
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





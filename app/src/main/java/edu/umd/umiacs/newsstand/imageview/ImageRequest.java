package edu.umd.umiacs.newsstand.imageview;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class ImageRequest {
    private final String TAG = "edu.umd.umiacs.newsstand.imageview.ImageRequest";
    private static final String ns = null;

    public final static int MAX_IMAGES = 500;

    private final String ITEM = "item";
    private final String IMAGE_URL = "media_html";
    private final String ARTICLE_URL = "redirect";
    private final String CAPTION = "caption";
    private final String HEIGHT = "height";
    private final String WIDTH = "width";
    private final String CLUSTER_ID = "cluster_id";
    private final String IMAGE_CLUSTER_ID = "image_cluster_id";
    private final String CLUSTER_SCORE = "cluster_score";
    private final String IS_DUPLICATE = "isDupe";

    public ArrayList<Image> parse(InputStream in)
            throws XmlPullParserException, IOException {
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

    private ArrayList<Image> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ArrayList<Image> images = new ArrayList<Image>();

        int numImagesFound = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Log.i(TAG, name);
            if (name.equals("channel")) {
            } else if (name.equals(ITEM)) {
                images.add(readImage(parser));
                numImagesFound++;
            } else {
                skip(parser);
            }
            if (numImagesFound >= MAX_IMAGES)
                break;
        }
        Log.i(TAG, "Found " + images.size() + " images");
        return images;
    }

    private Image readImage(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, ITEM);

        String imageURL = null;
        String articleURL = null;
        String caption = null;

        int height = 0;
        int width = 0;
        int clusterID = 0;
        int imageClusterID = 0;

        float clusterScore = 0.0f;
        boolean isDuplicate = false;

        while (parser.next() != XmlPullParser.END_TAG) {
            parser.getText();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String currentElement = parser.getName();
            if (currentElement.equals(IMAGE_URL)) {
                imageURL = read(parser, IMAGE_URL);
            } else if (currentElement.equals(ARTICLE_URL)) {
                articleURL = read(parser, ARTICLE_URL);
            } else if (currentElement.equals(CAPTION)) {
                caption = read(parser, CAPTION);
            } else if (currentElement.equals(HEIGHT)) {
                height = Integer.parseInt(read(parser, HEIGHT));
            } else if (currentElement.equals(WIDTH)) {
                width = Integer.parseInt(read(parser, WIDTH));
            } else if (currentElement.equals(CLUSTER_ID)) {
                clusterID = Integer.parseInt(read(parser, CLUSTER_ID));
            } else if (currentElement.equals(IMAGE_CLUSTER_ID)) {
                imageClusterID = Integer.parseInt(read(parser, IMAGE_CLUSTER_ID));
            } else if (currentElement.equals(CLUSTER_SCORE)) {
                clusterScore = Float.parseFloat(read(parser, CLUSTER_SCORE));
            } else if (currentElement.equals(IS_DUPLICATE)) {
                int val = Integer.parseInt(read(parser, IS_DUPLICATE));
                if (val == 1)
                    isDuplicate = true;
            } else {
                    skip(parser);
            }
        }
        return new Image(imageURL, articleURL, caption, height, width, clusterID, imageClusterID,
                clusterScore, isDuplicate);
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

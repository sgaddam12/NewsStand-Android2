package edu.umd.umiacs.newsstand.source.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import edu.umd.umiacs.newsstand.source.Source;
import edu.umd.umiacs.newsstand.source.Source.SourceType;

import android.util.Log;
import android.util.Xml;

public class SourceUpdateRequest {
    private final String TAG = "SourceUpdateRequest";
    private static final String ns = null;

    private final String OBJECT = "object";
    private final String NAME = "name";
    private final String LANG_CODE = "lang";
    private final String COUNTRY_CODE = "ccode";
    private final String COUNTRY_NAME = "cname";
    private final String FEED_LINK = "feed-link";
    private final String NUM_DOCS = "url-count";
    private final String LANG_NAME = "langname";
    private final String NATIVE_LANG_NAME = "native-langname";

    private SourceType mSourceType;

    public SourceUpdateRequest() {
        mSourceType = SourceType.FEED_SOURCE;
    }

    public SourceUpdateRequest(SourceType sourceType) {
        mSourceType = sourceType;
    }

    public ArrayList<Source> parse(InputStream in) throws XmlPullParserException, IOException {
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

    private ArrayList<Source> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Source> sources = new ArrayList<Source>();
        Log.i(TAG, "Read Feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("objects")) {
            } else if (name.equals(OBJECT)) {
                sources.add(readSource(parser));
            } else {
                skip(parser);
            }
        }
        Log.i(TAG, "Found " + sources.size() + " Sources");
        return sources;
    }

    private Source readSource(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, OBJECT);
        String name = null;
        String langCode = null;
        String countryCode = null;
        String countryName = null;
        String langName = null;
        String nativeLangName = null;
        int feedLink = 0;
        int numDocs = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String currentElement = parser.getName();
            //  Log.i(TAG, currentElement);
            if (currentElement.equals(NAME)) {
                name = readName(parser);
            } else if (currentElement.equals(LANG_CODE)) {
                langCode = readLanguageCode(parser);
            } else if (currentElement.equals(COUNTRY_CODE)) {
                countryCode = readCountryCode(parser);
            } else if (currentElement.equals(COUNTRY_NAME)) {
                countryName = readCountryName(parser);
            } else if (currentElement.equals(FEED_LINK)) {
                feedLink = readFeedLink(parser);
            } else if (currentElement.equals(NUM_DOCS)) {
                numDocs = readNumDocs(parser);
            } else if (currentElement.equals(LANG_NAME)) {
                langName = readLangName(parser);
            } else if (currentElement.equals(NATIVE_LANG_NAME)) {
                nativeLangName = readNativeLangName(parser);
            } else {
                skip(parser);
            }
        }

        if (mSourceType == SourceType.LANGUAGE_SOURCE) {
            return new Source(langCode, nativeLangName, langName, numDocs);
        } else if (mSourceType == SourceType.FEED_SOURCE) {
            return new Source(name, langCode, countryCode, countryName, feedLink, numDocs);
        } else {
            return null;
        }
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, NAME);
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, NAME);
        return name;
    }

    private String readLanguageCode(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, LANG_CODE);
        String langCode = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, LANG_CODE);
        return langCode;
    }

    private String readCountryCode(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, COUNTRY_CODE);
        String countryCode = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, COUNTRY_CODE);
        return countryCode;
    }

    private String readCountryName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, COUNTRY_NAME);
        String countryName = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, COUNTRY_NAME);
        return countryName;
    }

    private int readFeedLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, FEED_LINK);
        int feedLink = Integer.parseInt(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, FEED_LINK);
        return feedLink;
    }

    private int readNumDocs(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, NUM_DOCS);
        int numDocs = Integer.parseInt(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, NUM_DOCS);
        return numDocs;
    }

    private String readLangName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, LANG_NAME);
        String langName = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, LANG_NAME);
        return langName;
    }

    private String readNativeLangName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, NATIVE_LANG_NAME);
        String nativeLangName = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, NATIVE_LANG_NAME);
        return nativeLangName;
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

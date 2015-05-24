package edu.umd.umiacs.newsstand.general;

import android.os.AsyncTask;
import android.util.Log;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Brendan on 10/17/13.
 */

public class NoResultsRequest extends AsyncTask<String, Void, String> {
    public static String TAG = "edu.umd.umiacs.newsstand.general.NoResultsRequest";

    @Override
    protected String doInBackground(String... urls) {
        try {
            sendRequestNoResults(urls[0]);
        } catch (IOException e) {
            Log.e(TAG, "IOException");
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException");
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown");
        }
        return "";
    }

    private void sendRequestNoResults(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;

        try {
            stream = downloadUrl(urlString);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        //    Log.i(TAG, "Download URL " + urlString);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }
}

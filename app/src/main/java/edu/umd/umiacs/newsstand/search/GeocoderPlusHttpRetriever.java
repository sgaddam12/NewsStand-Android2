/* Taken from GeocoderPlus Library https://github.com/bricolsoftconsulting/GeocoderPlus */

package edu.umd.umiacs.newsstand.search;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class GeocoderPlusHttpRetriever {
    // Members
    DefaultHttpClient httpclient = null;

    // Constructor
    public GeocoderPlusHttpRetriever() {
        httpclient = new DefaultHttpClient();
    }

    // Document retrieval function
    public String retrieve(String url) throws ClientProtocolException, IOException {
        // Declare
        String response = null;

        // Connect to server and get JSON response
        HttpGet request = new HttpGet(url);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        response = httpclient.execute(request, responseHandler);

        // Return
        return response;
    }
}

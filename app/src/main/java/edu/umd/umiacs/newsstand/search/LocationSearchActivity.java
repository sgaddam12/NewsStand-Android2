/* GeocodeTask taken from GeocoderPlus Library https://github.com/bricolsoftconsulting/GeocoderPlus */

package edu.umd.umiacs.newsstand.search;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SearchView;

import edu.umd.umiacs.newsstand.MainActivity;

public class LocationSearchActivity extends Activity implements SearchView.OnQueryTextListener {
    private final String TAG = "Location Search Activitiy";

    public static final String URL_MAPS_GEOCODE = "http://maps.googleapis.com/maps/api/geocode/json";
    public static final String PARAM_SENSOR = "sensor";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_LANGUAGE = "language";
    public static final String PARAM_REGION = "region";

    private MainActivity mainActivity;

    // Members
    Locale mLocale;
    boolean mUseRegionBias = false;

    public LocationSearchActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, "Query Text Submit: " + query);

        GeocodeTask geocodeTask = new GeocodeTask();
        geocodeTask.execute(query);

        return true;
    }

    public List<GeocoderPlusAddress> geocodeLocation(String locationName) {
        // Geocode the location
        GeocoderPlusGeocoder geocoder = new GeocoderPlusGeocoder();
        try {
            List<GeocoderPlusAddress> addresses = geocoder.getFromLocationName(locationName);
            return addresses;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onGeocodeTaskComplete(GeocoderPlusAddress foundAddress) {
        mainActivity.dismissLocationSearchView();
        mainActivity.updateMapToAddress(foundAddress);
    }

    public class GeocodeTask extends AsyncTask<String, Void, List<GeocoderPlusAddress>> {
        // Declare
        private ProgressDialog mPleaseWaitDialog = null;

        public void showLocationPendingDialog() {
            Log.i(TAG, "Show location pending ");

            if (mPleaseWaitDialog != null) {
                return;
            }

            mPleaseWaitDialog = new ProgressDialog(mainActivity);
            mPleaseWaitDialog.setMessage("Searching for location...");
            mPleaseWaitDialog.setTitle("Please wait");
            mPleaseWaitDialog.setIndeterminate(true);
            mPleaseWaitDialog.show();
        }

        public void cancelLocationPendingDialog() {
            if (mPleaseWaitDialog != null) {
                mPleaseWaitDialog.dismiss();
                mPleaseWaitDialog = null;
            }
        }

        @Override
        protected void onPostExecute(List<GeocoderPlusAddress> addresses) {
            Log.i(TAG, "On Post Execute 1");

            super.onPostExecute(addresses);
            cancelLocationPendingDialog();

            Log.i(TAG, "On Post Execute 2");

            // Check the number of results
            if (addresses != null) {
                Log.i(TAG, "On Post Execute 3");
                if (addresses.size() > 1) {
                    Log.i(TAG, "On Post Execute 4");
                    // Determine which address to display
                    showLocationPicker(addresses);
                    Log.i(TAG, "On Post Execute 4b");
                } else {
                    Log.i(TAG, "On Post Execute 5");
                    // Display address
                    onGeocodeTaskComplete(addresses.get(0));
                }
            } else {
                Log.i(TAG, "On Post Execute 6");
                showAlert("No results found!", "Error");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLocationPendingDialog();
        }

        @Override
        protected List<GeocoderPlusAddress> doInBackground(String... args) {
            // Declare
            List<GeocoderPlusAddress> addresses;

            // Extract parameters
            String locationName = args[0];

            // Geocode
            addresses = geocodeLocation(locationName);

            // Return
            return addresses;
        }
    }

    ;

    private void showAlert(final String message, String title) {
        Log.i(TAG, "Show Alert ");

        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setMessage(message);
            builder.setCancelable(true);
            builder.setTitle(title);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void showLocationPicker(final List<GeocoderPlusAddress> results) {
        Log.i(TAG, "Show Location Picker");
        // Check input
        if (results.size() == 0) return;

        Log.i(TAG, "Show Location Picker 2");
        // Do not build dialog if the activity is finishing
        if (isFinishing()) return;

        Log.i(TAG, "Show Location Picker 3");
        // Convert the list of results to display strings
        final String[] items = getAddressStringArray(results);

        // Display alert
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        Log.i(TAG, "Show Location Picker 4");
        builder.setTitle("Did you mean:");
        Log.i(TAG, "Show Location Picker 5");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int itemIndex) {
                // Display the position
                Log.i(TAG, "Calling on geocode task complete ");
                onGeocodeTaskComplete(results.get(itemIndex));
            }
        });
        builder.create().show();
    }

    private String[] getAddressStringArray(List<GeocoderPlusAddress> results) {
        // Declare
        ArrayList<String> result = new ArrayList<String>();
        String[] resultType = new String[0];

        // Iterate over addresses
        for (int i = 0; i < results.size(); i++) {
            // Get the data
            String formattedAddress = results.get(i).getFormattedAddress();
            if (formattedAddress == null) formattedAddress = "";
            result.add(formattedAddress);
        }

        // Return
        if (result.size() == 0) {
            return null;
        } else {
            return (String[]) result.toArray(resultType);
        }
    }
}

	
	


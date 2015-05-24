/* Taken from GeocoderPlus Library https://github.com/bricolsoftconsulting/GeocoderPlus */

package edu.umd.umiacs.newsstand.search;

public class GeocoderPlusPosition {
    // Members
    double mLatitude;
    double mLongitude;

    // Getters and setters
    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    // Constructors
    public GeocoderPlusPosition() {
    }

    public GeocoderPlusPosition(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }
}

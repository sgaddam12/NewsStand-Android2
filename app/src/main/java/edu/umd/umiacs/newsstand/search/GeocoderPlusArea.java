/* Taken from GeocoderPlus Library https://github.com/bricolsoftconsulting/GeocoderPlus */

package edu.umd.umiacs.newsstand.search;

public class GeocoderPlusArea {
    // Members
    GeocoderPlusPosition mNorthEast;
    GeocoderPlusPosition mSouthWest;

    // Constructors
    public GeocoderPlusArea() {
    }

    public GeocoderPlusArea(GeocoderPlusPosition northEast, GeocoderPlusPosition southWest) {
        mNorthEast = northEast;
        mSouthWest = southWest;
    }

    // Getters and setters
    public GeocoderPlusPosition getNorthEast() {
        return mNorthEast;
    }

    public void setNorthEast(GeocoderPlusPosition northEast) {
        mNorthEast = northEast;
    }

    public GeocoderPlusPosition getSouthWest() {
        return mSouthWest;
    }

    public void setSouthWest(GeocoderPlusPosition southWest) {
        mSouthWest = southWest;
    }

    public double getLatitudeSpan() {
        double maxLatitude = mNorthEast.getLatitude();
        double minLatitude = mSouthWest.getLatitude();
        return maxLatitude - minLatitude;
    }

    public double getLongitudeSpan() {
        double maxLongitude = mNorthEast.getLongitude();
        double minLongitude = mSouthWest.getLongitude();
        return (maxLongitude - minLongitude);
    }

    public int getLatitudeSpanE6() {
        return (int) ((getLatitudeSpan()) * 1E6);
    }

    public int getLongitudeSpanE6() {
        return (int) ((getLongitudeSpan()) * 1E6);
    }
}

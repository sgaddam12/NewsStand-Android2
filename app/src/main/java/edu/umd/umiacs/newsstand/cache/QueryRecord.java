package edu.umd.umiacs.newsstand.cache;

import java.io.Serializable;

/**
 * Created by Brendan on 6/17/13.
 */
public class QueryRecord implements Serializable {
    private double latLow;
    private double lonLow;
    private double latHigh;
    private double lonHigh;

    public QueryRecord(DeviceGridCell deviceGridCell) {
        double lowX = deviceGridCell.lowerLeftX;
        double lowY = deviceGridCell.lowerLeftY;
        double highX = deviceGridCell.upperRightX;
        double highY = deviceGridCell.upperRightY;

        latLow = y2lat(lowY - 180.0);
        lonLow = lowX - 180.0;
        latHigh = y2lat(highY - 180.0);
        lonHigh = highX - 180.0;
    }

    public DeviceGridCell toDeviceGridCell() {
        double lx, ly, ux, uy;
        lx = lonLow + 180;
        ux = lonHigh + 180;
        ly = lat2y(latLow) + 180;
        uy = lat2y(latHigh) + 180;
        return new DeviceGridCell(lx, ly, ux, uy);
    }

    public QueryRecord(double latLow, double lonLow, double latHigh, double lonHigh) {
        this.latLow = latLow;
        this.lonLow = lonLow;
        this.latHigh = latHigh;
        this.lonHigh = lonHigh;
    }

    public double getLatLow() {
        return latLow;
    }

    public double getLonLow() {
        return lonLow;
    }

    public double getLatHigh() {
        return latHigh;
    }

    public double getLonHigh() {
        return lonHigh;
    }

    private double y2lat(double y) {
        return 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
    }

    private double lat2y(double y) {
        return Math.log(Math.tan((y / 180 * Math.PI + Math.PI / 2) / 2)) / Math.PI * 180;
    }

    public String toString() {
        return latLow + "," + lonLow + "," + latHigh + "," + lonHigh;
    }
}

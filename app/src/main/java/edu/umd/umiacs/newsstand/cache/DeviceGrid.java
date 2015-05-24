package edu.umd.umiacs.newsstand.cache;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Brendan on 6/3/13.
 */
public class DeviceGrid {
    private final static String TAG = "edu.umd.umiacs.newsstand.cache.DeviceGrid";

    private PointF mBounds;
    static final float[] gridDivision = {0.005f, 0.01f, 0.05f, 0.75f, 0.1f, 0.25f, 0.5f, 1.0f, 2.0f, 4.0f, 8.0f, 10.0f,
            12.0f, 15.0f, 18.0f, 20.0f, 24.0f, 30.0f, 36.0f, 40.0f, 45.0f, 60.0f, 72.0f, 90.0f, 120.0f,
            180.0f, 360.0f};
    private float xDiv;
    private float yDiv;

    public DeviceGrid(PointF bounds) {
        mBounds = bounds;
        createGrid();
    }

    private void createGrid() {
        for (int i = 0; i < gridDivision.length; i++)
            if (mBounds.x <= gridDivision[i]) {
                xDiv = gridDivision[i];
                break;
            }

        for (int i = 0; i < gridDivision.length; i++) {
            if (mBounds.y <= gridDivision[i]) {
                yDiv = gridDivision[i];
                break;
            }
        }
    }

    public ArrayList<DeviceGridCell> getGridCellsForWindowAndZoom(PointF lowerLeftCorner, PointF upperRightCorner,
                                                                  PointF windowBounds) {
        ArrayList<DeviceGridCell> gridCells = new ArrayList<DeviceGridCell>();

        float currentXDiv = 360.0f;
        float currentYDiv = 360.0f;

        for (int i = 0; i < gridDivision.length; i++)
            if (windowBounds.x <= gridDivision[i]) {
                currentXDiv = gridDivision[i];
                break;
            }

        for (int i = 0; i < gridDivision.length; i++)
            if (windowBounds.y <= gridDivision[i]) {
                currentYDiv = gridDivision[i];
                break;
            }

        //Log.i(TAG, "Xdiv: " + xDiv + " ydiv: "  + yDiv);

        int numColumns = (int) Math.round((360000.0f / (currentXDiv * 1000.0f)));
        int numRows = (int) Math.round((360000.0f / (currentYDiv * 1000.0f)));

        // Calculate lowerLeftCorner Grid cell
        DeviceGridCell lowerLeftCornerGridCell = null;
        for (int i = 1; i <= numColumns; i++) {
            float currentColVal = i * currentXDiv;
            if (lowerLeftCorner.x <= currentColVal) {
                for (int j = 1; j <= numRows; j++) {
                    float currentRowVal = j * currentYDiv;
                    if (lowerLeftCorner.y <= currentRowVal) {
                        lowerLeftCornerGridCell = new DeviceGridCell(currentColVal - currentXDiv, currentRowVal - currentYDiv,
                                currentColVal, currentRowVal);
                        break;
                    }
                }
                break;
            }
        }

        // Calculate upperRightCorner
        DeviceGridCell upperRightCornerGridCell = null;
        for (int i = 0; i < numColumns; i++) {
            float leftColVal = i * currentXDiv;
            float rightColVal = leftColVal + currentXDiv;
            if (upperRightCorner.x <= rightColVal && upperRightCorner.x >= leftColVal) {
                for (int j = 0; j < numRows; j++) {
                    float lowerRowVal = j * currentYDiv;
                    float upperRowVal = lowerRowVal + currentYDiv;
                    if (upperRightCorner.y <= upperRowVal && upperRightCorner.y >= lowerRowVal) {
                        upperRightCornerGridCell = new DeviceGridCell(leftColVal, lowerRowVal, rightColVal, upperRowVal);
                        break;
                    }
                }
                break;
            }
        }

        // Check how many grid cells the current window takes up
        // If lowerLeftCornerGridCell == upperRightCornerGridCell then it takes up 1
        // If lowerLeftCornerGridCell & upperRightCornerGridCell share same row or col then 2
        // else 4
        if (lowerLeftCornerGridCell.equals(upperRightCornerGridCell)) {
            gridCells.add(lowerLeftCornerGridCell);
        } else if (lowerLeftCornerGridCell.isSameRow(upperRightCornerGridCell) ||
                lowerLeftCornerGridCell.isSameRow(upperRightCornerGridCell)) {
            gridCells.add(lowerLeftCornerGridCell);
            gridCells.add(upperRightCornerGridCell);
        } else { // Four grid cells
            gridCells.add(lowerLeftCornerGridCell);
            gridCells.add(upperRightCornerGridCell);

            DeviceGridCell upperLeftCornerGridCell = new DeviceGridCell(lowerLeftCornerGridCell.getLowerLeftX(),
                    upperRightCornerGridCell.getLowerLeftY(), lowerLeftCornerGridCell.getUpperRightX(),
                    upperRightCornerGridCell.getUpperRightY());
            DeviceGridCell bottomRightCornerGridCell = new DeviceGridCell(lowerLeftCornerGridCell.getUpperRightX(),
                    lowerLeftCornerGridCell.getLowerLeftY(), upperRightCornerGridCell.getUpperRightX(),
                    upperRightCornerGridCell.getLowerLeftY());

            gridCells.add(upperLeftCornerGridCell);
            gridCells.add(bottomRightCornerGridCell);
        }

        return gridCells;
    }

    public ArrayList<PolylineOptions> getGridPolylines(PointF windowBounds) {
        ArrayList<PolylineOptions> polylines = new ArrayList<PolylineOptions>();

        float currentXDiv = 360.0f;
        float currentYDiv = 360.0f;

        for (int i = 0; i < gridDivision.length; i++)
            if (windowBounds.x <= gridDivision[i]) {
                currentXDiv = gridDivision[i];
                break;
            }

        for (int i = 0; i < gridDivision.length; i++)
            if (windowBounds.y <= gridDivision[i]) {
                currentYDiv = gridDivision[i];
                break;
            }

        //Log.i(TAG, "windowbounds " + windowBounds.x + " windowbounds y " + windowBounds.y +  "xdiv: " + currentXDiv + " ydiv: " + currentYDiv);

        int numColumns = (int) Math.round((360000.0f / (currentXDiv * 1000.0f)));
        int numRows = (int) Math.round((360000.0f / (currentYDiv * 1000.0f)));

        for (int i = 0; i < numColumns; i++) {
            float currentColVal = i * currentXDiv;
            double longitude = currentColVal - 180.0;
            //    Log.i(TAG, "Longitude: " + longitude);
            polylines.add(new PolylineOptions()
                    .add(new LatLng(-85.0511, longitude), new LatLng(85.0511, longitude))
                    .width(5)
                    .color(Color.BLACK));
        }

        for (int j = 0; j < numRows; j++) {
            float currentRowVal = j * currentYDiv;
            double latitude = y2lat(currentRowVal - 180);
            //   Log.i(TAG, "Latitude: " + latitude);
            polylines.add(new PolylineOptions()
                    .add(new LatLng(latitude, 0.0), new LatLng(latitude, 179.0))
                    .width(5)
                    .color(Color.BLACK));
            polylines.add(new PolylineOptions()
                    .add(new LatLng(latitude, -179.0), new LatLng(latitude, 0.0))
                    .width(5)
                    .color(Color.BLACK));
        }

        return polylines;
    }

    private double lat2y(double lat) {
        return 180 / Math.PI * Math.log(Math.tan(Math.PI / 4 + lat * (Math.PI / 180) / 2));
    }

    private double y2lat(double y) {
        return 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
    }
}

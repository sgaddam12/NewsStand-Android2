package edu.umd.umiacs.newsstand.cache;

/**
 * Created by Brendan on 6/3/13.
 */
public class DeviceGridCell {
    protected double lowerLeftX;
    protected double lowerLeftY;
    protected double upperRightX;
    protected double upperRightY;

    public boolean showOnScreen = true;

    public static double EPSILON = 5.96e-08;

    public DeviceGridCell(DeviceGridCell deviceGridCell) {
        this(deviceGridCell.getLowerLeftX(),
                deviceGridCell.getLowerLeftY(),
                deviceGridCell.getUpperRightX(),
                deviceGridCell.getUpperRightY(),
                deviceGridCell.showOnScreen);
    }

    public DiscretizedGridCell toDescretizedGridCell() {
        double x = upperRightX - lowerLeftX, y = upperRightY - lowerLeftY;
        int dimX = DeviceGrid.gridDivision.length, dimY = 0;
        for (int i = 0; i < DeviceGrid.gridDivision.length; i++)
            if (x <= DeviceGrid.gridDivision[i]) {
                dimX = i;
                break;
            }

        for (int i = 0; i < DeviceGrid.gridDivision.length; i++)
            if (y <= DeviceGrid.gridDivision[i]) {
                dimY = i;
                break;
            }
        int lx = (int) (Math.ceil(lowerLeftX / DeviceGrid.gridDivision[dimX] - EPSILON) + EPSILON),
                ly = (int) (Math.ceil(lowerLeftY / DeviceGrid.gridDivision[dimY] - EPSILON) + EPSILON);

        return new DiscretizedGridCell(lx, ly, dimX, dimY);

    }

    public DeviceGridCell(double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY) {
        this(lowerLeftX, lowerLeftY, upperRightX, upperRightY, true);
    }

    public DeviceGridCell(double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY, boolean showOnScreen) {
        this.lowerLeftX = lowerLeftX;
        this.lowerLeftY = lowerLeftY;
        this.upperRightX = upperRightX;
        this.upperRightY = upperRightY;
        this.showOnScreen = showOnScreen;
    }

    public double getLowerLeftX() {
        return lowerLeftX;
    }

    public double getLowerLeftY() {
        return lowerLeftY;
    }

    public double getUpperRightX() {
        return upperRightX;
    }

    public double getUpperRightY() {
        return upperRightY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        //if (!(obj instanceof DeviceGridCell) || !(obj instanceof DeviceGridCellMarker))
       //     return false;

        DeviceGridCell otherGridCell = (DeviceGridCell) obj;

        // Modify numbers so 0 can not be in denominator (or numerator)
        if (Math.abs((lowerLeftX + 1.0) / (otherGridCell.getLowerLeftX() + 1.0) - 1) < EPSILON &&
                Math.abs((lowerLeftY + 1.0) / (otherGridCell.getLowerLeftY() + 1.0) - 1) < EPSILON &&
                Math.abs((upperRightX + 1.0) / (otherGridCell.getUpperRightX() + 1.0) - 1) < EPSILON &&
                Math.abs((upperRightY + 1.0) / (otherGridCell.getUpperRightY() + 1.0) - 1) < EPSILON) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (int) ((lowerLeftX * 751 + lowerLeftY * 237 + upperRightX * 29 + upperRightY) * 1000.0 + EPSILON);
    }

    public boolean isSameRow(DeviceGridCell otherGridCell) {
        if (otherGridCell == null)
            return false;

        if (Math.abs((upperRightY + 1.0) / (otherGridCell.getUpperRightY() + 1.0) - 1) < EPSILON)
            return true;

        return false;
    }

    public boolean isSameColumn(DeviceGridCell otherGridCell) {
        if (otherGridCell == null) {
            return false;
        }

        if (Math.abs((upperRightX + 1.0) / (otherGridCell.getUpperRightX() + 1.0) - 1) < EPSILON)
            return true;

        return false;
    }

    @Override
    public String toString() {
        return "LowerLeft: (" + lowerLeftX + ", " + lowerLeftY + ")  UpperRight: (" + upperRightX +
                ", " + upperRightY + ")";
    }
}

package edu.umd.umiacs.newsstand.cache;

public class DiscretizedGridCell {
    public int lowX, lowY;
    public int dimX, dimY;

    public DiscretizedGridCell(int lowX, int lowY, int dimX, int dimY) {
        this.lowX = lowX;
        this.lowY = lowY;
        this.dimX = dimX;
        this.dimY = dimY;
    }

    public boolean isStay(DiscretizedGridCell laterCell) {
        return lowX == laterCell.lowX && lowY == laterCell.lowY
                && dimX == laterCell.dimX && dimY == laterCell.dimY;
    }

    public boolean isPan(DiscretizedGridCell laterCell) {
        return !(lowX == laterCell.lowX && lowY == laterCell.lowY)
                && dimX == laterCell.dimX && dimY == laterCell.dimY;
    }

    public boolean isZout(DiscretizedGridCell laterCell) {
        return dimX < laterCell.dimX || dimY < laterCell.dimY;
    }

    public boolean isZin(DiscretizedGridCell laterCell, int direction) {
        if (dimX <= laterCell.dimX || dimY <= laterCell.dimX)
            return false;
        int t1 = laterCell.lowX - 2 * lowX, t2 = laterCell.lowY - 2 * lowY;
        return t1 == direction / 2 && t2 == direction % 2;
    }
}

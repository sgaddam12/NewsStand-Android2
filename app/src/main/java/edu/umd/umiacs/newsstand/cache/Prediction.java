package edu.umd.umiacs.newsstand.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.PointF;
import android.util.Log;

/**
 * Created by Chang
 */
public class Prediction {
    private final static String TAG = "edu.umd.umiacs.newsstand.cache.Prediction";
    public static int threshold = 50;

    private static final float[] gridDivision = {0.005f, 0.01f, 0.05f, 0.1f, 0.25f, 0.5f, 1.0f, 2.0f, 4.0f, 8.0f, 10.0f,
            12.0f, 15.0f, 18.0f, 20.0f, 24.0f, 30.0f, 36.0f, 40.0f, 45.0f, 60.0f, 72.0f, 90.0f, 120.0f,
            180.0f, 360.0f};

    private float pStay = 0.431f;
    private float pPan = 0.0283f;
    private float[] pZin = {0.059f, 0.068f, 0.095f, 0.087f};
    private float pZout = 0.027f;

    private static void addProb(Map<DeviceGridCell, Double> maps, DeviceGridCell cell, double value) {
        if (!maps.containsKey(cell))
            maps.put(cell, value);
        else
            maps.put(cell, maps.get(cell) + value);
    }

    public void learnModel(ArrayList<QueryRecord> records) {
        if (records.size() < threshold)
            return;
        int stay = 0, pan = 0, zout = 0;
        int[] zin = {0, 0, 0, 0};
        DiscretizedGridCell last = records.get(0).toDeviceGridCell().toDescretizedGridCell();
        for (int i = 1; i < records.size(); ++i) {
            DiscretizedGridCell now = records.get(i).toDeviceGridCell().toDescretizedGridCell();
            if (last.isStay(now)) {
                stay++;
            } else if (last.isPan(now)) {
                pan++;
            } else if (last.isZout(now)) {
                zout++;
            } else {
                for (int j = 0; j < 4; ++j)
                    if (last.isZin(now, j)) {
                        zin[j]++;
                        break;
                    }
            }
        }
        int sum = stay + pan + zout + zin[0] + zin[1] + zin[2] + zin[3];
        pStay = ((float) stay) / sum;
        pPan = ((float) pan) / sum;
        pZout = ((float) zout) / sum;
        for (int i = 0; i < 4; ++i) {
            pZin[i] = ((float) zin[i]) / sum;
        }
        Log.i(TAG, stay + "\t" + pan + "\t" + zout + "\t" + zin[0] + "\t" + zin[1] + "\t" + zin[2] + "\t" + zin[3]);
    }

    public DeviceGridCell[] makePrediction(ArrayList<DeviceGridCell> grids, int K, PointF windowBounds, int cacheLimit) {

        long time = System.currentTimeMillis();

        float currentXDiv = 360.0f;
        float currentYDiv = 360.0f;

        float currentXDivZin = 360.0f;
        float currentYDivZin = 360.0f;

        float currentXDivZout = 360.0f;
        float currentYDivZout = 360.0f;

        for (int i = 0; i < gridDivision.length; i++)
            if (windowBounds.x <= gridDivision[i]) {
                currentXDiv = gridDivision[i];

                if (i == 0)
                    currentXDivZin = -1f;
                else
                    currentXDivZin = gridDivision[i - 1];

                if (i == gridDivision.length - 1)
                    currentXDivZout = -1f;
                else
                    currentXDivZout = gridDivision[i + 1];
                break;
            }

        for (int i = 0; i < gridDivision.length; i++)
            if (windowBounds.y <= gridDivision[i]) {
                currentYDiv = gridDivision[i];

                if (i == 0)
                    currentYDivZin = -1f;
                else
                    currentYDivZin = gridDivision[i - 1];

                if (i == gridDivision.length - 1)
                    currentYDivZout = -1f;
                else
                    currentYDivZout = gridDivision[i + 1];
                break;
            }

        int numColumns = (int) Math.round((360000.0f / (currentXDiv * 1000.0f)));
        int numRows = (int) Math.round((360000.0f / (currentYDiv * 1000.0f)));

        Map<DeviceGridCell, Double> DS = new HashMap<DeviceGridCell, Double>();
        Map<DeviceGridCell, Double> now_ds = new HashMap<DeviceGridCell, Double>();
        Map<DeviceGridCell, Double> new_ds = new HashMap<DeviceGridCell, Double>();

        for (DeviceGridCell cell : grids) {
            now_ds.put(cell, 1.0);
        }

        for (int k = 0; k < K; ++k) {
            for (Map.Entry<DeviceGridCell, Double> ent : now_ds.entrySet()) {
                DeviceGridCell cell = ent.getKey();
                double p = ent.getValue();

                int x = (int) (cell.lowerLeftX / currentXDiv);
                int y = (int) (cell.lowerLeftY / currentYDiv);

                // stay
                addProb(new_ds, cell, pStay * p);

                // pan
                for (int i = -1; i < 2; ++i)
                    for (int j = -1; j < 2; ++j)
                        if (i != 0 || j != 0) {
                            if (x + i >= 0 && x + i < numColumns && y + j >= 0 && y + j < numRows) {
                                DeviceGridCell c = new DeviceGridCell(
                                        cell.lowerLeftX + i * currentXDiv, cell.lowerLeftY + j * currentYDiv,
                                        cell.lowerLeftX + (i + 1) * currentXDiv, cell.lowerLeftY + (j + 1) * currentYDiv, false);
                                addProb(new_ds, c, pPan * p);
                            }
                        }

                // zoomin
                if (currentXDivZin > 0) {
                    int x1 = (int) (cell.lowerLeftX / currentXDivZin + DeviceGridCell.EPSILON);
                    int y1 = (int) (cell.lowerLeftY / currentYDivZin + DeviceGridCell.EPSILON);

                    for (int i = 0; i < 2; ++i)
                        for (int j = 0; j < 2; ++j) {
                            DeviceGridCell c = new DeviceGridCell(
                                    (x1 + i) * currentXDivZin, (y1 + j) * currentYDivZin,
                                    (x1 + i + 1) * currentXDivZin, (y1 + j + 1) * currentYDivZin, false);
                            addProb(new_ds, c, pZin[i * 2 + j] * p);
                        }
                }

                // zoomout
                if (currentXDivZout > 0) {
                    int x1 = (int) (cell.lowerLeftX / currentXDivZout + DeviceGridCell.EPSILON);
                    int y1 = (int) (cell.lowerLeftY / currentYDivZout + DeviceGridCell.EPSILON);

                    DeviceGridCell c = new DeviceGridCell(
                            (x1) * currentXDivZout, (y1) * currentYDivZout,
                            (x1 + 1) * currentXDivZout, (y1 + 1) * currentYDivZout, false);
                    addProb(new_ds, c, pZout * p);
                }
            }
            for (Map.Entry<DeviceGridCell, Double> ent : new_ds.entrySet()) {
                addProb(DS, ent.getKey(), ent.getValue());
            }
            Map<DeviceGridCell, Double> tmp = now_ds;
            now_ds = new_ds;
            new_ds = tmp;
            new_ds.clear();
        }

        DeviceGridCell[] ret = new DeviceGridCell[cacheLimit + 1];
        float[] scores = new float[cacheLimit + 1];

        int tot = 0;
        if (ret.length > 1) {
            for (DeviceGridCell cell : grids) {
                ret[tot] = cell;
                scores[tot++] = 100000f;
            }
        }

        for (Map.Entry<DeviceGridCell, Double> i : DS.entrySet()) {
            boolean f = false;
            for (DeviceGridCell cell : grids)
                if (cell.equals(i.getKey())) {
                    f = true;
                    break;
                }
            if (f)
                continue;
            int j = tot - 1;
            for (; j > 0; --j) {
                if (i.getValue() > scores[j]) {
                    scores[j + 1] = scores[j];
                    ret[j + 1] = ret[j];
                } else
                    break;
            }
            ret[j + 1] = i.getKey();
            scores[j + 1] = i.getValue().floatValue();
            if (tot < cacheLimit)
                tot++;
        }

        //	Log.i("prediction", "Prediction Time: "+(System.currentTimeMillis() - time)+" ms");
        return ret;
    }
}

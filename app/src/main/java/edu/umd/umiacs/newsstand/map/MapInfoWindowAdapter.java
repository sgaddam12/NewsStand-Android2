package edu.umd.umiacs.newsstand.map;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import edu.umd.umiacs.newsstand.R;

/**
 * Created by Brendan on 5/28/13.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater mInflater = null;

    public MapInfoWindowAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = mInflater.inflate(R.layout.info_layout, null);
        TextView title = (TextView) infoWindow.findViewById(R.id.infoWindowTitle);
        TextView subTitle = (TextView) infoWindow.findViewById(R.id.infoWindowSubtitle);

        title.setText(marker.getTitle());
        subTitle.setText(marker.getSnippet());

        //infoWindow.setAlpha(0.2f);
       // infoWindow.setBackgroundColor(R.color.black);

        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

}

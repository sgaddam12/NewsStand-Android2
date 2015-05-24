package edu.umd.umiacs.newsstand.location;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;

import edu.umd.umiacs.newsstand.R;

import java.util.ArrayList;

/**
 * Created by Brendan on 5/23/13.
 */
public class LocationListAdapter extends BaseAdapter {
    private static final String TAG = "edu.umd.umiacs.newsstand.location.LocationListAdapter";
    public final static int TYPE_LOCATION_ROW = 0;

    private static LayoutInflater inflater = null;
    private Context mContext;

    private ArrayList<Article> mArticles;
    private int mSelectedPosition;
    private boolean mShowTranslated;

    private int mRowUnselectedColor;
    private int mRowSelectedColor;
    private int mTextSelectedColor;
    private int mTextUnselectedColor;

    public LocationListAdapter(Context context, ArrayList<Article> articles) {
        mContext = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mArticles = articles;

        mRowUnselectedColor = mContext.getResources().getColor(R.color.white);
        mRowSelectedColor = mContext.getResources().getColor(R.color.blue);
        mTextSelectedColor = mContext.getResources().getColor(R.color.white);
        mTextUnselectedColor = mContext.getResources().getColor(R.color.full_blue);

        mSelectedPosition = -1;
    }

    @Override
    public int getCount() {
        if (mArticles != null)
            return mArticles.size();
        else
            return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_LOCATION_ROW;
    }

    public boolean areAllItemsSelectable() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_location, null);

        if (convertView != null) {
            Article currentArticle = mArticles.get(position);

            TextView keywordText = (TextView)convertView.findViewById(R.id.locationKeywordText);
            if (currentArticle != null && currentArticle.getKeyword() != null &&
                    !currentArticle.getKeyword().equals("")) {
                String escapedKeywordText = "\u200e" + currentArticle.getKeyword() + ": ";
                keywordText.setText(escapedKeywordText);
                keywordText.setVisibility(View.VISIBLE);
            } else {
                keywordText.setVisibility(View.GONE);
            }

            TextView locationText = (TextView) convertView.findViewById(R.id.locationText);

            if (!mShowTranslated || currentArticle.getTranslate_title() == null ||
                    currentArticle.getTranslate_title().equals("")) {
                String escapedTitle = "\u200e" + currentArticle.getTitle(); // Left justify RTL text
                locationText.setText(escapedTitle);
            } else
                locationText.setText(currentArticle.getTranslate_title());

            if (position == mSelectedPosition) {
                keywordText.setBackgroundColor(mRowSelectedColor);
                locationText.setBackgroundColor(mRowSelectedColor);
                locationText.setTextColor(mTextSelectedColor);
            } else {
                keywordText.setBackgroundColor(mRowUnselectedColor);
                locationText.setBackgroundColor(mRowUnselectedColor);
                locationText.setTextColor(mTextUnselectedColor);
            }
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        if (mArticles != null && position < mArticles.size()) {
            return mArticles.get(position).getTitle();
        }
        return null;
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
        this.notifyDataSetChanged();
    }

    public void toggleTranslateTitles() {
        mShowTranslated = !mShowTranslated;
        this.notifyDataSetChanged();
    }
}

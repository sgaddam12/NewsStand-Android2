package edu.umd.umiacs.newsstand.topstories;

import java.util.ArrayList;
import java.util.Locale;

import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.location.Article;
import edu.umd.umiacs.newsstand.source.Source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TopStoriesListAdapter extends BaseAdapter {
    public final static int TYPE_TOP_STORIES_ROW = 0;

    private static LayoutInflater inflater = null;
    private Context mContext;

    private int mSelectedPosition;
    private boolean mShowTranslated;

    private ArrayList<Article> mTopStories;

    private int unselectedColor;
    private int selectedColor;
    private int textSelectedColor;
    private int domainUnselectedColor;

    public TopStoriesListAdapter(Context context, ArrayList<Article> topStories) {
        mContext = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mTopStories = topStories;

        unselectedColor = android.R.color.white;
        selectedColor = R.color.blue;
        textSelectedColor = mContext.getResources().getColor(android.R.color.white);
        domainUnselectedColor = mContext.getResources().getColor(R.color.dark_green);
    }

    @Override
    public int getCount() {
        if (mTopStories != null)
            return mTopStories.size();
        else
            return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_TOP_STORIES_ROW;
    }

    public boolean areAllItemsSelectable() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_topstories, null);

        boolean selected = false;
        if (position == mSelectedPosition) {
            convertView.setBackgroundResource(selectedColor);
            selected = true;
        } else {
            convertView.setBackgroundResource(unselectedColor);
        }

        ImageView topicImage = (ImageView) convertView.findViewById(R.id.topStoriesTopicImage);
        TextView titleText = (TextView) convertView.findViewById(R.id.topStoriesTitleText);
        TextView timeText = (TextView) convertView.findViewById(R.id.topStoriesTimeText);
        TextView domainText = (TextView) convertView.findViewById(R.id.topStoriesDomainText);

        ImageButton imagesButton = (ImageButton) convertView.findViewById(R.id.topStoriesImagesButton);
        ImageButton videosButton = (ImageButton) convertView.findViewById(R.id.topStoriesVideosButton);
        ImageButton relatedButton = (ImageButton) convertView.findViewById(R.id.topStoriesRelatedButton);

        TextView descriptionText = (TextView) convertView.findViewById(R.id.topStoriesDescriptionText);

        if (mTopStories != null && position < mTopStories.size()) {
            Article currentArticle = mTopStories.get(position);

            // Set Topic Image
            int topicID = mContext.getResources().getIdentifier(
                    "edu.umd.umiacs.newsstand:drawable/marker_"
                            + currentArticle.getTopic().toLowerCase(Locale
                            .getDefault()) + "_original", null, null);
            topicImage.setImageResource(topicID);

            if (!mShowTranslated || currentArticle.getTranslate_title().equals("")) {
                titleText.setText(currentArticle.getTitle());
            } else {
                String escapedTitle = "\u200e" + currentArticle.getTranslate_title();
                titleText.setText(escapedTitle);
            }
            titleText.setOnClickListener((OnClickListener) mContext);
            titleText.setTag(position);

            timeText.setText(currentArticle.getTime());
            domainText.setText(currentArticle.getDomain());

            descriptionText.setText(currentArticle.getDescription());

            if (currentArticle.getNum_images() > 0) {
                imagesButton.setVisibility(View.VISIBLE);
                imagesButton.setOnClickListener((OnClickListener) mContext);
                imagesButton.setTag(1000 + position);
            } else {
                imagesButton.setVisibility(View.INVISIBLE);
            }

            if (currentArticle.getNum_videos() > 0) {
                videosButton.setVisibility(View.VISIBLE);
                videosButton.setOnClickListener((OnClickListener) mContext);
                videosButton.setTag(2000 + position);
            } else {
                videosButton.setVisibility(View.INVISIBLE);
            }

            if (currentArticle.getNum_docs() > 0) {
                relatedButton.setVisibility(View.VISIBLE);
                relatedButton.setOnClickListener((OnClickListener) mContext);
                relatedButton.setTag(3000 + position);
            } else {
                relatedButton.setVisibility(View.INVISIBLE);
            }


            if (selected) {
                domainText.setTextColor(textSelectedColor);
            } else {
                domainText.setTextColor(domainUnselectedColor);
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
        if (mTopStories != null && position < mTopStories.size()) {
            return mTopStories.get(position);
        }
        return null;
    }

    public void setSelectedPosition(int mSelectedPosition) {
        this.mSelectedPosition = mSelectedPosition;
        this.notifyDataSetChanged();
    }

    public void toggleTranslateTitles() {
        mShowTranslated = !mShowTranslated;
        this.notifyDataSetChanged();
    }

}

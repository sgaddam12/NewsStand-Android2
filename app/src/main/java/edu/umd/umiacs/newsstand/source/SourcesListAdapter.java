package edu.umd.umiacs.newsstand.source;

// Code modified from: http://w2davids.wordpress.com/android-sectioned-headers-in-listviews/

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

import edu.umd.umiacs.newsstand.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SourcesListAdapter extends BaseAdapter {
    public final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();
    public final ArrayAdapter<String> headers;
    public final static int TYPE_SECTION_HEADER = 0;
    public final static int TYPE_ADD_ROW = 1;
    public final static int TYPE_LANG_ROW = 2;
    public final static int TYPE_FEED_ROW = 3;

    private static LayoutInflater inflater = null;
    private Context mContext;

    private Source.SourceType mSelectedSourceType;

    private String mSelectedAllSource;
    private ArrayList<Source> mSelectedLanguageSources;
    private ArrayList<Source> mSelectedCountrySources;
    private ArrayList<Source> mSelectedFeedSources;

    private int mRowUnselectedColor;
    private int mRowSelectedColor;
    private int mTextSelectedColor;
    private int mTextUnselectedColor;

    public SourcesListAdapter(Context context) {
        headers = new ArrayAdapter<String>(context,
                R.layout.sources_list_header);
    }

    public SourcesListAdapter(Context context, String selectedAllSource,
                              ArrayList<Source> languageSources,
                              ArrayList<Source> countrySources, ArrayList<Source> feedSources) {
        mContext = context;
        headers = new ArrayAdapter<String>(context, R.layout.sources_list_header);
        mSelectedAllSource = selectedAllSource;
        mSelectedLanguageSources = languageSources;
        mSelectedCountrySources = countrySources;
        mSelectedFeedSources = feedSources;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mRowUnselectedColor = mContext.getResources().getColor(R.color.white);
        mRowSelectedColor = mContext.getResources().getColor(R.color.blue);
        mTextSelectedColor = mContext.getResources().getColor(R.color.white);
        mTextUnselectedColor = mContext.getResources().getColor(R.color.black);

        mSelectedSourceType = null;
    }

    public void addSection(String section, Adapter adapter) {
        this.headers.add(section);
        this.sections.put(section, adapter);
    }

    @Override
    public Object getItem(int position) {
        Adapter adapter = null;
        for (Object section : this.sections.keySet()) {
            adapter = sections.get(section);
            int size = adapter.getCount();

            // Check if position inside this section
            if (position == 0)
                return section;
            if (position < size)
                return adapter.getItem(position - 1);

            position -= size;
        }
        return null;
    }

    @Override
    public int getCount() {
        // Total together all sections plus one for each section header
        int total = 0;
        for (Adapter adapter : this.sections.values()) {
            total += adapter.getCount() + 1;
        }
        return total;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        int size = 0;
        int currentSection = 0;

        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            size = adapter.getCount() + 1;

            // Check if position inside this section
            if (position == 0)
                return TYPE_SECTION_HEADER;
            if (position < size)
                if (position == 1) {
                    return TYPE_ADD_ROW;
                } else {
                    if (currentSection == 1) {
                        return TYPE_LANG_ROW;
                    } else {
                        return TYPE_FEED_ROW;
                    }
                    //return type + adapter.getItemViewType(position - 1);
                }
            position -= size;
            //type += adapter.getViewTypeCount();
            currentSection++;
        }

        return -1;
    }

    public boolean areAllItemsSelectable() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    private String subtitleTextForNumDocsLang(int numDocs, String langCode) {
        String subtitleText = "";

        if (numDocs == 1) {
            subtitleText = "1 article ";
        } else {
            subtitleText = Integer.toString(numDocs) + " articles ";
        }

        if (langCode != null)
            subtitleText += "(" + langCode + ")";

        return subtitleText;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionNumber = 0;
        int size = 0;
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            size = adapter.getCount() + 1;

            // Check if position inside this section
            if (position == 0)
                return headers.getView(sectionNumber, convertView, parent);
            if (position < size) {
                if (sectionNumber == 0) { // All Sources
                    if (convertView == null)
                        convertView = inflater.inflate(R.layout.item_add_source, null);
                    if (convertView != null) {
                        TextView text = (TextView) convertView
                                .findViewById(R.id.addSourceCellText);
                        text.setText(mSelectedAllSource);
                        if (mSelectedSourceType != null &&
                                mSelectedSourceType == Source.SourceType.ALL_SOURCE) {
                            text.setTextColor(mTextSelectedColor);
                            text.setBackgroundColor(mRowSelectedColor);
                        } else {
                            text.setTextColor(mTextUnselectedColor);
                            text.setBackgroundColor(mRowUnselectedColor);
                        }
                    }
                } else if (sectionNumber == 1) {
                    if (position - 1 == 0) {
                        if (convertView == null)
                            convertView = inflater.inflate(R.layout.item_add_source, null);
                        if (convertView != null) {
                            TextView text = (TextView) convertView
                                    .findViewById(R.id.addSourceCellText);
                            text.setText("Add Language Filter");
                            if (mSelectedSourceType != null &&
                                    mSelectedSourceType == Source.SourceType.LANGUAGE_SOURCE) {
                                text.setTextColor(mTextSelectedColor);
                                text.setBackgroundColor(mRowSelectedColor);
                            } else {
                                text.setTextColor(mTextUnselectedColor);
                                text.setBackgroundColor(mRowUnselectedColor);
                            }
                        }
                    } else {
                        if (convertView == null)
                            convertView = inflater.inflate(R.layout.item_lang_source, null);
                        if (convertView != null) {
                            convertView.setBackgroundColor(mRowUnselectedColor);

                            TextView title = (TextView) convertView
                                    .findViewById(R.id.langSourcesRowTitle);

                            TextView subtitle = (TextView) convertView
                                    .findViewById(R.id.langSourcesRowSubtitle);
                            ImageButton cancelButton = (ImageButton) convertView.findViewById(R.id.langSourcesRowCancelButton);

                            cancelButton.setTag(Integer.valueOf(position - 2));
                            cancelButton.setOnClickListener((OnClickListener) mContext);

                            if (mSelectedLanguageSources != null) {
                                Source currentLangSource = mSelectedLanguageSources.get(position - 2);

                                int numDocs = currentLangSource.getNumDocs();

                                title.setText(currentLangSource.getName());
                                subtitle.setText(subtitleTextForNumDocsLang(numDocs,
                                        null));
                            }
                        }
                    }
                } else if (sectionNumber == 2) {
                    if (position - 1 == 0) {
                        if (convertView == null)
                            convertView = inflater.inflate(R.layout.item_add_source, null);
                        if (convertView != null) {
                            TextView text = (TextView) convertView.findViewById(R.id.addSourceCellText);
                            text.setText("Add Feed Filter");

                            if (mSelectedSourceType != null &&
                                    mSelectedSourceType == Source.SourceType.FEED_SOURCE) {
                                text.setTextColor(mTextSelectedColor);
                                text.setBackgroundColor(mRowSelectedColor);
                            } else {
                                text.setTextColor(mTextUnselectedColor);
                                text.setBackgroundColor(mRowUnselectedColor);
                            }
                        }
                    } else {
                        if (convertView == null)
                            convertView = inflater.inflate(R.layout.item_source, null);

                        if (convertView != null) {
                            convertView.setBackgroundColor(mRowUnselectedColor);

                            TextView title = (TextView) convertView
                                    .findViewById(R.id.sourcesRowTitle);
                            ImageView thumbnailImage = (ImageView) convertView
                                    .findViewById(R.id.sourcesRowThumbnailImage);
                            TextView subtitle = (TextView) convertView
                                    .findViewById(R.id.sourcesRowSubtitle);
                            ImageButton cancelButton = (ImageButton) convertView.findViewById(R.id.sourcesRowCancelButton);

                            cancelButton.setTag(Integer.valueOf(2000 + position - 2));
                            cancelButton.setOnClickListener((OnClickListener) mContext);

                            if (mSelectedFeedSources != null) {
                                Source currentFeedSource = mSelectedFeedSources.get(position - 2);

                                try {
                                    int numDocs = currentFeedSource.getNumDocs();
                                    String countryCode = currentFeedSource.getCountryCode();
                                    String language = currentFeedSource.getLangCode();

                                    title.setText(currentFeedSource.getName());
                                    subtitle.setText(subtitleTextForNumDocsLang(numDocs,
                                            language));

                                    int flagID = mContext.getResources().getIdentifier(
                                            "edu.umd.umiacs.newsstand:drawable/zcty_"
                                                    + countryCode.toLowerCase(Locale
                                                    .getDefault()), null, null);
                                    thumbnailImage.setImageResource(flagID);
                                } catch (Exception e) {
                                    Log.i("Exception", e.toString());
                                }
                            }
                        }
                    }
                } else {
                    return adapter.getView(position - 1, convertView, parent);
                }
                return convertView;
            }

            // Otherwise go to next section
            position -= size;
            sectionNumber++;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedPosition(Source.SourceType selectedSourceType) {
        mSelectedSourceType = selectedSourceType;
        this.notifyDataSetChanged();
    }
}

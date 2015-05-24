package edu.umd.umiacs.newsstand.source.dialogs;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.source.Source;

/**
 * Created by Brendan on 9/3/13.
 */
public class LanguageSourcesListAdapter extends BaseAdapter {
    private static final String TAG = "edu.umd.umiacs.newsstand.source.dialogs.LanguageSourcesListAdapter";
    public final static int TYPE_LOCATION_ROW = 0;

    private static LayoutInflater inflater = null;
    private Context mContext;

    private ArrayList<Source> mSources;
    private boolean mShowTranslated;

    private int mRowUnselectedColor;
    private int mRowSelectedColor;
    private int mTextSelectedColor;
    private int mTextUnselectedColor;

    public LanguageSourcesListAdapter(Context context, ArrayList<Source> sources) {
        mContext = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mSources = sources;

        mRowUnselectedColor = mContext.getResources().getColor(R.color.white);
        mRowSelectedColor = mContext.getResources().getColor(R.color.blue);
        mTextSelectedColor = mContext.getResources().getColor(R.color.white);
        mTextUnselectedColor = mContext.getResources().getColor(R.color.black);
    }

    @Override
    public int getCount() {
        if (mSources != null)
            return mSources.size();
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
            convertView = inflater.inflate(R.layout.dialog_lang_source, null);

        if (convertView != null) {
            TextView title = (TextView) convertView
                    .findViewById(R.id.dialogLangRowTitle);
            ImageView thumbnailImage = (ImageView) convertView
                    .findViewById(R.id.dialogLangRowThumbnailImage);
            TextView subtitle = (TextView) convertView
                    .findViewById(R.id.dialogLangRowSubtitle);
            CheckBox checkBox = (CheckBox) convertView
                    .findViewById(R.id.dialogLangRowCheckBox);

            Source currentLangSource = mSources.get(position);

            try {
                int numDocs = currentLangSource.getNumDocs();
                String countryCode = currentLangSource.getCountryCode();
                String language = currentLangSource.getLangCode();

                title.setText(currentLangSource.getName());
                subtitle.setText(subtitleTextForNumDocsLang(numDocs,
                        language));

                int flagID = mContext.getResources().getIdentifier(
                        "edu.umd.umiacs.newsstand:drawable/zcty_"
                                + countryCode.toLowerCase(Locale
                                .getDefault()), null, null);
                thumbnailImage.setImageResource(flagID);

                checkBox.setChecked(currentLangSource.isSelected());

                if (currentLangSource.isHighlight()) {
                    convertView.setBackgroundColor(mRowSelectedColor);
                    title.setTextColor(mTextSelectedColor);
                } else {
                    convertView.setBackgroundColor(mRowUnselectedColor);
                    title.setTextColor(mTextUnselectedColor);
                }
            } catch (Exception e) {
                Log.i("Exception", e.toString());
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
        if (mSources != null && position < mSources.size()) {
            return mSources.get(position).getName();
        }
        return null;
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

    public void setSelectedPosition(int selectedPosition, boolean highlight) {
        mSources.get(selectedPosition).setHighlight(highlight);
        if (highlight) {
            mSources.get(selectedPosition).setSelected(!mSources.get(selectedPosition).isSelected());
        }
        notifyDataSetChanged();
    }

    public ArrayList<Source> getSources () {
        return mSources;
    }
}

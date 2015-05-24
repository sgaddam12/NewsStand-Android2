package edu.umd.umiacs.newsstand.videoview;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.umd.umiacs.newsstand.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class VideoListAdapter extends BaseAdapter {
    public final static int TYPE_VIDEO_ROW = 0;

    private static LayoutInflater inflater = null;
    private Context mContext;

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    private ArrayList<Video> mVideos;


    public VideoListAdapter(Context context, ArrayList<Video> videos) {
        mContext = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mVideos = videos;
    }

    @Override
    public int getCount() {
        if (mVideos != null)
            return mVideos.size();
        else
            return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_VIDEO_ROW;
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
            convertView = inflater.inflate(R.layout.item_video, null);

        ImageView previewImage = (ImageView) convertView.findViewById(R.id.videoPreviewImage);
        TextView titleText = (TextView) convertView.findViewById(R.id.videoTitleText);
        TextView durationText = (TextView) convertView.findViewById(R.id.videoDurationText);
        TextView pubDateText = (TextView) convertView.findViewById(R.id.videoPubDateText);
        TextView sourceNameText = (TextView) convertView.findViewById(R.id.videoSourceNameText);

        if (mVideos != null && mVideos.size() >= position) {
            Video currentVideo = mVideos.get(position);

            Bitmap currentPreviewImage = currentVideo.getStoredImage();

            // Use Sergey Tarasevich Universal Image Loader
            imageLoader.displayImage(currentVideo.getImgPreview(), previewImage, animateFirstListener);

            titleText.setText(currentVideo.getTitle());
            durationText.setText(currentVideo.getDuration());
            pubDateText.setText(currentVideo.getPubDate());

            String sourceName = currentVideo.getSourceName();
            if (sourceName != null && sourceName.length() > 0) {
                sourceNameText.setText(sourceName);
            } else {
                sourceNameText.setText(currentVideo.getSourceDomain());
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
        if (mVideos != null && position < mVideos.size()) {
            return mVideos.get(position);
        }
        return null;
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;

                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}



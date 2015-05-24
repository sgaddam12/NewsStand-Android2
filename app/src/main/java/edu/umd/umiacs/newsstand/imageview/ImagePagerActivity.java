/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.umd.umiacs.newsstand.imageview;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.imageview.ImageViewZoom.DeactivableViewPager;
import edu.umd.umiacs.newsstand.imageview.ImageViewZoom.ImageViewTouch;
import edu.umd.umiacs.newsstand.webview.WebViewActivity;

import java.util.ArrayList;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImagePagerActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "edu.umd.umiacs.newsstand.imageview.ImagePagerActivity";

	private static final String STATE_POSITION = "STATE_POSITION";
    public static final String IMAGES = "IMAGES";
    public static final String IMAGE_URLS = "IMAGE_URLS";
    public static final String IMAGE_POSITION = "IMAGE_POSITION";

    private String mTitle;

    private ActionBar mActionBar;
    private Button mBackButton;
    private Button mTitleButton;
    private ImageButton mSourceArticleButton;

	DisplayImageOptions options;

	DeactivableViewPager pager;

    private ArrayList<Image> mImages;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_pager);

		Bundle bundle = getIntent().getExtras();

        mTitle = bundle.getString(MainActivity.TITLE);

		String[] imageUrls = bundle.getStringArray(IMAGE_URLS);
		int pagerPosition = bundle.getInt(IMAGE_POSITION, 0);
        mImages = (ArrayList<Image>)bundle.getSerializable(IMAGES);

		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_image_stub)
			.showImageOnFail(R.drawable.ic_image_stub)
			.resetViewBeforeLoading()
			.cacheOnDisc()
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .memoryCacheExtraOptions(3000, 3000)
            .build();
        imageLoader.getInstance().init(config);

        String[] captions = new String[mImages.size()];
        int i = 0;
        for (Image currentImage : mImages) {
            captions[i] = currentImage.getCaption();
            i++;
        }
        pager = (DeactivableViewPager) findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(imageUrls, captions));
		pager.setCurrentItem(pagerPosition);

        setupActionBar();
     }

    private void setupActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.action_image_pager);

            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);

            mBackButton = (Button) findViewById(R.id.imagePagerBackButton);

            mBackButton.setOnClickListener(this);
            mBackButton.setText(mTitle);

            mTitleButton = (Button) findViewById(R.id.imagePagerTitleButton);
            mTitleButton.setText(pager.getCurrentItem()+1 + " of " + mImages.size());

            mSourceArticleButton = (ImageButton) findViewById(R.id.imagePagerArticleButton);
            mSourceArticleButton.setOnClickListener(this);
        }
    }

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imagePagerBackButton:
                finish();
                break;
            case R.id.imagePagerArticleButton:
                displaySourceArticle();
                break;
        }
    }

    private void displaySourceArticle() {
        int selectedIndex = pager.getCurrentItem();
        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
        intent.putExtra("articleURL", mImages.get(selectedIndex).getArticleURL());
        intent.putExtra(MainActivity.TITLE, "Images");
        startActivity(intent);
    }

    private class ImagePagerAdapter extends PagerAdapter {

		private String[] images;
        private String[] captions;
		private LayoutInflater inflater;
        TextView captionTextView;

		ImagePagerAdapter(String[] images, String[] captions) {
			this.images = images;
			inflater = getLayoutInflater();
            this.captions = captions;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			ImageViewTouch imageView = (ImageViewTouch) imageLayout.findViewById(R.id.pager_image);
            imageView.setScaleEnabled(true);
            captionTextView = (TextView) imageLayout.findViewById(R.id.imageCaption);
            captionTextView.setText(mImages.get(pager.getCurrentItem()).getCaption());
            captionTextView.setVisibility(View.VISIBLE);
            pager.setClipToPadding(true);

            imageView.setOnScaleListener(new ImageViewTouch.OnPageScaleListener() {
                @Override
                public void onScaleBegin() {
                    pager.deactivate();
                }

                @Override
                public void onScaleEnd(float scale) {
                    if (scale > 1.0) {
                        //pager.deactivate(); // BCF 7/31/13 - fix issue with initial scale > 1.0
                        pager.activate();
                    } else {
                        pager.activate();
                    }
                }
            });


            imageView.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener(){
                @Override
                public void onSingleTapConfirmed() {
                    displaySourceArticle();
                }
            });


            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

			imageLoader.displayImage(images[position], imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
                    //captionTextView.setVisibility(View.GONE);
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					String message = null;
					switch (failReason.getType()) {
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
					}
					Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

					spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    captionTextView.setText(mImages.get(pager.getCurrentItem()).getCaption());
                    captionTextView.setVisibility(View.VISIBLE);
                    captionTextView.setGravity(Gravity.CENTER);
					spinner.setVisibility(View.GONE);
                    mTitleButton.setText(pager.getCurrentItem()+1 + " of " + mImages.size());
				}
			});

			((ViewPager) view).addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}
	}
}
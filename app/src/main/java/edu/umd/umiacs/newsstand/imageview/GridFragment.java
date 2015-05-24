package edu.umd.umiacs.newsstand.imageview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Brendan on 6/21/13.
 */
public class GridFragment extends Fragment implements View.OnClickListener {
    private final String TAG = "edu.umd.umiacs.newsstand.imageview.GridFragment";

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options;

    private ImageGridActivity.ImageDisplayType mImageDisplayType;

    private ArrayList<Image> mImages;
    private ArrayList<Boolean> mMultipleImagesInCluster; // For topic only ("More in Topic")

    private int screenWidth;
    private int screenHeight;

    private static final int TOPIC_IMAGE_SPACING = 10;

    private int numColumns;

    private GridView mGridView;

    private ImageButton enlargedImageButton;
    private Image enlargedImage;
    private TextView enlargedCaptionText;

    private RelativeLayout relativeLayout;
    private ImageButton enlargedImageBackground;
    private ImageButton copyEnlargedImageButton;

    private Image leftImage;
    private Image rightImage;
    private Image upImage;
    private Image downImage;
    private ImageButton leftImageButton;
    private ImageButton rightImageButton;
    private ImageButton upImageButton;
    private ImageButton downImageButton;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    private Image mCurrentImage;
    private int mSelectedIndex;

    private int mInitialIndex;
    private int mInitialOffset;

    private boolean mImageIsSelected;

    public GridFragment (ImageGridActivity.ImageDisplayType imageDisplayType,
                         ArrayList<Image> images) {
        mImageDisplayType = imageDisplayType;
        mImages = images;
    }

    public GridFragment (ImageGridActivity.ImageDisplayType imageDisplayType,
                         ArrayList<Image> images, int index, int offset) {
        mImageDisplayType = imageDisplayType;
        mImages = images;
        mInitialIndex = index;
        mInitialOffset = offset;
    }

    public GridFragment (ImageGridActivity.ImageDisplayType imageDisplayType,
                         ArrayList<Image> images, ArrayList<Boolean> multipleImagesInCluster) {
        mImageDisplayType = imageDisplayType;
        mImages = images;
        mMultipleImagesInCluster = multipleImagesInCluster;
    }

    public GridFragment (ImageGridActivity.ImageDisplayType imageDisplayType,
                         ArrayList<Image> images, ArrayList<Boolean> multipleImagesInCluster,
                         int index, int offset) {
        mImageDisplayType = imageDisplayType;
        mImages = images;
        mMultipleImagesInCluster = multipleImagesInCluster;
        mInitialIndex = index;
        mInitialOffset = offset;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder()
                .showStubImage(R.color.white)
                .cacheInMemory()
                .cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        getActivity().setContentView(R.layout.fragment_grid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_grid, container, false);
        mGridView = (GridView) V.findViewById(R.id.imageGridview);
        if (mImageDisplayType == ImageGridActivity.ImageDisplayType.TOPICS) {
            int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) TOPIC_IMAGE_SPACING, getResources().getDisplayMetrics());
            mGridView.setHorizontalSpacing(pixels);
            mGridView.setVerticalSpacing(pixels);
            mGridView.setPadding(pixels, pixels, pixels, pixels);
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedIndex = position;
                ImageGridActivity imageGridActivity = (ImageGridActivity)getActivity();
                imageGridActivity.changeImageEnlargedGridButtonState(mImageIsSelected);
                Log.i(TAG, "Selected " + mSelectedIndex);
                updateSurroundingImages();
                gridImageClicked(position);
            }
        });

        relativeLayout = (RelativeLayout) V.findViewById(R.id.imageRelativeLayout);
        enlargedImageBackground = (ImageButton) V.findViewById(R.id.imageEnlargedBackground);
        enlargedCaptionText = (TextView) V.findViewById(R.id.imageEnlargedCaption);

        enlargedImageBackground.setOnClickListener(this);

        // Gesture detection
        gestureDetector = new GestureDetector(V.getContext(), new ImageGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        mGridView.setAdapter(new ImageAdapter());
        return V;
    }

    // ================================================================================
    // Button/Image Click Listeners
    // ================================================================================

    private void gridImageClicked(int position) {
        ((ImageGridActivity)getActivity()).setEnlargedImageGrid(false);
        mImageIsSelected = true;
        mGridView.smoothScrollToPosition(position);
        displayImageAndCaption(mImages.get(position));
    }

    @Override
    public void onClick(View clickedView) {
        Log.i(TAG, "Click detected");
        switch (clickedView.getId()) {
            case R.id.imageEnlargedImage:
                onEnlargedImageClicked();
                return;
            case R.id.imageEnlargedBackground:
                onEnlargedBackgroundClicked();
                return;
        }
    }

    public void actionGridImageClicked() {
        scrollToPositionOffset(mInitialIndex, mInitialOffset);
        if (mImageIsSelected) {
            onEnlargedBackgroundClicked();
        } else {
            gridImageClicked(mSelectedIndex);
        }
    }

    // ================================================================================
    //  Set Listeners
    // ================================================================================
    private void setEnlargeImageListeners() {
        if (enlargedImageButton != null) {
            enlargedImageButton.setOnClickListener(this);
            enlargedImageButton.setOnTouchListener(gestureListener);
        }
    }

    // ================================================================================
    // Enlarge Image Calls
    // ================================================================================
    private void displayImageAndCaption(Image image) {
        screenWidth = mGridView.getWidth();
        screenHeight = mGridView.getHeight();
        if (mImageDisplayType == ImageGridActivity.ImageDisplayType.TOPICS) {
            boolean showMoreInTopicButton;
            if (mMultipleImagesInCluster.get(mSelectedIndex)) {
                showMoreInTopicButton = true;
            } else {
                showMoreInTopicButton = false;
            }
            ((ImageGridActivity)getActivity()).setShowMoreInTopic(showMoreInTopicButton);
        }


        if (enlargedImageBackground != null) {
            enlargedImageBackground.setVisibility(View.VISIBLE);
            ObjectAnimator backgroundAnimation = ObjectAnimator.ofFloat(enlargedImageBackground, "alpha", 0.0f, 0.5f);
            backgroundAnimation.setDuration(500);
            backgroundAnimation.start();
        }

        // Set up enlarged image
        enlargedImageButton = new ImageButton(getActivity());
        enlargedImageButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        RelativeLayout.LayoutParams layoutParams = getEnlargedImageLayoutParams();
        enlargedImageButton.setLayoutParams(layoutParams);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        enlargedImageButton.setPadding(50, 50, 50, 50);

        Log.i(TAG, "Gridview height " + mGridView.getHeight() + " width " + mGridView.getWidth());

        int maxWidth = mGridView.getWidth() - 100;
        int maxHeight = mGridView.getHeight() - 200;
        enlargedImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        enlargedImageButton.setMaxWidth(maxWidth);
        enlargedImageButton.setMaxHeight(maxHeight);
        enlargedImageButton.setId(R.id.imageEnlargedImage);
        enlargedImageButton.setOnClickListener(this);
        enlargedImageButton.setOnTouchListener(gestureListener);
        relativeLayout.addView(enlargedImageButton);

        imageLoader.displayImage(image.getImageURL(), enlargedImageButton, options);
        enlargedImageButton.setVisibility(View.VISIBLE);
        enlargedImage = image;
        ObjectAnimator imageAnimation = ObjectAnimator.ofFloat(enlargedImageButton.getBackground(), "alpha", 0.0f, 1.0f);
        imageAnimation.setDuration(300);
        imageAnimation.start();

        updateSurroundingImages();

        if (enlargedCaptionText != null) {
            enlargedCaptionText.setText(image.getCaption());
            enlargedCaptionText.bringToFront();
            enlargedCaptionText.setVisibility(View.VISIBLE);
            ObjectAnimator captionAnimation = ObjectAnimator.ofFloat(enlargedCaptionText, "alpha", 0.0f, 1.0f);
            captionAnimation.setDuration(300);
            captionAnimation.start();
        }

        mCurrentImage = image;
    }

    private void onEnlargedImageClicked() {
        if (mCurrentImage != null) {
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra("articleURL", mCurrentImage.getArticleURL());
            intent.putExtra(MainActivity.TITLE, "Images");
            startActivity(intent);
        }
    }

    private void onEnlargedBackgroundClicked() {
        ((ImageGridActivity)getActivity()).setShowMoreInTopic(false);
        ((ImageGridActivity)getActivity()).setEnlargedImageGrid(true);

        ObjectAnimator backgroundAnimation = ObjectAnimator.ofFloat(enlargedImageBackground, "alpha", 0.5f, 0.0f);
        backgroundAnimation.setDuration(500);
        backgroundAnimation.start();
        if (enlargedImageButton != null) {
            ObjectAnimator imageAnimation = ObjectAnimator.ofFloat(enlargedImageButton, "alpha", 1.0f, 0.0f);
            imageAnimation.setDuration(500);
            imageAnimation.start();
        }
        ObjectAnimator captionAnimation = ObjectAnimator.ofFloat(enlargedCaptionText, "alpha", 1.0f, 0.0f);
        captionAnimation.setDuration(500);
        captionAnimation.start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        enlargedImageBackground.setVisibility(View.GONE);
                        if (enlargedImageButton != null)
                            enlargedImageButton.setVisibility(View.GONE);
                        enlargedCaptionText.setVisibility(View.GONE);
                    }
                });
            }
        }, 500);

        mImageIsSelected = false;
        ImageGridActivity imageGridActivity = (ImageGridActivity)getActivity();
        imageGridActivity.changeImageEnlargedGridButtonState(mImageIsSelected);
    }

    // ================================================================================
    // Scale Images
    // ================================================================================
    private void scaleImageButtons() {
        if (screenWidth < screenHeight) { // Portrait
            float enlargedImageWidth = enlargedImageButton.getWidth();
            float enlargedImageHeight = enlargedImageButton.getHeight();

            if (enlargedImageWidth > 0 && enlargedImageWidth < screenWidth - 150) {
                Log.i(TAG, "screen width: " + screenWidth);
                float widthRatio = (screenWidth - 100) / enlargedImageWidth;
                if (enlargedImageHeight * widthRatio > screenHeight - 100) {
                    float heightRatio = (screenHeight - 100) / enlargedImageHeight;
                    enlargedImageButton.setScaleY(heightRatio);
                    enlargedImageButton.setScaleX(heightRatio);
                } else {
                    enlargedImageButton.setScaleX(widthRatio);
                    enlargedImageButton.setScaleY(widthRatio);
                }
            }
        }
    }

    private RelativeLayout.LayoutParams getEnlargedImageLayoutParams() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        return layoutParams;
    }

    // ================================================================================
    // Update Surrounding Images
    // ================================================================================

    private void updateSurroundingImages() {

        if (mSelectedIndex > 0) {
            leftImageButton = new ImageButton(getActivity());
            leftImageButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            leftImageButton.setLayoutParams(getEnlargedImageLayoutParams());
            leftImageButton.setPadding(50, 50, 50, 50);
            imageLoader.displayImage(mImages.get(mSelectedIndex - 1).getImageURL(), leftImageButton, options);
            leftImageButton.setX(-screenWidth);
            leftImage = mImages.get(mSelectedIndex - 1);
        } else {
            leftImageButton = null;
            leftImage = null;
        }

        if (mSelectedIndex < mImages.size() - 1) {
            rightImageButton = new ImageButton(getActivity());
            rightImageButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            rightImageButton.setLayoutParams(getEnlargedImageLayoutParams());
            rightImageButton.setPadding(50, 50, 50, 50);
            imageLoader.displayImage(mImages.get(mSelectedIndex + 1).getImageURL(), rightImageButton, options);
            rightImageButton.setX(2 * screenWidth);
            rightImage = mImages.get(mSelectedIndex + 1);
        } else {
            rightImageButton = null;
            rightImage = null;
        }

        if (mSelectedIndex >= numColumns) {
            upImageButton = new ImageButton(getActivity());
            upImageButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            upImageButton.setLayoutParams(getEnlargedImageLayoutParams());
            upImageButton.setPadding(50, 50, 50, 50);
            imageLoader.displayImage(mImages.get(mSelectedIndex - numColumns).getImageURL(), upImageButton, options);
            upImageButton.setY(-screenHeight);
            upImage = mImages.get(mSelectedIndex - numColumns);
        } else {
            upImageButton = null;
            upImage = null;
        }

        if (mSelectedIndex + numColumns < mImages.size()) {
            downImageButton = new ImageButton(getActivity());
            downImageButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            downImageButton.setLayoutParams(getEnlargedImageLayoutParams());
            downImageButton.setPadding(50, 50, 50, 50);
            imageLoader.displayImage(mImages.get(mSelectedIndex + numColumns).getImageURL(), downImageButton, options);
            downImageButton.setY(2 * screenHeight);
            downImage = mImages.get(mSelectedIndex + numColumns);
        } else {
            downImageButton = null;
            downImage = null;
        }
    }

    // ================================================================================
    // Move Images
    // ================================================================================

    private void updateDisplayMoreInTopic() {
        if (mImageDisplayType != ImageGridActivity.ImageDisplayType.TOPICS)
            return;

        boolean showMoreInTopicButton;
        if (mMultipleImagesInCluster.get(mSelectedIndex)) {
            showMoreInTopicButton = true;
        } else {
            showMoreInTopicButton = false;
        }
        ((ImageGridActivity)getActivity()).setShowMoreInTopic(showMoreInTopicButton);
    }

    private void onAnimateMoveX(float dx, long duration) {
        if (dx < 0 && mSelectedIndex == mImages.size() - 1)
            return;
        if (dx > 0 && mSelectedIndex == 0) {
            return;
        }

        final float dxFinal = dx;
        copyEnlargedImageButton = enlargedImageButton;

        enlargedCaptionText.setAlpha(0.0f);
        copyEnlargedImageButton.setOnClickListener(null);
        copyEnlargedImageButton.setOnTouchListener(null);
        copyEnlargedImageButton.setId(R.id.oldImageEnlargedImage);

        if (dx > 0) {
            enlargedImageButton = null;
            enlargedImageButton = leftImageButton;
            scaleImageButtons();
            enlargedImage = leftImage;
            mSelectedIndex--;
            updateSurroundingImages();
        } else {
            enlargedImageButton = null;
            enlargedImageButton = rightImageButton;
            scaleImageButtons();
            enlargedImage = rightImage;
            mSelectedIndex++;
            updateSurroundingImages();
        }

        int maxWidth = mGridView.getWidth() - 100;
        int maxHeight = mGridView.getHeight() - 300;
        enlargedImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        enlargedImageButton.setMaxWidth(maxWidth);
        enlargedImageButton.setMaxHeight(maxHeight);


        int measureWidth = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.EXACTLY);
        int measureHeight = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.EXACTLY);

        Log.i(TAG, "Measure width: " + measureWidth + " measure height " + measureHeight);

        relativeLayout.addView(enlargedImageButton);
        enlargedImageButton.measure(measureWidth, measureHeight);

        mGridView.smoothScrollToPosition(mSelectedIndex);
        enlargedImageButton.setId(R.id.imageEnlargedImage);

        // Check if image is taken off screen if not then animate to original spot then off screen
        if (copyEnlargedImageButton.getX() + dx < screenWidth && copyEnlargedImageButton.getX() + copyEnlargedImageButton.getWidth() + dx > 0) {
            ObjectAnimator imageAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "x", copyEnlargedImageButton.getX(),
                    copyEnlargedImageButton.getX() + dx);
            imageAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (dxFinal > 0) {
                        ObjectAnimator imageOffAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "x",
                                copyEnlargedImageButton.getX(), screenWidth + copyEnlargedImageButton.getMeasuredWidth() / 2);
                        imageOffAnimation.addListener(new RemoveOldImageAnimatorListener());
                        imageOffAnimation.setDuration(200);
                        imageOffAnimation.start();
                    } else {
                        ObjectAnimator imageOffAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "x",
                                copyEnlargedImageButton.getX(), -copyEnlargedImageButton.getWidth());
                        imageOffAnimation.addListener(new RemoveOldImageAnimatorListener());
                        imageOffAnimation.setDuration(200);
                        imageOffAnimation.start();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            imageAnimation.setDuration(duration);
            imageAnimation.start();

            ObjectAnimator otherImageAnimation = ObjectAnimator.ofFloat(enlargedImageButton, "x", enlargedImageButton.getX(),
                    enlargedImageButton.getX() + dxFinal);
            otherImageAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    updateDisplayMoreInTopic();

                    Log.i(TAG, "Slow screenWidth " + screenWidth + " width " + enlargedImageButton.getWidth());
                    ObjectAnimator otherImageAnimation = ObjectAnimator.ofFloat(enlargedImageButton,
                            "x", enlargedImageButton.getX(),
                            screenWidth / 2 - enlargedImageButton.getWidth() / 2);
                    otherImageAnimation.setDuration(200);
                    otherImageAnimation.addListener(new AddListenersNewImageAnimatorListener());
                    otherImageAnimation.start();

                    enlargedCaptionText.setText(enlargedImage.getCaption());
                    enlargedCaptionText.bringToFront();
                    ObjectAnimator captionAnimation = ObjectAnimator.ofFloat(enlargedCaptionText,
                            "alpha", 0.0f, 1.0f);
                    captionAnimation.setDuration(100);
                    captionAnimation.start();
                    enlargedImageButton.setOnTouchListener(gestureListener);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            otherImageAnimation.setDuration(400);
            otherImageAnimation.start();
        } else {
            updateDisplayMoreInTopic();

            ObjectAnimator imageAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "x", copyEnlargedImageButton.getX(),
                    copyEnlargedImageButton.getX() + dx);
            imageAnimation.addListener(new RemoveOldImageAnimatorListener());
            imageAnimation.setDuration(duration);
            imageAnimation.start();

            Log.i(TAG, "Fast screenWidth " + screenWidth + " measuredWidth " + enlargedImageButton.getWidth());

            float measuredWidth = enlargedImageButton.getMeasuredWidth();
            if (measuredWidth > screenWidth)
                measuredWidth = screenWidth;

            ObjectAnimator otherImageAnimation = ObjectAnimator.ofFloat(enlargedImageButton, "x", enlargedImageButton.getX(),
                    screenWidth / 2 - measuredWidth / 2);
            otherImageAnimation.addListener(new AddListenersNewImageAnimatorListener());
            otherImageAnimation.setDuration(duration);
            otherImageAnimation.start();

            enlargedCaptionText.setText(enlargedImage.getCaption());
            enlargedCaptionText.bringToFront();
            ObjectAnimator captionAnimation = ObjectAnimator.ofFloat(enlargedCaptionText,
                    "alpha", 0.0f, 1.0f);
            captionAnimation.setDuration(100);
            captionAnimation.start();
        }
    }

    private void onAnimateMoveY(float dy, long duration) {
        if (dy < 0 && mSelectedIndex + numColumns >= mImages.size())
            return;
        if (dy > 0 && mSelectedIndex - numColumns < 0) {
            return;
        }

        final float dyFinal = dy;
        copyEnlargedImageButton = enlargedImageButton;

        enlargedCaptionText.setAlpha(0.0f);
        copyEnlargedImageButton.setOnClickListener(null);
        copyEnlargedImageButton.setOnTouchListener(null);
        copyEnlargedImageButton.setId(R.id.oldImageEnlargedImage);

        if (dy > 0) {
            enlargedImageButton = null;
            updateSurroundingImages();
            enlargedImageButton = upImageButton;
            scaleImageButtons();
            enlargedImage = upImage;
            mSelectedIndex -= numColumns;
            updateSurroundingImages();
        } else {
            enlargedImageButton = null;
            updateSurroundingImages();
            enlargedImageButton = downImageButton;
            scaleImageButtons();
            enlargedImage = downImage;
            mSelectedIndex += numColumns;
            updateSurroundingImages();
        }

        int maxWidth = mGridView.getWidth() - 100;
        int maxHeight = mGridView.getHeight() - 200;
        enlargedImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        enlargedImageButton.setMaxWidth(maxWidth);
        enlargedImageButton.setMaxHeight(maxHeight);

        int measureWidth = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.EXACTLY);
        int measureHeight = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.EXACTLY);

        relativeLayout.addView(enlargedImageButton);
        enlargedImageButton.measure(measureWidth, measureHeight);

        mGridView.smoothScrollToPosition(mSelectedIndex);
        enlargedImageButton.setId(R.id.imageEnlargedImage);

        // Check if image is taken off screen if not then animate to original spot then off screen
        if (copyEnlargedImageButton.getY() + dy < screenHeight && copyEnlargedImageButton.getY() + copyEnlargedImageButton.getHeight() + dy > 0) {
            ObjectAnimator imageAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "y", copyEnlargedImageButton.getY(),
                    copyEnlargedImageButton.getY() + dy);
            imageAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (dyFinal > 0) {
                        ObjectAnimator imageOffAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "y",
                                copyEnlargedImageButton.getY(), screenHeight + copyEnlargedImageButton.getHeight() / 2);
                        imageOffAnimation.addListener(new RemoveOldImageAnimatorListener());
                        imageOffAnimation.setDuration(200);
                        imageOffAnimation.start();
                    } else {
                        ObjectAnimator imageOffAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "y",
                                copyEnlargedImageButton.getY(), -copyEnlargedImageButton.getHeight());
                        imageOffAnimation.addListener(new RemoveOldImageAnimatorListener());
                        imageOffAnimation.setDuration(200);
                        imageOffAnimation.start();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            imageAnimation.setDuration(duration);
            imageAnimation.start();

            ObjectAnimator otherImageAnimation = ObjectAnimator.ofFloat(enlargedImageButton, "y", enlargedImageButton.getY(),
                    enlargedImageButton.getY() + dyFinal);
            otherImageAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    updateDisplayMoreInTopic();

                    ObjectAnimator otherImageAnimation = ObjectAnimator.ofFloat(enlargedImageButton,
                            "y", enlargedImageButton.getY(),
                            screenHeight / 2 - enlargedImageButton.getMeasuredHeight() / 2);
                    otherImageAnimation.setDuration(200);
                    otherImageAnimation.addListener(new AddListenersNewImageAnimatorListener());
                    otherImageAnimation.start();

                    enlargedCaptionText.setText(enlargedImage.getCaption());
                    enlargedCaptionText.bringToFront();
                    ObjectAnimator captionAnimation = ObjectAnimator.ofFloat(enlargedCaptionText,
                            "alpha", 0.0f, 1.0f);
                    captionAnimation.setDuration(100);
                    captionAnimation.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            otherImageAnimation.setDuration(400);
            otherImageAnimation.start();
        } else {
            updateDisplayMoreInTopic();

            ObjectAnimator imageAnimation = ObjectAnimator.ofFloat(copyEnlargedImageButton, "y", copyEnlargedImageButton.getY(),
                    copyEnlargedImageButton.getY() + dy);
            imageAnimation.addListener(new RemoveOldImageAnimatorListener());
            imageAnimation.setDuration(duration);
            imageAnimation.start();

            float measuredHeight = enlargedImageButton.getMeasuredHeight();
            if (measuredHeight > screenHeight)
                measuredHeight = screenHeight;

            ObjectAnimator otherImageAnimation = ObjectAnimator.ofFloat(enlargedImageButton, "y", enlargedImageButton.getY(),
                    screenHeight / 2 - measuredHeight / 2);
            otherImageAnimation.addListener(new AddListenersNewImageAnimatorListener());
            otherImageAnimation.setDuration(duration);
            otherImageAnimation.start();

            enlargedCaptionText.setText(enlargedImage.getCaption());
            enlargedCaptionText.bringToFront();
            ObjectAnimator captionAnimation = ObjectAnimator.ofFloat(enlargedCaptionText,
                    "alpha", 0.0f, 1.0f);
            captionAnimation.setDuration(100);
            captionAnimation.start();
        }
    }

    // ================================================================================
    // ImageGestureDetector Class
    // ================================================================================

    class ImageGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i(TAG, "Fling detected");
            try {
                screenWidth = mGridView.getWidth();
                screenHeight = mGridView.getHeight();
                numColumns = mGridView.getNumColumns();

                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    final float distanceTimeFactor = 0.4f;
                    final float totalDx = (distanceTimeFactor * velocityX / 2);
                    onAnimateMoveX(totalDx, 400);
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    final float distanceTimeFactor = 0.4f;
                    final float totalDx = (distanceTimeFactor * velocityX / 2);
                    onAnimateMoveX(totalDx, 400);
                    return true;
                } else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    final float distanceTimeFactor = 0.4f;
                    final float totalDy = (distanceTimeFactor * velocityY / 2);
                    onAnimateMoveY(totalDy, 400);
                    return true;
                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    final float distanceTimeFactor = 0.4f;
                    final float totalDy = (distanceTimeFactor * velocityY / 2);
                    onAnimateMoveY(totalDy, 400);
                    return true;
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }

    // ================================================================================
    // ImageAdapter Class
    // ================================================================================

    class ImageAdapter extends BaseAdapter {
        private ArrayList<String> imageUrls;

        public ImageAdapter() {
            imageUrls = new ArrayList<String>();
            if (mImages != null) {
                for (Image currentImage : mImages)
                    imageUrls.add(currentImage.getImageURL());
            }
        }

        @Override
        public int getCount() {
            if (mImages != null)
                return mImages.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
            } else {
                imageView = (ImageView) convertView;
            }

            if (mImageDisplayType == ImageGridActivity.ImageDisplayType.MARK_DUPS)
                if (mImages.get(position).isDuplicate())
                    imageView.setAlpha(0.5f);
                else
                    imageView.setAlpha(1.0f);

            imageLoader.displayImage(imageUrls.get(position), imageView, options);

            return imageView;
        }
    }

    private class RemoveOldImageAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            relativeLayout.removeView(copyEnlargedImageButton);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    // ================================================================================
    // AddListenersNewImageAnimatorListener Class
    // ================================================================================

    private class AddListenersNewImageAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            setEnlargeImageListeners();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    // ================================================================================
    // Getters
    // ================================================================================

    public int getCurrentClusterID () {
        return mImages.get(mSelectedIndex).getClusterID();
    }

    public ImageGridActivity.ImageDisplayType getImageDisplayType () {
        return mImageDisplayType;
    }

    public int getSelectedIndex () {
        return mSelectedIndex;
    }

    public int getGridOffset () {
        int offset = 0;
        if (Build.VERSION.SDK_INT >= 16)
            offset = (int)(mGridView.getVerticalSpacing() * getResources().getDisplayMetrics().density);
        else
            offset = (int)(2 * getResources().getDisplayMetrics().density);

        int index = mGridView.getFirstVisiblePosition();
        final View first = mGridView.getChildAt(0);
        if (first != null) {
            offset -= first.getTop();
        }

        return offset;
    }

    public int getFirstVisibleIndex () {
        return mGridView.getFirstVisiblePosition();
    }

    public void scrollToPositionOffset (int index, int offset) {
        Log.i(TAG, "smooth scroll " + index + " " + offset);
        mGridView.smoothScrollToPositionFromTop(index, offset);
    }
}

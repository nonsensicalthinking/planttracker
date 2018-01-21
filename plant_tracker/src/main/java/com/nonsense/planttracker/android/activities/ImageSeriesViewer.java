package com.nonsense.planttracker.android.activities;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.listeners.OnSwipeTouchListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Derek Brooks on 1/20/2018.
 */

public class ImageSeriesViewer extends AppCompatActivity {

    private int mSelectedImageIndex;
    private ArrayList<String> files;
    private ImageView mPictureImageView;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_series_viewer);

        files = (ArrayList<String>) getIntent().getSerializableExtra("files");
        mSelectedImageIndex = files.size() - 1;

        bindUi();
    }

    private void bindUi()   {
        mPictureImageView = (ImageView)findViewById(R.id.pictureImageView);

        mPictureImageView.setOnTouchListener(new OnSwipeTouchListener(ImageSeriesViewer.this) {
            public void onSwipeRight() {
                if (mSelectedImageIndex > 0) {
                    mSelectedImageIndex--;
                    updateImageDisplayed();
                }
                else    {
                    Toast.makeText(ImageSeriesViewer.this,
                            "No more images to the left", Toast.LENGTH_SHORT).show();
                }
            }

            public void onSwipeLeft() {
                if (mSelectedImageIndex < files.size()-1)    {
                    mSelectedImageIndex++;
                    updateImageDisplayed();
                }
                else    {
                    Toast.makeText(ImageSeriesViewer.this,
                            "No more images to the right", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // load initial image
        updateImageDisplayed();
    }

    public void updateImageDisplayed() {
        mPictureImageView.setImageURI(Uri.fromFile(new File(files.get(mSelectedImageIndex))));
    }


}

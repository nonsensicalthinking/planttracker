package com.nonsense.planttracker.android.activities;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.listeners.OnSwipeTouchListener;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by Derek Brooks on 1/20/2018.
 */

public class ImageSeriesViewer extends AppCompatActivity {

    private int mSelectedImageIndex;
    private ArrayList<String> files;
    private ImageView mPictureImageView;

    private Matrix mOriginalMatrix;

    private LinearLayout imageViewLayout;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_series_viewer);

        files = (ArrayList<String>) getIntent().getSerializableExtra(
                AndroidConstants.INTENTKEY_FILE_LIST);

        mSelectedImageIndex = files.size() - 1;

        bindUi();
    }

    private View.OnTouchListener otl;

    @SuppressLint("ClickableViewAccessibility")
    private void bindUi()   {
        imageViewLayout = findViewById(R.id.imageViewLayout);
        mPictureImageView = findViewById(R.id.pictureImageView);
        otl = new OnSwipeTouchListener(ImageSeriesViewer.this) {

            public void onSwipeTop()    {
                setTheme(R.style.AppTheme_LightContainer);
            }

            public void onSwipeBottom()    {
                setTheme(R.style.AppTheme_DialogContainer);
            }


            public void onSwipeRight() {
                if (mSelectedImageIndex > 0) {
                    mSelectedImageIndex--;
                    Log.d("SWIPE", "RIGHT");
                    resetImageMatrix();
                    updateImageDisplayed();
                }
                else    {
                    Toast.makeText(ImageSeriesViewer.this,
                            "No more images to the left.", Toast.LENGTH_SHORT).show();
                    resetImageMatrix();
                }
            }

            public void onSwipeLeft() {
                if (mSelectedImageIndex < files.size()-1)    {
                    mSelectedImageIndex++;
                    Log.d("SWIPE", "LEFT");
                    resetImageMatrix();
                    updateImageDisplayed();
                }
                else    {
                    Toast.makeText(ImageSeriesViewer.this,
                            "No more images to the right.", Toast.LENGTH_SHORT).show();
                    resetImageMatrix();
                }
            }

            public void onDoubleTapio()   {
                resetImageMatrix();
                Toast.makeText(ImageSeriesViewer.this,
                        "Restored image.", Toast.LENGTH_SHORT).show();
            }
        };

        mPictureImageView.setOnTouchListener(otl);

        // load initial image
        updateImageDisplayed();
    }

    public void resetImageMatrix()   {
        try {
            Matrix matrix = new Matrix();
            int screenWidth = imageViewLayout.getWidth();
            int screenHeight = imageViewLayout.getHeight();

            int imgWidth = 1920;
            int imgHeight = 1080;

            ExifInterface exif = new ExifInterface(new FileInputStream(Uri.fromFile(new File(files.get(mSelectedImageIndex))).getPath()));

            int width = exif.getAttributeInt( ExifInterface.TAG_IMAGE_WIDTH, 1920 );
            int height = exif.getAttributeInt( ExifInterface.TAG_IMAGE_LENGTH, 1080);

            imgWidth = width;
            imgHeight = height;

            Log.e("", "Image Width : " + imgWidth + " > " + imgHeight);
            Log.e("", "screen Width : " + screenWidth + " > " + screenHeight);

            RectF drawableRect = new RectF(0, 0, imgWidth, imgHeight);
            RectF viewRect = new RectF(0, 0, screenWidth, screenHeight);
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);

            mPictureImageView.setImageMatrix(new Matrix(matrix));
            ((OnSwipeTouchListener)otl).matrix = new Matrix(matrix);
            ((OnSwipeTouchListener)otl).savedMatrix = new Matrix(matrix);
        }

        catch(Exception e)  {
            e.printStackTrace();
        }
    }
    
    public void updateImageDisplayed() {
        if ( mSelectedImageIndex < files.size() )
        {
            mPictureImageView.setImageURI(Uri.fromFile(new File(files.get(mSelectedImageIndex))));
        }
    }
}

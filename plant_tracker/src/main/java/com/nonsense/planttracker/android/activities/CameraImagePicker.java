package com.nonsense.planttracker.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.adapters.ImageAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Derek Brooks on 1/14/2018.
 */

public class CameraImagePicker extends AppCompatActivity {

    private String mBaseDir;
    private ArrayList<String> mFiles;
    private ImageAdapter mImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_image_picker);

        Intent intent = getIntent();

        mFiles = (ArrayList<String>)intent.getSerializableExtra("files");
        mBaseDir = intent.getStringExtra("baseDir");

        if (mBaseDir == null || mBaseDir.equals(""))    {
            mBaseDir = getExternalFilesDir("camera/").getPath();
        }

        bindUi();
    }

    private void bindUi()   {
        GridView gridview = (GridView) findViewById(R.id.imagePickerGridView);
        mImageAdapter = new ImageAdapter(this, mFiles, new File(mBaseDir), getLayoutInflater());
        gridview.setAdapter(mImageAdapter);
    }

    @Override
    public void onBackPressed()    {
        endActivity();
    }

    private void cancelActivity()   {
        deleteFiles(mFiles);
        setResult(RESULT_CANCELED);
        finish();
    }

    private void deleteFiles(ArrayList<String> files)   {
        for(String file : files)    {
            File f = new File(file);
            f.delete();
        }
    }

    private void endActivity()  {
        ArrayList<String> selectedFiles = new ArrayList<>();
        ArrayList<String> notSelectedFiles = new ArrayList<>();

        for(int x=0; x < mFiles.size(); x++)    {
            if (mImageAdapter.getSelected(x))   {
                selectedFiles.add(mBaseDir + "/" + mFiles.get(x));
            }
            else    {
                notSelectedFiles.add(mBaseDir + "/" + mFiles.get(x));
            }
        }

        deleteFiles(notSelectedFiles);

        Intent retIntent = new Intent();
        retIntent.putExtra(AndroidConstants.INTENTKEY_SELECTED_FILES, selectedFiles);

        setResult(RESULT_OK, retIntent);
        finish();
    }
}

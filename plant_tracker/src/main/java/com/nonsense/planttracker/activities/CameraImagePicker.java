package com.nonsense.planttracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.adapters.ImageAdapter;
import com.nonsense.planttracker.tracker.impl.GenericRecord;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

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

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(CameraImagePicker.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed()    {
        endActivity();
    }

    private void cancelActivity()   {
        setResult(RESULT_CANCELED);
        finish();
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

        Intent retIntent = new Intent();
        retIntent.putExtra("selectedFiles", selectedFiles);
        retIntent.putExtra("notSelectedFiles", notSelectedFiles);

        setResult(RESULT_OK, retIntent);
        finish();
    }
}

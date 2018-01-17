package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.nonsense.planttracker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 1/14/2018.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mFiles;

    private File mBasePath;
    private LayoutInflater mLayoutInflater;

    private TreeMap<Integer, Boolean> mFileSelections = new TreeMap<>();

    public ImageAdapter(Context c, ArrayList<String> files, File basePath, LayoutInflater layoutInflater) {
        mContext = c;
        mFiles = files;
        mBasePath = basePath;
        mLayoutInflater = layoutInflater;

        for(int x=0; x < mFiles.size(); x++)    {
            mFileSelections.put(x, false);
        }
    }

    public int getCount() {
        return mFiles.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public boolean getSelected(int position)    {
        if (mFileSelections.containsKey(position))  {
            return mFileSelections.get(position);
        }

        return false;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        View myView = convertView;
        if (myView == null) {
            myView = mLayoutInflater.inflate(R.layout.tile_image_picker, null);
        }

        final CheckBox checkBox = (CheckBox)myView.findViewById(R.id.check1);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mFileSelections.replace(position, b);
            }
        });

        ImageView pictureView = (ImageView) myView.findViewById(R.id.grid_item_image);
        pictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });

        File f = new File(mBasePath.getPath() + "/" + mFiles.get(position));
        pictureView.setImageURI(Uri.fromFile(f));

        return myView;
    }
}
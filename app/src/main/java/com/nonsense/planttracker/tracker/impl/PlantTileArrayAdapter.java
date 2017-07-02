package com.nonsense.planttracker.tracker.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonsense.planttracker.R;

import java.util.List;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantTileArrayAdapter extends ArrayAdapter<Plant> {

    private int viewResourceId;

    public PlantTileArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
    }

    public PlantTileArrayAdapter(Context context, int resource, List<Plant> items) {
        super(context, resource, items);
        viewResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(viewResourceId, null);
        }

        Plant p = getItem(position);

        if (p != null) {
            TextView tt1 = v.findViewById(R.id.firstLine);
            TextView tt2 = v.findViewById(R.id.secondLine);

            if (tt1 != null) {
                tt1.setText(p.getPlantName());
            }

            if (tt2 != null) {
                tt2.setText("Started " + p.getDaysFromStart() + " days ago, Grow Wk. " + p.getWeeksFromStart());
            }
        }
        else    {
            System.out.println("Plant index: " + position + " is null!");
        }

        return v;
    }

}
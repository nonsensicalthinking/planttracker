package com.nonsense.planttracker.tracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.Plant;

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
            TextView plantNameTextView = (TextView)v.findViewById(R.id.firstLine);
            TextView plantSummaryTextView = (TextView)v.findViewById(R.id.secondLine);
            TextView archivedTextView = (TextView)v.findViewById(R.id.archivedTextView);

            if (plantNameTextView != null) {
                plantNameTextView.setText(p.getPlantName());
            }

            if (plantSummaryTextView != null) {
                String flowerWeek = "";

                plantSummaryTextView.setText("Started " + p.getDaysFromStart() +
                        " days ago, Grow Wk. " + p.getWeeksFromStart());
            }

            if (p.isArchived()) {
                archivedTextView.setVisibility(View.VISIBLE);
            }
            else    {
                archivedTextView.setVisibility(View.INVISIBLE);
            }
        }

        return v;
    }
}
package com.nonsense.planttracker.tracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.Recordable;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantRecordableTileArrayAdapter extends ArrayAdapter<Recordable> {

    private int viewResourceId;
    private Plant currentPlant;

    public PlantRecordableTileArrayAdapter(Context context, int textViewResourceId, Plant plant) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        currentPlant = plant;
    }

    public PlantRecordableTileArrayAdapter(Context context, int resource, List<Recordable> items,
                                           Plant plant) {
        super(context, resource, items);
        viewResourceId = resource;
        currentPlant = plant;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(viewResourceId, null);
        }

        Calendar plantStartDate = currentPlant.getPlantStartDate();
        Calendar flowerStartDate = currentPlant.getFlowerStartDate();

        Recordable p = getItem(position);
        long growWeekCount = p.weeksSinceDate(plantStartDate);
        long flowerWeekCount = 0;

        if (flowerStartDate != null)    {
            flowerWeekCount = p.weeksSinceDate(flowerStartDate);
        }

        String weekDisplay = "";
        if (currentPlant.isFlowering()) {
            weekDisplay = "(W" + growWeekCount + "/" + flowerWeekCount + ") ";
        }
        else    {
            weekDisplay = "(W" + growWeekCount + ") ";
        }

        if (p != null) {
            TextView eventTypeTextView = (TextView)v.findViewById(R.id.observEventTypeTextView);
            if (eventTypeTextView != null) {
                eventTypeTextView.setText(p.getEventTypeString());
            }

            TextView recordableSummaryTextView = (TextView)v.findViewById(
                    R.id.recordableSummaryTextView);
            if (recordableSummaryTextView != null) {
                recordableSummaryTextView.setText(weekDisplay + p.Summary());
            }
        }

        return v;
    }
}

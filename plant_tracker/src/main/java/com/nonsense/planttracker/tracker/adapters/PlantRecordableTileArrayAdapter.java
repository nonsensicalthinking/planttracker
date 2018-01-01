package com.nonsense.planttracker.tracker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.Recordable;
import com.nonsense.planttracker.tracker.impl.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantRecordableTileArrayAdapter extends ArrayAdapter<GenericRecord> {

    private int viewResourceId;
    private Plant currentPlant;

    public PlantRecordableTileArrayAdapter(Context context, int textViewResourceId, Plant plant) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        currentPlant = plant;
    }

    public PlantRecordableTileArrayAdapter(Context context, int resource, List<GenericRecord> items,
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

        GenericRecord p = getItem(position);
        if (p != null) {
            // Build phase string
            int phaseCount = p.phaseCount;
            int stateWeekCount = p.weeksSincePhase;
            int growWeekCount = p.weeksSinceStart;

            String phaseDisplay = "[P" + phaseCount + "Wk." + stateWeekCount + "/" +
                    growWeekCount + "]";

            // date/relative weeks
            TextView dateTextView = (TextView)v.findViewById(R.id.dateTextView);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
            dateTextView.setText(sdf.format(p.time.getTime()) + " " +
                    ((phaseDisplay == null) ? "" : phaseDisplay));

            // display name
            TextView eventTypeTextView = (TextView)v.findViewById(R.id.observEventTypeTextView);
            eventTypeTextView.setText(p.displayName);

            // summary text
            TextView recordableSummaryTextView = (TextView)v.findViewById(
                    R.id.recordableSummaryTextView);
            recordableSummaryTextView.setText(p.getSummary());

        }

        return v;
    }


}

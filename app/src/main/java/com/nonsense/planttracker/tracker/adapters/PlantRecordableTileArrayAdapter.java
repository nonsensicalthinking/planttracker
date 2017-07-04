package com.nonsense.planttracker.tracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.Recordable;

import java.util.List;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantRecordableTileArrayAdapter extends ArrayAdapter<Recordable> {

    private int viewResourceId;

    public PlantRecordableTileArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
    }

    public PlantRecordableTileArrayAdapter(Context context, int resource, List<Recordable> items) {
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

        Recordable p = getItem(position);

        if (p != null) {
            TextView recordableSummaryTextView = v.findViewById(R.id.recordableSummaryTextView);
            TextView eventTypeTextView = v.findViewById(R.id.observEventTypeTextView);

            if (recordableSummaryTextView != null) {
                recordableSummaryTextView.setText(p.Summary());
            }

            if (eventTypeTextView != null) {
                eventTypeTextView.setText(p.getEventTypeString());
            }
        }

        return v;
    }
}

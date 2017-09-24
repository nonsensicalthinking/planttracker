package com.nonsense.planttracker.tracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.Group;

import java.util.List;
import java.util.Map;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class CustomEventTileArrayAdapter extends ArrayAdapter<Map.Entry<String, String>> {

    private int viewResourceId;

    public CustomEventTileArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
    }

    public CustomEventTileArrayAdapter(Context context, int resource,
                                       List<Map.Entry<String, String>> items) {
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

        Map.Entry<String, String> entry = getItem(position);

        if (entry != null) {
            TextView customEventTextView = (TextView)v.findViewById(R.id.firstLine);
            //TextView groupSummaryTextView = (TextView)v.findViewById(R.id.secondLine);
            //TextView archivedTextView = (TextView)v.findViewById(R.id.archivedTextView);

            if (customEventTextView != null) {
                customEventTextView.setText( "[" + entry.getKey() + "] " + entry.getValue() );
            }

            /*if (groupSummaryTextView != null) {
                groupSummaryTextView.setText("");
            }*/
        }

        return v;
    }
}
package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.activities.ImageSeriesViewer;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Plant;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantRecordableTileArrayAdapter extends ArrayAdapter<GenericRecord> {

    private int viewResourceId;
    private Plant currentPlant;
    private TreeMap<String, GenericRecord> recordTemplates = null;

    public PlantRecordableTileArrayAdapter(Context context, int textViewResourceId, Plant plant) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        currentPlant = plant;
    }

    public PlantRecordableTileArrayAdapter(Context context, int resource, List<GenericRecord> items,
                                           final TreeMap<String, GenericRecord> recordTemplates,
                                           Plant plant) {
        super(context, resource, items);
        viewResourceId = resource;
        currentPlant = plant;
        this.recordTemplates = recordTemplates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(viewResourceId, null);
        }

        final GenericRecord p = getItem(position);
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

            // TODO Get record id which should be a long representing the instant the template was created
            // TODO this is so we can change the display name of the template and still know which records are which
            // TODO ultimately we want to be able to make everything editable and apply across all records, store only data!
            GenericRecord template = recordTemplates.get(p.displayName);

            String displayName;
            String summaryTemplate;
            int color;

            if (template == null)   {
                displayName = p.displayName;
                summaryTemplate = p.summaryTemplate;
                color = p.color;
            }
            else    {
                displayName = template.displayName;
                summaryTemplate = template.summaryTemplate;
                color = template.color;
            }

            // display name
            TextView eventTypeTextView = (TextView)v.findViewById(R.id.observEventTypeTextView);
            eventTypeTextView.setText(displayName);
            GradientDrawable gradientDrawable = (GradientDrawable)eventTypeTextView.getBackground();
            gradientDrawable.setColor(color);

            // summary text
            TextView recordableSummaryTextView = (TextView)v.findViewById(
                    R.id.recordableSummaryTextView);

            recordableSummaryTextView.setText(p.getSummary(summaryTemplate));

            ImageView cameraIconImageView = (ImageView)v.findViewById(R.id.cameraIconImageView);
            if (p.images != null && p.images.size() > 0) {
                cameraIconImageView.setImageResource(R.drawable.ic_menu_camera);
                cameraIconImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), ImageSeriesViewer.class);
                        intent.putExtra(AndroidConstants.INTENTKEY_FILE_LIST, p.images);
                        getContext().startActivity(intent);
                    }
                });
            }

            ImageView dataPointIconImageView = (ImageView)v.findViewById(R.id.dataPointsImageView);
            if (p.dataPoints != null && p.dataPoints.size() > 0) {
                dataPointIconImageView.setImageResource(R.drawable.ic_menu_share);
            }
        }

        return v;
    }


}
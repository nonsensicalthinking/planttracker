package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

    static class ViewHolder {
        TextView dateTextView;
        TextView eventTypeTextView;
        TextView recordableSummaryTextView;
        ImageView cameraIconImageView;
        ImageView dataPointIconImageView;

        SimpleDateFormat sdf;
    }

    private ViewHolder viewHolder;

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

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            Log.d("IPV", "Record tile first run");
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(viewResourceId, null);

            viewHolder = new ViewHolder();
            viewHolder.dateTextView = (TextView)v.findViewById(R.id.dateTextView);
            viewHolder.sdf = new SimpleDateFormat("EEE, dd MMM yyyy");

            viewHolder.eventTypeTextView = (TextView)v.findViewById(R.id.observEventTypeTextView);
            viewHolder.recordableSummaryTextView = (TextView)v.findViewById(
                    R.id.recordableSummaryTextView);

            viewHolder.cameraIconImageView = (ImageView)v.findViewById(R.id.cameraIconImageView);

            viewHolder.dataPointIconImageView = (ImageView)v.findViewById(R.id.dataPointsImageView);

            v.setTag(viewHolder);
        }
        else    {
            Log.d("IPV", "Record tile reuse");

            viewHolder = (ViewHolder) convertView.getTag();
        }

        final GenericRecord p = getItem(position);
        if (p != null) {
            // Build phase string
            int phaseCount = p.phaseCount;
            int stateWeekCount = p.weeksSincePhase;
            int growWeekCount = p.weeksSinceStart;

            String phaseDisplay = "";
            if (p.phaseCount > 0)   {
                phaseDisplay = "[P" + phaseCount + "Wk." + stateWeekCount + "/" +
                        growWeekCount + "]";
            }
            else    {
                phaseDisplay = "[Wk. " +  growWeekCount + "]";
            }

            // date/relative weeks
            viewHolder.dateTextView.setText(viewHolder.sdf.format(p.time.getTime()) + " " +
                    ((phaseDisplay == null) ? "" : phaseDisplay));

            // TODO Get record id which should be a long representing the instant the template was
            // TODO created this is so we can change the display name of the template and still know
            // TODO which records are which ultimately we want to be able to make everything
            // TODO editable and apply across all records, store only data!
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
            //TextView eventTypeTextView = (TextView)v.findViewById(R.id.observEventTypeTextView);
            viewHolder.eventTypeTextView.setText(displayName);
            GradientDrawable gradientDrawable =
                    (GradientDrawable)viewHolder.eventTypeTextView.getBackground();
            gradientDrawable.setColor(color);

            // summary text
            viewHolder.recordableSummaryTextView.setText(p.getSummary(summaryTemplate));

            // images
            if (p.images != null && p.images.size() > 0) {
                viewHolder.cameraIconImageView.setImageResource(R.drawable.ic_menu_camera);
                viewHolder.cameraIconImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), ImageSeriesViewer.class);
                        intent.putExtra(AndroidConstants.INTENTKEY_FILE_LIST, p.images);
                        getContext().startActivity(intent);
                    }
                });
                viewHolder.cameraIconImageView.setVisibility(View.VISIBLE);
            }
            else    {
                viewHolder.cameraIconImageView.setVisibility(View.GONE);
            }

			// datapoints
            if (p.dataPoints != null && p.dataPoints.size() > 0) {
                viewHolder.dataPointIconImageView.setImageResource(R.drawable.ic_menu_share);
                //TODO add click handler to launch graphing stuff
                viewHolder.dataPointIconImageView.setVisibility(View.VISIBLE);
            }
            else    {
                viewHolder.dataPointIconImageView.setVisibility(View.GONE);
            }
        }

        Log.d("IPV", "Finished filling tile");

        return v;
    }


}

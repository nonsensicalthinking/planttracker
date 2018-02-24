package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
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
import com.nonsense.planttracker.android.activities.PlantTrackerUi;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Plant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantRecordableTileArrayAdapter extends ArrayAdapter<GenericRecord> {

    private static class ViewHolder {
        TextView dateTextView;
        TextView eventTypeTextView;
        TextView recordableSummaryTextView;
        ImageView cameraIconImageView;
        ImageView dataPointIconImageView;
    }
    
    private SimpleDateFormat sdf;
    private LayoutInflater inflater;
    private int viewResourceId;
    private Plant currentPlant;
    private TreeMap<String, GenericRecord> recordTemplates = null;
    private PlantTrackerUi ptui;

    public PlantRecordableTileArrayAdapter(Context context, int textViewResourceId, Plant plant,
                                           PlantTrackerUi ptui) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        currentPlant = plant;
        inflater = LayoutInflater.from(getContext());
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
        this.ptui = ptui;
    }

    public PlantRecordableTileArrayAdapter(Context context, int resource, List<GenericRecord> items,
                                           final TreeMap<String, GenericRecord> recordTemplates,
                                           Plant plant, PlantTrackerUi ptui) {
        super(context, resource, items);
        viewResourceId = resource;
        currentPlant = plant;
        this.recordTemplates = recordTemplates;
        inflater = LayoutInflater.from(getContext());
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
        this.ptui = ptui;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;

        Log.d("IPV", "Start of getView()");
        final GenericRecord p = getItem(position);

        if (convertView == null) {
            Log.d("IPV", "Record tile first run position:" + position);
            convertView = inflater.inflate(R.layout.tile_plant_recordable, null);

            viewHolder = new ViewHolder();
            viewHolder.dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);

            viewHolder.eventTypeTextView = (TextView)convertView.findViewById(
                    R.id.observEventTypeTextView);

            viewHolder.recordableSummaryTextView = (TextView)convertView.findViewById(
                    R.id.recordableSummaryTextView);

            //TODO Use bitflags to determine whether a record needs a view item, try to reduce the number of items needed to load view
            viewHolder.cameraIconImageView = (ImageView)convertView.findViewById(
                    R.id.cameraIconImageView);

            viewHolder.dataPointIconImageView = (ImageView)convertView.findViewById(
                    R.id.dataPointsImageView);

            convertView.setTag(viewHolder);
        }
        else    {
            Log.d("IPV", "Record tile reuse position: " + position);
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (p != null) {
            Runnable rPopulateTile = new Runnable() {
                @Override
                public void run() {
                    backgroundWork(viewHolder, p);
                }
            };

            Thread tPopulateTile = new Thread(rPopulateTile);
            tPopulateTile.start();
        }

        Log.d("IPV", "End of getView()");

        return convertView;
    }

    private void backgroundWork(final ViewHolder viewHolder, GenericRecord p)   {
        StringBuilder sBuilder = new StringBuilder();

        String phaseDisplay;
        String displayName;
        int color;
        String summary;
        boolean showCamera = (p.images != null && p.images.size() > 0);
        boolean showDataPoints = (p.dataPoints != null && p.dataPoints.size() > 0);

        String summaryTemplate;

        // Build phase string
        sBuilder.append(new SimpleDateFormat("EEE, dd MMM yyyy").format(p.time.getTime()));
        sBuilder.append(" ");

        int phaseCount = p.phaseCount;
        int stateWeekCount = p.weeksSincePhase;
        int growWeekCount = p.weeksSinceStart;

        if (p.phaseCount > 0)   {
            sBuilder.append("[P");
            sBuilder.append(phaseCount);
            sBuilder.append("Wk");
            sBuilder.append(stateWeekCount);
            sBuilder.append("/");
            sBuilder.append(growWeekCount);
            sBuilder.append("]");
        }
        else    {
            sBuilder.append("[Wk ");
            sBuilder.append(growWeekCount);
            sBuilder.append("]");
        }

        phaseDisplay = sBuilder.toString();


        // prepare display name of record
        // TODO Get record id which should be a long representing the instant the template was
        // TODO created this is so we can change the display name of the template and still know
        // TODO which records are which ultimately we want to be able to make everything
        // TODO editable and apply across all records, store only data!
        GenericRecord template = recordTemplates.get(p.displayName);


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

        summary = p.getSummary(summaryTemplate);

        // Tell the UI we're ready to update it
        Runnable rUpdateUi = new Runnable()  {
            @Override
            public void run() {
                fillTile(viewHolder, phaseDisplay, displayName, color, summary, showCamera,
                        showDataPoints, p.images);
            }
        };

        ptui.runOnUiThread(rUpdateUi);
    }

    private void fillTile(final ViewHolder viewHolder, String phaseDisplay, String displayName,
                            int color, String summary, boolean showCamera, boolean showDataPoints,
                            ArrayList<String> images)  {
        viewHolder.dateTextView.setText(phaseDisplay);

        viewHolder.eventTypeTextView.setText(displayName);
        GradientDrawable gradientDrawable =
                (GradientDrawable)viewHolder.eventTypeTextView.getBackground();
        gradientDrawable.setColor(color);

        viewHolder.recordableSummaryTextView.setText(summary);

        if (showCamera) {
            viewHolder.cameraIconImageView.setImageResource(R.drawable.ic_menu_camera);
            viewHolder.cameraIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ImageSeriesViewer.class);
                    intent.putExtra(AndroidConstants.INTENTKEY_FILE_LIST, images);
                    getContext().startActivity(intent);
                }
            });
            viewHolder.cameraIconImageView.setVisibility(View.VISIBLE);
        }
        else    {
            viewHolder.cameraIconImageView.setVisibility(View.GONE);
        }

        if (showDataPoints) {
            viewHolder.dataPointIconImageView.setImageResource(R.drawable.ic_menu_share);
            //TODO add click handler to launch graphing stuff
            viewHolder.dataPointIconImageView.setVisibility(View.VISIBLE);
        }
        else    {
            viewHolder.dataPointIconImageView.setVisibility(View.GONE);
        }
    }
}

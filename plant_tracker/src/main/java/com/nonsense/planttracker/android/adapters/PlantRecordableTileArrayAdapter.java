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

    private ViewHolder viewHolder;

    private SimpleDateFormat sdf;
    private LayoutInflater inflater;
    private int viewResourceId;
    private Plant currentPlant;
    private TreeMap<String, GenericRecord> recordTemplates = null;

    public PlantRecordableTileArrayAdapter(Context context, int textViewResourceId, Plant plant) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        currentPlant = plant;
        inflater = LayoutInflater.from(getContext());
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
    }

    public PlantRecordableTileArrayAdapter(Context context, int resource, List<GenericRecord> items,
                                           final TreeMap<String, GenericRecord> recordTemplates,
                                           Plant plant) {
        super(context, resource, items);
        viewResourceId = resource;
        currentPlant = plant;
        this.recordTemplates = recordTemplates;
        inflater = LayoutInflater.from(getContext());
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
//        ViewHolder viewHolder;

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
            fillTile(p);
            Runnable rPopulateTile = new Runnable() {
                @Override
                public void run() {

                    Runnable rUpdateUi = new Runnable()  {
                        @Override
                        public void run() {
                            fillTile(p);
                        }
                    };

                    ((PlantTrackerUi)getContext()).runOnUiThread(rUpdateUi);
                }
            };

            Thread tPopulateTile = new Thread(rPopulateTile);
        }

        Log.d("IPV", "Finished filling tile");

        return convertView;
    }

    protected void fillTile(GenericRecord p)   {
        StringBuilder sBuilder = new StringBuilder();

        // Build phase string
        int phaseCount = p.phaseCount;
        int stateWeekCount = p.weeksSincePhase;
        int growWeekCount = p.weeksSinceStart;

        String phaseDisplay = "";
        if (p.phaseCount > 0)   {
            sBuilder.append("[P");
            sBuilder.append(phaseCount);
            sBuilder.append("Wk");
            sBuilder.append(stateWeekCount);
            sBuilder.append("/");
            sBuilder.append(growWeekCount);
            sBuilder.append("]");

            phaseDisplay = sBuilder.toString();
        }
        else    {
            sBuilder.append("[Wk ");
            sBuilder.append(growWeekCount);
            sBuilder.append("]");
            phaseDisplay = sBuilder.toString();
        }

        // date/relative weeks
        sBuilder.setLength(0);
        sBuilder.append(new SimpleDateFormat("EEE, dd MMM yyyy").format(p.time.getTime()));
        sBuilder.append(" ");
        sBuilder.append(((phaseDisplay == null) ? "" : phaseDisplay));
        viewHolder.dateTextView.setText(sBuilder.toString());

        // TODO Get record id which should be a long representing the instant the template was
        // TODO created this is so we can change the display name of the template and still know
        // TODO which records are which ultimately we want to be able to make everything
        // TODO editable and apply across all records, store only data!
        GenericRecord template = null;//recordTemplates.get(p.displayName);

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
        viewHolder.eventTypeTextView.setText(displayName);
        GradientDrawable gradientDrawable =
                (GradientDrawable)viewHolder.eventTypeTextView.getBackground();
        gradientDrawable.setColor(color);

        // summary text
        //TODO make this pop-in with async... this call to getSummary is expensive!
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
}

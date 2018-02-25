package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

public class PlantRecordableTileArrayAdapter extends RecyclerView.Adapter<PlantRecordableTileArrayAdapter.RecordViewHolder> {

    class RecordViewHolder extends RecyclerView.ViewHolder    {
        TextView dateTextView;
        TextView eventTypeTextView;
        TextView recordableSummaryTextView;
        ImageView cameraIconImageView;
        ImageView dataPointIconImageView;

        public RecordViewHolder(View v) {
            super(v);

            dateTextView = (TextView)v.findViewById(R.id.dateTextView);
            eventTypeTextView = (TextView)v.findViewById(R.id.observEventTypeTextView);
            recordableSummaryTextView = (TextView)v.findViewById( R.id.recordableSummaryTextView);
            cameraIconImageView = (ImageView)v.findViewById(R.id.cameraIconImageView);
            dataPointIconImageView = (ImageView)v.findViewById(R.id.dataPointsImageView);
        }
    }

    private ArrayList<GenericRecord> list;
    private Context context;

    public PlantRecordableTileArrayAdapter(Context c, ArrayList<GenericRecord> l)   {
        list = l;
        context = c;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_plant_recordable,
                null);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecordViewHolder viewHolder, int position) {
        GenericRecord record = list.get(position);
        GenericRecord template = record.template;
        String displayName;
        int color;

        if (template == null)   {
            displayName = record.displayName;
            color = record.color;
        }
        else    {
            displayName = template.displayName;
            color = template.color;
        }

        viewHolder.dateTextView.setText(record.phaseDisplay);

        viewHolder.eventTypeTextView.setText(displayName);
        GradientDrawable gradientDrawable =
                (GradientDrawable)viewHolder.eventTypeTextView.getBackground();
        gradientDrawable.setColor(color);

        viewHolder.recordableSummaryTextView.setText(record.getSummary(record.summaryTemplate));

        if (record.hasImages) {
            viewHolder.cameraIconImageView.setImageResource(R.drawable.ic_menu_camera);
            viewHolder.cameraIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageSeriesViewer.class);
                    intent.putExtra(AndroidConstants.INTENTKEY_FILE_LIST, record.images);
                    context.startActivity(intent);
                }
            });
            viewHolder.cameraIconImageView.setVisibility(View.VISIBLE);
        }
        else    {
            viewHolder.cameraIconImageView.setVisibility(View.INVISIBLE);
        }

        if (record.hasDataPoints) {
            viewHolder.dataPointIconImageView.setImageResource(R.drawable.ic_menu_share);
            //TODO add click handler to launch graphing stuff
            viewHolder.dataPointIconImageView.setVisibility(View.VISIBLE);
        }
        else    {
            viewHolder.dataPointIconImageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return ((list!=null) ? list.size() : 0);
    }
}

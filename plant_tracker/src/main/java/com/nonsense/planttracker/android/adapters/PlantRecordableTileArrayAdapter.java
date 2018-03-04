package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.activities.ImageSeriesViewer;
import com.nonsense.planttracker.android.activities.PlantTrackerUi;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Plant;

import java.util.ArrayList;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantRecordableTileArrayAdapter extends
        RecyclerView.Adapter<PlantRecordableTileArrayAdapter.RecordViewHolder> {

    class RecordViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout layout;
        TextView dateTextView;
        TextView eventTypeTextView;
        TextView recordableSummaryTextView;
        ImageView cameraIconImageView;
        ImageView dataPointIconImageView;

        public RecordViewHolder(View v) {
            super(v);

            layout = (RelativeLayout) v.findViewById(R.id.recordableRelativeLayout);
            dateTextView = (TextView) v.findViewById(R.id.dateTextView);
            eventTypeTextView = (TextView) v.findViewById(R.id.observEventTypeTextView);
            recordableSummaryTextView = (TextView) v.findViewById(R.id.recordableSummaryTextView);
            cameraIconImageView = (ImageView) v.findViewById(R.id.cameraIconImageView);
            dataPointIconImageView = (ImageView) v.findViewById(R.id.dataPointsImageView);
        }
    }

    private Plant plant;
    private ArrayList<GenericRecord> list;
    private Context context;

    private GenericRecord selectedRecord;

    public PlantRecordableTileArrayAdapter(Context c, ArrayList<GenericRecord> l, Plant p)   {
        list = l;
        context = c;
        plant = p;

    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_plant_recordable,
                null);

        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecordViewHolder viewHolder, int position) {
        final int pos = position;
        final GenericRecord record = list.get(pos);
        GenericRecord template = record.template;
        String displayName;
        int color;

        viewHolder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedRecord = record;
                PopupMenu popup = new PopupMenu(context, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_plant_record_context, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete_from_here:
                                displayDeleteFromHereConfirm(selectedRecord);
                                return true;
                            case R.id.delete_from_all:
                                displayDeleteFromAllConfirm(selectedRecord);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                return true;
            }
        });

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

    private void displayDeleteFromHereConfirm(GenericRecord rec) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage("Are you sure you want to delete this record?");
        builder.setIcon(R.drawable.ic_growing_plant);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                plant.removeGenericRecord(rec);
                ((PlantTrackerUi)context).fillIndividualPlantView();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void displayDeleteFromAllConfirm(GenericRecord rec)  {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage("Are you sure you want to delete this record from all plants?");
        builder.setIcon(R.drawable.ic_growing_plant);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((PlantTrackerUi)context).deleteRecordFromAllPlants(rec);
                ((PlantTrackerUi)context).fillIndividualPlantView();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public int getItemCount() {
        return ((list!=null) ? list.size() : 0);
    }
}

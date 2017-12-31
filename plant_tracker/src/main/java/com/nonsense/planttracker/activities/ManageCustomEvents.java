package com.nonsense.planttracker.activities;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.adapters.CustomEventTileArrayAdapter;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Derek Brooks on 12/31/2017.
 */

public class ManageCustomEvents {
/*
    private void fillViewWithCustomEvents() {
        toolbar.setSubtitle("Custom Event Management");

        showFloatingActionButton();

        currentListView = PlantTrackerUi.ListDisplay.CustomEvents;

        setFloatingButtonTextAndAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO create new recordable activity
            }
        });

        ArrayList<Map.Entry<String, String>> events = new ArrayList<>();
        events.addAll(tracker.getGenericRecordTypes());

        final ArrayList<Map.Entry<String, String>> fEvents = events;

        CustomEventTileArrayAdapter adapter = new CustomEventTileArrayAdapter(getBaseContext(),
                R.layout.custom_event_list_tile, events);

        setEmptyViewCaption("No Custom Events Found");

        plantListView.setAdapter(adapter);

        plantListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantTrackerUi.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this custom event?");
                builder.setIcon(R.drawable.ic_growing_plant);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tracker.getPlantTrackerSettings().removeAutoCompleteKeyValuePair(
                                fEvents.get(position).getKey());

                        tracker.removeCustomEvent(fEvents.get(position).getKey());

                        fillViewWithCustomEvents();
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

                return true;
            }
        });

        plantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO display custom event information view and
                // TODO a way to add more
            }
        });
    }

    */
}

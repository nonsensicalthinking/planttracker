package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.Plant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Derek Brooks on 2/9/2018.
 */

public class PlantExportTileAdapter extends ArrayAdapter<Plant> {

//    private Plant currentPlant;
    private int viewResourceId;

    private ArrayList<Long> selectedPlants;

    public PlantExportTileAdapter(Context context, int textViewResourceId, List<Plant> items,
                                  ArrayList<Long> selectedPlants) {
        super(context, textViewResourceId, items);
        viewResourceId = textViewResourceId;
        //currentPlant = plant;

        this.selectedPlants = selectedPlants;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(viewResourceId, null);
        }

        final Plant p = getItem(position);

        if (p != null)  {
            TextView plantNameTextView = v.findViewById(R.id.plantNameTextView);
            plantNameTextView.setText(p.getPlantName());

            TextView exportSummary = v.findViewById(R.id.plantExportSummaryTextView);

            String summary = "" + p.getGroups().size() + " Groups - " + " 0 Record Types";

            exportSummary.setText(summary);

            CheckBox cb = v.findViewById(R.id.plantSelectedCheckBox);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)  {
                        if (!selectedPlants.contains(p.getPlantId()))    {
                            selectedPlants.add(p.getPlantId());
                        }
                    }
                    else    {
                        if (selectedPlants.contains(p.getPlantId()))    {
                            selectedPlants.remove(p.getPlantId());
                        }
                    }
                }
            });
        }

        return v;
    }
}

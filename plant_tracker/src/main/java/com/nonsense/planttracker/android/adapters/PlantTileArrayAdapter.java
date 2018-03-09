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

import java.util.List;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantTileArrayAdapter extends ArrayAdapter<Plant> {

    private List<Plant> selected;

    public PlantTileArrayAdapter(Context context, List<Plant> items, List<Plant> selected) {
        super(context, R.layout.tile_text_checkbox, items);
        this.selected = selected;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.tile_text_checkbox, null);
        }

        Plant p = getItem(position);

        if (p != null) {
            CheckBox cb = v.findViewById(R.id.checkBox);
            cb.setText(p.getPlantName());
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)  {
                        if (!selected.contains(p))  {
                            selected.add(p);
                        }
                    }
                    else    {
                        if (selected.contains(p))   {
                            selected.remove(p);
                        }
                    }
                }
            });

            cb.setChecked(selected.contains(p));
        }

        return v;
    }
}

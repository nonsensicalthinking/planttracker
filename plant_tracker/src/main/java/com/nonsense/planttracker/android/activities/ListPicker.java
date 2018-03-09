package com.nonsense.planttracker.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.adapters.GroupTileArrayAdapter;
import com.nonsense.planttracker.android.adapters.PlantTileArrayAdapter;
import com.nonsense.planttracker.android.adapters.PlantTileRecyclerViewAdapter;
import com.nonsense.planttracker.android.interf.IAction;
import com.nonsense.planttracker.android.interf.IImageCache;
import com.nonsense.planttracker.tracker.impl.Group;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.PlantTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeMap;

import static com.nonsense.planttracker.android.AndroidUtility.decodeSampledBitmapFromResource;

/**
 * Created by Derek Brooks on 3/8/2018.
 */

public class ListPicker extends AppCompatActivity {

    private String groupName;
    private Long groupId;
    private ArrayList<Plant> list;
    private ArrayList<Plant> selected;
    private ArrayList<Plant> initialSelection;

    private ListView groupListView;
    private Toolbar toolbar;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_picker);


        if (savedInstanceState != null) {
            groupName = savedInstanceState.getString("groupName");
            groupId = savedInstanceState.getLong("groupId");

            selected = (ArrayList<Plant>)savedInstanceState.getSerializable("selected");
            initialSelection = (ArrayList<Plant>)savedInstanceState.getSerializable(
                    "initialSelection");
            list = (ArrayList<Plant>)savedInstanceState.getSerializable("list");
        }
        else    {
            Intent i = getIntent();

            groupName = i.getStringExtra("groupName");
            groupId = i.getLongExtra("groupId", 0);

            selected = (ArrayList<Plant>) i.getSerializableExtra("selected");
            initialSelection = new ArrayList<Plant>(selected);
            list = (ArrayList<Plant>) i.getSerializableExtra(
                    AndroidConstants.INTENTKEY_LIST_PICKER_LIST);
        }

        bindUi();
        fillUi();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString("groupName", groupName);
        bundle.putLong("groupId", groupId);

        bundle.putSerializable("selected", selected);
        bundle.putSerializable("initialSelection", initialSelection);
        bundle.putSerializable("list", list);
    }

    private void bindUi()   {
        groupListView = findViewById(R.id.plantListView);
        toolbar = findViewById(R.id.toolbar2);
    }

    private void fillUi()   {
        toolbar.setTitle(groupName + " Group Members");
        groupListView.setAdapter(new PlantTileArrayAdapter(this, list, selected));
    }

    @Override
    public void onBackPressed()    {
        endActivity();
    }

    private void endActivity()  {
        ArrayList<Plant> unselected = new ArrayList<>(initialSelection);
        unselected.removeAll(selected);

        Intent i = new Intent();
        i.putExtra(AndroidConstants.INTENTKEY_LIST_PICKER_SELECTED, selected);
        i.putExtra("unselected", unselected);
        i.putExtra("groupId", groupId);

        setResult(RESULT_OK, i);

        finish();
    }
}

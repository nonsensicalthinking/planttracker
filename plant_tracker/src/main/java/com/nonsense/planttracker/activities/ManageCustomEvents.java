package com.nonsense.planttracker.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.adapters.CustomEventTileArrayAdapter;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.PlantTracker;

import java.util.ArrayList;

/**
 * Created by Derek Brooks on 12/31/2017.
 */

public class ManageCustomEvents extends AppCompatActivity {

    private static final int CREATE_GENERIC_RECORD_TEMPLATE_INTENT = 26;

    private PlantTracker tracker;
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;
    private ListView customRecordTemplateListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_custom_record_templates);

        Intent intent = getIntent();

        tracker = (PlantTracker) intent.getSerializableExtra("tracker");

        bindUi();
        fillUi();
    }

    private void bindUi()   {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingButton);
        customRecordTemplateListView = (ListView) findViewById(R.id.customRecordTemplateListView);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingButton);
    }

    private void fillUi()   {
        toolbar.setTitle("Manage Record Templates");

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageCustomEvents.this,
                        CreateRecordType.class);
                intent.putExtra("genericRecord", new GenericRecord(""));

                startActivityForResult(intent, CREATE_GENERIC_RECORD_TEMPLATE_INTENT);
            }
        });

        ArrayList<String> events = new ArrayList<>();
        events.addAll(tracker.getGenericRecordTypes());

        final ArrayList<String> fEvents = events;

        CustomEventTileArrayAdapter adapter = new CustomEventTileArrayAdapter(getBaseContext(),
                R.layout.custom_event_list_tile, events);

        customRecordTemplateListView.setAdapter(adapter);

        customRecordTemplateListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                           long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ManageCustomEvents.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this custom record template?");
                builder.setIcon(R.drawable.ic_growing_plant);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tracker.removeGenericRecordTemplate(fEvents.get(position));

                        fillUi();
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

        customRecordTemplateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO display custom event information view and
                // TODO a way to add more
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent retInt = new Intent();

        retInt.putExtra("tracker", tracker);

        setResult(Activity.RESULT_OK, retInt);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode) {

            case CREATE_GENERIC_RECORD_TEMPLATE_INTENT:
                if (resultCode == Activity.RESULT_OK)   {
                    GenericRecord record = (GenericRecord)returnedIntent.getSerializableExtra(
                            "genericRecord");

                    tracker.addGenericRecordTemplate(record);

                    fillUi();
                }
                break;
        }
    }

}

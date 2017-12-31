package com.nonsense.planttracker.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.adapters.DataPointTileArrayAdapter;
import com.nonsense.planttracker.tracker.adapters.PlantRecordableTileArrayAdapter;
import com.nonsense.planttracker.tracker.impl.GenericRecord;

import java.util.ArrayList;

public class CreateRecordType extends AppCompatActivity {

    private static final String DEFAULT_DATA_POINT_OPTION = "Select Data Point...";

    private GenericRecord record;

    private String selectedDataPointName;
    private DataPointTileArrayAdapter dpta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_record_type);

        Intent intent = getIntent();

        record = (GenericRecord)intent.getSerializableExtra("genericRecord");

        bindUi();
    }

    private void bindUi()   {
        final EditText recordNameEditText = (EditText)findViewById(R.id.recordNameEditText);
        recordNameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                record.displayName = recordNameEditText.getText().toString();
                return false;
            }
        });

        final CheckBox showNotesFieldCheckBox = (CheckBox)findViewById(R.id.showNotesFieldCheckBox);
        showNotesFieldCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                record.showNotes = b;
            }
        });

        bindDataPointList();

        bindSummaryTemplateInputs();

        final Button cancelButton = (Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        final Button okButton = (Button)findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent retInt = new Intent();

                retInt.putExtra("genericRecord", record);

                setResult(Activity.RESULT_OK, retInt);
                finish();
            }
        });
    }

    private void bindDataPointList()    {
        final ListView dataPointListView = (ListView)findViewById(R.id.dataPointsListView);
        final DataPointTileArrayAdapter dataPointTileArrayAdapter = new DataPointTileArrayAdapter(
                getBaseContext(), R.layout.plant_recordable_tile, record.dataPoints);

        dataPointListView.setAdapter(dataPointTileArrayAdapter);
        dataPointListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDataPointName = dataPointTileArrayAdapter.getItem(i);
            }
        });

        dataPointListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateRecordType.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this data point?");
                builder.setIcon(R.drawable.ic_growing_plant);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        record.dataPoints.remove(dataPointTileArrayAdapter.getItem(position));

                        bindUi();
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

        final Button addDataPointButton = (Button)findViewById(R.id.addButton);
        addDataPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDataPointCollectionDialog();
            }
        });
    }

    private void bindSummaryTemplateInputs()    {
        final EditText summaryTemplateEditText = (EditText)findViewById(
                R.id.summaryTemplateEditText);

        summaryTemplateEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                record.summaryTemplate = summaryTemplateEditText.getText().toString();
                return false;
            }
        });

        final Spinner insertDataPointSpinner = (Spinner)findViewById(R.id.insertDataPointSpinner);

        final ArrayList<String> dataPoints = new ArrayList<String>();
        dataPoints.add(DEFAULT_DATA_POINT_OPTION);
        dataPoints.addAll(record.dataPoints.keySet());

        if (dataPoints.size() == 0)  {
            insertDataPointSpinner.setEnabled(false);
            dataPoints.add("No Data Points");
        }
        else    {
            insertDataPointSpinner.setEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, dataPoints);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        insertDataPointSpinner.setAdapter(adapter);

        insertDataPointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = dataPoints.get(position);
                if (name != null)  {
                    switch(name)    {
                        case DEFAULT_DATA_POINT_OPTION:
                            break;

                        default:
                            summaryTemplateEditText.append("{" + name + "}");
                            insertDataPointSpinner.setSelection(0);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }

    private void displayDataPointCollectionDialog() {
        final Dialog dialog = new Dialog(CreateRecordType.this);
        dialog.setContentView(R.layout.dialog_new_data_point);

        final EditText dataPointNameEditText = (EditText)dialog.findViewById(
                R.id.dataPointNameEditText);

        final Spinner dataPointTypeSpinner = (Spinner)dialog.findViewById(
                R.id.dataPointTypeSpinner);
        final EditText defaultValueEditText = (EditText)dialog.findViewById(
                R.id.defaultValueEditText);

        final ArrayList<String> dataTypeOptions = new ArrayList<>();
        dataTypeOptions.add("Text");
        dataTypeOptions.add("Integer");
        dataTypeOptions.add("Decimal");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, dataTypeOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataPointTypeSpinner.setAdapter(adapter);

        dataPointTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = dataTypeOptions.get(position);
                switch (name)   {
                    case "Text":
                        defaultValueEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;

                    case "Integer":
                        defaultValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;

                    case "Double":
                        defaultValueEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        defaultValueEditText.setKeyListener(DigitsKeyListener.getInstance(
                                false,true));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        final Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        final Button okButton = (Button)dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object selectedObjectType = null;
                String name = dataTypeOptions.get(dataPointTypeSpinner.getSelectedItemPosition());
                switch (name)   {
                    case "Text":
                        selectedObjectType = defaultValueEditText.getText().toString();
                        break;

                    case "Integer":
                        if (!defaultValueEditText.getText().toString().equals("")) {
                            selectedObjectType = Integer.parseInt(
                                    defaultValueEditText.getText().toString());
                        }
                        else    {
                            selectedObjectType = new Integer(0);
                        }
                        break;

                    case "Decimal":
                        if (!defaultValueEditText.getText().toString().equals(""))   {
                            selectedObjectType = Double.parseDouble(
                                    defaultValueEditText.getText().toString());
                        }
                        else    {
                            selectedObjectType = new Double(0.0);
                        }
                        break;
                }

                record.setDataPoint(dataPointNameEditText.getText().toString(), selectedObjectType);

                bindUi();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}

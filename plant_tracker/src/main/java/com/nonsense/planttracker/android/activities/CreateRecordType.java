package com.nonsense.planttracker.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.adapters.DataPointTileArrayAdapter;
import com.nonsense.planttracker.tracker.impl.GenericRecord;

import java.util.ArrayList;

public class CreateRecordType extends AppCompatActivity {

    private static final String DEFAULT_DATA_POINT_OPTION = "Select Data Point...";

    private GenericRecord record;

    private EditText recordNameEditText;
    private TextView colorPreviewTextView;
    private CheckBox showNotesFieldCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_record_type);

        Intent intent = getIntent();

        record = (GenericRecord)intent.getSerializableExtra(
                AndroidConstants.INTENTKEY_GENERIC_RECORD);

        bindUi();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode) {
            case AndroidConstants.ACTIVITY_COLOR_PICKER:
                if (resultCode == RESULT_OK)    {
                    int color = returnedIntent.getIntExtra("color", 0);
                    record.color = color;
                    updateColorPickerPreview(color);
                }
                break;
        }
    }

    private void bindUi()   {
        recordNameEditText = (EditText)findViewById(R.id.recordNameEditText);
        recordNameEditText.setText(record.displayName);
        recordNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                record.displayName = recordNameEditText.getText().toString();
            }
        });

        colorPreviewTextView = (TextView)findViewById(R.id.colorPreviewTextView);

        final int color;
        if (record.color == 0)  {
             color = ColorPicker.generateRandomColor();
        }
        else    {
            color = record.color;
        }

        ((GradientDrawable)colorPreviewTextView.getBackground()).setColor(color);

        colorPreviewTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateRecordType.this, ColorPicker.class);
                intent.putExtra("color", color);

                startActivityForResult(intent, AndroidConstants.ACTIVITY_COLOR_PICKER);
            }
        });

        showNotesFieldCheckBox = (CheckBox)findViewById(R.id.showNotesFieldCheckBox);
        showNotesFieldCheckBox.setChecked(record.showNotes);
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

                retInt.putExtra(AndroidConstants.INTENTKEY_GENERIC_RECORD, record);

                setResult(Activity.RESULT_OK, retInt);
                finish();
            }
        });
    }

    private void bindDataPointList()    {
        final ListView dataPointListView = (ListView)findViewById(R.id.dataPointsListView);
        final DataPointTileArrayAdapter dataPointTileArrayAdapter = new DataPointTileArrayAdapter(
                getBaseContext(), R.layout.tile_plant_recordable, record.dataPoints);

        dataPointListView.setAdapter(dataPointTileArrayAdapter);
        dataPointListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayDataPointCollectionDialog(dataPointTileArrayAdapter.getItem(i));
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
                displayDataPointCollectionDialog(null);
            }
        });
    }

    private void bindSummaryTemplateInputs()    {
        final EditText summaryTemplateEditText = (EditText)findViewById(
                R.id.summaryTemplateEditText);

        summaryTemplateEditText.setText(record.summaryTemplate);

        summaryTemplateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                record.summaryTemplate = summaryTemplateEditText.getText().toString();
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
                            record.summaryTemplate = summaryTemplateEditText.getText().toString();
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

    private void displayDataPointCollectionDialog(String selectedDataPointName) {
        final Dialog dialog = new Dialog(CreateRecordType.this);
        dialog.setContentView(R.layout.dialog_new_data_point);

        final EditText dataPointNameEditText = (EditText)dialog.findViewById(
                R.id.dataPointNameEditText);

        dataPointNameEditText.setText(selectedDataPointName);

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
                        defaultValueEditText.setInputType(
                                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        if (selectedDataPointName != null)  {
            Object dpObj = record.getDataPoint(selectedDataPointName);

            if (dpObj != null)  {

                if (dpObj instanceof String)    {
                    dataPointTypeSpinner.setSelection(adapter.getPosition("Text"));
                    defaultValueEditText.setText((String)dpObj);
                }
                else if (dpObj instanceof Integer) {
                    dataPointTypeSpinner.setSelection(adapter.getPosition("Integer"));
                    defaultValueEditText.setText(((Integer)dpObj).toString());
                }
                else if (dpObj instanceof Double)  {
                    dataPointTypeSpinner.setSelection(adapter.getPosition("Decimal"));
                    defaultValueEditText.setText(((Double)dpObj).toString());
                }
            }
        }

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

    private void updateColorPickerPreview(int color) {
        ((GradientDrawable)colorPreviewTextView.getBackground()).setColor(color);
    }


}

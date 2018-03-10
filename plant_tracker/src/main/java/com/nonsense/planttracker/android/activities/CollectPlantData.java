package com.nonsense.planttracker.android.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.AndroidUtility;
import com.nonsense.planttracker.android.adapters.ImageViewRecyclerViewAdapter;
import com.nonsense.planttracker.tracker.impl.GenericRecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CollectPlantData extends AppCompatActivity {

    private SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private int selectedTab = 0;

    private GenericRecord record;
    private boolean applyToGroup;
    private long selectedGroup;

    private boolean showNotes;
    private TreeMap<String, Long> availableGroups;

    private Uri photoURI;
    private ArrayList<String> images = new ArrayList<>();

    private RecyclerView attachedImageRecyclerView;
    private ImageViewRecyclerViewAdapter adapter;
    private TextView attachedImageCountTextView;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_plant_data);

        Intent intent = getIntent();

        if (savedInstanceState != null) {
            String savedUri = savedInstanceState.getString("photoUri", null);
            if (savedUri != null)   {
                photoURI = Uri.parse(savedUri);
            }

            selectedTab = savedInstanceState.getInt("selectedTab");

            images = savedInstanceState.getStringArrayList("images");
            selectedGroup = savedInstanceState.getLong("selectedGroup");

            record = (GenericRecord) savedInstanceState.getSerializable("record");
            showNotes = savedInstanceState.getBoolean("showNotes");
            availableGroups = new TreeMap<>((Map<String, Long>)(savedInstanceState.getSerializable(
                    "availableGroups")));
        }
        else    {
            record = (GenericRecord) intent.getSerializableExtra(
                    AndroidConstants.INTENTKEY_GENERIC_RECORD);
            availableGroups = new TreeMap<>((Map<String, Long>)(intent.getSerializableExtra(
                    AndroidConstants.INTENTKEY_AVAILABLE_GROUPS)));
            showNotes = (boolean) intent.getBooleanExtra(AndroidConstants.INTENTKEY_SHOW_NOTES,
                    false);
        }

        bindUi();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle)   {
        super.onSaveInstanceState(bundle);

        if (photoURI != null)   {
            bundle.putString("photoUri", photoURI.toString());
        }

        bundle.putInt("selectedTab", selectedTab);

        bundle.putStringArrayList("images", images);
        bundle.putLong("selectedGroup", selectedGroup);
        bundle.putBoolean("applyToGroup", applyToGroup);

        bundle.putSerializable("record", record);
        bundle.putSerializable("availableGroups", availableGroups);
        bundle.putBoolean("showNotes", showNotes);
    }

    private void bindUi()   {
        bindTabs();

        bindGroupListSpinner();

        bindDataPoints();

        if (showNotes)  {
            bindNotes();
        }

        bindSharedControls();
    }

    private void bindTabs() {
        TabHost tabs = findViewById(R.id.tabHost);
        tabs.setup();

        TabHost.TabSpec dialogTab = tabs.newTabSpec("Tab1");
        dialogTab.setIndicator("Info");
        dialogTab.setContent(R.id.tab1);
        tabs.addTab(dialogTab);

        TabHost.TabSpec changeDateTab = tabs.newTabSpec("Tab2");
        changeDateTab.setIndicator("Set Date");
        changeDateTab.setContent(R.id.tab2);
        tabs.addTab(changeDateTab);

        TabHost.TabSpec changeTimeTab = tabs.newTabSpec("Tab3");
        changeTimeTab.setIndicator("Set Time");
        changeTimeTab.setContent(R.id.tab3);
        tabs.addTab(changeTimeTab);

        TabHost.TabSpec attachImagesTab = tabs.newTabSpec("Tab4");
        attachImagesTab.setIndicator("Attach Images");
        attachImagesTab.setContent(R.id.tab4);
        tabs.addTab(attachImagesTab);

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                selectedTab = tabs.getCurrentTab();

                // Hide the keyboard if we're on any of the static tabs
                if (!tabId.equals("Tab1")) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });


        tabs.setCurrentTab(selectedTab);
    }

    private void bindGroupListSpinner() {
        Spinner groupListSpinner = findViewById(R.id.groupListSpinner);

        final ArrayList<String> groupNames = new ArrayList<>(availableGroups.keySet());

        if (groupNames.size() == 0)  {
            groupListSpinner.setEnabled(false);
            groupNames.add("No Groups");

            CheckBox applyToGroupCheckBox = findViewById(R.id.applyToGroupCheckbox);

            applyToGroupCheckBox.setEnabled(false);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, groupNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupListSpinner.setAdapter(adapter);

        groupListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = groupNames.get(position);
                if (name != null && availableGroups != null && availableGroups.containsKey(name))  {
                    selectedGroup = availableGroups.get(name);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGroup = 0;
            }
        });
    }

    private void bindDataPoints()   {
        LinearLayout tab1 = findViewById(R.id.tab1);

        LinearLayout heading = new LinearLayout(this);
        TextView headingTextView = new TextView(this);
        headingTextView.setTextSize(34);
        headingTextView.setTypeface(Typeface.DEFAULT_BOLD);

        headingTextView.setText(record.displayName);
        heading.addView(headingTextView);
        tab1.addView(heading);

        for(String dataPoint : record.dataPoints.keySet())  {
            Object dpo = record.getDataPoint(dataPoint);
            if (dpo instanceof String)   {
                tab1.addView(bindStringInput(dataPoint, (String)dpo));
            }
            else if (dpo instanceof Integer)    {
                tab1.addView(bindIntegerInput(dataPoint, (Integer)dpo));
            }
            else if (dpo instanceof  Double)    {
                tab1.addView(bindDoubleInput(dataPoint, (Double)dpo));
            }
        }
    }

    private void bindNotes()    {
        final LinearLayout tab1 = (LinearLayout)findViewById(R.id.tab1);

        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        final TextView notesTextView = createNewLabel();
        notesTextView.setText("Notes");
        container.addView(notesTextView);

        final EditText editText = new EditText(this);
        container.addView(editText);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        editText.setSingleLine(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                record.notes = editText.getText().toString();
            }
        });

        tab1.addView(container);
    }

    private void bindSharedControls()   {
        record.time = Calendar.getInstance();

        final DatePicker datePicker = findViewById(R.id.eventDatePicker);
        datePicker.updateDate(record.time.get(Calendar.YEAR), record.time.get(Calendar.MONTH),
                record.time.get(Calendar.DAY_OF_MONTH));

        final TimePicker timePicker = findViewById(R.id.eventTimePicker);
        timePicker.setHour(record.time.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(record.time.get(Calendar.MINUTE));

        bindAttachImageTab();

        final CheckBox applyToGroupCheckBox = findViewById(R.id.applyToGroupCheckbox);
        applyToGroupCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applyToGroup = isChecked;
            }
        });

        final Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endActivity();
            }
        });

        final Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelActivity();
            }
        });
    }

    private void bindAttachImageTab()   {
        final Button cameraButton = findViewById(R.id.openCameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(CollectPlantData.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)    {
                    ActivityCompat.requestPermissions(CollectPlantData.this,
                            new String[] {Manifest.permission.CAMERA},
                            AndroidConstants.PERMISSION_REQ_CAMERA);
                }
                else    {
                    dispatchTakePictureIntent();
                }
            }
        });

        final Button galleryButton = findViewById(R.id.attachImagesButton);
        galleryButton.setEnabled(true);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(i, AndroidConstants.ACTIVITY_IMAGE_CHOOSER);
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(CollectPlantData.this);
        attachedImageRecyclerView = findViewById(R.id.attachedImageRecyclerView);
        adapter = getAttachedImageAdapter();
        attachedImageRecyclerView.setLayoutManager(llm);
        attachedImageRecyclerView.setAdapter(adapter);

        attachedImageCountTextView = findViewById(R.id.attachImageCountTextView);
        attachedImageCountTextView.setText(String.valueOf(images.size()));

        ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                Collections.swap(images, viewHolder.getAdapterPosition(),
                        target.getAdapterPosition());

                adapter.notifyItemMoved(viewHolder.getAdapterPosition(),
                        target.getAdapterPosition());

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                switch(direction)   {
                    case ItemTouchHelper.LEFT:
                        promptRemoveAttachment(pos);
                        break;
                    case ItemTouchHelper.RIGHT:
                        //TODO change image-plant association
                        Toast.makeText(CollectPlantData.this,
                                "Launch plant pick intent", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(pos);
                        break;
                }
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder
                    viewHolder) {
                return makeMovementFlags(ItemTouchHelper.DOWN | ItemTouchHelper.UP,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }
        };

        ItemTouchHelper ith = new ItemTouchHelper(ithCallback);

        ith.attachToRecyclerView(attachedImageRecyclerView);
    }

    private void promptRemoveAttachment(int pos)    {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                CollectPlantData.this);
        builder.setTitle(R.string.app_name);
        builder.setMessage("Are you sure you want to remove this attachment?");
        builder.setIcon(R.drawable.ic_growing_plant);
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String filePath = images.get(pos);
                        images.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        updateImageAttachmentCount();
                        File f = new File(filePath);
                        f.delete();
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                adapter.notifyItemChanged(pos);
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    private LinearLayout bindStringInput(final String key, String value)   {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView label = createNewLabel();
        label.setText(key);
        layout.addView(label);

        final EditText editText = createNewInput();
        layout.addView(editText);
        editText.setText(value);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                record.setDataPoint(key, editText.getText().toString());
            }
        });

        return layout;
    }

    private LinearLayout bindIntegerInput(final String key, Integer value)   {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView label = createNewLabel();
        label.setText(key);
        layout.addView(label);

        final EditText editText = createNewInput();
        layout.addView(editText);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setText(String.valueOf(value));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (!editText.getText().toString().equals(""))  {
                        Integer val = Integer.parseInt(editText.getText().toString());
                        record.setDataPoint(key, val);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //TODO add up and down button, whole number adjustment

        return layout;
    }

    private LinearLayout bindDoubleInput(final String key, Double value) {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView label = createNewLabel();
        label.setText(key);
        layout.addView(label);

        final EditText editText = createNewInput();
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setText(String.valueOf(value));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (!editText.getText().toString().equals("")) {
                        Double val = Double.parseDouble(editText.getText().toString());
                        record.setDataPoint(key, val);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        layout.addView(editText);

        //TODO add up and down button, .1 adjustment

        return layout;
    }

    private ImageViewRecyclerViewAdapter getAttachedImageAdapter()   {
        return new ImageViewRecyclerViewAdapter(CollectPlantData.this, images);
    }

    private void refreshImageAttachments()  {
        adapter.notifyDataSetChanged();
        attachedImageCountTextView.setText(String.valueOf(images.size()));
    }

    private void updateImageAttachmentCount()   {
        attachedImageCountTextView.setText(String.valueOf(images.size()));
    }

    private TextView createNewLabel()   {
        TextView label = new TextView(this);

        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
        label.setLayoutParams(params);
        label.setGravity(Gravity.CENTER);

        return label;
    }

    private EditText createNewInput()   {
        EditText editText = new EditText(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(params);

        return editText;
    }

    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode) {
            case AndroidConstants.ACTIVITY_IMAGE_CHOOSER:
                if (resultCode == -1)   {
                    if (returnedIntent.getClipData() != null)   {
                        int selectedItems = returnedIntent.getClipData().getItemCount();
                        for(int x = 0; x < selectedItems; x++)  {
                            Uri selected = returnedIntent.getClipData().getItemAt(x).getUri();
                            makeLocalCopyOfAttachedImage(selected);
                        }
                    }
                    else    {
                        // they may have only attached one image and running an older version of android
                        if (returnedIntent.getData() != null)   {
                            makeLocalCopyOfAttachedImage(returnedIntent.getData());
                        }
                    }

                    refreshImageAttachments();
                }
                break;

            case AndroidConstants.ACTIVITY_PLANT_CAM:
                if (resultCode == RESULT_OK)   {
                    File f = new File(photoURI.getPath());
                    String path = getExternalFilesDir(AndroidConstants.PATH_TRACKER_IMAGES) + "/" +
                            f.getName();
                    images.add(path);
                    dispatchTakePictureIntent();

                    refreshImageAttachments();
                }
                else    {
                    File f = new File(photoURI.getPath());
                    if (!f.delete())    {
                        Log.e("FILE I/O", "Unable to delete unused temp image file");
                    }
                }
                break;
        }
    }

    private void makeLocalCopyOfAttachedImage(Uri selected) {
        try {
            File f = new File(selected.getPath());
            String filePath =
                    getExternalFilesDir(AndroidConstants.PATH_TRACKER_IMAGES) +
                            "/" + f.getName() + ".jpg";

            InputStream is = getContentResolver().openInputStream(selected);
            AndroidUtility.copyUriToLocation(is, filePath);
            images.add(filePath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                String path = getApplicationContext().getPackageName();
                photoURI = FileProvider.getUriForFile(this,
                        path + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, AndroidConstants.ACTIVITY_PLANT_CAM);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = simpleDateFormat.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(AndroidConstants.PATH_TRACKER_IMAGES);
        return File.createTempFile(imageFileName,".jpg", storageDir);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case AndroidConstants.PERMISSION_REQ_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                }
                else {
                    Toast.makeText(CollectPlantData.this,
                            "You must allow camera usage to take pictures.",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void cancelActivity()   {
        if (record.images != null)  {
            for(String s : images)   {
                File f = new File(s);
                if (!f.delete()) {
                    Log.d("DELETE", "Unable to delete file: " + f.getPath());
                }
            }
        }

        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void endActivity()  {
        DatePicker datePicker = findViewById(R.id.eventDatePicker);
        TimePicker timePicker = findViewById(R.id.eventTimePicker);

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                timePicker.getHour(), timePicker.getMinute());

        record.time = cal;

        Intent retInt = new Intent();

        retInt.putExtra(AndroidConstants.INTENTKEY_GENERIC_RECORD, record);
        retInt.putExtra(AndroidConstants.INTENTKEY_APPLY_TO_GROUP, applyToGroup);
        retInt.putExtra(AndroidConstants.INTENTKEY_SELECTED_GROUP, selectedGroup);

        // so the image at the top of the list is the thumbnail
        Collections.reverse(images);
        retInt.putExtra(AndroidConstants.INTENTKEY_SELECTED_FILES, images);

        setResult(Activity.RESULT_OK, retInt);
        finish();
    }
}

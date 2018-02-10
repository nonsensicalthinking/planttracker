package com.nonsense.planttracker.android.activities;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.Zipper;
import com.nonsense.planttracker.android.adapters.PlantExportTileAdapter;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Group;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.PlantData;
import com.nonsense.planttracker.tracker.impl.PlantRequirements;
import com.nonsense.planttracker.tracker.impl.PlantTracker;
import com.nonsense.planttracker.tracker.impl.Utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Created by Derek Brooks on 12/28/2017.
 */

//TODO!

public class ImportExportData extends AppCompatActivity {
    private static String PT_FILE_EXTENSION = ".json";

    private Context context;

    // Export
    private PlantTracker tracker;

    // Import
    private ArrayList<Long> plantIds;
    private ArrayList<Plant> plantsInArchive;
    private Uri packageUri;

    // Both
    private boolean isExport;
    private final ArrayList<Long> selectedPlants = new ArrayList<>();


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_export);

        Intent intent = getIntent();

        isExport = intent.getBooleanExtra("isExport", false);
        tracker = (PlantTracker)intent.getSerializableExtra(
                AndroidConstants.INTENTKEY_PLANT_TRACKER);

        bindUi();
    }

    private void bindUi()   {
        ListView plantsListView = findViewById(R.id.plantsListView);

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelActivity();
            }
        });

        Button exportButton = findViewById(R.id.okButton);

        if (isExport)   {
            PlantExportTileAdapter plantExportTileAdapter =
                    new PlantExportTileAdapter(getBaseContext(),R.layout.tile_plant_import_export,
                            tracker.getAllPlants(), selectedPlants);

            plantsListView.setAdapter(plantExportTileAdapter);
            plantsListView.setVisibility(View.VISIBLE);

            Button selectArchiveButton = findViewById(R.id.selectArchiveButton);
            selectArchiveButton.setVisibility(View.GONE);
            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endExportActivity();
                }
            });
        }
        else    {
            exportButton.setText("Import");
            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endImportActivity();
                }
            });

            Button selectArchiveButton = findViewById(R.id.selectArchiveButton);
            selectArchiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/zip");
                    startActivityForResult(Intent.createChooser(intent, "Open folder"),
                            AndroidConstants.ACTIVITY_IMPORT_CHOOSER);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);

        switch (requestCode)    {
            case AndroidConstants.ACTIVITY_IMPORT_CHOOSER:
                if (resultCode == RESULT_OK)    {
                    try {
                        packageUri = returnedIntent.getData();
                        readPlantsFromArchive();
                        fillImportPlantList();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void fillImportPlantList()  {
        ListView plantsListView = findViewById(R.id.plantsListView);

        plantsListView.setVisibility(View.VISIBLE);

        PlantExportTileAdapter plantExportTileAdapter =
                new PlantExportTileAdapter(getBaseContext(),R.layout.tile_plant_import_export,
                        plantsInArchive, selectedPlants);

        plantsListView.setAdapter(plantExportTileAdapter);
    }

    @SuppressWarnings("unchecked")
    private void readPlantsFromArchive()    {
        try {
            ZipInputStream zis = new ZipInputStream(getContentResolver().openInputStream(packageUri));
            Gson g = new Gson();
            String zipInput = Zipper.extractJsonFileContents(
                    getContentResolver().openInputStream(packageUri), "/package.json");
            ArrayList<Long> packageContents = (ArrayList<Long>)g.fromJson(zipInput,
                    new TypeToken<ArrayList<Long>>(){}.getType());

            if (packageContents == null)    {
                Toast.makeText(ImportExportData.this, "Error: Couldn't open archive.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            plantsInArchive = new ArrayList<>();

            for(Long l : packageContents)   {
                Plant p = new Plant();
                String jsonPlantData = Zipper.extractJsonFileContents(
                        getContentResolver().openInputStream(packageUri),
                        "/" + l + ".json");

                PlantData plantData = (PlantData)g.fromJson(jsonPlantData,
                        new TypeToken<PlantData>(){}.getType());

                p.setPlantData(plantData);

                plantsInArchive.add(p);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void performBackup()    {
        Dialog d = com.nonsense.planttracker.android.Utility.
                displayOperationInProgressDialog(ImportExportData.this, "Export");

        ArrayList<String> files = new ArrayList<>();
        files.add(getExternalFilesDir(AndroidConstants.PATH_TRACKER_SETTINGS).getPath());
        files.add(getExternalFilesDir(AndroidConstants.PATH_TRACKER_DATA).getPath());
        files.add(getExternalFilesDir(AndroidConstants.PATH_TRACKER_IMAGES).getPath());

        String backupFileName = "full_backup_" + Utility.getDateTimeFileString();

        Zipper.compressTrackerData(files,
                getExternalFilesDir("archive").getPath() +
                        "/" + backupFileName + ".zip");

        d.hide();
    }

    private PlantRequirements preparePlantExport(long plantId)  {
        PlantRequirements pr = new PlantRequirements();
        Plant p = tracker.getPlantById(plantId);

        ArrayList<Long> groupsList = p.getGroups();
        if (groupsList.size() > 0)  {
            ArrayList<Group> groups = new ArrayList<>();

            for(Long id : groupsList)   {
                Group g = tracker.getGroup(id);
                if (g != null)  {
                    groups.add(g);
                }
            }

            pr.groups = groups;
        }

        ArrayList<Long> templates = p.getUniqueRecordTemplatesUsed();
        if (templates.size() > 0)   {
            ArrayList<GenericRecord> recordTemplates = new ArrayList<>();
            for(Long id : templates)    {
                recordTemplates.add(tracker.getPlantTrackerSettings().getGenericRecordTemplate(id));
            }

            pr.recordTemplates = recordTemplates;
        }

        return pr;
    }

    private void createArchive()    {
        ArrayList<String> filePaths = new ArrayList<>();

        try {
            Gson gson = new Gson();
            for(Long key : selectedPlants)  {
                String outputLocation =
                        getExternalFilesDir("temp").getPath() + "/" + key + "_req.json";

                writeTextFile(outputLocation, gson.toJson(preparePlantExport(key)));

                filePaths.add(outputLocation);
                filePaths.add(getExternalFilesDir(AndroidConstants.PATH_TRACKER_DATA).getPath() +
                        "/" + key + ".json");

                Plant p = tracker.getPlantById(key);
                filePaths.addAll(p.getAllImagesForPlant());
            }

            String selectedPathsJson = gson.toJson(selectedPlants);
            String zipPackagePath = getExternalFilesDir("temp").getPath() + "/package.json";
            writeTextFile(zipPackagePath, selectedPathsJson);

            filePaths.add(zipPackagePath);

            Zipper.compressTrackerData(filePaths,
                    getExternalFilesDir("testSelectedExport").getPath() +
                            "/partial_" + Utility.getDateTimeFileString() + ".zip");

            File folder = new File(getExternalFilesDir("temp").getPath());
            File[] files = folder.listFiles();
            for(File f : files)   {
                f.delete();
            }
        }
        catch(Exception e)  {
            e.printStackTrace();
        }
    }

    private void writeTextFile(String path, String data)   {
        String outputLocation = path;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputLocation));
            bw.write(data);
            bw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importSelectedPlantList()  {
        for(Plant p : plantsInArchive)  {
            if (selectedPlants.contains(p.getPlantId()))    {
                importPlant(p);
            }
        }
    }

    private void importPlant(Plant p)   {
        ContentResolver cr = getContentResolver();
        try {
            Zipper.extractFileToLocation(cr.openInputStream(packageUri),
                    p.getPlantId()+".json", getExternalFilesDir("plants").getPath());

            ArrayList<String> images = p.getAllImagesForPlant();

            for(String image : images)  {
                String fileName = image.substring(image.lastIndexOf('/')+1);
                Zipper.extractFileToLocation(cr.openInputStream(packageUri), fileName,
                        getExternalFilesDir(AndroidConstants.PATH_TRACKER_IMAGES).getPath());
            }

            //TODO tracker groups and record templates
            String reqJson = Zipper.extractJsonFileContents(cr.openInputStream(packageUri),
                    "/" + p.getPlantId() + "_req.json");

            Gson g = new Gson();
            PlantRequirements plantRequirements = g.fromJson(reqJson,
                    new TypeToken<PlantRequirements>(){}.getType());

            if (plantRequirements != null)  {
                if (plantRequirements.groups != null && plantRequirements.groups.size() > 0)    {
                    importGroups(plantRequirements.groups);

                }

                if (plantRequirements.recordTemplates != null &&
                        plantRequirements.recordTemplates.size() > 0)  {
                    importTemplates(plantRequirements.recordTemplates);
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void importGroups(ArrayList<Group> groups) {
        for(Group g : groups)   {
            if (tracker.getGroup(g.getGroupId()) == null) {
                tracker.addGroup(g);
            }
        }
    }

    private void importTemplates(ArrayList<GenericRecord> templates) {
        for(GenericRecord r : templates)    {
            if (tracker.getGenericRecordTemplate(r.id) == null) {
                tracker.addGenericRecordTemplate(r);
            }
        }
    }

    // Routines for sending the files places...
    // email files
    public void email(Context context, String emailTo, String emailCC, String subject,
                      String emailText, List<String> filePaths) {

        //need to "send multiple" to get more than one attachment
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailTo});
        emailIntent.putExtra(android.content.Intent.EXTRA_CC, new String[]{emailCC});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);

        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<Uri>();

        //convert from paths to Android friendly Parcelable Uri's
        for (String file : filePaths) {
            File fileIn = new File(file);
            Uri u = FileProvider.getUriForFile(context,
                    "com.nonsense.planttracker.provider", fileIn);

            uris.add(u);
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }


    // Activity related methods
    private void cancelActivity()   {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void endImportActivity()    {
        importSelectedPlantList();

        tracker.settingsChanged();

        setResult(RESULT_OK);
        finish();
    }

    private void endExportActivity()  {
        createArchive();

        setResult(RESULT_OK);
        finish();
        Toast.makeText(ImportExportData.this, "Export Complete.", Toast.LENGTH_SHORT).show();
    }
}

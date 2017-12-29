package com.nonsense.planttracker.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.PlantTracker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Derek Brooks on 12/28/2017.
 */

//TODO!

public class ExportData  extends AppCompatActivity {
    private static String PT_FILE_EXTENSION = ".json";

    private Context context;

    private PlantTracker tracker;

    /* Import / export */
    private void presentImportDialog() {
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        final ArrayList<String> fileNames = new ArrayList<>();

        final File[] files = downloadDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(PT_FILE_EXTENSION)) {
                    // build list of file names while we're at it...
                    fileNames.add(name);
                    return true;
                }

                return false;
            }
        });

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_import_plants);

        final ListView importPlantsListView = (ListView) dialog.findViewById(
                R.id.importPlantsListView);

        final ArrayList<String> selectedFiles = new ArrayList<>();

        importPlantsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        importPlantsListView.setAdapter(new ArrayAdapter<String>(
                getBaseContext(), android.R.layout.simple_list_item_1, fileNames) {

            public void onItemClick(AdapterView<?> adapter, View arg1, int index, long arg3) {
                if (selectedFiles.contains(fileNames.get(index))) {
                    selectedFiles.remove(fileNames.get(index));
                } else {
                    selectedFiles.add(fileNames.get(index));
                }
            }
        });

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<File> fileImportCollection = new ArrayList<File>();
                for (String fileName : selectedFiles) {
                    for (File f : files) {
                        if (f.getName().equals(fileName)) {
                            fileImportCollection.add(f);
                        }
                    }
                }

                tracker.importPlants(fileImportCollection);

                dialog.dismiss();
            }
        });

        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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




}

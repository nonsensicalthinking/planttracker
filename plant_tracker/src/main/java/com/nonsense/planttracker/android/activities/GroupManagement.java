package com.nonsense.planttracker.android.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.adapters.GroupTileArrayAdapter;
import com.nonsense.planttracker.android.interf.ICallback;
import com.nonsense.planttracker.tracker.impl.Group;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.PlantTracker;
import com.nonsense.planttracker.tracker.interf.IPlantTrackerListener;

import java.util.ArrayList;

/**
 * Created by Derek Brooks on 3/2/2018.
 */

public class GroupManagement extends AppCompatActivity implements IPlantTrackerListener {

    private PlantTracker plantTracker;
    private ListView groupListView;
    private boolean groupsModified = false;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        // TODO get tracker
        Intent startingIntent = getIntent();

        plantTracker = (PlantTracker)startingIntent.getSerializableExtra(
                AndroidConstants.INTENTKEY_PLANT_TRACKER);

        plantTracker.setPlantTrackerListener(this);

        bindView();

        fillGroups();
    }

    private void bindView() {
        //TODO add button

        groupListView = findViewById(R.id.groupListView);
        Toolbar gmToolbar = findViewById(R.id.gmToolbar);
        gmToolbar.setTitle("Manage Groups");
    }

    private void fillGroups()   {
        FloatingActionButton floatingButton = findViewById(R.id.floatingActionButton);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentAddGroupDialog(GroupManagement.this, plantTracker, new ICallback() {
                    @Override
                    public void callback() {
                        fillGroups();
                    }
                });
            }
        });

        final ArrayList<Group> groups = plantTracker.getAllGroups();

        GroupTileArrayAdapter adapter = new GroupTileArrayAdapter(GroupManagement.this,
                R.layout.tile_group_list, groups);

        setEmptyViewCaption("No Groups Found");
        
        groupListView.setAdapter(adapter);

        groupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                           long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupManagement.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this group?");
                builder.setIcon(R.drawable.ic_bundle_of_hay);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        plantTracker.removeGroup(groups.get(position).getGroupId());
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

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO display group information view with all current group members and
                // TODO a way to add more
            }
        });
    }

    static void presentAddGroupDialog(Context c, PlantTracker tracker, ICallback caller) {
        final Dialog dialog = new Dialog(c);
        dialog.setContentView(R.layout.dialog_add_group);

        final EditText groupNameEditText = (EditText) dialog.findViewById(R.id.groupNameEditText);
        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupNameEditText.getText().toString().isEmpty()) {
                    return;
                }

                tracker.addGroup(groupNameEditText.getText().toString());

                caller.callback();
                dialog.dismiss();
            }
        });

        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    static void presentRenameGroupDialog(Context c, PlantTracker tracker, long groupId,
                                          ICallback caller) {
        final Dialog dialog = new Dialog(c);
        dialog.setContentView(R.layout.dialog_rename_group);

        final EditText groupNameEditText = (EditText) dialog.findViewById(R.id.groupNameEditText);
        final TextView groupNameTextView = (TextView) dialog.findViewById(R.id.groupNameTextView);
        groupNameTextView.setText(tracker.getGroup(groupId).getGroupName());

        final long localGroupId = groupId;

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupNameEditText.getText().toString().isEmpty()) {
                    return;
                }

                tracker.renameGroup(localGroupId, groupNameEditText.getText().toString());
                caller.callback();
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


    private void setEmptyViewCaption(String caption) {
//        View emptyPlantListView = findViewById(R.id.emptyPlantListView);
//        TextView itemNotFoundCaptionText = (TextView) emptyPlantListView.findViewById(
//                R.id.itemNotFoundCaptionText);
//
//        if (itemNotFoundCaptionText != null) {
//            itemNotFoundCaptionText.setText(caption);
//        }
//
//        emptyPlantListView.invalidate();
    }

    private void setFloatingButtonTextAndAction(View.OnClickListener listener) {
        FloatingActionButton floatingButton = findViewById(R.id.floatingActionButton);
        floatingButton.setOnClickListener(listener);
    }

    @Override
    public void onBackPressed() {
        endActivity();
    }

    private void endActivity()  {
        if (groupsModified) {
            plantTracker.savePlantTrackerSettings();
            Intent i = new Intent();
            i.putExtra(AndroidConstants.INTENTKEY_PLANT_TRACKER, plantTracker);
            setResult(RESULT_OK, i);
        }
        else    {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    @Override
    public void plantUpdated(Plant p) {

    }

    @Override
    public void groupsUpdated() {
        groupsModified = true;
        fillGroups();
    }
}

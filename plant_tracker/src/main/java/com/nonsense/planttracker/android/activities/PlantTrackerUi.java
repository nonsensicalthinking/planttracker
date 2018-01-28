package com.nonsense.planttracker.android.activities;

// Additional source attribution for icon:
// Plant Icon website http://www.freepik.com/free-icon/plant-growing_743982.htm

// Note pad and pen icon: https://www.flaticon.com/free-icon/note-and-pen_70667

// Bundle of Hay http://ic8.link/5291 (https://icons8.com/icon/5291/hay)

// Moon phase icon by Haikinator https://www.iconfinder.com/Haikinator
// https://www.iconfinder.com/icons/248569/cloud_clouds_cloudy_crescent_forecast_moon_night_phase_phases_waning_weather_icon




import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.adapters.GroupTileArrayAdapter;
import com.nonsense.planttracker.android.adapters.PlantStateTileArrayAdapter;
import com.nonsense.planttracker.android.listeners.OnSwipeTouchListener;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Group;
import com.nonsense.planttracker.android.adapters.PlantRecordableTileArrayAdapter;
import com.nonsense.planttracker.android.adapters.PlantTileArrayAdapter;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.actions.PlantAction;
import com.nonsense.planttracker.tracker.impl.PlantTracker;
import com.nonsense.planttracker.tracker.impl.Utility;
import com.nonsense.planttracker.tracker.interf.IDialogHandler;
import com.nonsense.planttracker.tracker.interf.IPlantTrackerListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Stack;
import java.util.TreeMap;

public class PlantTrackerUi extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IPlantTrackerListener {

    private static String PT_FILE_EXTENSION = ".json";

    private static final String CREATE_NEW_GENERIC_RECORD_OPTION = "Create new record type...";
    private static final String ADD_NEW_RECORD = "-- Add new record --";

    private ViewSwitcher switcher;
    private LinearLayout allPlantsView;
    private LinearLayout individualPlantView;
    private Toolbar toolbar;

    private ListDisplay currentListView;

    // All plants view
    private ListView plantListView;

    // Individual plant view
    private TextView daysSinceGrowStartTextView;
    private TextView weeksSinceGrowStartTextView;
    private TextView weeksSinceStateStartTextView;
    private TextView daysSinceStateStartTextView;
    private TextView stateNameTextView;
    private TextView fromSeedTextView;
    private ListView recordableEventListView;
    private TableRow parentPlantTableRow;
    private Spinner addEventSpinner;
    private Menu individualPlantMenu;
    private SubMenu addToGroup;
    private SubMenu removeFromGroup;
    private ImageView mPlantImage;

    // Data
    private TreeMap<Integer, Long> menuItemToGroupIdMapping = new TreeMap<>();
    private Stack<Plant> parentPlantViewStack;
    private ArrayList<Plant> currentDisplayArray;
    private PlantDisplay plantDisplay = PlantDisplay.Active;
    private PlantTracker tracker;
    private Plant currentPlant;
    private long groupIdViewFilter;

    private enum PlantDisplay {
        All,
        Active,
        Archived,
        Group
    }

    private enum ListDisplay {
        Plants,
        Groups,
        CustomEvents,
        Phases
    }

    /*
     *** View Population ***
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plant_tracker_ui);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Showing Active Plants");

        switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        allPlantsView = (LinearLayout) findViewById(R.id.allPlantsView);
        individualPlantView = (LinearLayout) findViewById(R.id.individualPlantView);

        // all plants view
        plantListView = (ListView) findViewById(R.id.plantListView);
        plantListView.setEmptyView(findViewById(R.id.emptyPlantListView));

        bindIndividualPlantView();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerOpened(View drawerView) {
                drawerView.bringToFront();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        parentPlantViewStack = new Stack<>();
        tracker = new PlantTracker(getFilesDir().toString());

        tracker.setPlantTrackerListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        refreshDrawerGroups();

        fillViewWithPlants();
    }

    private void bindIndividualPlantView() {
        daysSinceGrowStartTextView = (TextView)findViewById(R.id.daysSinceGrowStartTextView);
        weeksSinceGrowStartTextView = (TextView)findViewById(R.id.weeksSinceGrowStartTextView);
        fromSeedTextView = (TextView) findViewById(R.id.fromSeedTextView);
        recordableEventListView = (ListView)findViewById(R.id.recordableEventListView);
        parentPlantTableRow = (TableRow)findViewById(R.id.parentPlantTableRow);
        addEventSpinner = (Spinner)findViewById(R.id.addEventSpinner);

        stateNameTextView = (TextView) findViewById(R.id.stateNameTextView);
        stateNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCollectPlantDataIntent(currentPlant.getPhaseChangeRecord(), true);
            }
        });

        weeksSinceStateStartTextView = (TextView)findViewById(
                R.id.weeksSinceStateStartTextView);

        daysSinceStateStartTextView = (TextView)findViewById(
                R.id.daysSinceStateStartTextView);

        mPlantImage = (ImageView)findViewById(R.id.lastCaptureImageView);
    }

    private void showFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingButton);
        fab.setVisibility(View.VISIBLE);
    }

    private void hideFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingButton);
        fab.setVisibility(View.GONE);
    }

    private void fillViewWithPlants() {
        toolbar.setTitle(R.string.app_name);

        setEmptyViewCaption("No Plants Found");

        showFloatingActionButton();

        currentListView = ListDisplay.Plants;

        setFloatingButtonTextAndAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentAddPlantDialog(0);
                fillViewWithPlants();
            }
        });

        switch (plantDisplay) {
            case All:
                currentDisplayArray = tracker.getAllPlants();
                toolbar.setSubtitle("Showing All Plants");
                break;
            case Active:
                currentDisplayArray = tracker.getActivePlants();
                toolbar.setSubtitle("Showing Active Plants");
                break;
            case Archived:
                currentDisplayArray = tracker.getArchivedPlants();
                toolbar.setSubtitle("Showing Archived Plants");
                break;
            case Group:
                Group g = tracker.getGroup(groupIdViewFilter);
                ArrayList<Plant> plants = tracker.getActivePlants();
                ArrayList<Plant> displayList = new ArrayList<>();
                for (Plant p : plants) {
                    if (p.getGroups().contains(g.getGroupId())) {
                        displayList.add(p);
                    }
                }

                currentDisplayArray = displayList;
                toolbar.setSubtitle("Showing Group: " + g.getGroupName());
                break;
        }

        PlantTileArrayAdapter adapter = new PlantTileArrayAdapter(getBaseContext(),
                R.layout.tile_plant_list, currentDisplayArray);

        plantListView.setAdapter(adapter);
        plantListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantTrackerUi.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this plant?");
                builder.setIcon(R.drawable.ic_growing_plant);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tracker.removePlant(currentDisplayArray.get(position));
                        fillViewWithPlants();
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
        plantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                currentPlant = currentDisplayArray.get(position);
                toolbar.setSubtitle("");
                fillIndividualPlantView();
                switcherToNext();
            }
        });
    }

    private void fillViewWithGroups() {
        toolbar.setSubtitle("Group Management");

        showFloatingActionButton();

        currentListView = ListDisplay.Groups;

        setFloatingButtonTextAndAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentAddGroupDialog();
            }
        });

        final ArrayList<Group> groups = tracker.getAllGroups();

        GroupTileArrayAdapter adapter = new GroupTileArrayAdapter(getBaseContext(),
                R.layout.tile_group_list, groups);

        setEmptyViewCaption("No Groups Found");

        plantListView.setAdapter(adapter);

        plantListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantTrackerUi.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this group?");
                builder.setIcon(R.drawable.ic_bundle_of_hay);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tracker.removeGroup(groups.get(position).getGroupId());
                        fillViewWithGroups();
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

        plantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO display group information view with all current group members and
                // TODO a way to add more
            }
        });
    }

    private void fillViewWithPlantPhases() {
        toolbar.setSubtitle("Plant Phase Management");

        showFloatingActionButton();

        currentListView = ListDisplay.Phases;

        setFloatingButtonTextAndAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//TODO implement when new activity is in place.
            }
        });

        final ArrayList<String> plantStates = new ArrayList<>();
        plantStates.addAll(tracker.getPlantTrackerSettings().getStateAutoComplete());

        PlantStateTileArrayAdapter adapter = new PlantStateTileArrayAdapter(getBaseContext(),
                R.layout.tile_plant_state_list, plantStates);

        setEmptyViewCaption("No Plant Phases Found");

        plantListView.setAdapter(adapter);

        plantListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantTrackerUi.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this custom event?");
                builder.setIcon(R.drawable.ic_growing_plant);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tracker.removePlantState(plantStates.get(position));

                        fillViewWithPlantPhases();
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

        plantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO display custom event information view and
                // TODO a way to add more
            }
        });
    }

    private void fillIndividualPlantView() {

        hideFloatingActionButton();

        toolbar.setTitle(currentPlant.getPlantName());

        if (currentPlant.getThumbnail() != null)    {
            String thumbnail = currentPlant.getThumbnail();
            if (thumbnail != null)  {
                mPlantImage.setImageURI(Uri.fromFile(new File(currentPlant.getThumbnail())));
                mPlantImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchImageSeriesViewer(currentPlant.getAllImagesForPlant());
                    }
                });
            }
        }
        else    {
            mPlantImage.setOnClickListener(null);
            mPlantImage.setImageResource(R.drawable.ic_growing_plant);
        }

        daysSinceGrowStartTextView.setText(String.valueOf(Utility.calcDaysFromTime(
                currentPlant.getPlantStartDate(), Calendar.getInstance())));
        weeksSinceGrowStartTextView.setText(String.valueOf(Utility.calcWeeksFromTime(
                currentPlant.getPlantStartDate(), Calendar.getInstance())));

        if (currentPlant.getPlantData().currentStateStartDate == null)  {
            daysSinceStateStartTextView.setText("--");
            weeksSinceStateStartTextView.setText("--");
        }
        else {
            daysSinceStateStartTextView.setText(String.valueOf(Utility.calcDaysFromTime(
                    currentPlant.getPlantData().currentStateStartDate, Calendar.getInstance())));

            weeksSinceStateStartTextView.setText(String.valueOf(Utility.calcWeeksFromTime(
                    currentPlant.getPlantData().currentStateStartDate, Calendar.getInstance())));
        }

        fromSeedTextView.setText((currentPlant.isFromSeed() ? R.string.seed : R.string.clone));

        ArrayList<String> eventOptions = new ArrayList<>();
        eventOptions.add(ADD_NEW_RECORD);
        eventOptions.addAll(tracker.getGenericRecordTypes());
        eventOptions.add(CREATE_NEW_GENERIC_RECORD_OPTION);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, eventOptions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        addEventSpinner.setAdapter(adapter);
        addEventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = adapter.getItem(position);

                switch (selectedItem) {
                    case ADD_NEW_RECORD:
                        break;

                    case CREATE_NEW_GENERIC_RECORD_OPTION:
                        Intent intent = new Intent(PlantTrackerUi.this,
                                CreateRecordType.class);
                        intent.putExtra(AndroidConstants.INTENTKEY_GENERIC_RECORD,
                                new GenericRecord(""));

                        startActivityForResult(intent,
                                AndroidConstants.ACTIVITY_CREATE_GENERIC_RECORD_TEMPLATE);
                        break;

                    default:
                        launchCollectPlantDataIntent(tracker.getGenericRecordTemplate(selectedItem),
                                true);
                        break;
                }

                addEventSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String stateName = currentPlant.getCurrentStateName();
        if (stateName != null && !stateName.equals("")) {
            stateNameTextView.setText(currentPlant.getCurrentStateName());
        } else {
            stateNameTextView.setText(R.string.set_phase);
        }

        final Plant parentPlant = tracker.getPlantById(currentPlant.getParentPlantId());
        TextView parentPlantTextView = (TextView) findViewById(R.id.parentPlantIdTextView);
        parentPlantTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentPlantViewStack.push(currentPlant);
                currentPlant = parentPlant;
                fillIndividualPlantView();
            }
        });

        if (!currentPlant.isFromSeed() && currentPlant.getParentPlantId() > 0) {
            if (parentPlant != null) {
                parentPlantTextView.setText(parentPlant.getPlantName());
            } else {
                // couldn't find the parent plant
                parentPlantTextView.setText(R.string.record_not_found);
            }

            parentPlantTableRow.setVisibility(View.VISIBLE);
        } else {
            parentPlantTableRow.setVisibility(View.GONE);
        }

        PlantRecordableTileArrayAdapter plantRecordableAdapter =
                new PlantRecordableTileArrayAdapter(getBaseContext(),
                        R.layout.tile_plant_recordable, currentPlant.getAllGenericRecords(),
                        tracker.getAllRecordTemplates(), currentPlant);

        recordableEventListView.setAdapter(plantRecordableAdapter);
        recordableEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                presentRecordableEventSummaryDialog(i);
            }
        });

        recordableEventListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantTrackerUi.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to delete this event?");
                builder.setIcon(R.drawable.ic_growing_plant);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        currentPlant.removeGenericRecord(position);
                        fillIndividualPlantView();
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
    }

    private void launchCollectPlantDataIntent(GenericRecord record, boolean showNotes) {
        Intent intent = new Intent(PlantTrackerUi.this, CollectPlantData.class);
        intent.putExtra(AndroidConstants.INTENTKEY_AVAILABLE_GROUPS,
                getAvailableGroupsForPlant(currentPlant));
        intent.putExtra(AndroidConstants.INTENTKEY_GENERIC_RECORD, record);
        intent.putExtra(AndroidConstants.INTENTKEY_SHOW_NOTES, showNotes);

        startActivityForResult(intent, AndroidConstants.ACTIVITY_GENERIC_RECORD);
    }

    private TreeMap<String, Long> getAvailableGroupsForPlant(Plant p)   {
        TreeMap<String, Long> availableGroups = new TreeMap<>();
        for (Long key : p.getGroups()) {
            Group g = tracker.getGroup(key);
            availableGroups.put(g.getGroupName(), g.getGroupId());
        }

        return availableGroups;
    }

    private void bindAttachImagesControls(Dialog dialog) {
        Button openCameraButton = (Button) dialog.findViewById(R.id.openCameraButton);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FIXME open camera activity
            }
        });

        Button attachImagesButton = (Button) dialog.findViewById(R.id.attachImagesButton);
        attachImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FIXME open picture browser for selecting existing images
            }
        });

    }

    private void setFloatingButtonTextAndAction(View.OnClickListener listener) {
        FloatingActionButton floatingButton = (FloatingActionButton) findViewById(R.id.floatingButton);
        floatingButton.setOnClickListener(listener);
    }

    private void setEmptyViewCaption(String caption) {

        View emptyPlantListView = findViewById(R.id.emptyPlantListView);
        TextView itemNotFoundCaptionText = (TextView) emptyPlantListView.findViewById(R.id.itemNotFoundCaptionText);

        if (itemNotFoundCaptionText != null) {
            itemNotFoundCaptionText.setText(caption);
        }

        emptyPlantListView.invalidate();
    }

    /*
     *** Primany UI View Event handling ***
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (switcher.getCurrentView() == individualPlantView) {
            if (parentPlantViewStack.size() > 0) {
                currentPlant = parentPlantViewStack.pop();
                fillIndividualPlantView();
            } else {
                switcherToPrevious();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.plant_tracker_ui, menu);
        individualPlantMenu = menu;
        individualPlantMenu.setGroupVisible(0, false);
        individualPlantMenu.setGroupVisible(1, false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_rename:
                presentRenameDialog();
                break;

            case R.id.action_delete_plant_really:
                tracker.deletePlant(currentPlant);
                switcherToPrevious();
                break;

            case R.id.action_archive_plant:
                currentPlant.archivePlant();
                break;

            case R.id.action_unarchive_plant:
                currentPlant.unarchivePlant();
                break;

            case R.id.action_clone_plant:
                presentAddPlantDialog(currentPlant.getPlantId());
                break;

            case R.id.add_group:
                presentAddGroupDialog();
                break;

//            case R.id.action_set_reminder:
//                setReminder();
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Drawer nav
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_all_plants:
                plantDisplay = PlantDisplay.All;
                if (switcher.getCurrentView() != allPlantsView) {
                    switcherToPrevious();
                } else {
                    fillViewWithPlants();
                }
                break;

            case R.id.nav_active_plants:
                plantDisplay = PlantDisplay.Active;
                if (switcher.getCurrentView() != allPlantsView) {
                    switcherToPrevious();
                } else {
                    fillViewWithPlants();
                }
                break;

            case R.id.nav_archived_plants:
                plantDisplay = PlantDisplay.Archived;
                if (switcher.getCurrentView() != allPlantsView) {
                    switcherToPrevious();
                } else {
                    fillViewWithPlants();
                }
                break;

            case R.id.nav_manage_groups:
                if (switcher.getCurrentView() != allPlantsView) {
                    switcherToPrevious();
                    fillViewWithGroups();
                } else {
                    fillViewWithGroups();
                }
                break;

            case R.id.nav_manage_events:
                Intent manageRecordTemplates = new Intent(PlantTrackerUi.this,
                        ManageRecordTemplates.class);

                //TODO pass data
                manageRecordTemplates.putExtra("tracker", tracker);

                startActivityForResult(manageRecordTemplates,
                        AndroidConstants.ACTIVITY_MANAGE_RECORD_TEMPLATES);
                break;

//            case R.id.nav_manage_states:
//                if (switcher.getCurrentView() != allPlantsView) {
//                    switcherToPrevious();
//                    fillViewWithPlantPhases();
//                } else {
//                    fillViewWithPlantPhases();
//                }
//                break;

            case R.id.nav_delete:
                presentDeleteAllPlantsDialog();
                break;

            case R.id.nav_add_plant:
                presentAddPlantDialog(0);
                break;

            case R.id.nav_about_plant_tracker:
                presentAboutDialog();
                break;

            default:
                if (switcher.getCurrentView() == individualPlantView) {
                    switcherToPrevious();
                }

                groupIdViewFilter = menuItemToGroupIdMapping.get(item.getItemId());
                plantDisplay = PlantDisplay.Group;
                fillViewWithPlants();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Individual plant menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentPlant != null) {
            MenuItem archivedMenuItem = individualPlantMenu.findItem(R.id.action_archive_plant);
            MenuItem activeMenuItem = individualPlantMenu.findItem(R.id.action_unarchive_plant);

            if (currentPlant.isArchived()) {
                archivedMenuItem.setVisible(false);
                activeMenuItem.setVisible(true);
            } else {
                archivedMenuItem.setVisible(true);
                activeMenuItem.setVisible(false);
            }

            // prepare add to group submenu
            if (addToGroup == null) {
                addToGroup = (SubMenu) individualPlantMenu.findItem(R.id.action_groups).getSubMenu()
                        .addSubMenu(9, 1, 1, "Add to ...");
            }

            addToGroup.clear();

            ArrayList<Group> nonMembershipGroups = tracker.getGroupsPlantIsNotMemberOf(
                    currentPlant.getPlantId());

            for (Group g : nonMembershipGroups) {
                final Group currentGroup = g;
                MenuItem m = addToGroup.add(g.getGroupName());
                m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final long groupId = currentGroup.getGroupId();
                        tracker.addMemberToGroup(currentPlant.getPlantId(), groupId);
                        return true;
                    }
                });
            }

            if (nonMembershipGroups.size() == 0) {
                individualPlantMenu.findItem(R.id.action_groups).getSubMenu()
                        .setGroupVisible(9, false);
            } else {
                individualPlantMenu.findItem(R.id.action_groups).getSubMenu()
                        .setGroupVisible(9, true);
            }

            // prepare remove from group submenu
            if (removeFromGroup == null) {
                removeFromGroup = (SubMenu) individualPlantMenu.findItem(R.id.action_groups)
                        .getSubMenu().addSubMenu(10, 2, 2,"Remove from ...");
            }

            removeFromGroup.clear();

            ArrayList<Group> membershipGroups = tracker.getGroupsPlantIsMemberOf(
                    currentPlant.getPlantId());

            for (Group mg : membershipGroups) {
                final Group currentGroup = mg;

                MenuItem m = removeFromGroup.add(mg.getGroupName());
                m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final long groupId = currentGroup.getGroupId();
                        tracker.removeMemberFromGroup(currentPlant.getPlantId(), groupId);
                        return true;
                    }
                });
            }

            if (membershipGroups.size() == 0) {
                individualPlantMenu.findItem(R.id.action_groups).getSubMenu()
                        .setGroupVisible(10, false);
            } else {
                individualPlantMenu.findItem(R.id.action_groups).getSubMenu()
                        .setGroupVisible(10, true);
            }

            // prepare rename groups menu
            MenuItem renameGroupMenuItem = individualPlantMenu.findItem(R.id.rename_group);
            SubMenu renameGroupSubMenu = renameGroupMenuItem.getSubMenu();

            renameGroupSubMenu.clear();

            for (Group g : tracker.getAllGroups()) {
                final long groupId = g.getGroupId();
                MenuItem renameMenuItem = renameGroupSubMenu.add(g.getGroupName());
                renameMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        presentRenameGroupDialog(groupId);
                        refreshDrawerGroups();
                        return true;
                    }
                });
            }

            // prepare delete groups menu
            MenuItem deleteGroupItem = individualPlantMenu.findItem(R.id.delete_group);
            final SubMenu deleteGroupSubMenu = deleteGroupItem.getSubMenu();

            deleteGroupSubMenu.clear();

            for (Group g : tracker.getAllGroups()) {
                final long groupId = g.getGroupId();
                MenuItem menuItem = deleteGroupSubMenu.add(g.getGroupName());
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        tracker.removeGroup(groupId);
                        refreshDrawerGroups();
                        return true;
                    }
                });
            }
        }

        return true;
    }

    private void presentGenericEventDialog(int layoutId, IDialogHandler dialogHandler) {
        presentGenericEventDialog("", "", layoutId, dialogHandler);
    }

    private void presentGenericEventDialog(String code, String displayName, final int layoutId,
                                           IDialogHandler dialogHandler) {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);
        dialog.setContentView(R.layout.dialog_generic_event);
        TabHost tabs = (TabHost) dialog.findViewById(R.id.tabHost);
        tabs.setup();
        tabs.setCurrentTab(0);

        TabHost.TabSpec dialogTab = tabs.newTabSpec("Tab1");
        dialogTab.setIndicator("Info");
        dialogTab.setContent(layoutId);
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
                // Hide the keyboard if we're on any of the static tabs
                if (!tabId.equals("Tab1")) {
                    View view = dialog.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });

        bindAttachImagesControls(dialog);

        dialogHandler.bindDialog(dialog);

        dialog.show();
    }

    private Calendar getEventCalendar(final Dialog dialog) {
        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.
                eventDatePicker);

        final TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.eventTimePicker);


        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(),
                datePicker.getDayOfMonth(), timePicker.getHour(),
                timePicker.getMinute());

        return cal;
    }

    private String formatDate(Calendar c) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
        return sdf.format(c.getTime());
    }

    private void refreshDrawerGroups() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int count = 400;
        Menu drawerMenu = navigationView.getMenu();
        MenuItem sm = drawerMenu.findItem(R.id.viewsMenuItem);
        sm.getSubMenu().removeGroup(334);

        ArrayList<Group> allGroups = tracker.getNonEmptyGroups();
        allGroups.sort(new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                return o1.getGroupName().compareTo(o2.getGroupName());
            }
        });

        for (Group g : allGroups) {
            MenuItem groupMenuItem = sm.getSubMenu().add(334, count, count, "Group: " +
                    g.getGroupName());
            groupMenuItem.setIcon(R.drawable.ic_bundle_of_hay);
            menuItemToGroupIdMapping.put(groupMenuItem.getItemId(), g.getGroupId());
            count++;
        }
    }

    // switch to all plants
    private void switcherToPrevious() {
        parentPlantViewStack.clear();   // clear the view stack
        switcher.showPrevious();
        individualPlantMenu.setGroupVisible(0, false);
        individualPlantMenu.setGroupVisible(1, false);
        fillViewWithPlants();
    }

    // switch to individual plant
    private void switcherToNext() {
        switcher.showNext();
        individualPlantMenu.setGroupVisible(0, true);
        individualPlantMenu.setGroupVisible(1, true);
    }

    public void refreshListView() {
        if (switcher.getCurrentView() == individualPlantView) {
            fillIndividualPlantView();
        } else {
            switch (currentListView) {
                case Plants:
                    fillViewWithPlants();
                    break;

                case Groups:
                    fillViewWithGroups();
                    break;

                case CustomEvents: // custom events
                    //fillViewWithCustomEvents();
                    break;

                case Phases: // plant phase
                    fillViewWithPlantPhases();
                    break;
            }
        }
    }

    private IDialogHandler getAddPlantDialogHandler(final long parentPlantId) {
        return new IDialogHandler() {
            @Override
            public void bindDialog(final Dialog dialog) {
                LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.applyToGroupLayout);
                layout.setVisibility(View.GONE);

                Button okButton = (Button) dialog.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText plantNameEditText = (EditText) dialog.findViewById(
                                R.id.plantNameEditText);

                        String plantName = plantNameEditText.getText().toString();

                        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.originRadioGroup);
                        boolean isFromSeed;
                        int selectedId = rg.getCheckedRadioButtonId();
                        RadioButton selectedOrigin = (RadioButton) dialog.findViewById(selectedId);
                        RadioButton cloneRadioButton = (RadioButton) dialog.findViewById(
                                R.id.cloneRadioButton);

                        if (parentPlantId > 0) {
                            isFromSeed = false;
                        } else {
                            if (selectedOrigin == cloneRadioButton &&
                                    selectedOrigin.isChecked()) {
                                isFromSeed = false;
                            } else {
                                isFromSeed = true;
                            }
                        }

                        Calendar c = getEventCalendar(dialog);

                        if (parentPlantId > 0) {
                            tracker.addPlant(c, plantName, parentPlantId);
                        } else {
                            tracker.addPlant(c, plantName, isFromSeed);
                        }

                        dialog.dismiss();

                        fillViewWithPlants();
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
        };
    }

    private void presentDeleteAllPlantsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PlantTrackerUi.this);
        builder.setTitle(R.string.app_name);
        builder.setMessage("Are you sure you want to DELETE ALL PLANTS?");
        builder.setIcon(R.drawable.ic_growing_plant);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                tracker.deleteAllPlants();
                currentPlant = null;
                if (switcher.getCurrentView() == individualPlantView) {
                    switcherToPrevious();
                } else {
                    fillViewWithPlants();
                }

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NoNoNoNoNo1!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void presentRecordableEventSummaryDialog(int eventIndex) {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);

        GenericRecord r = currentPlant.getAllGenericRecords().get(eventIndex);

        TextView eventTypeTextView;
        TextView dateTextView;

        //FIXME create event summary view dialog
    }

    private void presentRenameDialog() {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);
        dialog.setContentView(R.layout.dialog_rename);
        final EditText renameEditText = (EditText) dialog.findViewById(R.id.renameEditText);
        renameEditText.setText(currentPlant.getPlantName());

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentPlant.setPlantName(renameEditText.getText().toString());

                dialog.dismiss();

                fillIndividualPlantView();

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

    private void presentAddPlantDialog(final long parentPlantId) {
        presentGenericEventDialog(R.id.dialogNewPlantLayout,
                getAddPlantDialogHandler(parentPlantId));
    }

    private void presentAboutDialog() {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);
        dialog.setContentView(R.layout.dialog_about);

        dialog.show();
    }

    private void presentAddGroupDialog() {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);
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
                tracker.savePlant(currentPlant);
                refreshDrawerGroups();
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

    private void presentRenameGroupDialog(long groupId) {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);
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
                refreshDrawerGroups();
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

    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode) {

            case AndroidConstants.ACTIVITY_GENERIC_RECORD:
                if (resultCode == Activity.RESULT_OK) {
                    GenericRecord record = (GenericRecord) returnedIntent.getSerializableExtra(
                            AndroidConstants.INTENTKEY_GENERIC_RECORD);

                    boolean applyToGroup = (boolean) returnedIntent.getBooleanExtra(
                            AndroidConstants.INTENTKEY_APPLY_TO_GROUP, false);

                    long selectedGroup = (long) returnedIntent.getLongExtra(
                            AndroidConstants.INTENTKEY_SELECTED_GROUP, 0);

                    record.images = (ArrayList<String>)returnedIntent.
                            getSerializableExtra(AndroidConstants.INTENTKEY_SELECTED_FILES);

                    PlantAction action = new PlantAction(record);
                    if (applyToGroup && selectedGroup > 0) {
                        tracker.performEventForPlantsInGroup(selectedGroup, action);
                    } else {
                        action.runAction(currentPlant);
                    }

                    fillIndividualPlantView();
                }
                break;

            case AndroidConstants.ACTIVITY_CREATE_GENERIC_RECORD_TEMPLATE:
                if (resultCode == Activity.RESULT_OK)   {
                    GenericRecord record = (GenericRecord)returnedIntent.getSerializableExtra(
                            AndroidConstants.INTENTKEY_GENERIC_RECORD);

                    tracker.addGenericRecordTemplate(record);

                    fillIndividualPlantView();
                }
                break;

            case AndroidConstants.ACTIVITY_MANAGE_RECORD_TEMPLATES:
                if (resultCode == Activity.RESULT_OK)   {
                    PlantTracker passedTracker = (PlantTracker) returnedIntent.getSerializableExtra(
                            AndroidConstants.INTENTKEY_PLANT_TRACKER);
                    tracker.setPlantTrackerSettings(passedTracker.getPlantTrackerSettings());

                    refreshListView();
                }
                break;
        }
    }

    private void launchImageSeriesViewer(ArrayList<String> files)   {
        Intent intent = new Intent(PlantTrackerUi.this,
                ImageSeriesViewer.class);

        intent.putExtra(AndroidConstants.INTENTKEY_FILE_LIST, files);
        startActivityForResult(intent, 97);
    }

    private void setReminder()  {
//        Intent reminderIntent = new Intent(this, PTBroadcastServiceIntent.class);
//        PendingIntent pi = PendingIntent.getBroadcast(getBaseContext(), 1, reminderIntent, 0);
//
//        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
//
//        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
//        manager.set( AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 1000*5, pi );
//
//        Toast.makeText(getBaseContext(), "Created reminder for 60s from now...", Toast.LENGTH_LONG).show();
    }

    /* Begin IPlantTrackerListener */
    @Override
    public void plantUpdated() {
        refreshListView();
    }

    @Override
    public void groupsUpdated() {
        refreshDrawerGroups();
        refreshListView();
    }
}
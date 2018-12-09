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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.android.AndroidUtility;
import com.nonsense.planttracker.android.MultipartUtility;
import com.nonsense.planttracker.android.interf.IAction;
import com.nonsense.planttracker.android.interf.ICallback;
import com.nonsense.planttracker.android.interf.IImageCache;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Group;
import com.nonsense.planttracker.android.adapters.PlantRecordableTileArrayAdapter;
import com.nonsense.planttracker.android.adapters.PlantTileRecyclerViewAdapter;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.actions.PlantAction;
import com.nonsense.planttracker.tracker.impl.PlantTracker;
import com.nonsense.planttracker.tracker.impl.Utility;
import com.nonsense.planttracker.tracker.interf.IDialogHandler;
import com.nonsense.planttracker.tracker.interf.IPlantTrackerListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import static com.nonsense.planttracker.android.AndroidUtility.decodeSampledBitmapFromResource;
import static com.nonsense.planttracker.android.activities.GroupManagement.presentAddGroupDialog;

public class PlantTrackerUi extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IPlantTrackerListener {

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

    //private static String PT_FILE_EXTENSION = ".json";

    private static final String CREATE_NEW_GENERIC_RECORD_OPTION = "Create new record type...";
    private static final String ADD_NEW_RECORD = "-- Add new record --";

    private ViewSwitcher switcher;
    private LinearLayout allPlantsView;
    private LinearLayout individualPlantView;
    private Toolbar toolbar;

    // All plants view
    private RecyclerView plantListView;

    // Individual plant view
    private TextView daysSinceGrowStartTextView;
    private TextView weeksSinceGrowStartTextView;
    private TextView weeksSinceStateStartTextView;
    private TextView daysSinceStateStartTextView;
    private TextView stateNameTextView;
    private TextView fromSeedTextView;
    private RecyclerView recordableEventListView;
    private Spinner addEventSpinner;
    private Menu individualPlantMenu;
    private ImageView mPlantImage;

    // Data
    private TreeMap<Integer, Long> menuItemToGroupIdMapping = new TreeMap<>();
    private Stack<Plant> parentPlantViewStack;
    private ArrayList<Plant> currentDisplayArray;
    private PlantDisplay plantDisplay = PlantDisplay.Active;
    private PlantTracker tracker;
    private Plant currentPlant;
    private long groupIdViewFilter;
    private ArrayList<String> mUriPaths = new ArrayList<>();

    // cache: https://developer.android.com/topic/performance/graphics/cache-bitmap.html
    private LruCache<String, Bitmap> imageCache;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plant_tracker_ui);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Showing Active Plants");

        switcher = findViewById(R.id.viewSwitcher);
        allPlantsView = findViewById(R.id.allPlantsView);
        individualPlantView = findViewById(R.id.individualPlantView);

        // all plants view
        plantListView = findViewById(R.id.plantListView);
        final LinearLayoutManager llm = new LinearLayoutManager(this);
        plantListView.setLayoutManager(llm);


        //FIXME
        //plantListView.setEmptyView(findViewById(R.id.emptyPlantListView));

        bindIndividualPlantView();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerOpened(View drawerView) {
                drawerView.bringToFront();
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState != null) {
            currentPlant = (Plant)savedInstanceState.getSerializable("currentPlant");
            groupIdViewFilter = savedInstanceState.getLong("groupIdViewFilter");
            parentPlantViewStack = (Stack<Plant>)savedInstanceState.getSerializable(
                    "parentPlantViewStack");

//            currentDisplayArray = (ArrayList<Plant>) savedInstanceState.getSerializable(
//                    "currentDisplayArray");

            boolean individualPlantView = savedInstanceState.getBoolean("individualPlantView",
                    false);

            if (individualPlantView)    {
                switchToIndividualPlant();
            }
        }
        else    {
            parentPlantViewStack = new Stack<>();
        }

        createImageCache();

        if (tracker == null)   {
            tracker = new PlantTracker(getExternalFilesDir("").getPath());
        }

        tracker.setPlantTrackerListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        refreshDrawerGroups();

        updateMainActivityView();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putSerializable("currentPlant", currentPlant);
        bundle.putLong("groupIdViewFilter", groupIdViewFilter);

        bundle.putSerializable("parentPlantViewStack", parentPlantViewStack);
        //bundle.putSerializable("currentDisplayArray", currentDisplayArray);
        bundle.putSerializable("tracker", tracker);

        if (switcher != null && switcher.getCurrentView() == individualPlantView) {
            bundle.putBoolean("individualPlantView", true);
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

                    if (record.images != null && record.images.size() > 0) {
                        for(String img : record.images) {
                            int index = img.lastIndexOf('/') + 1;
                            String fileName = img.substring(index>0?index:0);
                            tracker.getPlantTrackerSettings().addImageChangesSinceLastSync(fileName);
                        }
                    }

                    record.template = tracker.getGenericRecordTemplate(record.displayName);

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

                    updateMainActivityView();
                }
                break;

            case AndroidConstants.ACTIVITY_MANAGE_GROUPS:
                if (resultCode == RESULT_OK)    {
                    PlantTracker passedTracker = (PlantTracker) returnedIntent.getSerializableExtra(
                            AndroidConstants.INTENTKEY_PLANT_TRACKER);

                    rehashPlantTracker();

                    updateMainActivityView();
                }
                break;

            case AndroidConstants.ACTIVITY_IMPORT_CHOOSER:
                if (resultCode == Activity.RESULT_OK)   {
                    tracker.importFinished();
                    updateMainActivityView();
                    Toast.makeText(PlantTrackerUi.this, "Finished plant import.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

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
                switchToPlantList();
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

        // activate some stuff if we're looking at the individual plant view
        if (switcher.getCurrentView() == individualPlantView)   {
            individualPlantMenu.setGroupVisible(0, true);
            individualPlantMenu.setGroupVisible(1, true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_rename:
                AndroidUtility.presentRenameDialog(PlantTrackerUi.this, tracker, currentPlant,
                        new ICallback() {
                    @Override
                    public void callback() {
                        fillIndividualPlantView();
                    }
                });
                break;

            case R.id.action_delete_plant_really:
                tracker.deletePlant(currentPlant);
                switchToPlantList();
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

            case R.id.action_change_start_date:
                presentChangePlantStartDateDialog();
                break;

            case R.id.add_group:
                presentAddGroupDialog(PlantTrackerUi.this, tracker, new ICallback() {

                    @Override
                    public void callback() {
                        refreshDrawerGroups();
                    }
                });
                break;

            case R.id.action_copy:
                presentCopyDialog();
                break;

//            case R.id.action_set_reminder:
//                setReminder();
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_all_plants:
                plantDisplay = PlantDisplay.All;
                if (switcher.getCurrentView() != allPlantsView) {
                    switchToPlantList();
                } else {
                    fillViewWithPlants();
                }
                break;

            case R.id.nav_active_plants:
                plantDisplay = PlantDisplay.Active;
                if (switcher.getCurrentView() != allPlantsView) {
                    switchToPlantList();
                } else {
                    fillViewWithPlants();
                }
                break;

            case R.id.nav_archived_plants:
                plantDisplay = PlantDisplay.Archived;
                if (switcher.getCurrentView() != allPlantsView) {
                    switchToPlantList();
                } else {
                    fillViewWithPlants();
                }
                break;

            case R.id.nav_manage_groups:
                if (switcher.getCurrentView() != allPlantsView) {
                    switchToPlantList();
                    launchManageGroupsIntent();
                } else {
                    launchManageGroupsIntent();
                }
                break;

            case R.id.nav_sync_settings:
                presentSetSyncAddressDialog(this, tracker, new ICallback() {
                    @Override
                    public void callback() {
                    }
                });
                break;

            case R.id.nav_backup:
                String syncAddress = tracker.getPlantTrackerSettings().getSyncServerAddress();
                if (syncAddress != null) {
                    syncWithPrivateStash(syncAddress);
                }
                else {
                    Toast.makeText(PlantTrackerUi.this, "You must set a Sync Address in Sync Settings first!",
                            Toast.LENGTH_LONG).show();
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

            case R.id.nav_delete:
                AlertDialog alert = AndroidUtility.presentDeleteAllPlantsDialog(
                        PlantTrackerUi.this, new ICallback() {
                            @Override
                            public void callback() {
                                tracker.deleteAllPlants();
                                currentPlant = null;
                                if (switcher.getCurrentView() == individualPlantView) {
                                    switchToPlantList();
                                } else {
                                    fillViewWithPlants();
                                }
                            }
                        });

                break;

            case R.id.nav_add_plant:
                presentAddPlantDialog(0);
                break;

            case R.id.nav_about_plant_tracker:
                presentAboutDialog();
                break;

            default:
                if (switcher.getCurrentView() == individualPlantView) {
                    switchToPlantList();
                }

                groupIdViewFilter = menuItemToGroupIdMapping.get(item.getItemId());
                plantDisplay = PlantDisplay.Group;
                fillViewWithPlants();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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

            // Prepare sub menus
            buildAddToGroupSubMenu();
            buildRemoveFromGroupSubMenu();
        }

        return true;
    }

    private void buildAddToGroupSubMenu()   {
        if (currentPlant != null) {
            ArrayList<Group> nonMembershipGroups = tracker.getGroupsPlantIsNotMemberOf(
                    currentPlant.getPlantId());

            SubMenu groupsSubMenu = individualPlantMenu.findItem(R.id.action_groups).
                    getSubMenu();

            if (nonMembershipGroups.size() == 0 || groupsSubMenu.findItem(2) != null) {
                //groupsSubMenu.setGroupVisible(9, false);
                Log.d("GROUPS", "Setting add to group submenu Invisible");
            }
            else {
                SubMenu addToGroup = groupsSubMenu.addSubMenu(9, 2, 1,
                        "Add to ...");
                groupsSubMenu.setGroupVisible(9, true);
                Log.d("GROUPS", "Setting add to group submenu Visible");

                addToGroup.clear();

                for (Group g : nonMembershipGroups) {
                    final Group currentGroup = g;
                    MenuItem m = addToGroup.add(g.getGroupName());
                    m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            final long groupId = currentGroup.getGroupId();
                            tracker.addMemberToGroup(currentPlant.getPlantId(), groupId);
                            //addToGroup.removeItem(m.getItemId());
                            return true;
                        }
                    });
                }
            }
        }
    }

    private void buildRemoveFromGroupSubMenu()  {
        ArrayList<Group> membershipGroups = tracker.getGroupsPlantIsMemberOf(
                currentPlant.getPlantId());

        SubMenu groupsSubMenu = individualPlantMenu.findItem(R.id.action_groups).getSubMenu();

        if (membershipGroups.size() == 0 || groupsSubMenu.findItem(3) != null) {
            //groupsSubMenu.setGroupVisible(10, false);
            Log.d("GROUPS", "Setting remove from group submenu Invisible");
        } else {
            SubMenu removeFromGroup = groupsSubMenu.addSubMenu(10, 3, 2,
                    "Remove from ...");

            // prepare remove from group submenu
            groupsSubMenu.setGroupVisible(10, true);
            Log.d("GROUPS", "Setting remove from group submenu Visible");

            removeFromGroup.clear();

            for (Group mg : membershipGroups) {
                final Group currentGroup = mg;

                if (currentGroup != null)   {
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
            }
        }
    }


    /*
        Cache display element references
     */
    private void bindIndividualPlantView() {
        daysSinceGrowStartTextView = findViewById(R.id.daysSinceGrowStartTextView);
        weeksSinceGrowStartTextView = findViewById(R.id.weeksSinceGrowStartTextView);
        fromSeedTextView = findViewById(R.id.fromSeedTextView);
        recordableEventListView = findViewById(R.id.recordableEventListView);
        recordableEventListView.addItemDecoration(new DividerItemDecoration(
                PlantTrackerUi.this, DividerItemDecoration.VERTICAL));

        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recordableEventListView.setLayoutManager(llm);
        addEventSpinner = findViewById(R.id.addEventSpinner);

        stateNameTextView = findViewById(R.id.stateNameTextView);
        stateNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCollectPlantDataIntent(currentPlant.getPhaseChangeRecord(), true);
            }
        });

        weeksSinceStateStartTextView = findViewById(R.id.weeksSinceStateStartTextView);
        daysSinceStateStartTextView = findViewById(R.id.daysSinceStateStartTextView);
        mPlantImage = findViewById(R.id.lastCaptureImageView);
    }

    private void bindAttachImagesControls(Dialog dialog) {
        Button openCameraButton = dialog.findViewById(R.id.openCameraButton);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FIXME open camera activity
            }
        });

        Button attachImagesButton = dialog.findViewById(R.id.attachImagesButton);
        attachImagesButton.setEnabled(false);
//        attachImagesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //FIXME open picture browser for selecting existing images
//            }
//        });

    }


    /*
        Fill display elements
     */
    private void fillViewWithPlants() {
        toolbar.setTitle(R.string.app_name);
        toolbar.setElevation(100);

        setEmptyViewCaption("No Plants Found");

        showFloatingActionButton();

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

        PlantTileRecyclerViewAdapter adapter = new PlantTileRecyclerViewAdapter(
                PlantTrackerUi.this, currentDisplayArray,
                new IAction<Plant>() {

                    @Override
                    public void exec(Plant p) {
                        currentPlant = p;
                        toolbar.setSubtitle("");
                        fillIndividualPlantView();
                        switchToIndividualPlant();
                    }
                },
                new IAction<Plant>() {
                    @Override
                    public void exec(Plant p) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                PlantTrackerUi.this);
                        builder.setTitle(R.string.app_name);
                        builder.setMessage("Are you sure you want to delete this plant?");
                        builder.setIcon(R.drawable.ic_growing_plant);
                        builder.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        tracker.removePlant(p);
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
                    }
                }, cache);

        plantListView.setAdapter(adapter);
    }

    public void fillIndividualPlantView() {

        Log.d("IPV", "Beginning IPV Fill");

        hideFloatingActionButton();

        toolbar.setTitle(currentPlant.getPlantName());

        // display the options menu
        invalidateOptionsMenu();

        if (currentPlant.getThumbnail() != null)    {
            String thumbnail = currentPlant.getThumbnail();
            if (thumbnail != null)  {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = cache.getImage(currentPlant.getThumbnail());

                        Runnable updateUi = new Runnable() {
                            @Override
                            public void run() {
                                mPlantImage.setImageBitmap(bitmap);
                                mPlantImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        launchImageSeriesViewer(
                                                currentPlant.getAllImagesForPlant());
                                    }
                                });

                                Log.d("IPV", "End of loadThumb thread");
                            }
                        };

                        PlantTrackerUi.this.runOnUiThread(updateUi);
                    }
                };

                Thread loadImage = new Thread(runnable);
                loadImage.start();

            }
        }
        else    {
            mPlantImage.setOnClickListener(null);
            mPlantImage.setImageResource(R.drawable.ic_growing_plant);
            mPlantImage.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        Log.d("IPV", "Loading fields with text...");

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

        Log.d("IPV", "Listing record templates in drop down");

        ArrayList<String> eventOptions = new ArrayList<>();
        eventOptions.add(ADD_NEW_RECORD);
        eventOptions.addAll(tracker.getGenericRecordTypes());
        eventOptions.add(CREATE_NEW_GENERIC_RECORD_OPTION);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_spinner_lightfg, eventOptions);

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

        Log.d("IPV", "Finished record spinner filling");


        String stateName = currentPlant.getCurrentStateName();
        if (stateName != null && !stateName.equals("")) {
            stateNameTextView.setText(currentPlant.getCurrentStateName());
        } else {
            stateNameTextView.setText(R.string.set_phase);
        }

        final Plant parentPlant = tracker.getPlantById(currentPlant.getParentPlantId());
        TextView parentPlantTextView = findViewById(R.id.parentPlantIdTextView);
        parentPlantTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentPlantViewStack.push(currentPlant);
                currentPlant = parentPlant;
                fillIndividualPlantView();
            }
        });

        TextView parentPlantLabelTextView = findViewById(R.id.parentPlantLabelTextView);
        if (!currentPlant.isFromSeed() && currentPlant.getParentPlantId() > 0) {
            if (parentPlant != null) {
                parentPlantTextView.setText(parentPlant.getPlantName());
            } else {
                // couldn't find the parent plant
                parentPlantTextView.setText(R.string.record_not_found);
            }

            parentPlantTextView.setVisibility(View.VISIBLE);
            parentPlantLabelTextView.setVisibility(View.VISIBLE);
        } else {
            parentPlantTextView.setText("");
            parentPlantTextView.setVisibility(View.INVISIBLE);
            parentPlantLabelTextView.setVisibility(View.INVISIBLE);
        }

        Log.d("IPV", "Finished state info text stuff");

        Log.d("IPV", "Creating the PlantRecordableTileArrayAdapter...");
        PlantRecordableTileArrayAdapter plantRecordableAdapter =
                new PlantRecordableTileArrayAdapter(PlantTrackerUi.this,
                        currentPlant.getAllGenericRecords(), currentPlant);

        Log.d("IPV", "Creating the PlantRecordableTileArrayAdapter...finished");

        recordableEventListView.setAdapter(plantRecordableAdapter);
    }


    /*
        Refresh display
     */
    private void refreshDrawerGroups() {
        NavigationView navigationView = findViewById(R.id.nav_view);
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
            MenuItem groupMenuItem = sm.getSubMenu().add(334, count, count,
                    "Group: " + g.getGroupName());
            groupMenuItem.setIcon(R.drawable.ic_bundle_of_hay);
            menuItemToGroupIdMapping.put(groupMenuItem.getItemId(), g.getGroupId());
            count++;
        }
    }

    public void updateMainActivityView() {
        if (switcher.getCurrentView() == individualPlantView) {
            fillIndividualPlantView();
        }
        else {
            fillViewWithPlants();
        }
    }

    /*
        Other intents
     */
    private void launchManageGroupsIntent() {
        Intent groupsIntent = new Intent(this, GroupManagement.class);
        groupsIntent.putExtra(AndroidConstants.INTENTKEY_PLANT_TRACKER, tracker);

        startActivityForResult(groupsIntent, AndroidConstants.ACTIVITY_MANAGE_GROUPS);
    }

    private void launchCollectPlantDataIntent(GenericRecord record, boolean showNotes) {
        Intent intent = new Intent(PlantTrackerUi.this, CollectPlantData.class);
        intent.putExtra(AndroidConstants.INTENTKEY_AVAILABLE_GROUPS,
                tracker.getAvailableGroupsForPlant(currentPlant));
        intent.putExtra(AndroidConstants.INTENTKEY_GENERIC_RECORD, record);
        intent.putExtra(AndroidConstants.INTENTKEY_SHOW_NOTES, showNotes);

        startActivityForResult(intent, AndroidConstants.ACTIVITY_GENERIC_RECORD);
    }

    private void launchImageSeriesViewer(ArrayList<String> files)   {
        Intent intent = new Intent(PlantTrackerUi.this,
                ImageSeriesViewer.class);

        intent.putExtra(AndroidConstants.INTENTKEY_FILE_LIST, files);
        startActivityForResult(intent, 97);
    }



    /*
        Display element manipulation
     */
    private void showFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.floatingButton);
        fab.setVisibility(View.VISIBLE);
    }

    private void hideFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.floatingButton);
        fab.setVisibility(View.GONE);
    }

    private void setFloatingButtonTextAndAction(View.OnClickListener listener) {
        FloatingActionButton floatingButton = findViewById(R.id.floatingButton);
        floatingButton.setOnClickListener(listener);
    }

    private void setEmptyViewCaption(String caption) {

        View emptyPlantListView = findViewById(R.id.emptyPlantListView);
        TextView itemNotFoundCaptionText = emptyPlantListView.findViewById(
                R.id.itemNotFoundCaptionText);

        if (itemNotFoundCaptionText != null) {
            itemNotFoundCaptionText.setText(caption);
        }

        emptyPlantListView.invalidate();
    }

    private Calendar getEventCalendar(final Dialog dialog) {
        final DatePicker datePicker = dialog.findViewById(R.id.
                eventDatePicker);

        final TimePicker timePicker = dialog.findViewById(R.id.eventTimePicker);

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(),
                datePicker.getDayOfMonth(), timePicker.getHour(),
                timePicker.getMinute());

        return cal;
    }

    private void switchToPlantList() {
        parentPlantViewStack.clear();   // clear the view stack
        switcher.showPrevious();
        individualPlantMenu.setGroupVisible(0, false);
        individualPlantMenu.setGroupVisible(1, false);
        fillViewWithPlants();
    }

    private void switchToIndividualPlant() {
        switcher.showNext();
    }


    /*
        Dialogs
     */
    private IDialogHandler getAddPlantDialogHandler(final long parentPlantId) {
        return new IDialogHandler() {
            @Override
            public void bindDialog(final Dialog dialog) {
                LinearLayout layout = dialog.findViewById(R.id.applyToGroupLayout);
                layout.setVisibility(View.GONE);

                if (parentPlantId > 0)  {
                    RadioGroup rg = dialog.findViewById(R.id.originRadioGroup);
                    rg.setVisibility(View.GONE);

                    EditText plantNameEditText = dialog.findViewById(
                            R.id.plantNameEditText);

                    Plant parentPlant = tracker.getPlantById(parentPlantId);
                    plantNameEditText.setText(parentPlant.getPlantName() + ".1");
                }


                Button okButton = dialog.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText plantNameEditText = dialog.findViewById(
                                R.id.plantNameEditText);

                        String plantName = plantNameEditText.getText().toString();

                        RadioGroup rg = dialog.findViewById(R.id.originRadioGroup);
                        boolean isFromSeed;
                        int selectedId = rg.getCheckedRadioButtonId();
                        RadioButton selectedOrigin = dialog.findViewById(selectedId);
                        RadioButton cloneRadioButton = dialog.findViewById(R.id.cloneRadioButton);

                        if (parentPlantId > 0) {
                            isFromSeed = false;
                        } else {
                            if (selectedOrigin.isChecked() &&
                                    selectedOrigin == cloneRadioButton) {
                                isFromSeed = false;
                            } else {
                                isFromSeed = true;
                            }
                        }

                        Calendar c = getEventCalendar(dialog);

                        if (parentPlantId > 0) {
                            currentPlant = tracker.addPlant(c, plantName, parentPlantId);
                        } else {
                            currentPlant = tracker.addPlant(c, plantName, isFromSeed);
                        }

                        dialog.dismiss();

                        updateMainActivityView();
                    }
                });

                Button cancelButton = dialog.findViewById(R.id.cancelButton);
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

    private void presentGenericEventDialog(final int layoutId, IDialogHandler dialogHandler) {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);
        dialog.setContentView(R.layout.dialog_generic_event);
        TabHost tabs = dialog.findViewById(R.id.tabHost);
        tabs.setup();
        tabs.setCurrentTab(0);

        TabHost.TabSpec dialogTab = tabs.newTabSpec("Tab1");
        if (layoutId > 0) {
            dialogTab.setIndicator("Info");
            dialogTab.setContent(layoutId);
            tabs.addTab(dialogTab);
        }

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
//        tabs.addTab(attachImagesTab);

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

    private void presentCopyDialog()    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Copy plant...");

        View v = this.getLayoutInflater().inflate(R.layout.dialog_copy_plant, null);
        builder.setView(v);

        final EditText input = v.findViewById(R.id.copyCountEditText);
        input.setText("");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int plantCount = Integer.parseInt(input.getText().toString());

                tracker.copyPlant(currentPlant.getPlantId(), plantCount);
                dialog.dismiss();
                Toast.makeText(PlantTrackerUi.this,
                        "Created " + plantCount + " copies.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void presentRecordableEventSummaryDialog(int eventIndex) {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);

        GenericRecord r = currentPlant.getAllGenericRecords().get(eventIndex);

        TextView eventTypeTextView;
        TextView dateTextView;

        //FIXME create event summary view dialog
    }

    private void presentAddPlantDialog(final long parentPlantId) {
        presentGenericEventDialog(R.id.dialogNewPlantLayout,
                getAddPlantDialogHandler(parentPlantId));
    }

    private void presentChangePlantStartDateDialog()    {
        presentGenericEventDialog(0,
                new IDialogHandler() {
                    @Override
                    public void bindDialog(final Dialog dialog) {
                        Button okButton = dialog.findViewById(R.id.okButton);
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatePicker dp = dialog.findViewById(R.id.eventDatePicker);
                                TimePicker tp = dialog.findViewById(R.id.eventTimePicker);

                                Calendar c = Calendar.getInstance();
                                c.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), tp.getHour(),
                                        tp.getMinute());
                                currentPlant.changePlantStartDate(c);
                                dialog.hide();
                            }
                        });

                        Button cancelButton = dialog.findViewById(R.id.cancelButton);
                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.hide();
                            }
                        });
                    }
                });
    }

    private void presentAboutDialog() {
        final Dialog dialog = new Dialog(PlantTrackerUi.this);
        dialog.setContentView(R.layout.dialog_about);

        dialog.show();
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


    /*
        IPlantTrackerListener
     */
    @Override
    public void plantUpdated(Plant p) {
        updateMainActivityView();
    }

    @Override
    public void groupsUpdated() {
        refreshDrawerGroups();
        updateMainActivityView();
    }

    /*
        Image caching
     */
    private void createImageCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private IImageCache cache = new IImageCache() {
        @Override
        public Bitmap getImage(String path) {
            if (path == null)   {
                return null;
            }

            Bitmap bmap;
            if ((bmap=imageCache.get(path)) == null)   {
                bmap = decodeSampledBitmapFromResource(new File(path), 400,
                        300);

                imageCache.put(path, bmap);
            }

            return bmap;
        }
    };

    public void deleteRecordFromAllPlants(GenericRecord rec)    {
        tracker.deleteRecordFromAllPlants(rec);
    }

    private void rehashPlantTracker()   {
        tracker.dismantle();
        tracker = new PlantTracker(getExternalFilesDir("").getPath());
        tracker.setPlantTrackerListener(this);
    }

    private void syncWithPrivateStash(String host) {
        String plantFolderPath = getExternalFilesDir("").getPath() + "/plants/";
        String cameraFolderPath = getExternalFilesDir("").getPath() + "/camera/";
        ArrayList<File> masterImageUploadList = new ArrayList<>();
        ArrayList<File> masterJsonUploadList = new ArrayList<>();

        if (true) {//!tracker.getPlantTrackerSettings().hasSynced()) {
            // Send it all we've never synced with the server before.
            File folder = new File(plantFolderPath);
            File[] plants = folder.listFiles();
            for(int x=0; x < plants.length; x++) {
                masterJsonUploadList.add(plants[x]);
            }

            File cameraFolder = new File(cameraFolderPath);
            File[] images = cameraFolder.listFiles();
            for(int x=0; x < images.length; x++) {
                masterImageUploadList.add(images[x]);
            }
        }
        else {
            // Send only the change we've seen since the last sync
            TreeMap<String, ArrayList<String>> changes =
                    tracker.getPlantTrackerSettings().getChangesSinceLastSync();

            if (changes != null) {
                if (changes.containsKey("plants")) {
                    for(String s : changes.get("plants")) {
                        File f = new File(plantFolderPath + s + ".json");
                        masterJsonUploadList.add(f);
                    }
                }

                if (changes.containsKey("images")) {
                    for(String s : changes.get("images")) {
                        File f = new File(cameraFolderPath + s);
                        masterImageUploadList.add(f);
                    }
                }
            }
        }

        if (masterJsonUploadList.size() == 0 && masterImageUploadList.size() == 0) {
            Toast.makeText(PlantTrackerUi.this, "No plant data to sync.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    String requestURL = "http://" + host + "/plants/sync_plants";

                    for(File f : masterJsonUploadList) {
                        sendFile(requestURL,"json_files[]", f);
                    }

                    for(File f : masterImageUploadList) {
                        sendFile(requestURL,"plant_images[]", f);
                    }

                    // We have to reset the sync state when we've finished pushing changes successfully.
                    tracker.getPlantTrackerSettings().resetChangesSinceSync();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Runnable updateUi = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlantTrackerUi.this, "Finished plant data sync.",
                                Toast.LENGTH_SHORT).show();
                    }
                };

                PlantTrackerUi.this.runOnUiThread(updateUi);
            }
        };

        Thread syncPlantData = new Thread(runnable);
        syncPlantData.start();
    }

    private void sendFile(String url, String fieldName, File f) {
        try {
            MultipartUtility multipart = new MultipartUtility(url, "UTF-8");

            Gson g = new Gson();
            String groupsJson = g.toJson(tracker.getAllGroups());

            multipart.addFormField("groups_json", groupsJson);
            multipart.addFilePart(fieldName, f);

            List<String> response = multipart.finish();

            Log.v("rht", "SERVER REPLIED:");

            for (String line : response) {
                Log.v("rht", "Line : "+line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void presentSetSyncAddressDialog(Context c, PlantTracker tracker, ICallback caller) {
        final Dialog dialog = new Dialog(c);
        dialog.setContentView(R.layout.dialog_set_sync_address);

        final EditText groupNameEditText = dialog.findViewById(R.id.syncAddressEditText);

        if (tracker.getPlantTrackerSettings().getSyncServerAddress() != null) {
            groupNameEditText.setText(tracker.getPlantTrackerSettings().getSyncServerAddress());
        }

        Button okButton = dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.getPlantTrackerSettings().setSyncServerAddress(
                        groupNameEditText.getText().toString());

                caller.callback();
                dialog.dismiss();
            }
        });

        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}

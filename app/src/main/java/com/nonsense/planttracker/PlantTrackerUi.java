package com.nonsense.planttracker;

// TODO: Give attributionhttp://www.freepik.com/free-icon/plant-growing_743982.htm

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.nonsense.planttracker.tracker.impl.EventRecord;
import com.nonsense.planttracker.tracker.impl.ObservationRecord;
import com.nonsense.planttracker.tracker.impl.PlantRecordableTileArrayAdapter;
import com.nonsense.planttracker.tracker.impl.PlantTileArrayAdapter;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.PlantTracker;
import com.nonsense.planttracker.tracker.impl.Recordable;

import java.util.ArrayList;
import java.util.Calendar;

public class PlantTrackerUi extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewSwitcher switcher;
    private LinearLayout allPlantsView;
    private LinearLayout individualPlantView;

    // All plants view
    private ListView plantListView;

    // Individual plant view
    private TextView plantNameTextView;
    private TextView daysSinceGrowStartTextView;
    private TextView weeksSinceGrowStartTextView;
    private TextView daysSinceFlowerStartTextView;
    private TextView weeksSinceFlowerStartTextView;
    private TextView growStartTextView;
    private TextView fromSeedTextView;
    private ListView recordableEventListView;
    private CheckBox floweringCheckBox;
    private Button waterButton;
    private Button feedButton;
    private Button observationButton;
    private FloatingActionButton fap;

    private PlantTileArrayAdapter plantTileAdapter;
    private PlantRecordableTileArrayAdapter plantRecordableAdapter;

    // Data
    private PlantTracker tracker;
    private Plant currentPlant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_tracker_ui);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switcher = (ViewSwitcher)findViewById(R.id.viewSwitcher);
        allPlantsView = (LinearLayout)findViewById(R.id.allPlantsView);
        individualPlantView = (LinearLayout)findViewById(R.id.individualPlantView);

        // all plants view
        plantListView = (ListView)findViewById(R.id.plantListView);

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tracker = new PlantTracker(getFilesDir() + "/plants/");

        fillViewWithPlants();
    }

    private void bindIndividualPlantView()  {
        plantNameTextView = (TextView)findViewById(R.id.plantNameTextView);
        daysSinceGrowStartTextView = (TextView)findViewById(R.id.daysSinceGrowStartTextView);
        daysSinceFlowerStartTextView = (TextView)findViewById(R.id.daysSinceFlowerStartTextView);
        weeksSinceGrowStartTextView = (TextView)findViewById(R.id.weeksSinceGrowStartTextView);
        weeksSinceFlowerStartTextView = (TextView)findViewById(R.id.weeksSinceFlowerStartTextView);
        growStartTextView = (TextView)findViewById(R.id.growStartTextView);
        fromSeedTextView = (TextView)findViewById(R.id.fromSeedTextView);
        recordableEventListView = (ListView)findViewById(R.id.recordableEventListView);

        waterButton = (Button)findViewById(R.id.waterButton);
        waterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(PlantTrackerUi.this);
                dialog.setContentView(R.layout.dialog_water_event);
                dialog.setTitle("Record watering...");

                Button okButton = (Button)dialog.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText pHEditText = (EditText)dialog.findViewById(R.id.pHEditText);
                        String phInput = pHEditText.getText().toString();
                        double d = Double.parseDouble(phInput);
                        currentPlant.waterPlant(d);

                        dialog.dismiss();

                        fillIndividualPlantView();

                    }
                });

                Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                try {
                    dialog.show();
                }
                catch(Exception e)  {
                    e.printStackTrace();
                }
            }
        });

        feedButton = (Button)findViewById(R.id.feedButton);
        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(PlantTrackerUi.this);
                dialog.setContentView(R.layout.dialog_feed_event);
                dialog.setTitle("Record feeding...");

                Button okButton = (Button)dialog.findViewById(R.id.feedOkButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText pHEditText = (EditText)dialog.findViewById(R.id.feedPhEditText);
                        String phInput = pHEditText.getText().toString();
                        double ph = Double.parseDouble(phInput);

                        EditText feedStrengthEditText = (EditText)dialog.findViewById(
                                R.id.feedStrengthEditText);
                        String feedStrengthInput = feedStrengthEditText.getText().toString();
                        double str = Double.parseDouble(feedStrengthInput);

                        currentPlant.feedPlant(str, ph);

                        dialog.dismiss();

                        fillIndividualPlantView();
                    }
                });

                Button cancelButton = (Button)dialog.findViewById(R.id.feedCancelButton);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                try {
                    dialog.show();
                }
                catch(Exception e)  {
                    e.printStackTrace();
                }
            }
        });

        observationButton = (Button)findViewById(R.id.observeButton);
        observationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(PlantTrackerUi.this);
                dialog.setContentView(R.layout.dialog_observation_event);
                dialog.setTitle("Record observation...");

                Button okButton = (Button)dialog.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText rhMinEditText = (EditText)dialog.findViewById(R.id.rhMinEditText);
                        EditText rhMaxEditText = (EditText)dialog.findViewById(R.id.rhMaxEditText);
                        EditText tempMinEditText = (EditText)dialog.findViewById(R.id.tempMinEditText);
                        EditText tempMaxEditText = (EditText)dialog.findViewById(R.id.tempMaxEditText);
                        EditText observationsEditText = (EditText)dialog.findViewById(
                                R.id.observationNotesEditText);

                        String rhMin = rhMinEditText.getText().toString();
                        String rhMax = rhMaxEditText.getText().toString();
                        String tempMin = tempMinEditText.getText().toString();
                        String tempMax = tempMaxEditText.getText().toString();
                        int minTemp = Integer.parseInt(tempMin);
                        int maxTemp = Integer.parseInt(tempMax);
                        int minRh = Integer.parseInt(rhMin);
                        int maxRh = Integer.parseInt(rhMax);
                        String observations = observationsEditText.getText().toString();

                        currentPlant.addObservation(maxRh, minRh, maxTemp, minTemp, observations);

                        dialog.dismiss();

                        fillIndividualPlantView();

                    }
                });

                Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                try {
                    dialog.show();
                }
                catch(Exception e)  {
                    e.printStackTrace();
                }

            }
        });

        floweringCheckBox = (CheckBox)findViewById(R.id.floweringCheckBox);
    }

    private void fillViewWithPlants()   {
        PlantTileArrayAdapter adapter = new PlantTileArrayAdapter(getBaseContext(),
                R.layout.plant_list_tile, tracker.getAllPlants());

        plantListView.setAdapter(adapter);
        plantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                currentPlant = tracker.getAllPlants().get(position);
                fillIndividualPlantView();
                switcher.showNext();
            }
        });
    }

    private void fillIndividualPlantView()  {
        plantNameTextView.setText(currentPlant.getPlantName());
        daysSinceGrowStartTextView.setText("" + currentPlant.getDaysFromStart());
        daysSinceFlowerStartTextView.setText("" + currentPlant.getDaysFromFlowerStart());
        weeksSinceGrowStartTextView.setText("" + currentPlant.getWeeksFromStart());
        weeksSinceFlowerStartTextView.setText("" + currentPlant.getDaysFromFlowerStart());
        growStartTextView.setText("" + currentPlant.getPlantStartDate().getTime());
        fromSeedTextView.setText("" + (currentPlant.isFromSeed()?"From Seed":"Clone"));
        //TODO if it is from a clone, link to another plant!

        floweringCheckBox.setOnCheckedChangeListener(null);
        floweringCheckBox.setChecked(currentPlant.getVegFlowerState() == Plant.VegFlower.Flower);
        floweringCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)  {
                    currentPlant.switchToFlower();
                    fillIndividualPlantView();
                }
                else    {
                    currentPlant.switchToVeg();
                    fillIndividualPlantView();

                }
            }
        });

        plantRecordableAdapter = new PlantRecordableTileArrayAdapter(
                getBaseContext(), R.layout.plant_recordable_tile,
                currentPlant.getAllRecordableEvents());

        recordableEventListView.setAdapter(plantRecordableAdapter);
        recordableEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Dialog dialog = new Dialog(PlantTrackerUi.this);

                Recordable r = currentPlant.getAllRecordableEvents().get(i);

                TextView eventTypeTextView;
                TextView dateTextView;

                if (r instanceof EventRecord)   {
                    EventRecord er = (EventRecord)r;
                    dialog.setContentView(R.layout.dialog_display_recordable_event);
                    eventTypeTextView = (TextView)dialog.findViewById(R.id.observEventTypeTextView);
                    dateTextView = (TextView)dialog.findViewById(R.id.dateTextView);

                    eventTypeTextView.setText(er.getEventString());
                    dateTextView.setText(r.getTimestamp().getTime().toString());
                    TextView phTextView = (TextView)dialog.findViewById(R.id.pHTextView);
                    TextView foodStrengthTextView = (TextView)dialog.findViewById(
                            R.id.foodStrengthTextView);

                    if (er.getEventType() == EventRecord.PlantEvent.Food ||
                            er.getEventType() == EventRecord.PlantEvent.Water)  {
                        phTextView.setText(""+er.getpH());
                        foodStrengthTextView.setText(""+er.getFoodStrength());
                        RelativeLayout phLayout = (RelativeLayout)dialog.findViewById(
                                R.id.waterLayout);
                        phLayout.setVisibility(View.VISIBLE);
                    }
                    else    {
                        RelativeLayout phLayout = (RelativeLayout)dialog.findViewById(
                                R.id.waterLayout);
                        phLayout.setVisibility(View.INVISIBLE);
                    }

                }
                else if(r instanceof ObservationRecord) {
                    ObservationRecord or = (ObservationRecord)r;
                    dialog.setContentView(R.layout.dialog_display_observation);
                    eventTypeTextView = (TextView)dialog.findViewById(R.id.observEventTypeTextView);
                    dateTextView = (TextView)dialog.findViewById(R.id.observDateTextView);
                    TextView tempTextView = (TextView)dialog.findViewById(R.id.tempTextView);
                    TextView rhTextView = (TextView)dialog.findViewById(R.id.rhTextView);
                    TextView notesTextView = (TextView)dialog.findViewById(R.id.notesTextView);

                    eventTypeTextView.setText("Observation");
                    dateTextView.setText(r.getTimestamp().getTime().toString());
                    tempTextView.setText(or.getTempLow() + "/" + or.getTempHigh());
                    rhTextView.setText(or.getRhLow() + "/" + or.getRhHigh());
                    notesTextView.setText(or.getNotes());
                }

                try {
                    dialog.show();
                }
                catch(Exception e)  {
                    e.printStackTrace();
                }            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))   {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (switcher.getCurrentView() == individualPlantView)  {
            switcher.showPrevious();
        }
        else    {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.plant_tracker_ui, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all_plants) {
            if (switcher.getCurrentView() != allPlantsView) {
                switcher.showPrevious();
            }
        }
        else if (id == R.id.nav_delete) {
            ArrayList<Plant> plants = tracker.getAllPlants();

            for (Plant p : plants)   {
                tracker.deletePlantFileData(p);
            }

            plants.clear();

            fillViewWithPlants();
        }
        else if (id == R.id.nav_add_plant)  {
            final Dialog dialog = new Dialog(PlantTrackerUi.this);
            dialog.setContentView(R.layout.dialog_new_plant);
            dialog.setTitle("New plant...");

            Button okButton = (Button)dialog.findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText plantNameEditText = (EditText)dialog.findViewById(
                            R.id.plantNameEditText);

                    String plantName = plantNameEditText.getText().toString();

                    RadioGroup rg = (RadioGroup)dialog.findViewById(R.id.originRadioGroup);
                    boolean isFromSeed;
                    int selectedId = rg.getCheckedRadioButtonId();
                    RadioButton selectedOrigin =(RadioButton)dialog.findViewById(selectedId);
                    RadioButton cloneRadioButton = (RadioButton)dialog.findViewById(
                            R.id.cloneRadioButton);
                    if (selectedOrigin == cloneRadioButton &&
                            selectedOrigin.isChecked())    {
                        isFromSeed = false;
                    }
                    else    {
                        isFromSeed = true;
                    }

                    Calendar c = Calendar.getInstance();

                    DatePicker datePicker = (DatePicker)dialog.findViewById(R.id.datePicker);
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth();
                    int dayOfMonth = datePicker.getDayOfMonth();

                    c.set(year, month, dayOfMonth);

                    tracker.addPlant(c, plantName, isFromSeed);

                    dialog.dismiss();

                    fillViewWithPlants();
                }
            });

            Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            try {
                dialog.show();
            }
            catch(Exception e)  {
                e.printStackTrace();
            }
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

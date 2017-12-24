package com.nonsense.planttracker.tracker.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Derek Brooks on 9/10/2017.
 */
public class PlantData {
    public long plantId;
    public long parentPlantId;
    public String plantName;
    public Calendar startDate;
    public boolean isFromSeed;
    public boolean isArchived;
    public Calendar currentStateStartDate;
    public String currentStateName;
    public ArrayList<EventRecord> recordableEvents;
    public ArrayList<ObservationRecord> observationRecords;
    public ArrayList<GenericRecord> genericRecords;
    public ArrayList<Long> groupIds;

    public PlantData()  {
        recordableEvents = new ArrayList<>();
        genericRecords = new ArrayList<>();
        groupIds = new ArrayList<>();
    }
}

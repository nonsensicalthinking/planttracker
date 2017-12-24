package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

@SuppressWarnings("Since15")
public class Plant {

    private static final long serialVersionUID = 2153335460648792L;

    private PlantData plantData;

    // TODO store grouping auto-complete as part of settings

    private transient ArrayList<IPlantUpdateListener> updateListeners;

    public Plant()  {
        updateListeners = new ArrayList<>();
    }

    public Plant(Calendar growStartDate, String plantName, boolean isFromSeed) {
        updateListeners = new ArrayList<>();

        plantData = new PlantData();
        plantData.plantName = plantName;
        plantData.plantId = System.currentTimeMillis();
        plantData.startDate = growStartDate;
        plantData.recordableEvents = new ArrayList<>();
        plantData.observationRecords = new ArrayList<>();
        plantData.isFromSeed = isFromSeed;
        plantData.groupIds = new ArrayList<>();
    }

    public void addUpdateListener(IPlantUpdateListener pul) {
        if (updateListeners == null)    {
            updateListeners = new ArrayList<>();
        }

        updateListeners.add(pul);
    }

    public String getPlantName()    {
        return plantData.plantName;
    }

    public void setPlantName(String name) {
        plantData.plantName = name;

        notifyUpdateListeners();
    }

    public void setParentPlantId(long id) {
        plantData.parentPlantId = id;
    }

    public long getParentPlantId()  {
        return plantData.parentPlantId;
    }

    public long getPlantId()    {
        return plantData.plantId;
    }

    public boolean isFromSeed()
    {
        return plantData.isFromSeed;
    }

    public int getRecordableEventCount()    {
        return plantData.recordableEvents.size();
    }

    // FixMe we need to handle this better. Dirty hack to get json objects working
    // we don't need to sort like this, the problem could be resolved with a json exlcusion on the
    // sorted list
    public ArrayList<Recordable> getAllRecordableEvents()  {
        ArrayList<Recordable> rec = new ArrayList<>();

        for(EventRecord er : plantData.recordableEvents)    {
            rec.add((Recordable)er);
        }

        for(ObservationRecord or : plantData.observationRecords) {
            rec.add((Recordable)or);
        }

        return sortEvents(rec);
    }

    public void startGrow(Calendar c) {
        long currentDay = calcDaysFromTime(c);
        long currentWeek = calcWeeksFromTime(c);

        plantData.recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.GrowStart, c));

        sortEvents();

        notifyUpdateListeners();
    }

    public void feedPlant(double foodStrength, double pH, Calendar cal)  {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);

        plantData.recordableEvents.add(new EventRecord(currentDay, currentWeek, EventRecord.PlantEvent.Food,
                foodStrength, pH, cal));

        sortEvents();

        notifyUpdateListeners();
    }

    public GenericRecord getWaterPlantRecord()   {
        GenericRecord record = new GenericRecord("Water");
        record.setDataPoint("pH", new Double(6.5));

        return record;
    }

    public GenericRecord getFeedPlantRecord()   {
        GenericRecord record = new GenericRecord("Feeding");
        record.setDataPoint("pH", new Double(6.5));
        record.setDataPoint("Food Strength", new Double(0.5));

        return record;
    }

    public void finalizeRecord(GenericRecord record)    {
        plantData.genericRecords.add(record);

        sortEvents();
        notifyUpdateListeners();
    }

    public void addObservation(int rhHigh, int rhLow, int tempHigh, int tempLow, String notes,
                               Calendar cal)    {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);

        plantData.observationRecords.add(new ObservationRecord(currentDay, currentWeek, rhHigh, rhLow,
                tempHigh, tempLow, notes, cal));

        sortEvents();

        notifyUpdateListeners();
    }

    public long getDaysFromStart()   {
        return calcDaysFromTime(plantData.startDate);
    }

    public long getWeeksFromStart()   {
        return calcWeeksFromTime(plantData.startDate);
    }

    public long calcDaysFromTime(Calendar start)    {
        Calendar end = Calendar.getInstance();

        Date startDate = start.getTime();
        Date endDate = end.getTime();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24);

        return diffDays + 1;    // shift to 1 based
    }

    public long calcWeeksFromTime(Calendar start)   {
        Calendar end = Calendar.getInstance();

        Date startDate = start.getTime();
        Date endDate = end.getTime();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24 * 7);

        return diffDays + 1;    // shift to 1 based
    }

    public long getDaysFromStateStart() {
        if (plantData.currentStateStartDate != null)  {
            return calcDaysFromTime(plantData.currentStateStartDate);
        }

        return -1;
    }

    public long getWeeksFromStateStart()    {
        if (plantData.currentStateStartDate != null)  {
            return calcWeeksFromTime(plantData.currentStateStartDate);
        }

        return -1;
    }

    public void changePlantingDate(Calendar c)  {
        plantData.startDate = c;

        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();
        plantData.recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.ChangePlantingDate, c));

        sortEvents();

        notifyUpdateListeners();
    }

    public void addGeneralEvent(String generalEventName, String generalEventAbbrev,
                                String eventNotes, Calendar cal)  {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);
        plantData.recordableEvents.add(new EventRecord(currentDay, currentWeek, generalEventName,
                generalEventAbbrev, eventNotes, cal));

        sortEvents();

        notifyUpdateListeners();
    }

    public Calendar getPlantStartDate() {
        return plantData.startDate;
    }

    private void sortEvents()   {
        plantData.recordableEvents.sort(new Comparator<Recordable>() {
            @Override
            public int compare(Recordable o1, Recordable o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
    }

    private ArrayList<Recordable> sortEvents(ArrayList<Recordable> recordables) {
        recordables.sort(new Comparator<Recordable>() {
            @Override
            public int compare(Recordable o1, Recordable o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });

        return recordables;
    }

    private void notifyUpdateListeners()    {
        if (updateListeners != null)    {
            for(IPlantUpdateListener pul : updateListeners) {
                pul.plantUpdate(this);
            }
        }
    }

    public void archivePlant() {
        plantData.isArchived = true;
        notifyUpdateListeners();
    }

    public void unarchivePlant()   {
        plantData.isArchived = false;
        notifyUpdateListeners();
    }

    public boolean isArchived() {
        return plantData.isArchived;
    }

    public void regeneratePlantId() {
        plantData.plantId = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object p)  {
        return ((Plant)p).getPlantId() == getPlantId();
    }

    public void addGroup(long groupId)  {
        if (!plantData.groupIds.contains(groupId))    {
            plantData.groupIds.add(groupId);
        }
    }

    public void removeGroup(long groupId)   {
        plantData.groupIds.remove(groupId);
    }

    public ArrayList<Long> getGroups() {
        return plantData.groupIds;
    }

    public Recordable removeRecordableEvent(int pos)  {
        Recordable r = plantData.recordableEvents.remove(pos);
        updatePlant();
        return r;
    }

    public void changePlantState(Calendar cal, String stateName)  {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);

        plantData.recordableEvents.add(new EventRecord(currentDay, currentWeek, stateName, cal));

        updatePlant();
    }

    public String getCurrentStateName() {
        return plantData.currentStateName;
    }

    private void updatePlant()  {
        sortEvents();

        boolean stateFound = false;
        for(int x = plantData.recordableEvents.size()-1; x >= 0; x--) {
            Recordable event = plantData.recordableEvents.get(x);
            EventRecord rec = (EventRecord)event;

            if (rec.getEventType() == EventRecord.PlantEvent.State) {
                plantData.currentStateStartDate = rec.getTimestamp();
                plantData.currentStateName = rec.getEventText();
                stateFound = true;
                break;
            }

            if (!stateFound) {
                plantData.currentStateStartDate = null;
                plantData.currentStateName = null;
            }

        }

        notifyUpdateListeners();
    }

    public PlantData getPlantData() {
        return plantData;
    }

    public void setPlantData(PlantData pd)  {
        plantData = pd;
    }

}

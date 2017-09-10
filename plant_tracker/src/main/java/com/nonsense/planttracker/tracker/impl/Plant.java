package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

@SuppressWarnings("Since15")
public class Plant implements Serializable {

    private static final long serialVersionUID = 2153335460648792L;

    public enum VegFlower   {
        Veg,
        Flower
    }

    private long plantId;
    private long parentPlantId;
    private String plantName;
    private Calendar startDate;
    private ArrayList<Recordable> recordableEvents;
    private VegFlower vegFlowerState;
    private Calendar flowerStartDate;
    private boolean isFromSeed;
    private boolean isArchived;
    private ArrayList<Long> groupIds;
    private Calendar currentStateStartDate;
    private String currentStateName;

    // TODO store grouping auto-complete as part of settings

    private transient ArrayList<IPlantUpdateListener> updateListeners;

    public Plant(Calendar growStartDate, String plantName, boolean isFromSeed) {
        this.plantName = plantName;
        plantId = System.currentTimeMillis();
        startDate = growStartDate;
        recordableEvents = new ArrayList<>();
        vegFlowerState = VegFlower.Veg;
        updateListeners = new ArrayList<>();
        this.isFromSeed = isFromSeed;
        groupIds = new ArrayList<>();
    }

    public void addUpdateListener(IPlantUpdateListener pul) {
        if (updateListeners == null)    {
            updateListeners = new ArrayList<>();
        }

        updateListeners.add(pul);
    }

    public String getPlantName()    {
        return plantName;
    }

    public void setPlantName(String name) {
        plantName = name;

        notifyUpdateListeners();
    }

    public void setParentPlantId(long id) {
        parentPlantId = id;
    }

    public long getParentPlantId()  {
        return parentPlantId;
    }

    public long getPlantId()    {
        return plantId;
    }

    public boolean isFromSeed()
    {
        return isFromSeed;
    }

    public int getRecordableEventCount()    {
        return recordableEvents.size();
    }

    public ArrayList<Recordable> getAllRecordableEvents()  {
        return recordableEvents;
    }

    public void startGrow(Calendar c) {
        long currentDay = calcDaysFromTime(c);
        long currentWeek = calcWeeksFromTime(c);

        recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.GrowStart, c));

        sortEvents();

        notifyUpdateListeners();
    }

    public void feedPlant(double foodStrength, double pH, Calendar cal)  {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);

        recordableEvents.add(new EventRecord(currentDay, currentWeek, EventRecord.PlantEvent.Food,
                foodStrength, pH, cal));

        sortEvents();

        notifyUpdateListeners();
    }

    public void waterPlant(double pH, Calendar cal)   {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);

        recordableEvents.add(new EventRecord(currentDay, currentWeek, EventRecord.PlantEvent.Water,
                0.0, pH, cal));

        sortEvents();

        notifyUpdateListeners();
    }

    public void addObservation(int rhHigh, int rhLow, int tempHigh, int tempLow, String notes,
                               Calendar cal)    {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);

        recordableEvents.add(new ObservationRecord(currentDay, currentWeek, rhHigh, rhLow,
                tempHigh, tempLow, notes, cal));

        sortEvents();

        notifyUpdateListeners();
    }

    public long getDaysFromStart()   {
        return calcDaysFromTime(startDate);
    }

    public long getWeeksFromStart()   {
        return calcWeeksFromTime(startDate);
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
        if (currentStateStartDate != null)  {
            return calcDaysFromTime(currentStateStartDate);
        }

        return -1;
    }

    public long getWeeksFromStateStart()    {
        if (currentStateStartDate != null)  {
            return calcWeeksFromTime(currentStateStartDate);
        }

        return -1;
    }

    public void changePlantingDate(Calendar c)  {
        startDate = c;

        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();
        recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.ChangePlantingDate, c));

        sortEvents();

        notifyUpdateListeners();
    }

    public void addGeneralEvent(String generalEventName, String generalEventAbbrev,
                                String eventNotes, Calendar cal)  {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);
        recordableEvents.add(new EventRecord(currentDay, currentWeek, generalEventName,
                generalEventAbbrev, eventNotes, cal));

        sortEvents();

        notifyUpdateListeners();
    }

    public Calendar getPlantStartDate() {
        return startDate;
    }

    private void sortEvents()   {
        recordableEvents.sort(new Comparator<Recordable>() {
            @Override
            public int compare(Recordable o1, Recordable o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
    }

    private void notifyUpdateListeners()    {
        for(IPlantUpdateListener pul : updateListeners) {
            pul.plantUpdate(this);
        }
    }

    public void archivePlant() {
        isArchived = true;
        notifyUpdateListeners();
    }

    public void unarchivePlant()   {
        isArchived = false;
        notifyUpdateListeners();
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void regeneratePlantId() {
        plantId = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object p)  {
        return ((Plant)p).getPlantId() == getPlantId();
    }

    public void addGroup(long groupId)  {
        if (!groupIds.contains(groupId))    {
            groupIds.add(groupId);
        }
    }

    public void removeGroup(long groupId)   {
        groupIds.remove(groupId);
    }

    public ArrayList<Long> getGroups() {
        return groupIds;
    }

    public Recordable removeRecordableEvent(int pos)  {
        Recordable r = recordableEvents.remove(pos);
        updatePlant();
        return r;
    }

    public void changePlantState(Calendar cal, String stateName)  {
        long currentDay = calcDaysFromTime(cal);
        long currentWeek = calcWeeksFromTime(cal);

        recordableEvents.add(new EventRecord(currentDay, currentWeek, stateName, cal));

        updatePlant();
    }

    public String getCurrentStateName() {
        return currentStateName;
    }

    private void updatePlant()  {
        sortEvents();

        boolean stateFound = false;
        for(int x = recordableEvents.size()-1; x >= 0; x--) {
            Recordable event = recordableEvents.get(x);
            EventRecord rec = (EventRecord)event;

            if (rec.getEventType() == EventRecord.PlantEvent.State) {
                currentStateStartDate = rec.getTimestamp();
                currentStateName = rec.getEventText();
                stateFound = true;
                break;
            }

            if (!stateFound) {
                currentStateStartDate = null;
                currentStateName = null;
            }

        }

        notifyUpdateListeners();
    }

}

package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

public class Plant implements Serializable {
    public enum VegFlower   {
        Veg,
        Flower
    }

    private long plantId;
    private String plantName;
    private Calendar startDate;
    private ArrayList<Recordable> recordableEvents;
//    private ArrayList<EventRecord> eventRecords;
//    private ArrayList<ObservationRecord> observationRecords;
    private VegFlower vegFlowerState;
    private Calendar flowerStartDate;
    private boolean isFromSeed;

    private transient ArrayList<IPlantUpdateListener> updateListeners;

    public Plant(Calendar growStartDate, String plantName, boolean isFromSeed) {
        this.plantName = plantName;
        plantId = System.currentTimeMillis();
        startDate = growStartDate;
        recordableEvents = new ArrayList<>();
        vegFlowerState = VegFlower.Veg;
        updateListeners = new ArrayList<>();
        this.isFromSeed = isFromSeed;
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
        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();

        recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.GrowStart));

        notifyUpdateListeners();
    }

    public void endGrow(Calendar c)   {
        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();

        recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.GrowEnd));

        notifyUpdateListeners();
    }

    public void feedPlant(double foodStrength, double pH)  {
        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();

        recordableEvents.add(new EventRecord(currentDay, currentWeek, EventRecord.PlantEvent.Food,
                foodStrength, pH));

        notifyUpdateListeners();
    }

    public void waterPlant(double pH)   {
        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();

        recordableEvents.add(new EventRecord(currentDay, currentWeek, EventRecord.PlantEvent.Water,
                0.0, pH));

        notifyUpdateListeners();
    }

    public void switchToFlower()    {
        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();

        recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.FloweringState));

        setPlantState(VegFlower.Flower);

        flowerStartDate = Calendar.getInstance();

        notifyUpdateListeners();
    }

    public void switchToVeg()   {
        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();

        recordableEvents.add(new EventRecord(currentDay, currentWeek,
                EventRecord.PlantEvent.VegetationState));

        setPlantState(VegFlower.Veg);

        flowerStartDate = null;

        notifyUpdateListeners();
    }

    public VegFlower getVegFlowerState()    {
        return vegFlowerState;
    }

    public void addObservation(int rhHigh, int rhLow, int tempHigh, int tempLow, String notes)    {
        long currentDay = getDaysFromStart();
        long currentWeek = getWeeksFromStart();

        recordableEvents.add(new ObservationRecord(currentDay, currentWeek, rhHigh, rhLow,
                tempHigh, tempLow, notes));

        notifyUpdateListeners();
    }

    public long getDaysFromStart()   {
        return calcDaysFromTime(startDate);
    }

    public long getWeeksFromStart()   {
        return calcWeeksFromTime(startDate);
    }

    public long getDaysFromFlowerStart() {
        if (flowerStartDate == null)    {
            return 0;
        }

        return calcDaysFromTime(flowerStartDate);
    }

    public long getWeeksFromFlowerStart()   {
        if (flowerStartDate == null)    {
            return 0;
        }
        return calcWeeksFromTime(flowerStartDate);
    }

    public long calcDaysFromTime(Calendar start)    {
        Calendar end = Calendar.getInstance();

        Date startDate = start.getTime();
        Date endDate = end.getTime();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24);

        return diffDays;
    }

    public long calcWeeksFromTime(Calendar start)   {
        Calendar end = Calendar.getInstance();

        Date startDate = this.startDate.getTime();
        Date endDate = end.getTime();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24 * 7);

        return diffDays;
    }

    private void setPlantState(VegFlower state)    {
        vegFlowerState = state;
    }

    public Calendar getPlantStartDate() {
        return startDate;
    }

    private void notifyUpdateListeners()    {
        for(IPlantUpdateListener pul : updateListeners) {
            pul.plantUpdate(this);
        }
    }

}

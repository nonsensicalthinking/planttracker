package com.nonsense.planttracker.tracker.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

// FIXME rework this to be poly, there are too many points to remember to change like this
public class EventRecord extends Recordable {

    public enum PlantEvent  {
        GrowStart,
        GrowEnd,
        Water,
        Food,
        VegetationState,
        FloweringState,
        ChangePlantingDate,
        ChangeFloweringDate,
        GeneralEvent,
        State
    }

    public PlantEvent event;
    public double foodStrength;
    public double pH;
    public Calendar dateChangedTo;

    // general event
    public String generalEventName;
    public String generalEventAbbrev;
    public String eventNotes;

    public EventRecord()    {

    }

    // for other style events
    public EventRecord(long dayCount, long weekCount, PlantEvent e, Calendar cal)    {
        super(dayCount, weekCount, cal);
        event = e;
    }

    // for date change event types
    public EventRecord(long dayCount, long weekCount, PlantEvent e, Calendar cal,
                       Calendar timestamp) {
        super(dayCount, weekCount, cal);
        event = e;
        dateChangedTo = timestamp;
    }

    // for food/water events
    public EventRecord(long dayCount, long weekCount, PlantEvent e, double foodStrength, double pH,
                       Calendar cal){
        super(dayCount, weekCount, cal);
        this.event = e;
        this.foodStrength = foodStrength;
        this.pH = pH;
    }

    // for generic events
    public EventRecord(long dayCount, long weekCount, String generalEventName,
                       String generalEventAbbrev, String eventNotes, Calendar cal)   {
        super(dayCount, weekCount, cal);
        this.event = PlantEvent.GeneralEvent;
        this.generalEventName = generalEventName;
        this.generalEventAbbrev = generalEventAbbrev;
        this.eventNotes = eventNotes;
    }

    // for state change
    public EventRecord(long dayCount, long weekCount, String stateName, Calendar cal)   {
        super(dayCount, weekCount, cal);
        this.event = PlantEvent.State;
        this.eventNotes = stateName;
    }

    public double getFoodStrength() {
        return foodStrength;
    }

    public double getpH()   {
        return pH;
    }

    public PlantEvent getEventType()    {
        return event;
    }

    public String getEventString()   {
        switch(event)   {
            case GrowStart:
                return "Grow Start";
            case GrowEnd:
                return "Grow End";
            case Water:
                return "Water";
            case Food:
                return "Food";
            case VegetationState:
                return "Vegetation";
            case FloweringState:
                return "Flowering";
            case ChangePlantingDate:
                return "Changed planting date";
            case ChangeFloweringDate:
                return "Changed flowering date";
            case GeneralEvent:
                return generalEventName;
            case State:
                return "Changed state to: ";
            default:
                return "";
        }
    }

    public String getEventText()    {
        switch(event)   {
            case Water:
                return "pH: " + pH;
            case Food:
                return "ph: " + pH + " Food Str.: " + foodStrength;
            case ChangePlantingDate:
                return "to: " + getDateChangedToAsString();
            case ChangeFloweringDate:
                return "to: " + getDateChangedToAsString();
            case GeneralEvent:
                return eventNotes;
            case State:
                return eventNotes;
            default:
                return "";
        }
    }

    public String getDateChangedToAsString()    {
        return formatDate(dateChangedTo);
    }

    private String formatDate(Calendar c)   {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
        return sdf.format(c.getTime());
    }

    @Override
    public String Summary() {
        return formatDate(timestamp) + " " + getEventString() + " " + getEventText();
    }

    public String getEventTypeString()    {
        switch(event)   {
            case GrowStart:
                return "GS";
            case GrowEnd:
                return "GE";
            case Water:
                return "W";
            case Food:
                return "F";
            case VegetationState:
                return "2VS";
            case FloweringState:
                return "2FS";
            case ChangePlantingDate:
                return "CPD";
            case ChangeFloweringDate:
                return "CFL";
            case State:
                return "SC";
            case GeneralEvent:
                return generalEventAbbrev;
            default:
                return "";
        }
    }

}

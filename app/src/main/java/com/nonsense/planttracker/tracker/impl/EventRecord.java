package com.nonsense.planttracker.tracker.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

public class EventRecord extends Recordable {

    public enum PlantEvent  {
        GrowStart,
        GrowEnd,
        Water,
        Food,
        VegetationState,
        FloweringState,
        ChangePlantingDate,
        ChangeFloweringDate
    }

    private PlantEvent event;
    private double foodStrength;
    private double pH;
    private Calendar dateChangedTo;

    // for other style events
    public EventRecord(long dayCount, long weekCount, PlantEvent e)    {
        super(dayCount, weekCount);
        event = e;
    }

    public EventRecord(long dayCount, long weekCount, PlantEvent e, Calendar timestamp) {
        super(dayCount, weekCount);
        event = e;
        dateChangedTo = timestamp;
    }

    // for food/water events
    public EventRecord(long dayCount, long weekCount, PlantEvent e, double foodStrength, double pH){
        super(dayCount, weekCount);
        this.event = e;
        this.foodStrength = foodStrength;
        this.pH = pH;
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
            default:
                return "";
        }
    }

}

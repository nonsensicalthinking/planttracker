package com.nonsense.planttracker.tracker.impl;

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
        FloweringState
    }

    private PlantEvent event;
    private double foodStrength;
    private double pH;

    // for other style events
    public EventRecord(long dayCount, long weekCount, PlantEvent e)    {
        super(dayCount, weekCount);
        event = e;
    }

    public EventRecord(long dayCount, long weekCount, PlantEvent e, Calendar timestamp) {
        super(dayCount, weekCount);
        event = e;
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
            default:
                return "";
        }
    }

    @Override
    public String Summary() {
        return timestamp.getTime() + " " + getEventString() + " " + getEventText();
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
            default:
                return "";
        }
    }

}

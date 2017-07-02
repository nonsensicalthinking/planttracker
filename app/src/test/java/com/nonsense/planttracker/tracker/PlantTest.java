package com.nonsense.planttracker.tracker;

import com.nonsense.planttracker.tracker.impl.EventRecord;
import com.nonsense.planttracker.tracker.impl.ObservationRecord;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Calendar;

/**
 * Created by Derek Brooks on 7/1/2017.
 */
public class PlantTest {

    @Test
    public void feedPlant() {
        Plant p = new Plant(Calendar.getInstance(), "#1");

        p.feedPlant(0.5, 6.5);

        Assert.assertEquals(1, p.getEventRecordCount());
        EventRecord er = p.getAllEventRecords().next();
        Assert.assertEquals(0.5, er.getFoodStrength());
        Assert.assertEquals(6.5, er.getpH());
        Assert.assertEquals(EventRecord.PlantEvent.Food, er.getEventType());
    }

    @Test
    public void waterPlant() {
        Plant p = new Plant(Calendar.getInstance(), "#1");

        p.waterPlant(6.5);

        Assert.assertEquals(1, p.getEventRecordCount());
        EventRecord er = p.getAllEventRecords().next();
        Assert.assertEquals(6.5, er.getpH());
        Assert.assertEquals(EventRecord.PlantEvent.Water, er.getEventType());
    }

    @Test
    public void switchToFlower() {
        Plant p = new Plant(Calendar.getInstance(), "#1");

        p.switchToFlower();

        Assert.assertEquals(Plant.VegFlower.Flower, p.getVegFlowerState());
        Assert.assertEquals(1, p.getEventRecordCount());
        EventRecord er = p.getAllEventRecords().next();
        Assert.assertEquals(EventRecord.PlantEvent.FloweringState, er.getEventType());
    }

    @Test
    public void switchToVeg() {
        Plant p = new Plant(Calendar.getInstance(), "#1");

        p.switchToVeg();

        Assert.assertEquals(Plant.VegFlower.Veg, p.getVegFlowerState());
        Assert.assertEquals(1, p.getEventRecordCount());
        EventRecord er = p.getAllEventRecords().next();
        Assert.assertEquals(EventRecord.PlantEvent.VegetationState, er.getEventType());
    }

    @Test
    public void startGrow() {
        Plant p = new Plant(Calendar.getInstance(), "#1");
        Calendar c = Calendar.getInstance();

        p.startGrow(c);

        Assert.assertEquals(1, p.getEventRecordCount());
        EventRecord er = p.getAllEventRecords().next();
        Assert.assertEquals(c.get(Calendar.DAY_OF_YEAR),
                er.getTimestamp().get(Calendar.DAY_OF_YEAR));
        Assert.assertEquals(EventRecord.PlantEvent.GrowStart, er.getEventType());
    }

    @Test
    public void endGrow()   {
        Plant p = new Plant(Calendar.getInstance(), "#1");
        Calendar c = Calendar.getInstance();

        p.endGrow(c);

        Assert.assertEquals(1, p.getEventRecordCount());
        EventRecord er = p.getAllEventRecords().next();
        Assert.assertEquals(c.get(Calendar.DAY_OF_YEAR),
                er.getTimestamp().get(Calendar.DAY_OF_YEAR));
        Assert.assertEquals(EventRecord.PlantEvent.GrowEnd, er.getEventType());
    }

    @Test
    public void addObservation() {
        Plant p = new Plant(Calendar.getInstance(), "#1");

        p.addObservation(35, 45, 65, 75, "asdf");

        Assert.assertEquals(1, p.getObservationRecordCount());
        ObservationRecord or = p.getAllObservationRecords().next();
        Assert.assertEquals("asdf", or.getNotes());
        Assert.assertEquals(35, or.getRhLow());
        Assert.assertEquals(45, or.getRhHigh());
        Assert.assertEquals(65, or.getTempLow());
        Assert.assertEquals(75, or.getTempHigh());
    }

    private int updateEventCount = 0;
    @Test
    public void updateEvents()  {
        Plant p = new Plant(Calendar.getInstance(), "#1");
        p.addUpdateListener(new IPlantUpdateListener() {
            @Override
            public void plantUpdate(Plant p) {
                updateEventCount += 1;
            }
        });

        Calendar c = Calendar.getInstance();
        p.startGrow(c);
        p.endGrow(c);
        p.switchToVeg();
        p.switchToFlower();
        p.waterPlant(6.5);
        p.feedPlant(0.5, 6.5);
        p.addObservation(35, 45, 65, 75, "asdf");
        p.setPlantName("New Plant Name");

        Assert.assertEquals(8, updateEventCount);
    }

/*
    @Test
    public void getDaysFromStart() throws Exception {

    }

    @Test
    public void getWeeksFromStart() throws Exception {

    }
*/
}
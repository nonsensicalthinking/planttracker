package com.nonsense.planttracker.tracker.impl;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

public class ObservationRecord extends Recordable {
    private int rhLow;
    private  int rhHigh;
    private  int tempHigh;
    private  int tempLow;
    private  String notes;

    public ObservationRecord(long dayCount, long weekCount, int rhLow, int rhHigh, int tempLow,
                             int tempHigh, String notes)  {
        super(dayCount, weekCount);
        this.rhHigh = rhHigh;
        this.rhLow = rhLow;
        this.tempHigh = tempHigh;
        this.tempLow = tempLow;
        this.notes = notes;
    }

    @Override
    public String Summary() {
        return timestamp.getTime() + "[" + rhLow + "/" + rhHigh +
                "][" + tempLow + "/" + tempHigh + "] Obs. notes: " + notes;
    }

    public int getRhLow() {
        return rhLow;
    }

    public int getRhHigh() {
        return rhHigh;
    }

    public int getTempHigh() {
        return tempHigh;
    }

    public int getTempLow() {
        return tempLow;
    }

    public String getNotes() {
        return notes;
    }

    public String getEventTypeString()    {
        return "O";
    }
}

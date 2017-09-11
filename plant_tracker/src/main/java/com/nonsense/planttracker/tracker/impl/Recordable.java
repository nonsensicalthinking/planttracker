package com.nonsense.planttracker.tracker.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Derek Brooks on 6/30/2017.
 */



public abstract class Recordable extends Datable {
    public Calendar timestamp;
    public long dayCount;
    public long weekCount;

    public Recordable(long dayCount, long weekCount) {
        this(dayCount, weekCount, Calendar.getInstance());
    }

    public Recordable(long dayCount, long weekCount, Calendar c)    {
        this.timestamp = c;
        this.dayCount = dayCount;
        this.weekCount = weekCount;
    }

    public Calendar getTimestamp() {
        return (Calendar)timestamp.clone();
    }

    public long getDayCount()   {
        return dayCount;
    }

    public long getWeekCount()  {
        return weekCount;
    }

    public abstract String Summary();

    public abstract String getEventTypeString();

    // Calculate where this event is on a timeline
    public long weeksSinceDate(Calendar date)   {
        return calcWeeksFromTimeToTime(date, timestamp);
    }
}

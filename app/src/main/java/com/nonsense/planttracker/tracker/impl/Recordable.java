package com.nonsense.planttracker.tracker.impl;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

public abstract class Recordable implements Serializable {
    Calendar timestamp;
    private long dayCount;
    private long weekCount;

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
}

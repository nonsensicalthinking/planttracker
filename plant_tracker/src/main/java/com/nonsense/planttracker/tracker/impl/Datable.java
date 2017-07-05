package com.nonsense.planttracker.tracker.impl;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Derek Brooks on 7/5/2017.
 */

public class Datable  {
    public long calcDaysFromTimeToTime(Calendar start, Calendar end)    {
        Date startDate = start.getTime();
        Date endDate = end.getTime();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24);

        return diffDays + 1;    // shift to 1 based
    }

    public long calcWeeksFromTimeToTime(Calendar start, Calendar end)   {
        Date startDate = start.getTime();
        Date endDate = end.getTime();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24 * 7);

        return diffDays + 1;    // shift to 1 based
    }
}

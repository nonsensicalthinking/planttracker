package com.nonsense.planttracker.tracker.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 12/23/2017.
 */

public class GenericRecord implements Serializable {

    public String displayName;
    public Calendar time;
    public String notes;
    public TreeMap<String, Object> dataPoints;

    public GenericRecord(String displayName)  {
        this.displayName = displayName;

        this.dataPoints = new TreeMap<>();
        this.time = Calendar.getInstance();
    }

    public void setDataPoint(String key, Object value) {
        if (dataPoints.containsKey(key))    {
            dataPoints.remove(key);
        }

        dataPoints.put(key, value);
    }

    public Object getDataPoint(String key)    {
        return dataPoints.get(key);
    }
}

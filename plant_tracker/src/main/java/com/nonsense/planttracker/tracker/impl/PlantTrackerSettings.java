package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.interf.ISettingsChangedListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 7/3/2017.
 */


public class PlantTrackerSettings implements Serializable {
    private transient ISettingsChangedListener listener;
    private ArrayList<String> keys;
    private TreeMap<String, String> genericEventKeyValuePairs;

    public PlantTrackerSettings()   {
        keys = new ArrayList<String>();
        genericEventKeyValuePairs = new TreeMap<String,String>();
    }

    public ArrayList<String> getAutoCompleteKeys()  {
        return keys;
    }

    public void addAutoCompleteKeyValuePair(String key, String value)   {
        keys.add(key);
        genericEventKeyValuePairs.put(key, value);
        settingsChanged();
    }

    public String getAutoCompleteValueForKey(String key)  {
        return genericEventKeyValuePairs.get(key);
    }

    public void setListener(ISettingsChangedListener l) {
        listener = l;
    }

    private void settingsChanged()  {
        listener.settingsChanged();
    }
}


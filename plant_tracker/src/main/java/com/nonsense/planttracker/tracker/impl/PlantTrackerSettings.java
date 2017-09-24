package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.exceptions.GroupNotFoundException;
import com.nonsense.planttracker.tracker.interf.ISettingsChangedListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 7/3/2017.
 */


public class PlantTrackerSettings implements Serializable {
    private static final long serialVersionUID = 6952312342L;

    private transient ISettingsChangedListener listener;
    private ArrayList<String> keys;
    private TreeMap<String, String> genericEventKeyValuePairs;
    private ArrayList<Group> groups;
    private ArrayList<String> stateAutoComplete;


    public PlantTrackerSettings()   {
        keys = new ArrayList<String>();
        genericEventKeyValuePairs = new TreeMap<String,String>();
        groups = new ArrayList<Group>();
        stateAutoComplete = new ArrayList<String>();
    }

    public ArrayList<String> getAutoCompleteKeys()  {
        return keys;
    }

    public ArrayList<String> getAutoCompleteValues() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(genericEventKeyValuePairs.values());

        return list;
    }

    public boolean addAutoCompleteKeyValuePair(String key, String value)   {
        if (genericEventKeyValuePairs.containsKey(key) ||
                genericEventKeyValuePairs.containsValue(value)) {
            return false;
        }

        keys.add(key);
        genericEventKeyValuePairs.put(key, value);
        settingsChanged();

        return true;
    }

    public String getAutoCompleteValueForKey(String key)  {
        return genericEventKeyValuePairs.get(key);
    }

    public String getAutoCompleteKeyForValue(String value)  {
        for(Map.Entry<String, String> entry : genericEventKeyValuePairs.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }

        return null;
    }


    public void setListener(ISettingsChangedListener l) {
        listener = l;
    }

    private void settingsChanged()  {
        listener.settingsChanged();
    }

    public void addGroup(Group g)  {
        if (!groups.contains(g))    {
            groups.add(g);
        }

        settingsChanged();
    }

    public void removeGroup(long groupId)   {
        Group g = new Group(groupId, "");
        removeGroup(g);
    }

    public void removeGroup(Group g)   {
        groups.remove(g);
        settingsChanged();
    }

    public Group getGroup(long groupId) throws GroupNotFoundException {
        Group g = new Group(groupId, null);
        if (groups.contains(g)) {
            return groups.get(groups.indexOf(g));
        }

        throw new GroupNotFoundException("Unable to locate group with id: " + groupId);
    }

    public final ArrayList<Group> getGroups() {
        return groups;
    }

    public void addStateAutoComplete(String stateName)  {
        if (stateAutoComplete == null)  {
            stateAutoComplete = new ArrayList<String>();
        }

        if (!stateAutoComplete.contains(stateName))  {
            stateAutoComplete.add(stateName);
        }

        settingsChanged();
    }

    public ArrayList<String> getStateAutoComplete() {
        return stateAutoComplete;
    }
}


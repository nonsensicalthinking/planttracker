package com.nonsense.planttracker.tracker.impl;

import com.google.gson.annotations.Expose;
import com.nonsense.planttracker.tracker.exceptions.GroupNotFoundException;
import com.nonsense.planttracker.tracker.interf.ISettingsChangedListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 7/3/2017.
 */


public class PlantTrackerSettings implements Serializable {
    private static final long serialVersionUID = 6952312342L;

    @Expose(serialize = false, deserialize = false)
    private transient ISettingsChangedListener listener;

    private TreeMap<String, GenericRecord> genericRecordTemplates;
    private ArrayList<Group> groups;
    private ArrayList<String> stateAutoComplete;

    PlantTrackerSettings()   {
        genericRecordTemplates = new TreeMap<>();
        groups = new ArrayList<Group>();
        stateAutoComplete = new ArrayList<String>();
    }

    void setListener(ISettingsChangedListener l) {
        listener = l;
    }

    private void settingsChanged()  {
        // use of a listener is not required!
        if (listener != null)   {
            listener.settingsChanged();
        }
    }

    void addGroup(Group g)  {
        if (!groups.contains(g))    {
            groups.add(g);
        }

        settingsChanged();
    }

    void removeGroup(long groupId)   {
        Group g = new Group(groupId, "");
        removeGroup(g);
    }

    private void removeGroup(Group g)   {
        groups.remove(g);
        settingsChanged();
    }

    Group getGroup(long groupId) {
        Group g = new Group(groupId, null);
        if (groups.contains(g)) {
            return groups.get(groups.indexOf(g));
        }

        return null;
    }

    final ArrayList<Group> getGroups() {
        return groups;
    }

    public boolean addStateAutoComplete(String stateName)  {
        if (stateAutoComplete == null)  {
            stateAutoComplete = new ArrayList<String>();
        }

        if (!stateAutoComplete.contains(stateName))  {
            stateAutoComplete.add(stateName);
            settingsChanged();
            return true;
        }

        return false;
    }

    public ArrayList<String> getStateAutoComplete() {
        return stateAutoComplete;
    }

    void removeStateAutoComplete(String key) {
        stateAutoComplete.remove(key);
    }

    public void addGenericRecordTemplate(GenericRecord record)  {
        if (genericRecordTemplates.containsKey(record.displayName)) {
            genericRecordTemplates.remove(record.displayName);
        }

        genericRecordTemplates.put(record.displayName, record);

        settingsChanged();
    }

    public void removeGenericRecordTemplate(GenericRecord record) {
        if (genericRecordTemplates.containsKey(record.displayName)) {
            genericRecordTemplates.remove(record.displayName);
        }

        settingsChanged();
    }

    Set<String> getGenericRecordNames() {
        if (genericRecordTemplates == null) {
            genericRecordTemplates = new TreeMap<>();
        }

        return genericRecordTemplates.keySet();
    }

    GenericRecord getGenericRecordTemplate(String name)  {
        try {
            GenericRecord record = genericRecordTemplates.get(name);

            if (record != null) {
                return (GenericRecord)record.clone();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    void removeGenericRecordTemplate(String name)    {
        genericRecordTemplates.remove(name);
        settingsChanged();
    }

    final TreeMap<String, GenericRecord> getAllGenericRecordTemplates()    {
        return genericRecordTemplates;

    }


}

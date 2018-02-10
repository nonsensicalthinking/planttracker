package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 6/30/2017.
 */

@SuppressWarnings("Since15")
public class Plant implements Serializable  {

    private static final long serialVersionUID = 2153335460648792L;

    private PlantData plantData;

    // TODO store grouping auto-complete as part of settings

    private transient ArrayList<IPlantUpdateListener> updateListeners;

    public Plant()  {
        updateListeners = new ArrayList<>();
    }

    public Plant(Calendar growStartDate, String plantName, boolean isFromSeed) {
        updateListeners = new ArrayList<>();

        plantData = new PlantData();
        plantData.plantName = plantName;
        plantData.plantId = System.currentTimeMillis();
        plantData.startDate = growStartDate;
        plantData.isFromSeed = isFromSeed;
        plantData.groupIds = new ArrayList<>();
    }

    public void addUpdateListener(IPlantUpdateListener pul) {
        if (updateListeners == null)    {
            updateListeners = new ArrayList<>();
        }

        updateListeners.add(pul);
    }

    public String getPlantName()    {
        return plantData.plantName;
    }

    public void setPlantName(String name) {
        plantData.plantName = name;

        notifyUpdateListeners();
    }

    public void setParentPlantId(long id) {
        plantData.parentPlantId = id;
    }

    public long getParentPlantId()  {
        return plantData.parentPlantId;
    }

    public long getPlantId()    {
        return plantData.plantId;
    }

    public boolean isFromSeed()
    {
        return plantData.isFromSeed;
    }

    public ArrayList<GenericRecord> getAllGenericRecords()  {
        ArrayList<GenericRecord> rec = new ArrayList<>();

        for(GenericRecord er : plantData.genericRecords)    {
            rec.add((GenericRecord)er);
        }

        return sortEvents(rec);
    }

    public GenericRecord getWaterPlantRecord()   {
        GenericRecord record = new GenericRecord("Water");
        record.setDataPoint("pH", new Double(6.5));
        record.summaryTemplate = "pH of water {pH}";

        return record;
    }

    public GenericRecord getFeedPlantRecord()   {
        GenericRecord record = new GenericRecord("Feeding");
        record.setDataPoint("pH", new Double(6.5));
        record.setDataPoint("Food Strength", new Double(0.5));
        record.summaryTemplate = "pH of food {pH} with strength of {Food Strength}";

        return record;
    }

    public GenericRecord getPhaseChangeRecord() {
        GenericRecord record = new GenericRecord("Changing Phase");
        record.setDataPoint("Phase Name", new String());
        record.summaryTemplate = "Plant entered a new phase, {Phase Name}";

        return record;
    }

    public void finalizeRecord(GenericRecord record)    {
        plantData.genericRecords.add(record);

        sortEvents();
        updateSummaryInformation();
        notifyUpdateListeners();
    }

    // Days//Weeks//Counts

    public long getDaysFromStart()   {
        return getDaysFromStart(Calendar.getInstance());
    }

    public long getDaysFromStart(Calendar c)    {
        return Utility.calcDaysFromTime(plantData.startDate, c);
    }

    public long getWeeksFromStart()   {
        return getWeeksFromStart(plantData.startDate);
    }

    public long getWeeksFromStart(Calendar c)   {
        return Utility.calcWeeksFromTime(plantData.startDate, c);
    }

    public long getDaysFromStateStart() {
        return getDaysFromStateStart(plantData.currentStateStartDate);
    }

    public long getDaysFromStateStart(Calendar c)   {
        if (c != null)  {
            return Utility.calcDaysFromTime(plantData.currentStateStartDate, c);
        }

        return -1;
    }

    public long getWeeksFromStateStart()    {
        return getWeeksFromStateStart(plantData.currentStateStartDate);
    }

    public long getWeeksFromStateStart(Calendar c)    {
        if (c != null)  {
            return Utility.calcWeeksFromTime(plantData.currentStateStartDate, c);
        }

        return -1;
    }

    public Calendar getPlantStartDate() {
        return plantData.startDate;
    }

    public void changePlantStartDate(Calendar startDate)    {
        plantData.startDate = startDate;

        updateSummaryInformation();
        notifyUpdateListeners();
    }

    // plant object maintenance
    private void sortEvents()   {
        plantData.genericRecords.sort(new Comparator<GenericRecord>() {
            @Override
            public int compare(GenericRecord o1, GenericRecord o2) {
                return o1.time.compareTo(o2.time);
            }
        });
    }
	
    private ArrayList<GenericRecord> sortEvents(ArrayList<GenericRecord> records) {
        records.sort(new Comparator<GenericRecord>() {
            @Override
            public int compare(GenericRecord o1, GenericRecord o2) {
                return o1.time.compareTo(o2.time);
            }
        });

        return records;
    }

    public void removeGenericRecord(int recordPosition)    {
        plantData.genericRecords.remove(recordPosition);

        updateSummaryInformation();
        notifyUpdateListeners();
    }

    private void updateSummaryInformation() {
        Calendar nearestPhaseDate = null;
        int phaseCount = 0;

        sortEvents();

        for(GenericRecord record : plantData.genericRecords)    {
            // set plant phase
            if (record.dataPoints.containsKey("Phase Name")) {
                plantData.currentStateName = (String)record.dataPoints.get("Phase Name");
                plantData.currentStateStartDate = record.time;
                nearestPhaseDate = record.time;
                phaseCount++;
            }

            if (nearestPhaseDate != null)   {
                record.phaseCount = phaseCount;
                record.weeksSincePhase = Utility.calcWeeksFromTime(nearestPhaseDate, record.time);
            }

            record.weeksSinceStart = Utility.calcWeeksFromTime(getPlantStartDate(),
                    record.time);

            if (record.images != null)  {
                if (record.images.size() > 0)   {
                    plantData.thumbnail = record.images.get(record.images.size()-1);
                }
            }

            // TODO update other plant summary fields
        }
    }

    private void notifyUpdateListeners()    {
        if (updateListeners != null)    {
            for(IPlantUpdateListener pul : updateListeners) {
                pul.plantUpdate(this);
            }
        }
    }

    public ArrayList<Long> getUniqueRecordTemplatesUsed()  {
        ArrayList<Long> uniqueRecords = new ArrayList<>();

        for(GenericRecord gr : plantData.genericRecords)    {
            if (!uniqueRecords.contains(gr.id))   {
                uniqueRecords.add(gr.id);
            }
        }

        return uniqueRecords;
    }

    public void archivePlant() {
        plantData.isArchived = true;
        notifyUpdateListeners();
    }

    public void unarchivePlant()   {
        plantData.isArchived = false;
        notifyUpdateListeners();
    }

    public boolean isArchived() {
        return plantData.isArchived;
    }

    public void regeneratePlantId() {
        plantData.plantId = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object p)  {
        return ((Plant)p).getPlantId() == getPlantId();
    }

    public void addGroup(long groupId)  {
        if (!plantData.groupIds.contains(groupId))    {
            plantData.groupIds.add(groupId);
        }
    }

    public void removeGroup(long groupId)   {
        plantData.groupIds.remove(groupId);
    }

    public ArrayList<Long> getGroups() {
        return plantData.groupIds;
    }

    public String getCurrentStateName() {
        return plantData.currentStateName;
    }

    public PlantData getPlantData() {
        return plantData;
    }

    public void setPlantData(PlantData pd)  {
        plantData = pd;
    }

    public void plantLoadFinished() {
        updateSummaryInformation();
    }

    public String getThumbnail()    {
        return plantData.thumbnail;
    }

    public ArrayList<String> getAllImagesForPlant() {
        ArrayList<String> files = new ArrayList<>();

        for(GenericRecord rec : plantData.genericRecords)   {
            if (rec.images != null && rec.images.size() > 0)    {
                files.addAll(rec.images);
            }
        }

        return files;
    }
}

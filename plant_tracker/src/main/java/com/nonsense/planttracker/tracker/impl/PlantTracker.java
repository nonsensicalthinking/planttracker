package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.exceptions.GroupNotFoundException;
import com.nonsense.planttracker.tracker.exceptions.PlantNotFoundException;
import com.nonsense.planttracker.tracker.interf.IPlantEventDoer;
import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;
import com.nonsense.planttracker.tracker.interf.ISettingsChangedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Derek Brooks on 6/30/2017.
 */
public class PlantTracker implements IPlantUpdateListener, ISettingsChangedListener {
    private PlantTrackerSettings settings;
    private ArrayList<Plant> plants;
    private transient ArrayList<Plant> activePlants;
    private transient ArrayList<Plant> archivedPlants;
    private String plantFolderPath;

    private static final String FILE_EXTENSION = ".ser";
    private static final String SETTINGS_FOLDER = "/settings/";
    private static final String SETTINGS_FILE = "tracker_settings.ser";
    private static final String PLANTS_FOLDER = "/plants/";


    public PlantTracker()   {
        this("plants/");
    }

    public PlantTracker(String plantFolderPath)   {
        this.plantFolderPath = plantFolderPath;

        File settingsFile = new File(plantFolderPath + SETTINGS_FOLDER + SETTINGS_FILE);
        if (!settingsFile.exists())  {
            settings = new PlantTrackerSettings();
            settings.setListener(this);
            savePlantTrackerSettings();
       }
        else    {
            loadPlantTrackerSettings();
        }

        plants = new ArrayList<>();
        archivedPlants = new ArrayList<>();
        activePlants = new ArrayList<>();
        loadPlants();
    }

    public Iterator<Plant> getIteratorForAllPlants()    {
        return plants.iterator();
    }

    public ArrayList<Plant> getAllPlants()  {
        return plants;
    }

    public ArrayList<Plant> getActivePlants()   {
        activePlants.clear();
        for(Plant p : plants)   {
            if (!p.isArchived())    {
                activePlants.add(p);
            }
        }

        return activePlants;
    }

    public ArrayList<Plant> getArchivedPlants() {
        archivedPlants.clear();
        for(Plant p : plants)   {
            if (p.isArchived()) {
                archivedPlants.add(p);
            }
        }

        return archivedPlants;
    }

    public void addPlant(String plantName, boolean isFromSeed)  {
        addPlant(Calendar.getInstance(), plantName, isFromSeed);
    }

    public void addPlant(Calendar c, String plantName, boolean isFromSeed)    {
        Plant p = new Plant(c, plantName, isFromSeed);
        p.addUpdateListener(this);
        plants.add(p);
        plantUpdate(p);
    }

    public void addPlant(Calendar c, String plantName, long parentPlantId)  {
        Plant p = new Plant(c, plantName, false);
        p.addUpdateListener(this);
        p.setParentPlantId(parentPlantId);
        plants.add(p);
        plantUpdate(p);
    }

    public void removePlant(int plantIndex) {
        deletePlantFileData(plants.get(plantIndex));
        plants.remove(plantIndex);
    }

    public void saveAllPlants()    {
        File folder = new File(plantFolderPath);
        if (!folder.exists())   {
            folder.mkdir();
        }

        // save each plant to a file individually into the plants folder
        for(Plant p : plants)   {
            savePlant(p);
        }
    }

    public void savePlant(Plant p)  {
        File folder = new File(plantFolderPath + PLANTS_FOLDER);
        if (!folder.exists())   {
            folder.mkdir();
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(plantFolderPath + PLANTS_FOLDER +
                    p.getPlantId() + FILE_EXTENSION);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(p);
            oos.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlants()    {
        // search plants folder for plant files
        List<String> results = new ArrayList<String>();
        File[] files = new File(plantFolderPath + PLANTS_FOLDER).listFiles();
        if (files != null)  {
            for (File file : files) {
                try
                {
                    Plant p = loadPlant(file);
                    attachPlant(p);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void attachPlant(Plant p)   {
        if (p == null)  {
            return;
        }

        // we're loading a copy of a plant, make the id unique so we don't overwrite the original
        if (plants.contains(p)) {
            p.regeneratePlantId();
        }

        p.addUpdateListener(this);
        plants.add(p);

        if (p.isArchived()) {
            archivedPlants.add(p);
        }
        else    {
            activePlants.add(p);
        }
    }

    public boolean importPlants(ArrayList<File> files)   {
        for(File f : files) {
            Plant p = loadPlant(f);
            if (p != null)  {
                attachPlant(p);
                savePlant(p);
            }
        }

        return true;
    }

    private Plant loadPlant(File file)    {
        try
        {
            FileInputStream fis = new FileInputStream(plantFolderPath + PLANTS_FOLDER +
                    file.getName());
            ObjectInputStream ois = new ObjectInputStream(fis);

            Plant p = (Plant)ois.readObject();
            return p;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void savePlantTrackerSettings() {
        File folder = new File(plantFolderPath + SETTINGS_FOLDER);
        if (!folder.exists())   {
            folder.mkdir();
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(plantFolderPath +
                    SETTINGS_FOLDER + SETTINGS_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(settings);
            oos.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlantTrackerSettings() {
        // search plants folder for plant files
        try
        {
            FileInputStream fis = new FileInputStream(plantFolderPath + SETTINGS_FOLDER +
                    SETTINGS_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);

            PlantTrackerSettings p = (PlantTrackerSettings)ois.readObject();
            p.setListener(this);
            settings = p;
        }
        catch (Exception e) {
            e.printStackTrace();
            File f = new File(plantFolderPath + SETTINGS_FOLDER + SETTINGS_FILE);
            f.delete();
            System.out.println("Deleted invalid settings data: " + f.getName());
        }
    }

    public void deletePlant(Plant p)    {
        deletePlantFileData(p);
        plants.remove(p);
    }

    public void deleteAllPlants()    {
        for(Plant p : plants)   {
            deletePlantFileData(p);
        }

        plants.clear();
    }

    private void deletePlantFileData(Plant p)   {
        File plantFile = new File(plantFolderPath + PLANTS_FOLDER + p.getPlantId() +
                FILE_EXTENSION);
        if (plantFile.exists() && plantFile.isFile())   {
            plantFile.delete();
        }
    }

    public PlantTrackerSettings getPlantTrackerSettings()   {
        return settings;
    }

    @Override
    public void plantUpdate(Plant p) {
        savePlant(p);
    }

    @Override
    public void settingsChanged() {
        savePlantTrackerSettings();
    }

    public Plant getPlantById(long plantId) throws PlantNotFoundException {
        for(Plant p : plants)   {
            if (p.getPlantId() == plantId)  {
                return p;
            }
        }

        return null;
    }

    public long addGroup(String groupName)  {
        return addGroup(System.currentTimeMillis(), groupName);
    }

    private long addGroup(long groupId, String groupName)    {
        Group g = new Group(groupId, groupName);
        settings.addGroup(g);

        return g.getGroupId();
    }

    public void removeGroup(long groupId)   {
        settings.removeGroup(groupId);
        for (Plant p : getAllPlants())  {
            if (p.getGroups().remove(groupId))  {
                savePlant(p);
            }
        }
    }

    public void addMemberToGroup(long plantId, long groupId) {
        Group g = settings.getGroup(groupId);
        Plant p = getPlantById(plantId);
        p.addGroup(g.getGroupId());
        savePlant(p);
    }

    public void removeMemberFromGroup(long plantId, long groupId)   {
        Group g = settings.getGroup(groupId);
        Plant p = getPlantById(plantId);
        p.removeGroup(g.getGroupId());
        savePlant(p);
    }

    public ArrayList<Group> getGroupsPlantIsNotMemberOf(long plantId)   {
        Plant p = getPlantById(plantId);
        ArrayList<Group> groups = settings.getGroups();
        ArrayList<Group> nonMemberGroups = new ArrayList<>();

        for (Group g : groups)  {
            if (!p.getGroups().contains(g.getGroupId())) {
                nonMemberGroups.add(g);
            }
        }

        return nonMemberGroups;
    }

    public ArrayList<Group> getGroupsPlantIsMemberOf(long plantId)  {
        Plant p = getPlantById(plantId);
        ArrayList<Group> groups = new ArrayList<>();

        for(long groupId : p.getGroups())   {
            groups.add(settings.getGroup(groupId));
        }

        return groups;
    }

    public Group getGroup(long groupId) {
        return settings.getGroup(groupId);
    }

    public ArrayList<Group> getAllGroups() {
        return settings.getGroups();
    }

    public ArrayList<Plant> getMembersOfGroup(long groupId) {
        ArrayList<Plant> activeGroupMembers = new ArrayList<>();
        for(Plant p : getActivePlants())    {
            if (p.getGroups().contains(groupId))    {
                activeGroupMembers.add(p);
            }
        }

        return activeGroupMembers;
    }

    public void renameGroup(long groupId, String name)  {
        Group g = getGroup(groupId);
        g.setGroupName(name);
        settingsChanged();
    }

    public void performEventForPlantsInGroup(long groupId, IPlantEventDoer doer)  {
        ArrayList<Plant> plants = getMembersOfGroup(groupId);
        for(Plant p : plants)   {
            doer.doEventToPlant(p);
        }
    }
}

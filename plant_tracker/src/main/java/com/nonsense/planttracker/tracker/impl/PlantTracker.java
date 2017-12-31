package com.nonsense.planttracker.tracker.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nonsense.planttracker.tracker.exceptions.GroupNotFoundException;
import com.nonsense.planttracker.tracker.exceptions.PlantNotFoundException;
import com.nonsense.planttracker.tracker.impl.PlantActions.PlantAction;
import com.nonsense.planttracker.tracker.interf.IPlantEventDoer;
import com.nonsense.planttracker.tracker.interf.IPlantTrackerListener;
import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;
import com.nonsense.planttracker.tracker.interf.ISettingsChangedListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Derek Brooks on 6/30/2017.
 */
public class PlantTracker implements IPlantUpdateListener, ISettingsChangedListener {
    private PlantTrackerSettings settings;
    private ArrayList<Plant> plants;
    private transient IPlantTrackerListener uiListener;
    private transient ArrayList<Plant> activePlants;
    private transient ArrayList<Plant> archivedPlants;
    private String plantFolderPath;

    private static final String FILE_EXTENSION = ".json";
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

    public void removePlant(Plant p)    {
        deletePlantFileData(p);
        plants.remove(p);
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
            String filePath = plantFolderPath + PLANTS_FOLDER + p.getPlantId() + FILE_EXTENSION;
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            Gson g = new Gson();
            String json = g.toJson(p.getPlantData());

            System.out.println("plant json: " + json);

            bw.write(json);
            bw.close();
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
                try {
                    if (file.getName().endsWith(FILE_EXTENSION)) {
                        Plant p = loadPlant(file);
                        attachPlant(p);
                    }
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

        p.plantLoadFinished();

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
        String filePath = plantFolderPath + PLANTS_FOLDER + file.getName();

        try
        {
            Plant p = new Plant();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            Gson g = new Gson();

            Type plantType = new TypeToken<PlantData>(){}.getType();

            StringBuilder sb = new StringBuilder();
            while(br.ready())   {
                sb.append(br.readLine());
            }

            PlantData plantData = g.fromJson(sb.toString(), plantType);
            p.setPlantData(plantData);

            return p;
        }
        catch (Exception e) {
            e.printStackTrace();
            File f = new File(filePath);
           // f.delete();
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

        settingsChanged();
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

        uiListener.groupsUpdated();

        return g.getGroupId();
    }

    public void removeGroup(long groupId)   {
        settings.removeGroup(groupId);
        for (Plant p : getAllPlants())  {
            if (p.getGroups().remove(groupId))  {
                savePlant(p);
            }
        }

        uiListener.groupsUpdated();
    }

    public void addMemberToGroup(long plantId, long groupId) {
        Group g = settings.getGroup(groupId);
        Plant p = getPlantById(plantId);
        p.addGroup(g.getGroupId());
        savePlant(p);
        uiListener.groupsUpdated();
    }

    public void removeMemberFromGroup(long plantId, long groupId)   {
        Group g = settings.getGroup(groupId);
        Plant p = getPlantById(plantId);
        p.removeGroup(g.getGroupId());
        savePlant(p);
        uiListener.groupsUpdated();
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

    public final ArrayList<Group> getEmptyGroups()    {
        ArrayList<Group> groups = settings.getGroups();
        ArrayList<Group> emptyGroups = new ArrayList<>();

        for(Group g : groups)   {
            if (getMemberCountOfGroup(g.getGroupId()) == 0) {
                emptyGroups.add(g);
            }
        }

        return emptyGroups;
    }

    public final ArrayList<Group> getNonEmptyGroups()   {
        ArrayList<Group> groups = new ArrayList<>();
        ArrayList<Group> emptyGroups = getEmptyGroups();

        groups.addAll(settings.getGroups());

        groups.removeAll(emptyGroups);

        return groups;
    }

    private int getMemberCountOfGroup(long groupId) {
        int total = 0;
        ArrayList<Plant> activeGroupMembers = new ArrayList<>();
        for(Plant p : getActivePlants())    {
            if (p.getGroups().contains(groupId))    {
                total++;
            }
        }

        return total;
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

    public void performEventForPlantsInGroup(long groupId, PlantAction action)  {
        ArrayList<Plant> plants = getMembersOfGroup(groupId);
        for(Plant p : plants)   {
            action.runAction(p);
        }
    }

    public void setPlantTrackerListener(IPlantTrackerListener listener) {
        uiListener = listener;
    }

    public void removePlantState(String key)    {
        getPlantTrackerSettings().removeStateAutoComplete(key);

        settingsChanged();
    }

    public void addGenericRecordTemplate(GenericRecord record)  {
        settings.addGenericRecordTemplate(record);
    }

    public Set<String> getGenericRecordTypes()   {
        return settings.getGenericRecordNames();
    }

    public GenericRecord getGenericRecordTemplate(String name)  {
        return settings.getGenericRecordTemplate(name);
    }
}
















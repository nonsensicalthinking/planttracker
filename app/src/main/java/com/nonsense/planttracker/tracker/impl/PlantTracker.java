package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;
import com.nonsense.planttracker.tracker.interf.ISettingsChangedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Derek Brooks on 6/30/2017.
 */
public class PlantTracker implements IPlantUpdateListener, ISettingsChangedListener {
    private PlantTrackerSettings settings;
    private ArrayList<Plant> plants;
    private transient ArrayList<Plant> activePlants;
    private transient ArrayList<Plant> archivedPlants;
    private String plantFolderPath;

    public PlantTracker()   {
        this("plants/");
    }

    public PlantTracker(String plantFolderPath)   {
        this.plantFolderPath = plantFolderPath;

        File settingsFile = new File(plantFolderPath + "/settings/tracker_settings.ser");
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
        File folder = new File(plantFolderPath+"/plants/");
        if (!folder.exists())   {
            folder.mkdir();
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(plantFolderPath + "/plants/" + p.getPlantId() + ".ser");
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
        File[] files = new File(plantFolderPath + "/plants/").listFiles();
        if (files != null)  {
            for (File file : files) {
                try
                {
                    FileInputStream fis = new FileInputStream(plantFolderPath + "/plants/" + file.getName());
                    ObjectInputStream ois = new ObjectInputStream(fis);

                    Plant p = (Plant)ois.readObject();
                    p.addUpdateListener(this);
                    plants.add(p);

                    if (p.isArchived()) {
                        archivedPlants.add(p);
                    }
                    else    {
                        activePlants.add(p);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    File f = new File(plantFolderPath + "/plants/" + file.getName());
                    f.delete();
                    System.out.println("Deleted invalid plant data: " + file.getName());
                }
            }
        }
    }

    private void savePlantTrackerSettings() {
        File folder = new File(plantFolderPath+ "/settings/");
        if (!folder.exists())   {
            folder.mkdir();
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(plantFolderPath + "/settings/tracker_settings.ser");
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
            FileInputStream fis = new FileInputStream(plantFolderPath + "/settings/tracker_settings.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);

            PlantTrackerSettings p = (PlantTrackerSettings)ois.readObject();
            p.setListener(this);
            settings = p;
        }
        catch (Exception e) {
            e.printStackTrace();
            File f = new File(plantFolderPath + "/settings/tracker_settings.scr");
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
        File plantFile = new File(plantFolderPath + "/plants/" + p.getPlantId() + ".ser");
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

    public Plant getPlantById(long plantId) {
        for(Plant p : plants)   {
            if (p.getPlantId() == plantId)  {
                return p;
            }
        }

        return null;
    }
}

package com.nonsense.planttracker.tracker.impl;

import com.nonsense.planttracker.tracker.interf.IPlantUpdateListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Derek Brooks on 6/30/2017.
 */
public class PlantTracker implements IPlantUpdateListener {
    private ArrayList<Plant> plants;
    private String plantFolderPath;

    public PlantTracker()   {
        this("plants/");
    }

    public PlantTracker(String plantFolderPath)   {
        this.plantFolderPath = plantFolderPath;
        plants = new ArrayList<>();
        loadPlants();
    }

    public Iterator<Plant> getIteratorForAllPlants()    {
        return plants.iterator();
    }

    public ArrayList<Plant> getAllPlants()  {
        return plants;
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
        File folder = new File(plantFolderPath);
        if (!folder.exists())   {
            folder.mkdir();
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(plantFolderPath + "/" + p.getPlantId() + ".ser");
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
        File[] files = new File(plantFolderPath).listFiles();
        if (files != null)  {
            for (File file : files) {
                try
                {
                    FileInputStream fis = new FileInputStream(plantFolderPath + "/" + file.getName());
                    ObjectInputStream ois = new ObjectInputStream(fis);

                    Plant p = (Plant)ois.readObject();
                    p.addUpdateListener(this);
                    plants.add(p);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    File f = new File(plantFolderPath + "/" + file.getName());
                    f.delete();
                    System.out.println("Deleted invalid plant data: " + file.getName());
                }
            }
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
        File plantFile = new File(plantFolderPath + "/" + p.getPlantId() + ".ser");
        if (plantFile.exists() && plantFile.isFile())   {
            plantFile.delete();
        }
    }

    @Override
    public void plantUpdate(Plant p) {
        savePlant(p);
    }
}

package com.nonsense.planttracker.tracker.impl;

import java.util.Iterator;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class Main {

    public static void main(String[] args)  {
        PlantTracker pt = new PlantTracker();

        Iterator<Plant> planterator = pt.getIteratorForAllPlants();
        int x=0;
        for(Plant p=null; planterator.hasNext(); p=planterator.next())  {
            if (p==null)    {
                continue;
            }

            System.out.println("Plant: " + p.getPlantName());

            x++;
        }
        System.out.println("Number of plants loaded after startup: " + x);

        pt.addPlant("My first plant", true);
    }

}

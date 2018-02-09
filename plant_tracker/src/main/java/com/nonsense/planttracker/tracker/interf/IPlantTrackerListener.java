package com.nonsense.planttracker.tracker.interf;

import com.nonsense.planttracker.tracker.impl.Plant;

/**
 * Created by Derek Brooks on 9/23/2017.
 */

public interface IPlantTrackerListener {

    public void plantUpdated(Plant p);
    public void groupsUpdated();

}

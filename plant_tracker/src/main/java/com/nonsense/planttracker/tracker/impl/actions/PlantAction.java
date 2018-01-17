package com.nonsense.planttracker.tracker.impl.actions;

import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Plant;

/**
 * Created by Derek Brooks on 12/23/2017.
 */

public class PlantAction {

    private GenericRecord record;

    public PlantAction(GenericRecord record)  {
        this.record = record;
    }

    public void runAction(Plant p) {
        p.finalizeRecord(record);
    }
}

package com.nonsense.planttracker.tracker.exceptions;

/**
 * Created by Derek Brooks on 7/29/2017.
 */

public class PlantNotFoundException extends RuntimeException {
    public PlantNotFoundException(String message)   {
        super(message);
    }
}

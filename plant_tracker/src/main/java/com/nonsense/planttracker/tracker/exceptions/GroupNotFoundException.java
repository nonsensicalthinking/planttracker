package com.nonsense.planttracker.tracker.exceptions;

/**
 * Created by Derek Brooks on 7/29/2017.
 */

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(String message)   {
        super(message);
    }
}

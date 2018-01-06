package com.nonsense.planttracker.tracker.impl;

import java.io.Serializable;

/**
 * Created by Derek Brooks on 7/29/2017.
 */

public class Group implements Serializable  {
    private static final long serialVersionUID = 43534553446L;
    private long groupId;
    private String groupName;

    public Group(long groupId, String groupName)    {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public Group(String groupName)  {
        this(System.currentTimeMillis(), groupName);
    }

    public String getGroupName()  {
        return groupName;
    }

    public void setGroupName(String groupName)  {
        this.groupName = groupName;
    }

    public long getGroupId()    {
        return groupId;
    }

    @Override
    public boolean equals(Object o)    {
        return groupId == ((Group)o).getGroupId();
    }
}

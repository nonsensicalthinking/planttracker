package com.nonsense.planttracker.tracker.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Derek Brooks on 12/23/2017.
 */

public class GenericRecord implements Serializable {

    public String displayName;
    public Calendar time;
    public String notes;
    public TreeMap<String, Object> dataPoints;
    public String summaryTemplate;

    public GenericRecord(String displayName)  {
        this.displayName = displayName;

        this.dataPoints = new TreeMap<>();
        this.time = Calendar.getInstance();
    }

    public void setDataPoint(String key, Object value) {
        if (dataPoints.containsKey(key))    {
            dataPoints.remove(key);
        }

        dataPoints.put(key, value);
    }

    public Object getDataPoint(String key)    {
        return dataPoints.get(key);
    }

    public String getSummary()  {
        if (summaryTemplate == null)    {
            return "";
        }

        String buildTemplate = summaryTemplate;
        Pattern p = Pattern.compile("\\{(.*?)\\}");
        Matcher m = p.matcher(summaryTemplate);

        ArrayList<String> placeholders = new ArrayList<>();
        while(m.find()) {
            String ph = m.group();
            if (!placeholders.contains(ph))  {
                placeholders.add(ph);
            }
        }

        String summary = summaryTemplate;
        for(String ph : placeholders)   {
            String key = ph.replace('{', ' ')
                    .replace('}', ' ').trim();

            String regex = ph.replace("{", "\\{")
                    .replace("}", "\\}");

            if (dataPoints.containsKey(key)) {
                summary = summary.replaceAll(regex, dataPoints.get(key).toString());
            }
        }

        if (notes != null)  {
            summary += ", Notes: " + notes;
        }

        return summary;
    }
}

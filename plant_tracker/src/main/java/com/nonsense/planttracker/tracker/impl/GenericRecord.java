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

public class GenericRecord implements Serializable, Cloneable {

    public long id;
    public String displayName = "";
    public int color = 0;
    public Calendar time;
    public String notes = "";
    public TreeMap<String, Object> dataPoints = new TreeMap<>();
    public String summaryTemplate = "";
    public String summary = "";
    public boolean showNotes = false;
    public int weeksSincePhase = 0;
    public int weeksSinceStart = 0;
    public int phaseCount = 0;
    public ArrayList<String> images;

    public transient GenericRecord template;
    public transient String phaseDisplay = "";
    public transient boolean hasImages = false;
    public transient boolean hasDataPoints = false;

    public GenericRecord(String displayName)  {
        this.displayName = displayName;
        this.time = Calendar.getInstance();
        this.id = this.time.getTimeInMillis();
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

    public String getSummary(String summaryTemplate)  {
        if (summary == null)    {
            if (summaryTemplate != null) {
                String buildTemplate = summaryTemplate;
                Pattern p = Pattern.compile("\\{(.*?)\\}");
                Matcher m = p.matcher(summaryTemplate);

                ArrayList<String> placeholders = new ArrayList<>();
                while (m.find()) {
                    String ph = m.group();
                    if (!placeholders.contains(ph)) {
                        placeholders.add(ph);
                    }
                }

                summary = summaryTemplate;
                for (String ph : placeholders) {
                    String key = ph.replace('{', ' ')
                            .replace('}', ' ').trim();

                    String regex = ph.replace("{", "\\{")
                            .replace("}", "\\}");

                    if (dataPoints.containsKey(key)) {
                        summary = summary.replaceAll(regex, dataPoints.get(key).toString());
                    }
                }
            }

            if (showNotes && notes != null && !notes.equals(""))  {
                // append comma if we already have some summary text
                if (summary != null && !summary.equals("")) {
                    summary += ", ";
                }

                summary += "Notes: " + notes;
            }
        }

        return summary;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GenericRecord record = (GenericRecord)super.clone();

        return record;
    }
}

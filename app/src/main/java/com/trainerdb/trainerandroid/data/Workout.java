package com.trainerdb.trainerandroid.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dcotrim on 20/09/2016.
 */
public class Workout {
    public String key;
    public String name;
    public String source;
    public String author;
    public float intensityFactor;
    public long totalTicks;
    public long createdAt;
    public long tss;
    public  Map<String, Boolean> tags = new HashMap<>();

    public Workout() {

    }
    @Override
    public String toString() {
        return name.toString()+"#"+String.valueOf(tss)+"#"+String.valueOf(intensityFactor);
    }
}

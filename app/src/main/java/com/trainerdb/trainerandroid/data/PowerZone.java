package com.trainerdb.trainerandroid.data;

/**
 * Created by dcotrim on 18/07/2016.
 */
public class PowerZone {
    String description;
    int low, high;

    public int getHigh() {
        return high;
    }

    public int getLow() {
        return low;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public PowerZone() {

    }

    public PowerZone(String description, int low, int high) {
        this.description = description;
        this.low = low;
        this.high = high;
    }
}
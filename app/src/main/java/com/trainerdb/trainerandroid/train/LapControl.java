package com.trainerdb.trainerandroid.train;

import com.trainerdb.trainerandroid.TrainApplication;
import java.io.Serializable;

/**
 * Created by Daniel on 30/06/2016.
 */

public class LapControl implements Serializable {
    private int count;
    private double sumHr, sumWatts, sumCadence, sumSpeed;
    private double wattsMin, wattsMax, hrMin, hrMax, cadenceMin, cadenceMax, speedMin, speedMax;
    private double distanceStart, distanceEnd;
    private int lapNum;
    private String name;
    private long start, end;
    private boolean isEmptyDistance;
    private NormalizedPower npLap;
    private int ftp = TrainApplication.getFTP();
    private int recPerSec;

    public LapControl() {
        clear();
    }

    public LapControl(int recPerSec, int lapNum, String name, long start, long end) {
        super();
        this.lapNum = lapNum;
        this.name = name;
        this.start = start;
        this.end = end;
        this.recPerSec = recPerSec;
        npLap = new NormalizedPower(ftp, recPerSec);
    }

    public void clear() {
        count = lapNum = 0;
        start = end = 0;
        name = "";
        sumHr = sumWatts = sumCadence = sumSpeed = 0;
        wattsMax = hrMax = cadenceMax = speedMax = 0;
        distanceEnd = distanceStart = 0;
        cadenceMin = Double.MAX_VALUE;
        speedMin = Double.MAX_VALUE;
        wattsMin = Double.MAX_VALUE;
        hrMin = Double.MAX_VALUE;
        isEmptyDistance = true;
    }

    public void add() {
        count++;
    }

    public void setHr(double value) {
        sumHr += value;
        if (value > hrMax) hrMax = value;
        else if (value < hrMin) hrMin = value;
    }

    public void setWatts(double value) {
        sumWatts += value;
        if (value > wattsMax) wattsMax = value;
        else if (value < wattsMin) wattsMin = value;

        this.npLap.set(value);
    }

    public void setCadence(double value) {
        sumCadence += value;
        if (value > cadenceMax) cadenceMax = value;
        else if (value < cadenceMin) cadenceMin = value;
    }

    public void setSpeed(double value) {
        sumSpeed += value;
        if (value > speedMax) speedMax = value;
        else if (value < speedMin) speedMin = value;
    }

    public double getHr() {
        return sumHr / count;
    }

    public double getCadence() {
        return sumCadence / count;
    }

    public double getSpeed() {
        return sumSpeed / count;
    }

    public double getWatts() {
        return sumWatts / count;
    }

    public double getHrMin() {
        return hrMin;
    }

    public double getHrMax() {
        return hrMax;
    }

    public double getCadenceMax() {
        return cadenceMax;
    }

    public double getCadenceMin() {
        return cadenceMin;
    }

    public double getSpeedMax() {
        return speedMax;
    }

    public double getSpeedMin() {
        return speedMin;
    }

    public double getWattsMax() {
        return wattsMax;
    }

    public double getWattsMin() {
        return wattsMin;
    }

    public void setDistance(double distance) {
        if (isEmptyDistance) {
            distanceStart = distance;
            isEmptyDistance = false;
        }
        distanceEnd = distance;
    }

    public int getCount() {
        return count;
    }

    public double getDistance() {
        return distanceEnd - distanceStart;
    }

    public boolean isRunning(long now) {
        return start <= now && end >= now ? true : false;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getLapNum() {
        return lapNum;
    }

    public void setLapNum(int lapNum) {
        this.lapNum = lapNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public NormalizedPower getNp() {
        return npLap;
    }

    public double getKj() {
        return sumWatts / (recPerSec * 1000);
    }
}

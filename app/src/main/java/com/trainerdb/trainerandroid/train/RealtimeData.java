package com.trainerdb.trainerandroid.train;

/**
 * Created by dcotrim on 22/06/2016.
 */
public class RealtimeData {
    private String name;
    private double hr;
    private double watts;
    private double speed;
    private double cadence;
    private double distance;
    private long msecs;
    private long lapMsecs;
    private long lapMsecsRemaining;
    private double load;
    private String deviceStatus;
    private int lap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHr() {
        return hr;
    }

    public void setHr(double hr) {
        this.hr = hr;
    }

    public double getWatts() {
        return watts;
    }

    public void setWatts(double watts) {
        this.watts = watts;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getCadence() {
        return cadence;
    }

    public void setCadence(double cadence) {
        this.cadence = cadence;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getMsecs() {
        return msecs;
    }

    public void setMsecs(long msecs) {
        this.msecs = msecs;
    }

    public long getLapMsecs() {
        return lapMsecs;
    }

    public void setLapMsecs(long lapMsecs) {
        this.lapMsecs = lapMsecs;
    }

    public long getLapMsecsRemaining() {
        return lapMsecsRemaining;
    }

    public void setLapMsecsRemaining(long lapMsecsRemaining) {
        this.lapMsecsRemaining = lapMsecsRemaining;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public int getLap() {
        return lap;
    }

    public void setLap(int lap) {
        this.lap = lap;
    }
}

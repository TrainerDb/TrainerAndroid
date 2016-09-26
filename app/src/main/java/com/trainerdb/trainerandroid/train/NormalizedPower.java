package com.trainerdb.trainerandroid.train;

import java.io.Serializable;

/**
 * Created by dcotrim on 12/07/2016.
 */
public class NormalizedPower implements Serializable {
    Rolling watts30;
    private int count = 0;
    private double rollingSum, ftp = 0;
    private int recPerSec;

    public NormalizedPower(double ftp, int recPerSec) {
        this.recPerSec = recPerSec;
        this.ftp = ftp;
        watts30 = new Rolling(recPerSec * 30);
    }

    public void clear() {
        watts30.clear();
        count = 0;
        rollingSum = 0;
    }

    public void set(double watts) {
        watts30.add(watts);
        count++;
        rollingSum += Math.pow(watts30.getAverage(), 4);
    }

    public double getNp() {
        if (count == 0)
            return 0;
        return Math.pow(rollingSum / count, 0.25);
    }

    public double getIntensityFactor() {
        return getNp() / ftp;
    }

    public double getTss() {
        double normWork = getNp() * (count / recPerSec);
        double rawTSS = normWork * getIntensityFactor();
        double workInAnHourAtCP = ftp * 3600;
        return rawTSS / workInAnHourAtCP * 100.0;
    }
}

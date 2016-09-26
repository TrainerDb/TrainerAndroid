package com.trainerdb.trainerandroid.train;

import android.net.Uri;
import android.support.v4.content.FileProvider;
import com.trainerdb.trainerandroid.TrainApplication;
import com.trainerdb.trainerandroid.data.LapPoint;
import com.trainerdb.trainerandroid.data.WorkoutData;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 11/07/2016.
 */
public class TrainState implements Serializable {
    public WorkoutData workout;
    //public WorkoutRecord record;

    public double wattsSum, cadenceSum, speedSum, hrSum;
    public double distance;
    public double ftp = TrainApplication.getFTP();
    public int hertz, count;

    public int hrMax;
    public int speedMax;
    public int cadenceMax;

    public int lapBeep, lap;
    public boolean recording = false, cancelCalled = false;
    public boolean isNewLap = false;
    public long now = 0;

    public List<LapControl> lapsControl= new ArrayList<>();

    public ArrayList<Integer> watts = new ArrayList<>(); // 1s samples [watts]
    public ArrayList<Integer> hr = new ArrayList<>(); // 1s samples [bpm]
    public ArrayList<Double> speed = new ArrayList<>(); // 1s samples [km/h]
    public ArrayList<Integer> cadence = new ArrayList<>(); // 1s samples [rpm]

    public Rolling watts30;
    private int recPerSec;

    public TrainState(WorkoutData workout, int recPerSec) {
        this.recPerSec = recPerSec;
        this.workout = workout;
        watts30 = new Rolling(recPerSec * 30);
        clear();
    }

    public void clear() {
        hertz = count = 0;
        wattsSum = hrSum = speedSum = cadenceSum = 0;
        distance = 0;

        // set initial
        cadenceMax = 600;
        hrMax = 220;
        speedMax = 50;
        lapBeep = 0;

        watts30.clear();
        watts.clear();
        hr.clear();
        speed.clear();
        cadence.clear();

        lapsControl.clear();

        for (LapPoint lap : workout.getErg().getLapPoints()) {
            lapsControl.add(new LapControl(recPerSec, lap.lapNum, lap.name, lap.x, lap.y));
        }
    }

    private void newLapInfo(long now, double hr, double cad, double watts, double speed, double distance) {
        for (LapControl lap : this.lapsControl) {
            if (lap.isRunning(now)) {
                lap.add();
                lap.setCadence(cad);
                lap.setHr(hr);
                lap.setWatts(watts);
                lap.setSpeed(speed);
                lap.setDistance(distance);
            }
        }
    }

    public LapControl getMainLap() {
        return lapsControl.get(0);
    }

    public LapControl getCurrentLap() {
        return this.lapsControl.get(lap);
    }

    public void workoutStart(File recordFile, long startTime) {
    }

    private void createInterval(LapControl avgLap) {
    }

    public void workoutComplete(File recordFile, long totalTicks,
                                byte[] bitmap, byte[] bitmapMin) {
        LapControl workoutLap = this.lapsControl.get(0);

        for (LapControl lap : this.lapsControl) {
            if (lap.getCount() > 0)
                createInterval(lap);
        }

        Uri fileUri = FileProvider.getUriForFile(TrainApplication.getAppContext(),
                "com.trainerandroid.fileprovider", recordFile);

    }

    public void updateRealtimeData(RealtimeData rt) {
        count++;
        now = rt.getMsecs();
        distance = rt.getDistance();
        lap = rt.getLap();

        if (isNewLap) isNewLap = false;

        this.newLapInfo(now, rt.getHr(), rt.getCadence(), rt.getWatts(),
                rt.getSpeed(), rt.getDistance());

        watts30.add(rt.getWatts());

        //wbalSum += rt.getWbal();
        wattsSum += rt.getWatts();
        hrSum += rt.getHr();
        cadenceSum += rt.getCadence();
        speedSum += rt.getSpeed();

        hertz++;

        // did we get 5 samples (5hz refresh rate) ?
        if (hertz == recPerSec) {
            //int b = wbalSum / 5.0f;
            //wbal << b;
            int w = (int) (wattsSum / recPerSec);
            watts.add(w);
            int h = (int) (hrSum / recPerSec);
            hr.add(h);
            double s = (speedSum / recPerSec);
            speed.add(s);
            int c = (int) (cadenceSum / recPerSec);
            cadence.add(c);

            // clear for next time
            hertz = 0;
            wattsSum = hrSum = speedSum = cadenceSum = 0;
        }
    }


}

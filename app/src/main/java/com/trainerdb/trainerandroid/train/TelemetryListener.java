package com.trainerdb.trainerandroid.train;


import com.trainerdb.trainerandroid.data.WorkoutData;

import java.io.File;

/**
 * Created by Daniel on 23/06/2016.
 */
public interface TelemetryListener {
    void telemetryUpdate(RealtimeData rtData);
    void setNow(long msecs);
    void lapComplete(long now, int lapNum);
    void newText(long now, String text);
    void workoutComplete(WorkoutData workout, File recordFile, long startTime, long totalTicks);
    void telemetryStart(WorkoutData workout, File recordFile, long startTime);
    void telemetryStop();
    void telemetryResume();
    void telemetryPause();
}

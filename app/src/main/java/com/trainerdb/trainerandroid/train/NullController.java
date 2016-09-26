package com.trainerdb.trainerandroid.train;

import com.trainerdb.trainerandroid.data.RealtimeController;

/**
 * Created by dcotrim on 22/06/2016.
 */
public class NullController extends RealtimeController {
    private int count = 0;
    double load;

    public NullController(DeviceConfiguration device){
        super(device);
    }

    @Override
    public void getRealtimeData(RealtimeData rtData) {
        rtData.setName("Null");
        rtData.setWatts(load + randomWithRange(0, 10) - 5);
        rtData.setSpeed(25 + randomWithRange(0, 4) - 2);
        rtData.setCadence(85 + randomWithRange(0, 9) - 5);
        rtData.setHr(145 + randomWithRange(0, 2) - 2);
        rtData.setLoad(load);
        //processRealTimeData(rtData);

        count++;
    }

    @Override
    public void setLoad(double load) {
        this.load = load;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
}

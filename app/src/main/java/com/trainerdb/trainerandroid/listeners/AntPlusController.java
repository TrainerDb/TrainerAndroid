package com.trainerdb.trainerandroid.listeners;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController;
import com.trainerdb.trainerandroid.data.RealtimeController;
import com.trainerdb.trainerandroid.train.DeviceConfiguration;
import com.trainerdb.trainerandroid.train.RealtimeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 25/06/2016.
 */
public class AntPlusController extends RealtimeController {
    AsyncScanController<AntPlusHeartRatePcc> hrScanCtrl;
    int hr = 0;
    double load, cadence, speed, watts;
    String deviceStatus;
    List<Ant> devices = new ArrayList<>();

    public AntPlusController(DeviceConfiguration device){
        super(device);
    }

    @Override
    public void getRealtimeData(RealtimeData rtData) {
        rtData.setName("ANT+");
        rtData.setWatts(watts);
        rtData.setSpeed(speed);
        rtData.setCadence(cadence);
        rtData.setHr(hr);
        rtData.setLoad(load);
        rtData.setDeviceStatus(deviceStatus);
        processRealTimeData(rtData);

        deviceStatus = null;
    }

    @Override
    public void setLoad(double load) {
        this.load = load;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public void setCadence(double cadence) {
        this.cadence = cadence;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }


    @Override
    public void start() {
        devices.clear();
        devices.add(new AntHeartRate(0, this));
        devices.add(new AntSpeedDistance(0, this));
    }

    @Override
    public void stop() {
        for (Ant device: devices)
            device.stop();
    }
}

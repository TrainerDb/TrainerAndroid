package com.trainerdb.trainerandroid.train;

import com.trainerdb.trainerandroid.TrainApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 23/06/2016.
 */
public class DeviceConfigurations {

    private static List<DeviceConfiguration> readConfig() {
        List<DeviceConfiguration> entries = new ArrayList<>();
        DeviceConfiguration c = new DeviceConfiguration();
        c.setName("DEVICE");
        c.setTypeEnum(TrainApplication.getDeviceType());
        entries.add(c);

        return entries;
    }

    public static List<DeviceConfiguration> getList(){
        return readConfig();
    }
}

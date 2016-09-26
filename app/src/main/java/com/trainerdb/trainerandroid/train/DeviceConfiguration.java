package com.trainerdb.trainerandroid.train;

import com.google.firebase.database.Exclude;
import com.trainerdb.trainerandroid.data.RealtimeController;

/**
 * Created by Daniel on 23/06/2016.
 */
public class DeviceConfiguration {
    private DeviceType type;
    private String name;
    //private int wheelSize;
    //private int postProcess;
    private RealtimeController controller;

    @Exclude
    public DeviceType getTypeEnum() {
        return type;
    }

    @Exclude
    public void setTypeEnum(DeviceType type) {
        this.type = type;
    }

    public String getType() {
        if (type == null) {
            return null;
        } else {
            return type.name();
        }
    }

    public void setType(String typeString) {
        // Get enum from string
        if (typeString == null) {
            this.type = null;
        } else {
            this.type = DeviceType.valueOf(typeString);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
/*
    public int getWheelSize() {
        return wheelSize;
    }

    public void setWheelSize(int wheelSize) {
        this.wheelSize = wheelSize;
    }

    public int getPostProcess() {
        return postProcess;
    }

    public void setPostProcess(int postProcess) {
        this.postProcess = postProcess;
    }*/

    @Exclude
    public RealtimeController getController() {
        return controller;
    }
    @Exclude
    public void setController(RealtimeController controller) {
        this.controller = controller;
    }

    public enum DeviceType {
        DEV_NULL, DEV_ANTLOCAL
    }

    public DeviceConfiguration() {

    }
}

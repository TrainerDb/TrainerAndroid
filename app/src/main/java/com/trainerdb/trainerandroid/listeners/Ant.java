package com.trainerdb.trainerandroid.listeners;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

/**
 * Created by Daniel on 25/06/2016.
 */
public abstract class Ant //extends Base<Object>
{
    protected PccReleaseHandle<?> releaseHandle;    //Handle class
    protected int deviceNumber = 0;
    AntPlusController controller;

    //setup listeners and logging
    public Ant(int pDeviceNumber, AntPlusController controller)
    {
        deviceNumber = pDeviceNumber;
        this.controller = controller;
    }


    public AntPluginPcc.IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new AntPluginPcc.IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
            //if we lose a device zero out its values
            controller.setDeviceStatus(deviceNumber + ": " + newDeviceState);
            if (newDeviceState.equals(DeviceState.DEAD)) {
                zeroReadings();
            }
            else if (newDeviceState.equals(DeviceState.SEARCHING)){
                zeroReadings();
            }
        }
    };

    abstract protected void zeroReadings();

    abstract protected void requestAccess();

    public void stop() {
        if (releaseHandle != null) {
            releaseHandle.close();
        }
        releaseHandle = null;
    }


}




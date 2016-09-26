package com.trainerdb.trainerandroid.listeners;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.trainerdb.trainerandroid.TrainApplication;

import java.math.BigDecimal;
import java.util.EnumSet;

/**
 * Created by Daniel on 25/06/2016.
 */
public class AntHeartRate extends Ant
{
    public AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc> mResultReceiver;

    public AntHeartRate(int pDeviceNumber, final AntPlusController controller) {
        super(pDeviceNumber, controller);

        mResultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
            //Handle the result, connecting to events on success or reporting failure to user.
            @Override
            public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode, DeviceState initialDeviceState)
            {
                controller.setDeviceStatus("AntHeartRate: " + resultCode);
                if(resultCode == com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult.SUCCESS) {
                    deviceNumber = result.getAntDeviceNumber();
                    result.subscribeHeartRateDataEvent(
                            new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
                                @Override
                                public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags, final int computedHeartRate, final long heartBeatCount, final BigDecimal heartBeatEventTime, final AntPlusHeartRatePcc.DataState dataState) {
                                    controller.setHr(computedHeartRate);
                                }
                            }
                    );
                }
                else {
                    if (resultCode == com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult.SEARCH_TIMEOUT) {
                        requestAccess();
                    }
                }
            }
        };

        requestAccess();
    }



    @Override
    protected void requestAccess() {
        releaseHandle = AntPlusHeartRatePcc.requestAccess(TrainApplication.getAppContext(), deviceNumber, 0, mResultReceiver, mDeviceStateChangeReceiver);
    }

    @Override
    public void zeroReadings()
    {
        controller.setHr(0);
    }
}

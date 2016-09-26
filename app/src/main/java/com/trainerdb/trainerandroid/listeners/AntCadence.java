package com.trainerdb.trainerandroid.listeners;

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
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
public class AntCadence extends Ant {
    public AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeCadencePcc> mResultReceiver;

    public AntCadence(int pDeviceNumber, final AntPlusController controller) {
        super(pDeviceNumber, controller);

        mResultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeCadencePcc>() {
            //Handle the result, connecting to events on success or reporting failure to user.
            @Override
            public void onResultReceived(AntPlusBikeCadencePcc result, RequestAccessResult resultCode, DeviceState initialDeviceStateCode) {
                controller.setDeviceStatus("AntCadence: " + resultCode);
                if (resultCode == com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult.SUCCESS) {
                    deviceNumber = result.getAntDeviceNumber();
                    result.subscribeRawCadenceDataEvent(
                            new AntPlusBikeCadencePcc.IRawCadenceDataReceiver() {
                                @Override
                                public void onNewRawCadenceData(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final BigDecimal timestampOfLastEvent, final long cumulativeRevolutions) {
                                    //controller.setHr(computedHeartRate);
                                }
                            }
                    );

                    result.subscribeCalculatedCadenceEvent(
                            new AntPlusBikeCadencePcc.ICalculatedCadenceReceiver() {
                                @Override
                                public void onNewCalculatedCadence(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final BigDecimal calculatedCadence) {
                                    controller.setCadence(calculatedCadence.doubleValue());
                                }
                            }
                    );
                } else if (resultCode == com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult.SEARCH_TIMEOUT) {
                        requestAccess();
                }
            }
        };
        requestAccess();
    }


    @Override
    protected void requestAccess() {
        releaseHandle = AntPlusBikeCadencePcc.requestAccess(TrainApplication.getAppContext().getApplicationContext(), deviceNumber, 0, true, mResultReceiver,   mDeviceStateChangeReceiver);
    }

    @Override
    protected void zeroReadings() {
        controller.setCadence(0);
    }
}

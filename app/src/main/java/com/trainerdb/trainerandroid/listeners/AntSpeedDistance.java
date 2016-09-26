package com.trainerdb.trainerandroid.listeners;

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc;
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
public class AntSpeedDistance extends Ant {

    public AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> mResultReceiver;

    public AntSpeedDistance(int pDeviceNumber, final AntPlusController controller) {
        super(pDeviceNumber, controller);

        mResultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc>() {
            //Handle the result, connecting to events on success or reporting failure to user.
            @Override
            public void onResultReceived(AntPlusBikeSpeedDistancePcc result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
                controller.setDeviceStatus("AntSpeedDistance: " + resultCode);
                if (resultCode == com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult.SUCCESS) {
                    deviceNumber = result.getAntDeviceNumber();

                    result.subscribeMotionAndSpeedDataEvent(
                            new AntPlusBikeSpeedDistancePcc.IMotionAndSpeedDataReceiver() {
                                @Override
                                public void onNewMotionAndSpeedData(long l, EnumSet<EventFlag> enumSet, boolean b) {

                                }
                            }
                    );
                    result.subscribeCalculatedSpeedEvent(
                            new AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver(
                                    new BigDecimal(TrainApplication.getWheelCircunference() / 1000)) {
                                @Override
                                public void onNewCalculatedSpeed(long estTimestamp, EnumSet<EventFlag> eventFlags, final BigDecimal calculatedSpeed) {
                                    controller.setSpeed(calculatedSpeed.doubleValue() * 3.6);
                                }
                            }
                    );
                    result.subscribeRawSpeedAndDistanceDataEvent(
                            new AntPlusBikeSpeedDistancePcc.IRawSpeedAndDistanceDataReceiver() {
                                @Override
                                public void onNewRawSpeedAndDistanceData(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final BigDecimal timestampOfLastEvent, final long cumulativeRevolutions) {
                                    //controller.setHr(0);
                                }
                            }
                    );

                    if (result.isSpeedAndCadenceCombinedSensor()) {
                        AntPlusBikeCadencePcc.requestAccess(TrainApplication.getAppContext(), deviceNumber, 0, true,
                                new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeCadencePcc>() {
                                    @Override
                                    public void onResultReceived(AntPlusBikeCadencePcc resultCadence, RequestAccessResult resultCodeCadence, DeviceState initialDeviceStateCode) {
                                        if (resultCodeCadence == RequestAccessResult.SUCCESS) {
                                            resultCadence.subscribeCalculatedCadenceEvent(
                                                    new AntPlusBikeCadencePcc.ICalculatedCadenceReceiver() {
                                                        @Override
                                                        public void onNewCalculatedCadence(long estTimestamp, EnumSet<EventFlag> eventFlags, final BigDecimal calculatedCadence) {
                                                            controller.setCadence(calculatedCadence.doubleValue());
                                                        }
                                                    }
                                            );
                                        }
                                    }
                                }, new AntPluginPcc.IDeviceStateChangeReceiver() {
                                    @Override
                                    public void onDeviceStateChange(DeviceState deviceState) {
                                        if (deviceState.equals(DeviceState.DEAD)) {
                                            //zeroReadings();
                                        }
                                    }
                                });
                    }


                } else {
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
        releaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(TrainApplication.getAppContext(), deviceNumber, 0, true, mResultReceiver, mDeviceStateChangeReceiver);
    }

    @Override
    protected void zeroReadings() {
        controller.setCadence(0);
        controller.setSpeed(0);
    }
}

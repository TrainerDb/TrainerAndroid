package com.trainerdb.trainerandroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.trainerdb.trainerandroid.IGetAsyncListener;
import com.trainerdb.trainerandroid.R;
import com.trainerdb.trainerandroid.TrainApplication;
import com.trainerdb.trainerandroid.TrainWorkoutView;
import com.trainerdb.trainerandroid.data.WorkoutData;
import com.trainerdb.trainerandroid.data.WorkoutService;
import com.trainerdb.trainerandroid.train.RealtimeData;
import com.trainerdb.trainerandroid.train.TelemetryEnum;
import com.trainerdb.trainerandroid.train.TelemetryListener;
import com.trainerdb.trainerandroid.train.TelemetryView;
import com.trainerdb.trainerandroid.train.TrainController;
import com.trainerdb.trainerandroid.train.TrainFile;
import com.trainerdb.trainerandroid.train.TrainState;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrainActivity extends AppCompatActivity implements TelemetryListener, View.OnClickListener {

    TrainWorkoutView trainWorkoutView;
    public TrainState s;

    public TextView tvTrainWorkout;
    ToneGenerator toneG;
    TextView tvTrainTotalTime, tvTrainIntervalTime;

    HashMap<Integer, TelemetryEnum> telemetryMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        tvTrainTotalTime = (TextView) findViewById(R.id.tvTrainTotalTime);
        tvTrainIntervalTime = (TextView) findViewById(R.id.tvTrainIntervalTime);
        trainWorkoutView = (TrainWorkoutView) findViewById(R.id.vwTrainWorkout);
        tvTrainWorkout = (TextView) findViewById(R.id.tvTrainWorkout);

        setTelemetry(R.id.telemetry11, TelemetryEnum.POWER);
        setTelemetry(R.id.telemetry12, TelemetryEnum.TARGET);
        setTelemetry(R.id.telemetry13, TelemetryEnum.LP_POWER);
        setTelemetry(R.id.telemetry14, TelemetryEnum.SPEED);
        setTelemetry(R.id.telemetry23, TelemetryEnum.LP_HR);
        setTelemetry(R.id.telemetry24, TelemetryEnum.KM);
        setTelemetry(R.id.telemetry31, TelemetryEnum.HR);
        setTelemetry(R.id.telemetry32, TelemetryEnum.CADENCE);
        setTelemetry(R.id.telemetry33, TelemetryEnum.POWER_30S);
        setTelemetry(R.id.telemetry34, TelemetryEnum.LAP);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.train_toolbar);
        setSupportActionBar(myToolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = this.getIntent();
            String key = intent.getStringExtra("workout_key");
            WorkoutService.getWorkoutData(key, new IGetAsyncListener<WorkoutData>() {
                @Override
                public void onDataGet(boolean success, WorkoutData data) {
                    TrainActivity.this.s = new TrainState(data, 5);
                    TrainController controller = new TrainController(data);
                    TrainApplication.setTrainController(controller);

                    trainWorkoutView.setState(s);
                    TrainActivity.this.getSupportActionBar().setTitle(s.workout.name);

                    getController().clearListeners();
                    getController().addListener(TrainActivity.this);
                    getController().connect();
                }
            });

            /*if (record == null) {
                this.s = new TrainState(workout, 5);
                controller = new TrainController(workout);
            }
            else {
                FileController fileController =  new FileController(record, workout, 5);
                this.s = fileController.resetState();
                controller = new TrainController(workout, fileController.getFile(), s.now, (int) s.lap, s.distance);
            }*/

        } else {
            this.s = (TrainState) savedInstanceState.getSerializable("state");
            trainWorkoutView.setState(s);
            TrainController controller =  getController();
            controller.clearListeners();
            controller.addListener(this);
            if (controller == null) {
                throw new RuntimeException("Application Error.");
            } else {
                if (controller.getWorkout().key != s.workout.key) {
                    throw new RuntimeException("Application Error.");
                }
            }
        }


    }

    @Override
    protected void onDestroy() {
        if (toneG != null)
            toneG.release();

        super.onDestroy();
    }

    private void setTelemetry(Integer id, TelemetryEnum type) {
        TelemetryView telemetry = (TelemetryView) findViewById(id);
        telemetry.setType(type);
        telemetry.setOnClickListener(this);
        telemetryMap.put(id, type);
    }

    public static Set<Integer> getKeysByValue(HashMap<Integer, TelemetryEnum> map, TelemetryEnum value) {
        Set<Integer> keys = new HashSet<>();
        for (Map.Entry<Integer, TelemetryEnum> entry : map.entrySet()) {
            if (value == entry.getValue()) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    private void setTelemetryData(TelemetryEnum type, String data) {
        for (Integer key : getKeysByValue(telemetryMap, type)) {
            ((TelemetryView) findViewById(key)).setValue(data);
        }
    }

    private void setTelemetryColor(TelemetryEnum type, int color) {
        for (Integer key : getKeysByValue(telemetryMap, type)) {
            ((TelemetryView) findViewById(key)).setBackgroundColor(color);
        }
    }

    @Override
    public void onClick(final View view) {
        if (!(view instanceof TelemetryView))
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_train_dialog, null);
        final Spinner type = (Spinner) dialogView.findViewById(R.id.spinnerWidget);
        builder.setView(dialogView)
                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setTelemetry(view.getId(), TelemetryEnum.valueOf(type.getSelectedItem().toString()));
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setTitle("Telemetry");
        builder.create();
        builder.show();
        //toastMessage(String.valueOf(view.getId()));
    }

    public TrainController getController() {
        return TrainApplication.getTrainController();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("state", s);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (getController().recordFile != null) {
            checkCancelWorkout();
        } else
            finish();
    }

    @Override
    public void telemetryPause() {
        s.recording = false;
    }

    @Override
    public void telemetryResume() {
        s.recording = true;
    }

    @Override
    public void telemetryStart(WorkoutData workout, File recordFile, long startTime) {
        s.clear();
        s.recording = true;
        s.cancelCalled = false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trainWorkoutView.invalidate();
            }
        });

        s.workoutStart(recordFile, startTime);
    }

    @Override
    public void telemetryStop() {
        s.recording = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trainWorkoutView.invalidate();
            }
        });
    }

    @Override
    public void newText(long now, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTrainWorkout.setText(text);
            }
        });
    }

    @Override
    public void telemetryUpdate(final RealtimeData rt) {
        final String powerText = String.valueOf((int) rt.getWatts());
        final String heartText = String.valueOf((int) rt.getHr());
        final String cadenceText = String.valueOf((int) rt.getCadence());
        final String speedText = String.valueOf(String.format("%.2f", rt.getSpeed()));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTelemetryData(TelemetryEnum.POWER, powerText);
                setTelemetryData(TelemetryEnum.CADENCE, cadenceText);
                setTelemetryData(TelemetryEnum.HR, heartText);
                setTelemetryData(TelemetryEnum.SPEED, speedText);
            }
        });

        if (rt.getDeviceStatus() != null && !rt.getDeviceStatus().isEmpty())
            toastMessage(rt.getDeviceStatus());

        if (!s.recording) return;

        s.updateRealtimeData(rt);

        final double load = rt.getLoad();
        if (load == -100) return;

        // BEEP
        long seconds = (rt.getLapMsecsRemaining() / 1000);
        if (seconds <= 5 && seconds != s.lapBeep) {
            s.lapBeep = (int) seconds;
            if (toneG == null)
                toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        }

        final String totalTIme = TrainApplication.formatTime(rt.getMsecs());
        final String lapRemaining = TrainApplication.formatTime(rt.getLapMsecsRemaining());

        final String loadText = String.valueOf((int) load);
        final String kmText = String.format("%.2f", s.distance);
        final String kjText = String.format("%.1f", s.getMainLap().getKj());

        final String avgWattsText = String.valueOf((int) s.getMainLap().getWatts());
        final String avgSpeedText = String.valueOf((int) s.getMainLap().getSpeed());
        final String avgCadenceText = String.valueOf((int) s.getMainLap().getCadence());
        final String avgHrText = String.valueOf((int) s.getMainLap().getHr());

        final String maxWattsText = String.valueOf((int) s.getMainLap().getWattsMax());
        final String maxSpeedText = String.valueOf((int) s.getMainLap().getSpeedMax());
        final String maxCadenceText = String.valueOf((int) s.getMainLap().getCadenceMax());
        final String maxHrText = String.valueOf((int) s.getMainLap().getHrMax());

        final String lapText = String.valueOf(rt.getLap());
        final String avgWattsLapText = String.valueOf((int) s.getCurrentLap().getWatts());
        final String avgSpeedLapText = String.valueOf((int) s.getCurrentLap().getSpeed());
        final String avgCadenceLapText = String.valueOf((int) s.getCurrentLap().getCadence());
        final String avgHrLapText = String.valueOf((int) s.getCurrentLap().getHr());

        final String tssText = String.format("%.1f", s.getMainLap().getNp().getTss());
        final String npText = String.valueOf((int) s.getMainLap().getNp().getNp());
        final String intensityText = String.format("%.2f", s.getMainLap().getNp().getIntensityFactor());

        final String kmLapText = String.format("%.2f", s.getCurrentLap().getDistance());
        final String kjLapText = String.format("%.1f", s.getCurrentLap().getKj());

        final String avg30WattsText = String.valueOf((int) s.watts30.getAverage());

        final String tssLapText = String.format("%.1f", s.getCurrentLap().getNp().getTss());
        final String npLapText = String.valueOf((int) s.getCurrentLap().getNp().getNp());
        final String intensityLapText = String.format("%.2f", s.getCurrentLap().getNp().getIntensityFactor());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTrainTotalTime.setText(totalTIme);
                tvTrainIntervalTime.setText(lapRemaining);
                setTelemetryData(TelemetryEnum.TARGET, loadText);
                setTelemetryData(TelemetryEnum.KM, kmText);
                setTelemetryData(TelemetryEnum.KJ, kjText);

                setTelemetryData(TelemetryEnum.AVG_POWER, avgWattsText);
                setTelemetryData(TelemetryEnum.AVG_SPEED, avgSpeedText);
                setTelemetryData(TelemetryEnum.AVG_CAD, avgCadenceText);
                setTelemetryData(TelemetryEnum.AVG_HR, avgHrText);

                setTelemetryData(TelemetryEnum.MAX_POWER, maxWattsText);
                setTelemetryData(TelemetryEnum.MAX_SPEED, maxSpeedText);
                setTelemetryData(TelemetryEnum.MAX_CAD, maxCadenceText);
                setTelemetryData(TelemetryEnum.MAX_HR, maxHrText);

                setTelemetryData(TelemetryEnum.LAP, lapText);
                setTelemetryData(TelemetryEnum.LP_POWER, avgWattsLapText);
                setTelemetryData(TelemetryEnum.LP_SPEED, avgSpeedLapText);
                setTelemetryData(TelemetryEnum.LP_CAD, avgCadenceLapText);
                setTelemetryData(TelemetryEnum.LP_HR, avgHrLapText);

                setTelemetryData(TelemetryEnum.LP_NP, npLapText);
                setTelemetryData(TelemetryEnum.LP_TSS, tssLapText);
                setTelemetryData(TelemetryEnum.LP_IF, intensityLapText);
                setTelemetryData(TelemetryEnum.LP_KM, kmLapText);
                setTelemetryData(TelemetryEnum.LP_KJ, kjLapText);

                setTelemetryData(TelemetryEnum.POWER_30S, avg30WattsText);

                setTelemetryData(TelemetryEnum.NP, npText);
                setTelemetryData(TelemetryEnum.TSS, tssText);
                setTelemetryData(TelemetryEnum.IF, intensityText);

                setTelemetryColor(TelemetryEnum.POWER, Color.TRANSPARENT);
                setTelemetryColor(TelemetryEnum.POWER_30S, Color.TRANSPARENT);
                if (load > 0) {
                    if (rt.getWatts() < (load * 0.95) || rt.getWatts() > (load * 1.05)) {
                        int color = rt.getWatts() < load ? Color.BLUE : Color.RED;
                        setTelemetryColor(TelemetryEnum.POWER, color);
                    }
                    if (s.watts30.getAverage() < (load * 0.95) || s.watts30.getAverage() > (load * 1.05)) {
                        int color = s.watts30.getAverage() < load ? Color.BLUE : Color.RED;
                        setTelemetryColor(TelemetryEnum.POWER_30S, color);
                    }
                }
            }
        });

        if (s.hertz == 0)
            trainWorkoutView.postInvalidate();
    }

    @Override
    public void lapComplete(long now, int lapNum) {
        toastMessage("Lap Complete: " + String.valueOf(lapNum));
    }

    private void checkCancelWorkout() {
        s.cancelCalled = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder abuilder = new AlertDialog.Builder(TrainActivity.this);
                abuilder.setMessage("Stop/share workout?");
                abuilder.setNegativeButton("Stop", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        getController().stop();
                        getController().disconnect();
                        finish();
                    }
                });
                abuilder.setPositiveButton("Stop and Share", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        getController().stop();
                        getController().disconnect();
                        new TrainFile(getController().recordFile).shareFile(TrainActivity.this);
                        finish();
                    }
                });
                abuilder.setNeutralButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alert = abuilder.create();
                alert.show();
                TrainActivity.this.invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void workoutComplete(WorkoutData workout, File recordFile, long startTime, long totalTicks) {
        s.recording = false;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        trainWorkoutView.getCachedBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

        ByteArrayOutputStream streamMini = new ByteArrayOutputStream();
        Bitmap.createScaledBitmap(trainWorkoutView.getCachedBitmap(), 100, 66, true).compress(Bitmap.CompressFormat.PNG, 100, streamMini);

        s.workoutComplete(recordFile, totalTicks,
                stream.toByteArray(), streamMini.toByteArray());

        if (!s.cancelCalled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder abuilder = new AlertDialog.Builder(TrainActivity.this);
                    abuilder.setMessage("Share workout?");
                    abuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            getController().disconnect();
                            new TrainFile(getController().recordFile).shareFile(TrainActivity.this);
                            finish();
                        }
                    });
                    abuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getController().disconnect();

                            finish();
                        }
                    });
                    AlertDialog alert = abuilder.create();
                    alert.show();
                    TrainActivity.this.invalidateOptionsMenu();
                }
            });
        }


    }

    @Override
    public void setNow(long now) {
        s.now = now;
    }

    private void setToolbarMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.tbTrainPlay);
        setToolbarMenu(item);

    }

    private void setToolbarMenu(MenuItem item) {
        item.setIcon(android.R.drawable.ic_media_play);
        if (s.recording && !getController().isPaused) {
            item.setIcon(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_train, menu);
        setToolbarMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        else if (id == R.id.tbTrainPlay) {
            if (!s.recording && !getController().isPaused) {
                toastMessage("Start");
                getController().start();
            } else if (getController().isPaused) {
                toastMessage("Resume");
                getController().start();
            } else {
                AlertDialog.Builder abuilder = new AlertDialog.Builder(this);
                abuilder.setMessage("Pause workout?");
                abuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        toastMessage("Pause");
                        getController().start();
                        setToolbarMenu(item);
                    }
                });
                abuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alert = abuilder.create();
                alert.show();
            }
            setToolbarMenu(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void toastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, message, duration);
                toast.show();
            }
        });
    }
}

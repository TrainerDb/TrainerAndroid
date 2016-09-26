package com.trainerdb.trainerandroid.train;

import com.opencsv.CSVWriter;
import com.trainerdb.trainerandroid.TrainApplication;
import com.trainerdb.trainerandroid.data.WorkoutData;
import com.trainerdb.trainerandroid.listeners.AntPlusController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TrainController {
    public class Status {
        public static final int RT_RUNNING = 0x0100;        // is running now
        public static final int RT_PAUSED = 0x0200;        // is paused
        //public static final int RT_RECORDING = 0x0400;        // is recording to disk
        //public static final int RT_WORKOUT = 0x0800;        // is running a workout
        //public static final int RT_STREAMING = 0x1000;        // is streaming to a remote peer
        public static final int RT_CONNECTED = 0x2000;        // is connected to devices
        public static final int RT_RESTART = 0x4000;        // is connected to devices
    }

    private List<TelemetryListener> listeners = new ArrayList<TelemetryListener>();

    private final int REFRESHRATE = 200;
    private final int SAMPLERATE = 1000;
    private final int LOADRATE = 1000;

    private List<DeviceConfiguration> devices;
    private List<DeviceConfiguration> activeDevices;
    private StopWatch session_time, lap_time, load_period;
    private Timer gui_timer;
    private Timer load_timer;
    private Timer disk_timer;

    private WorkoutData workout;

    public boolean isRunning = false, isPaused = false;

    public int status = 0;
    private double displayPower, displayHeartRate, displayCadence, displaySpeed;
    //double displaySMO2, displayTHB, displayO2HB, displayHHB;
    double displayDistance, displayWorkoutDistance;

    int displayLap;            // user increment for Lap
    int displayWorkoutLap;     // which Lap in the workout are we at?
    long total_msecs, lap_msecs, load_msecs;
    long session_elapsed_msec, lap_elapsed_msec;
    String displayText;

    long load;

    CSVWriter csvWriter;
    public File recordFile;

    private long startTime;

    public TrainController(WorkoutData workout, File file, long now, int curLap, double distance) {
        this(workout);

        try {
            recordFile = file;
            csvWriter = new CSVWriter(new FileWriter(recordFile, true), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        load_msecs = session_elapsed_msec = now + 1;
        displayWorkoutLap = curLap;
        displayDistance = distance;
        setStatusFlags(Status.RT_RESTART);
    }

    public TrainController(WorkoutData workout) {
        displayWorkoutLap = displayLap = 0;
        //wbalr = wbal = 0;
        load_msecs = total_msecs = lap_msecs = 0;
        displayWorkoutDistance = displayDistance = displayPower = displayHeartRate =
                displaySpeed = displayCadence = 0;
        //displaySMO2 = displayTHB = displayO2HB = displayHHB = 0;
        //displayLRBalance = displayLTE = displayRTE = displayLPS = displayRPS = 0;
        displayText = "";

        this.workout = workout;

        session_time = new StopWatch();
        lap_time = new StopWatch();
        load_period = new StopWatch();
    }

    public WorkoutData getWorkout() {
        return workout;
    }

    public void configChanged() {
        devices = DeviceConfigurations.getList();

        for (DeviceConfiguration device : devices) {
            switch (device.getTypeEnum()) {
                case DEV_NULL:
                    device.setController(new NullController(device));
                    break;
                case DEV_ANTLOCAL:
                    device.setController(new AntPlusController(device));
                    break;
            }
        }
    }

    public void start() {
        // Unpause
        if ((status & Status.RT_PAUSED) == Status.RT_PAUSED) {
            session_time.start();
            lap_time.start();
            load_period.start();

            clearStatusFlags(Status.RT_PAUSED);

            //if ((status & Status.RT_RECORDING) == Status.RT_RECORDING)
            disk_timer = new Timer();
            disk_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    diskUpdate();
                }
            }, 0, SAMPLERATE);

            //if ((status & Status.RT_WORKOUT) == Status.RT_WORKOUT)
            load_timer = new Timer();
            load_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    loadUpdate();
                }
            }, 0, LOADRATE);

            notifyResume();
        }
        // Pause
        else if ((status & Status.RT_RUNNING) == Status.RT_RUNNING) {
            session_elapsed_msec += session_time.elapsed();
            lap_elapsed_msec += lap_time.elapsed();
            setStatusFlags(Status.RT_PAUSED);

            load_msecs += load_period.restart();

            //if ((status & Status.RT_RECORDING) == Status.RT_RECORDING)
            disk_timer.cancel();
            disk_timer.purge();
            //if ((status & Status.RT_WORKOUT) == Status.RT_WORKOUT)
            load_timer.cancel();
            load_timer.purge();

            notifyPause();

        }
        else if((status & Status.RT_RESTART) == Status.RT_RESTART) {
            clearStatusFlags(Status.RT_RESTART);
            setStatusFlags(Status.RT_RUNNING);

            notifyResume();

            load_period.start();
            session_time.start();
            lap_time.start();

            disk_timer = new Timer();
            disk_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    diskUpdate();
                }
            }, 0, SAMPLERATE);

            load_timer = new Timer();
            load_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    loadUpdate();
                }
            }, 0, LOADRATE);   // start recording
        }
        else if ((status & Status.RT_CONNECTED) == Status.RT_CONNECTED) {
            load = 100;
            setStatusFlags(Status.RT_RUNNING);

            load_period.start();
            session_time.start();
            session_elapsed_msec = 0;
            lap_time.start();
            lap_elapsed_msec = 0;

            //if ((status & Status.RT_WORKOUT) == Status.RT_WORKOUT)
            load_timer = new Timer();
            load_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    loadUpdate();
                }
            }, 0, LOADRATE);   // start recording

            //setStatusFlags(Status.RT_RECORDING);

            Calendar now = Calendar.getInstance();
            startTime = now.getTime().getTime();

            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            String fileName = format.format(now.getTime()) + ".csv";

            recordFile = new File(TrainApplication.getAppContext().getExternalFilesDir(null), fileName);
            try {
                csvWriter = new CSVWriter(new FileWriter(recordFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String[] header = "secs, cad, hr, km, kph, nm, watts, alt, lon, lat, headwind, slope, temp, interval, lrbalance, lte, rte, lps, rps, smo2, thb, o2hb, hhb".split(",");
            csvWriter.writeNext(header);
            disk_timer = new Timer();
            disk_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    diskUpdate();
                }
            }, 0, SAMPLERATE);

            //gui_timer.start()
            notifyStart(workout, recordFile, startTime);
        }
    }

    public void connect() {
        if ((status & Status.RT_CONNECTED) == Status.RT_CONNECTED) return;

        configChanged();
        activeDevices = devices;

        for (DeviceConfiguration device : activeDevices)
            device.getController().start();

        status |= Status.RT_CONNECTED;
        gui_timer = new Timer();
        gui_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                guiUpdate();
            }
        }, 0, REFRESHRATE);
    }

    public void disconnect() {
        // don't try to disconnect if running or not connected
        if ((status & Status.RT_RUNNING) == Status.RT_RUNNING || ((status & Status.RT_CONNECTED) == 0))
            return;

        for (DeviceConfiguration dev : activeDevices)
            dev.getController().stop();

        clearStatusFlags(Status.RT_CONNECTED);

        gui_timer.cancel();
        gui_timer.purge();
    }

    public void stop() {
        if ((status & Status.RT_RUNNING) == 0) return;
        clearStatusFlags(Status.RT_RUNNING | Status.RT_PAUSED);

        // if ((status & Status.RT_RECORDING) == Status.RT_RECORDING) {
        disk_timer.cancel();
        disk_timer.purge();

        // close and reset File
        try {
            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        load_timer.cancel();
        load_timer.purge();

        load_msecs = 0;
        notifySetNow(load_msecs);
        notifyWorkoutComplete(workout, recordFile, startTime, session_elapsed_msec + session_time.elapsed());

        displayWorkoutLap = displayLap = 0;
        displayText = "";
        //wbalr = 0;
        //wbal = WPRIME;
        session_elapsed_msec = 0;
        session_time.restart();
        lap_elapsed_msec = 0;
        lap_time.restart();
        displayWorkoutDistance = displayDistance = 0;
        guiUpdate();

        notifyStop();
        return;
    }

    private void diskUpdate() {

        long secs;

        long torq = 0, altitude = 0;
        //QTextStream recordFileStream(recordFile);

        // convert from milliseconds to secondes
        total_msecs = session_elapsed_msec + session_time.elapsed();
        secs = total_msecs;
        secs /= 1000.0;

        String[] values = new String[]{String.valueOf(secs),
                String.valueOf((int)displayCadence), String.valueOf((int)displayHeartRate),
                String.format(Locale.ROOT, "%.2f", displayDistance),
                String.format(Locale.ROOT, "%.2f", displaySpeed),
                "", String.format(Locale.ROOT, "%.2f", displayPower),
                "", "", "", "", "", "",
                String.valueOf(displayLap + displayWorkoutLap),
                "", "", "", "", "", "", "", "", "",
        };
        csvWriter.writeNext(values);

    }

    private void loadUpdate() {
        load_msecs += load_period.restart();
        load = (int) workout.getErg().wattsAt(load_msecs);
        int curLap = workout.getErg().lapAt(load_msecs);

        if (displayWorkoutLap != curLap) {
            notifyLapComplete(load_msecs, displayWorkoutLap);
        }
        displayWorkoutLap = curLap;

        String text = workout.getErg().textAt(load_msecs);
        if (!displayText.equals(text)) {
            notifyNewText(load_msecs, text);
        }
        displayText = text;


        if (load <= 0) {
            this.stop();
        } else {
            for (DeviceConfiguration dev : activeDevices)
                dev.getController().setLoad(load);
            notifySetNow(load_msecs);
        }
    }

    private void guiUpdate() {
        RealtimeData rtData = new RealtimeData();
        rtData.setLap(displayLap + displayWorkoutLap); // user laps + predefined workout lap
        //rtData.mode = mode; //ERG

        if ((status & Status.RT_RUNNING) == Status.RT_RUNNING || (status & Status.RT_CONNECTED) == Status.RT_CONNECTED) {
            for (DeviceConfiguration device : activeDevices) {
                RealtimeData local = new RealtimeData();
                device.getController().getRealtimeData(local);

                /*if (Devices[dev].type == DEV_ANTLOCAL || Devices[dev].type == DEV_NULL) {
                    rtData.setHb(local.getSmO2(), local.gettHb()); //only moxy data from ant and robot devices right now
                }*/
                rtData.setLoad(local.getLoad());
                rtData.setHr(local.getHr());
                rtData.setCadence(local.getCadence());
                rtData.setSpeed(local.getSpeed());
                rtData.setDistance(local.getDistance());
                rtData.setWatts(local.getWatts());
                rtData.setDeviceStatus(local.getDeviceStatus());
            }
        }
        if ((status & Status.RT_RUNNING) == Status.RT_RUNNING && (status & Status.RT_PAUSED) == 0) {
            displayDistance += displaySpeed / (5 * 3600); // assumes 200ms refreshrate
            rtData.setDistance(displayDistance);

            // time
            total_msecs = session_elapsed_msec + session_time.elapsed();
            lap_msecs = lap_elapsed_msec + lap_time.elapsed();

            rtData.setMsecs(total_msecs);
            rtData.setLapMsecs(lap_msecs);

            long lapTimeRemaining = workout.getErg().nextLapTime(load_msecs) - load_msecs;

            if (lapTimeRemaining < 0) {
                lapTimeRemaining = workout.getErg().getDuration() - load_msecs;
                if (lapTimeRemaining < 0)
                    lapTimeRemaining = 0;
            }
            rtData.setLapMsecsRemaining(lapTimeRemaining);
        } else {
            rtData.setDistance(displayDistance);
            rtData.setMsecs(session_elapsed_msec);
            rtData.setLapMsecs(lap_elapsed_msec);
        }

        displayPower = rtData.getWatts();
        displayCadence = rtData.getCadence();
        displayHeartRate = rtData.getHr();
        displaySpeed = rtData.getSpeed();

        notifyTelemetryUpdate(rtData);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void addListener(TelemetryListener toAdd) {
        listeners.add(toAdd);
    }

    private void notifyTelemetryUpdate(RealtimeData rtData) {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.telemetryUpdate(rtData);
    }

    private void notifyLapComplete(long now, int lapNum) {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.lapComplete(now, lapNum);
    }

    private void notifyNewText(long now, String text) {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.newText(now, text);
    }
    private void notifySetNow(long now) {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.setNow(now);
    }

    private void notifyWorkoutComplete(WorkoutData workout, File recordFile, long startTime, long totalTicks) {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.workoutComplete(workout, recordFile, startTime, totalTicks);
    }

    private void notifyStart(WorkoutData workout, File recordFile, long startTime) {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.telemetryStart(workout, recordFile, startTime);
    }

    private void notifyStop() {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.telemetryStop();
    }

    private void notifyPause() {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.telemetryPause();
    }

    private void notifyResume() {
        for (TelemetryListener hl : listeners)
            if (hl != null)
                hl.telemetryResume();
    }

    private void setStatusFlags(int flags) {
        status |= flags;
        this.isRunning = ((status & Status.RT_RUNNING) == Status.RT_RUNNING);
        this.isPaused = ((status & Status.RT_PAUSED) == Status.RT_PAUSED);
    }

    private void clearStatusFlags(int flags) {
        status &= ~flags;
        this.isRunning = ((status & Status.RT_RUNNING) == Status.RT_RUNNING);
        this.isPaused = ((status & Status.RT_PAUSED) == Status.RT_PAUSED);
    }
}

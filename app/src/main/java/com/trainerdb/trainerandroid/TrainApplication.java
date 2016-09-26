package com.trainerdb.trainerandroid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.trainerdb.trainerandroid.data.PowerZone;
import com.trainerdb.trainerandroid.train.DeviceConfiguration;
import com.trainerdb.trainerandroid.train.TrainController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dcotrim on 21/09/2016.
 */
public class TrainApplication extends Application {
    private static Context context;
    private static TrainController trainController;
    private static SharedPreferences preferences;

    public static String formatTime(long milliSeconds) {
        long second = (milliSeconds / 1000) % 60;
        long minute = (milliSeconds / (1000 * 60)) % 60;
        long hour = (milliSeconds / (1000 * 60 * 60)) % 24;
        if (hour > 0)
            return String.format("%d:%02d:%02d", hour, minute, second);
        else
            return String.format("%02d:%02d", minute, second);

    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getFTP() {
        return Integer.parseInt(preferences.getString("ftp", "200"));
    }

    public static Context getAppContext() {
        return TrainApplication.context;
    }

    public static int getVirtualPowerId() {
        return  Integer.parseInt(preferences.getString("virtual", "0"));
    }
    public static DeviceConfiguration.DeviceType getDeviceType() {
        return DeviceConfiguration.DeviceType.valueOf(preferences.getString("device", "DEV_ANTLOCAL"));
    }
    public static void setTrainController(TrainController newTrainController) {
        if (trainController != null) {
            trainController.stop();
            trainController.disconnect();
        }
        trainController = newTrainController;
    }

    public static TrainController getTrainController() {
        return trainController;
    }

    public static boolean isFavorite(String key) {
        Set<String> values = preferences.getStringSet("workout_favorite", null);
        if (values != null && values.contains(key))
            return true;
        return false;
    }

    public static void addFavorite(String key) {
        Set<String> def = new HashSet<>();
        Set<String> values = new HashSet<>(preferences.getStringSet("workout_favorite", def));
        if (!values.contains(key)) {
            values.add(key);
            preferences.edit().putStringSet("workout_favorite", values).apply();
        }
    }

    public static void removeFavorite(String key) {
        Set<String> def = new HashSet<>();
        Set<String> values = new HashSet<>(preferences.getStringSet("workout_favorite", def));
        if (values.contains(key)) {
            values.remove(key);
            preferences.edit().putStringSet("workout_favorite", values).apply();
        }
    }

    private static double calcPerimeter(double rim, double tire) {
        if (rim > 0 && tire > 0) {
            return Math.round((rim + tire) * Math.PI);
        }
        return 0;
    }

    public static double getWheelCircunference() {
        double rim = Double.parseDouble(preferences.getString("rim", "622"));
        double tire = Double.parseDouble(preferences.getString("tire", "46"));
        return calcPerimeter(rim, tire);
    }

    public static List<PowerZone> getPowerZones() {
        int ftp = getFTP();
        List<PowerZone> powerZones = new ArrayList<>();
        powerZones.add(0, new PowerZone("Active Recovery", 0, (int) (0.55 * ftp)));
        powerZones.add(1, new PowerZone("Endurance", (int) (0.55 * ftp), (int) (0.75 * ftp)));
        powerZones.add(2, new PowerZone("Tempo", (int) (0.75 * ftp), (int) (0.90 * ftp)));
        powerZones.add(3, new PowerZone("Lactate Threshold", (int) (0.90 * ftp), (int) (1.05 * ftp)));
        powerZones.add(4, new PowerZone("VO2 Max", (int) (1.05 * ftp), (int) (1.2 * ftp)));
        powerZones.add(5, new PowerZone("Anaerobic Capacity", (int) (1.2 * ftp), (int) (1.5 * ftp)));
        powerZones.add(6, new PowerZone("Neuromuscular Power", (int) (1.5 * ftp), Integer.MAX_VALUE));
        return powerZones;
    }
}

package com.trainerdb.trainerandroid.data;

import android.graphics.PointF;

import com.trainerdb.trainerandroid.TrainApplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dcotrim on 22/06/2016.
 */
public class ErgFile implements Serializable {
    private List<ErgPoint> coursePoints;
    private List<TextPoint> textPoints;
    private List<LapPoint> lapPoints;
    private long duration = 0;

    public long getDuration() {
        return duration;
    }

    public List<ErgPoint> getCoursePoints() {
        return coursePoints;
    }

    public List<LapPoint> getLapPoints() {
        return lapPoints;
    }

    public List<TextPoint> getTextPoints() {
        return textPoints;
    }

    public ErgFile(List<ErgPoint> points, List<LapPoint> laps, List<TextPoint> texts) {
        //List<String> lines = new ArrayList<String>();
        coursePoints = (points == null ? new ArrayList<ErgPoint>() : points);
        textPoints = (texts == null ? new ArrayList<TextPoint>() : texts);;
        lapPoints = (laps == null ? new ArrayList<LapPoint>() : laps);;

        duration = (long) coursePoints.get(coursePoints.size() - 1).t;

        int ftp = TrainApplication.getFTP();
        for (ErgPoint point: coursePoints) {
            point.w = (int) ((point.w / 100) * ftp);
        }

        Boolean newLapWorkout = false;
        if (this.lapPoints.size() == 0)
            newLapWorkout = true;
        else {
            LapPoint first = this.lapPoints.get(0);
            if (first.x != 0 || first.y != duration)
                newLapWorkout = true;
        }
        if (newLapWorkout)
            newLap(0, duration, 0, "Workout");
    }

    private void newLap(long x, long y, int num, String name) {
        LapPoint newLap = new LapPoint();
        newLap.x = x;
        newLap.y = y;
        newLap.name = name;
        newLap.lapNum = num;
        lapPoints.add(newLap);
    }

    public int lapAt(long x) {
        if (x < 0 || x > duration) return -100;   // out of bounds!!!

        int lapNum = 0;
        long temp = 0;
        if (lapPoints.size() > 0) {
            for (int i = 0; i < lapPoints.size(); i++) {
                LapPoint lap = lapPoints.get(i);
                if (x >= lap.x && x <= lap.y) {
                    if (lap.x > temp) {
                        temp = lap.x;
                        lapNum = lap.lapNum;
                    }
                }
            }
        }
        return lapNum;
    }

    public String textAt(long x) {
        String text = "";
        if (x < 0 || x > duration) return "-1";   // out of bounds!!!

        long groupStart = -1;
        long start = 0;
        if (textPoints.size() > 0) {
            for (int i = 0; i < textPoints.size(); i++) {
                TextPoint tPoint = textPoints.get(i);
                if (groupStart != tPoint.x) {
                    groupStart = tPoint.x;
                    start = groupStart;
                } else
                    start += tPoint.duration;

                if (x < start) continue;
                if (x > (start + (tPoint.duration))) continue;

                return tPoint.text;// + TrainApplication.formatTime(tPoint.duration + start - x);
            }
        }
        return text;
    }

    public double wattsAt(long x) {
        if (x < 0 || x > duration) return -100;

        int rightPoint, leftPoint;
        leftPoint = 0;
        rightPoint = 1;

        while (x < coursePoints.get(leftPoint).t || x > coursePoints.get(rightPoint).t) {
            if (x < coursePoints.get(leftPoint).t) {
                leftPoint--;
                rightPoint--;
            } else if (x > coursePoints.get(rightPoint).t) {
                leftPoint++;
                rightPoint++;
            }
        }

        if (coursePoints.get(leftPoint).w == coursePoints.get(rightPoint).w)
            return coursePoints.get(rightPoint).w;

        double deltaW = coursePoints.get(rightPoint).w - coursePoints.get(leftPoint).w;
        double deltaT = coursePoints.get(rightPoint).t - coursePoints.get(leftPoint).t;
        double offT = x - coursePoints.get(leftPoint).t;
        double factor = offT / deltaT;

        double nowW = coursePoints.get(leftPoint).w + (deltaW * factor);

        return nowW;
    }

    public long nextLapTime(long x) {
        //if (!isValid()) return -1; // not a valid ergfile

        long next = duration;
        long temp = 0;
        // do we need to return the Lap marker?
        if (lapPoints.size() > 0) {
            for (int i = 0; i < lapPoints.size(); i++) {
                LapPoint lap = lapPoints.get(i);
                if (x >= lap.x && x <= lap.y) {
                    if (lap.x > temp) {
                        temp = lap.x;
                        next = lap.y;
                    }
                }
                if (x < lap.x && lap.x < next) next = lap.x;
                if (x < lap.y && lap.y < next) next = lap.y;
            }
        }
        return next; // nope, no marker ahead of there
    }
}

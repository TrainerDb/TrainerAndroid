package com.trainerdb.trainerandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.trainerdb.trainerandroid.data.ErgPoint;
import com.trainerdb.trainerandroid.data.PowerZone;
import com.trainerdb.trainerandroid.data.Workout;
import com.trainerdb.trainerandroid.data.WorkoutData;
import com.trainerdb.trainerandroid.data.WorkoutService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dcotrim on 16/06/2016.
 */
public class WorkoutView extends View {
    protected List<ErgPoint> ergs = null;
    protected float maxVx = 0;
    protected float minVx = 0;
    protected float maxY = 0;
    protected float minY = 0;
    protected WorkoutData workout;

    protected boolean initialDrawingIsPerformed = false;

    protected float IHEIGHT = 0;
    protected float THEIGHT = 0;
    protected float BHEIGHT = 0;
    protected float LWIDTH = 0;
    protected float RWIDTH = 0;
    protected float XTICLENGTH = 10;
    protected float YTICLENGTH = 0;
    protected float XTICS = 10;
    protected float YTICS = 10;
    protected float SPACING = 2; // between labels and tics (if there are tics)
    protected float XMOVE = 5; // how many to move X when cursoring
    protected float YMOVE = 1; // how many to move Y when cursoring
    protected float POWERSCALEWIDTH = 0;
    protected boolean GRIDLINES = true;
    //LOG = false;

    protected int ftp;

    public WorkoutView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (this.isInEditMode()) {
            //region foi layout preview
            ergs = new ArrayList<ErgPoint>();
            ergs.add(new ErgPoint(0, 50));
            ergs.add(new ErgPoint(50, 65));
            ergs.add(new ErgPoint(50, 95));
            ergs.add(new ErgPoint(80, 95));
            ergs.add(new ErgPoint(80, 50));
            ergs.add(new ErgPoint(150, 50));
            ergs.add(new ErgPoint(150, 95));
            ergs.add(new ErgPoint(250, 95));
            ergs.add(new ErgPoint(250, 40));
            ergs.add(new ErgPoint(300, 40));
            ergs.add(new ErgPoint(300, 98));
            ergs.add(new ErgPoint(400, 98));
            ergs.add(new ErgPoint(400, 40));
            ergs.add(new ErgPoint(450, 40));
            ergs.add(new ErgPoint(450, 96));
            ergs.add(new ErgPoint(550, 96));
            ergs.add(new ErgPoint(550, 40));
            ergs.add(new ErgPoint(600, 30));
            //endregion

            ftp = 210;
            recompute();
        } else {
            ftp = TrainApplication.getFTP();
        }

    }

    public void setWorkout(Workout workout) {
        WorkoutService.getWorkoutData(workout.key, new IGetAsyncListener<WorkoutData>() {
            @Override
            public void onDataGet(boolean success, WorkoutData data) {
                WorkoutView.this.workout = data;
                WorkoutView.this.ergs = data.getErg().getCoursePoints();

                recompute();
                postInvalidate();
            }
        });
    }

    protected void recompute() {
        float maxYTemp = 0;
        maxVx = 0;
        maxY = this.ftp * 1.2f;

        for (ErgPoint erg : ergs) {
            if (erg.t > maxVx) maxVx = (float)erg.t;
            if (erg.w > maxYTemp) maxYTemp =(float) erg.w;
        }

        if (maxY <= maxYTemp) maxY = maxYTemp * 1.2f;
        //if (maxY > (maxYTemp * 2) && maxY >= 400) maxY = maxYTemp * 1.5F;
        //if (maxY <maxYTemp) maxY = maxYTemp * 1.5f;
    }

    protected PointF transform(float seconds, float watts) {
        float xratio = canvas().width() / (maxVx - minVx);
        float yratio = canvas().height() / (maxY - minY);

        return new PointF(canvas().left - (minVx * xratio) + (seconds * xratio), canvas().bottom - (watts * yratio));
    }


    private int zoneColor(int zone) {
        switch (zone) {
            case 0:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE1);
            case 1:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE2);
            case 2:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE3);
            case 3:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE4);
            case 4:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE5);
            case 5:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE6);
            case 6:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE7);
            case 7:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE8);
            case 8:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE9);
            case 9:
                return ContextCompat.getColor(this.getContext(), R.color.COLORZONE10);
            default:
                return Color.argb(128, 128, 128, 128);
        }
    }

    private void paintPowerScale(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        float FTPy = transform(0, ftp).y;

        canvas.drawLine(canvas().left, FTPy, canvas().right, FTPy, paint);

        if (!isInEditMode() && POWERSCALEWIDTH > 0) {
           int numZones = TrainApplication.getPowerZones().size();
            for (int i = 0; i < numZones; i++) {
                PowerZone zone = TrainApplication.getPowerZones().get(i);

                float ylow = transform(0, zone.getLow()).y;
                float yhigh = transform(0, zone.getHigh()).y;

                if (yhigh < canvas().top)
                    yhigh = canvas().top;

                float ymid = (ylow + yhigh) / 2;

                RectF bound = new RectF(left().right - POWERSCALEWIDTH, yhigh, left().right, ylow);

                Paint pZone = new Paint();
                pZone.setColor(zoneColor(i));
                pZone.setStyle(Paint.Style.FILL);
                pZone.setTextSize(30);
                pZone.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawRect(bound, pZone);

                if (i < numZones - 1) {
                    Rect boundText = new Rect();
                    String label = String.format("%.0f%%", (zone.getHigh() / (float) ftp * 100));
                    pZone.getTextBounds(label, 0, label.length(), boundText);
                    canvas.drawText(label, left().right - SPACING - boundText.width() - POWERSCALEWIDTH, yhigh - (pZone.ascent() / 2), pZone);
                }

                if (GRIDLINES && yhigh > canvas().top) {
                    Rect boundText = new Rect();
                    String label = String.valueOf(zone.getHigh());
                    pZone.getTextBounds(label, 0, label.length(), boundText);
                    canvas.drawText(label, canvas().right - boundText.width() - SPACING, yhigh - SPACING, pZone);

                    pZone.setAlpha(128);
                    pZone.setStrokeWidth(5);
                    canvas.drawLine(canvas().left, yhigh, canvas().right, yhigh, pZone);

                    boundText = new Rect();
                    label = "Z" + String.valueOf(i + 1);
                    pZone.setTextSize(50);
                    pZone.getTextBounds(label, 0, label.length(), boundText);
                    canvas.drawText(label, canvas().centerX() - (boundText.width() / 2), ymid - (pZone.ascent() / 2) - SPACING, pZone);


                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initialDrawingIsPerformed = false;
    }


    protected RectF geometry() {
        return new RectF(0, 0, this.getWidth(), this.getHeight());
    }

    protected RectF canvas() {
        RectF all = geometry();
        return new RectF(LWIDTH, THEIGHT, all.width() - RWIDTH, all.height() - IHEIGHT - BHEIGHT);
    }

    protected RectF left() {
        RectF all = geometry();
        return new RectF(0, THEIGHT, LWIDTH, all.height() - IHEIGHT - BHEIGHT);
    }

    protected RectF right() {
        RectF all = geometry();
        return new RectF(all.width() - RWIDTH, THEIGHT, RWIDTH, all.height() - IHEIGHT - BHEIGHT);
    }

    protected RectF bottom() {
        RectF all = geometry();
        return new RectF(LWIDTH, all.height() - BHEIGHT, all.width() - RWIDTH, BHEIGHT);
    }

    protected RectF bottomgap() {
        RectF all = geometry();
        return new RectF(LWIDTH, all.height() - (IHEIGHT + BHEIGHT), all.width() - LWIDTH - RWIDTH, IHEIGHT);
    }

    protected RectF top() {
        RectF all = geometry();
        return new RectF(LWIDTH, 0, all.width() - LWIDTH - RWIDTH, THEIGHT);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ergs == null) return;

        canvas.drawColor(Color.BLACK);

        RectF left = left();
        RectF bottom = bottom();
        RectF top = top();

        Paint markerPen = new Paint();
        markerPen.setColor(Color.RED);
        markerPen.setStrokeWidth(5);

        if (YTICLENGTH > 0)
            canvas.drawLine(left.right, left.top, left.right, left.bottom, markerPen); //Y
        if (XTICLENGTH > 0)
            canvas.drawLine(bottom.left, bottom.top, bottom.right, bottom.top, markerPen); //X

        // start with 5 min tics and get longer and longer
        int tsecs = 1 * 60 * 1000; // 1 minute tics
        float xrange = maxVx - minVx;
        while (xrange / tsecs > XTICS && tsecs < xrange) {
            if (tsecs == 120000) tsecs = 300000;
            else tsecs *= 2;
        }

        for (int i = (int) minVx; i <= maxVx; i += tsecs) {

            // paint a tic
            float x = transform(i, 0).x;

            if (XTICLENGTH > 0) { // we can make the tics disappear
                canvas.drawLine(x, bottom.top, x, bottom.top + XTICLENGTH, markerPen);
            }

            // always paint the label
            String label = TrainApplication.formatTime(i);
            markerPen.setTextSize(30);
            markerPen.setTypeface(Typeface.DEFAULT_BOLD);
            Rect bound = new Rect();
            markerPen.getTextBounds(label, 0, label.length(), bound);
            canvas.drawText(label, x - (bound.width() / 2), bottom.top - markerPen.ascent() + XTICLENGTH + (XTICLENGTH > 0 ? SPACING : 0), markerPen);
        }


        Path path = new Path();
        PointF first = transform((float)ergs.get(0).t, 0);
        path.moveTo(first.x, first.y);

        for (int i = 0; i < ergs.size(); i++) {
            PointF here = transform((float)ergs.get(i).t, (float)ergs.get(i).w);
            path.lineTo(here.x, here.y);
        }
        PointF last = transform((float)ergs.get(ergs.size() - 1).t, 0);
        path.lineTo(last.x, last.y);
        path.lineTo(first.x, first.y);

        Paint pBlue = new Paint();
        pBlue.setColor(Color.rgb(56, 170, 234));//#39A8E8);
        pBlue.setStyle(Paint.Style.FILL);

        canvas.drawPath(path, pBlue);
        paintPowerScale(canvas);
    }
}

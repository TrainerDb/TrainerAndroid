package com.trainerdb.trainerandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.trainerdb.trainerandroid.train.TrainState;

/**
 * Created by dcotrim on 23/06/2016.
 */
public class TrainWorkoutView extends WorkoutView {
    private TrainState state;
    Paint pPower = new Paint();
    Paint pHr = new Paint();
    Paint pCadence = new Paint();
    Paint defaultPaint = new Paint();
    Paint pText = new Paint();
    Paint pSelectInterval = new Paint();
    Bitmap cachedBitmap;
    PointF lastPointWatts, lastPointHr, lastPointCadence;
    Rect srcZoom;
    int selectedInterval = 0;

    public enum Transform {
        WATTS, HR, CADENCE, SPEED
    }

    public TrainWorkoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pCadence.setColor(Color.WHITE);
        pCadence.setStrokeWidth(5);
        pPower.setColor(Color.YELLOW);
        pPower.setStrokeWidth(5);
        pHr.setColor(Color.RED);
        pHr.setStrokeWidth(5);
        pText.setColor(Color.WHITE);
        pText.setTextAlign(Paint.Align.CENTER);
        pSelectInterval.setColor(Color.argb(64, 255, 255, 0));
        pSelectInterval.setStyle(Paint.Style.FILL);

        IHEIGHT = 10;
        THEIGHT = 35;
        BHEIGHT = 45;
        LWIDTH = 75;
        RWIDTH = 35;
        POWERSCALEWIDTH = 5;
    }

    public void setState(TrainState state) {
        this.state = state;
        this.workout = state.workout;
        this.ergs = workout.getErg().getCoursePoints();
        recompute();
        postInvalidate();
    }

    public Bitmap getCachedBitmap() {
        return cachedBitmap;
    }

    protected PointF transform(float seconds, float watts, Transform type) {
        RectF c = canvas();

        float xratio = c.width() / (maxVx - minVx);
        float yratio = c.height() / (maxY - minY);

        return new PointF(c.left - (minVx * xratio) + (seconds * xratio), c.bottom - (watts * yratio));
    }

    private void paintTelemetry(boolean firstDraw, Canvas canvas) {
        if (!firstDraw && !state.recording) return;

        long now = state.now;

        PointF here;
        if (state.watts.size() > 0) {
            int pos = state.watts.size() - 1;
            if (lastPointWatts == null) {
                lastPointWatts = transform(0, state.watts.get(0), Transform.WATTS);
                for (int i = 1; i <= pos; i++) {
                    here = transform(i * 1000, state.watts.get(i), Transform.WATTS);
                    canvas.drawLine(lastPointWatts.x, lastPointWatts.y, here.x, here.y, pPower);
                    lastPointWatts = here;
                }
            } else {
                here = transform(now, state.watts.get(pos), Transform.WATTS);
                canvas.drawLine(lastPointWatts.x, lastPointWatts.y, here.x, here.y, pPower);
                lastPointWatts = here;
            }
        }

        if (state.cadence.size() > 1) {
            int pos = state.cadence.size() - 1;
            if (lastPointCadence == null) {
                lastPointCadence = transform(0, state.cadence.get(0), Transform.CADENCE);
                for (int i = 1; i <= pos; i++) {
                    here = transform(i * 1000, state.cadence.get(i), Transform.CADENCE);
                    canvas.drawLine(lastPointCadence.x, lastPointCadence.y, here.x, here.y, pCadence);
                    lastPointCadence = here;
                }
            } else {
                here = transform(now, state.cadence.get(pos), Transform.CADENCE);
                canvas.drawLine(lastPointCadence.x, lastPointCadence.y, here.x, here.y, pCadence);
                lastPointCadence = here;
            }
        }

        if (state.hr.size() > 1) {
            int pos = state.hr.size() - 1;
            if (lastPointHr == null) {
                lastPointHr = transform(0, state.hr.get(0), Transform.HR);
                for (int i = 1; i <= pos; i++) {
                    here = transform(i * 1000, state.hr.get(i), Transform.HR);
                    canvas.drawLine(lastPointHr.x, lastPointHr.y, here.x, here.y, pHr);
                    lastPointHr = here;
                }
            } else {
                here = transform(now, state.hr.get(pos), Transform.HR);
                canvas.drawLine(lastPointHr.x, lastPointHr.y, here.x, here.y, pHr);
                lastPointHr = here;
            }
        }

    }

    private void paintNow(Canvas canvas) {
        if (!state.recording) return;

        // get now
        float px = transform(state.now, 0).x;

        Paint linePen = new Paint();
        linePen.setColor(Color.YELLOW);
        linePen.setStrokeWidth(5);

        canvas.drawLine(px, canvas().top, px, canvas().bottom, linePen);
    }

    private void selectInterval(Canvas canvas) {
        if (selectedInterval != 0) {

        }
    }

    private Rect calcIntervalCanvas() {
        Rect rectInterval = new Rect();
        long timeLeft = state.now - (60 * 1000);
        long timeRight = state.now + (2 * 60 * 1000);

        float left = transform(timeLeft, 0).x;
        float right = transform(timeRight, 0).x;

        (new RectF(left, canvas().top, right, canvas().bottom)).round(rectInterval);
        return rectInterval;
    }

    private void doInitialDrawing(Canvas canvas) {
    }

    public void setSelectedInterval(int selectedInterval) {
        this.selectedInterval = selectedInterval;
        this.postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (state == null) {
            super.onDraw(canvas);
            return;
        }

        //Rect srcFull = new Rect(0, 0, getWidth(), getHeight());
        //RectF rectInterval =  new RectF(0, 0, getWidth(), getHeight() / 3);
        //RectF rectFull = new RectF(0, getHeight() / 3, getWidth(), getHeight());

        if (!initialDrawingIsPerformed) {
            lastPointWatts = lastPointCadence = lastPointHr = null;
            super.onDraw(canvas);
            this.cachedBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888); //Change to lower bitmap config if possible.
            Canvas cacheCanvas = new Canvas(this.cachedBitmap);
            super.onDraw(cacheCanvas);
            doInitialDrawing(cacheCanvas);
            paintTelemetry(true, cacheCanvas);
            canvas.drawBitmap(this.cachedBitmap, 0, 0, defaultPaint);
            //canvas.drawBitmap(this.cachedBitmap, srcFull, rectInterval, defaultPaint);
            //canvas.drawBitmap(this.cachedBitmap, srcFull, rectFull, defaultPaint);
            initialDrawingIsPerformed = true;
        } else {
            //calcIntervalCanvas();
            Canvas cacheCanvas = new Canvas(this.cachedBitmap);
            paintTelemetry(false, cacheCanvas);
            canvas.drawBitmap(this.cachedBitmap, 0, 0, defaultPaint);
            //canvas.drawBitmap(cachedBitmap, srcFull, rectFull, defaultPaint);
            //canvas.drawBitmap(cachedBitmap, calcIntervalCanvas(), rectInterval, defaultPaint);
            paintNow(canvas);
            selectInterval(canvas);
        }
    }
}

package com.trainerdb.trainerandroid.train;

import com.trainerdb.trainerandroid.TrainApplication;

/**
 * Created by Daniel on 23/06/2016.
 */
public class StopWatch {

    /**
     * The start time.
     */
    private long startTime = -1;
    /**
     * The stop time.
     */
    private long stopTime = -1;

    /**
     * <p>Constructor.</p>
     */
    public StopWatch() {
    }

    /**
     * <p>Start the stopwatch.</p>
     *
     * <p>This method starts a new timing session, clearing any previous values.</p>
     */
    public void start() {
        stopTime = -1;
        startTime = System.currentTimeMillis();
    }


    public long restart() {
        long elapsed = elapsed();
        start();
        return elapsed;
    }

    /**
     * <p>Stop the stopwatch.</p>
     *
     * <p>This method ends a new timing session, allowing the time to be retrieved.</p>
     */
    public void stop() {
        stopTime = System.currentTimeMillis();
    }

    /**
     * <p>Reset the stopwatch.</p>
     *
     * <p>This method clears the internal values to allow the object to be reused.</p>
     */
    public void reset() {
        startTime = -1;
        stopTime = -1;
    }

    /**
     * <p>Split the time.</p>
     *
     * <p>This method sets the stop time of the watch to allow a time to be extracted.
     * The start time is unaffected, enabling {@link #unsplit()} to contine the
     * timing from the original start point.</p>
     */
    public void split() {
        stopTime = System.currentTimeMillis();
    }

    /**
     * <p>Remove a split.</p>
     *
     * <p>This method clears the stop time. The start time is unaffected, enabling
     * timing from the original start point to continue.</p>
     */
    public void unsplit() {
        stopTime = -1;
    }

    /**
     * <p>Suspend the stopwatch for later resumption.</p>
     *
     * <p>This method suspends the watch until it is resumed. The watch will not include
     * time between the suspend and resume calls in the total time.</p>
     */
    public void suspend() {
        stopTime = System.currentTimeMillis();
    }

    /**
     * <p>Resume the stopwatch after a suspend.</p>
     *
     * <p>This method resumes the watch after it was suspended. The watch will not include
     * time between the suspend and resume calls in the total time.</p>
     */
    public void resume() {
        startTime += (System.currentTimeMillis() - stopTime);
        stopTime = -1;
    }

    /**
     * <p>Get the time on the stopwatch.</p>
     *
     * <p>This is either the time between start and latest split, between start
     * and stop, or the time between the start and the moment this method is called.</p>
     *
     * @return the time in milliseconds
     */
    public long elapsed() {
        if (stopTime == -1) {
            if (startTime == -1) {
                return 0;
            }
            return (System.currentTimeMillis() - this.startTime);
        }
        return (this.stopTime - this.startTime);
    }

    /**
     * <p>Gets a summary of the time that the stopwatch recorded as a string.</p>
     *
     * <p>The format used is ISO8601-like,
     * <i>hours</i>:<i>minutes</i>:<i>seconds</i>.<i>milliseconds</i>.</p>
     *
     * @return the time as a String
     */
    public String toString() {
        return TrainApplication.formatTime(elapsed());
    }



}

package com.trainerdb.trainerandroid.activities;


import com.trainerdb.trainerandroid.data.Workout;

import java.util.Comparator;

/**
 * Created by Daniel on 09/07/2016.
 */
public class WorkoutComparator implements Comparator<Workout> {

    WorkoutListOptions options;

    public WorkoutComparator(WorkoutListOptions options) {
        this.options = options;
    }

    public static int compare(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public int compare(Workout x, Workout y) {
        // TODO: Handle null x or y values
        for (WorkoutListParent parent : options.getList()) {
            for (WorkoutListItem item : parent.filters) {
                if (item.active) {
                    switch (item.type) {
                        case sortName:
                            if (item.asc) {
                                int value = x.name.compareTo(y.name);
                                if (value != 0)
                                    return value;
                            } else {
                                int value = y.name.compareTo(x.name);
                                if (value != 0)
                                    return value;
                            }
                        case sortDuration:
                            if (item.asc) {
                                int value = compare(x.totalTicks, y.totalTicks);
                                if (value != 0)
                                    return value;
                            } else {
                                int value =compare(y.totalTicks, x.totalTicks);
                                if (value != 0)
                                    return value;
                            }
                        case sortTss:
                            if (item.asc) {
                                int value = Float.compare(x.tss, y.tss);
                                if (value != 0)
                                    return value;
                            } else {
                                int value = Float.compare(y.tss, x.tss);
                                if (value != 0)
                                    return value;
                            }
                        case sortIntensity:
                            if (item.asc) {
                                int value = Float.compare(x.intensityFactor, y.intensityFactor);
                                if (value != 0)
                                    return value;
                            } else {
                                int value = Float.compare(y.intensityFactor, x.intensityFactor);
                                if (value != 0)
                                    return value;
                            }
                    }
                }
            }
        }
        return 0;
    }
}

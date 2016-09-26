package com.trainerdb.trainerandroid.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trainerdb.trainerandroid.IGetAsyncListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dcotrim on 20/09/2016.
 */
public class WorkoutService {

    public static final List<Workout> WORKOUTS = new ArrayList<Workout>();
    public static final Map<String, Workout> WORKOUT_MAP = new HashMap<String, Workout>();

    public static final Map<String, WorkoutData> WORKOUT_DATA_MAP = new HashMap<String, WorkoutData>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        //getData(null);
    }

    public static void getWorkoutData(String key, final IGetAsyncListener complete) {
        if (WORKOUT_DATA_MAP.containsKey(key)) {
            if (complete != null)
                complete.onDataGet(true, WORKOUT_DATA_MAP.get(key));
            return;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("workoutCreator").child(key);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                WorkoutData item = dataSnapshot.getValue(WorkoutData.class);
                WORKOUT_DATA_MAP.put(item.key, item);

                if (complete != null) {
                    complete.onDataGet(true, item);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void getWorkouts(final IGetAsyncListener complete) {
        if (WORKOUTS.size() > 0) {
            if (complete != null)
                complete.onDataGet(true, WORKOUTS);
            return;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("workouts");
        myRef.orderByChild("totalTicks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Workout workout = messageSnapshot.getValue(Workout.class);
                    addItem(workout);
                }
                if (complete != null) {
                    complete.onDataGet(true, WORKOUTS);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void addItem(Workout item) {
        WORKOUTS.add(item);
        WORKOUT_MAP.put(item.key, item);
    }
}

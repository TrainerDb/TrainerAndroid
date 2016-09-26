package com.trainerdb.trainerandroid.activities;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trainerdb.trainerandroid.R;
import com.trainerdb.trainerandroid.TrainApplication;
import com.trainerdb.trainerandroid.WorkoutView;
import com.trainerdb.trainerandroid.data.Workout;
import com.trainerdb.trainerandroid.data.WorkoutData;
import com.trainerdb.trainerandroid.data.WorkoutService;

/**
 * A fragment representing a single Workout detail screen.
 * This fragment is either contained in a {@link WorkoutListActivity}
 * in two-pane mode (on tablets) or a {@link WorkoutDetailActivity}
 * on handsets.
 */
public class WorkoutDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Workout mItem;
    private WorkoutData mData;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WorkoutDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = WorkoutService.WORKOUT_MAP.get(getArguments().getString(ARG_ITEM_ID));
            mData = WorkoutService.WORKOUT_DATA_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            WorkoutView wokroutView = (WorkoutView) activity.findViewById(R.id.workout_detail);

            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.name);
                wokroutView.setWorkout(mItem);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.workout_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            //((TextView) rootView.findViewById(R.id.workout_detail)).setText(mItem.name);
            ((TextView) rootView.findViewById(R.id.tvWorkoutDetailName)).setText(mItem.name);
            ((TextView) rootView.findViewById(R.id.tvWorkoutDetailDescription)).setText(mData.description);
            ((TextView) rootView.findViewById(R.id.tvWorkoutDetailGoal)).setText(mData.goals);
            ((TextView) rootView.findViewById(R.id.tvWorkoutDetailDuration)).setText(TrainApplication.formatTime(mData.totalTicks));
            ((TextView) rootView.findViewById(R.id.tvWorkoutDetailIf)).setText(String.valueOf(mData.intensityFactor));
            ((TextView) rootView.findViewById(R.id.tvWorkoutDetailTss)).setText(String.valueOf(mData.tss));
        }

        return rootView;
    }
}

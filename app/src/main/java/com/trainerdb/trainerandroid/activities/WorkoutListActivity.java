package com.trainerdb.trainerandroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.trainerdb.trainerandroid.IGetAsyncListener;
import com.trainerdb.trainerandroid.R;
import com.trainerdb.trainerandroid.TrainApplication;
import com.trainerdb.trainerandroid.WorkoutView;
import com.trainerdb.trainerandroid.data.Workout;
import com.trainerdb.trainerandroid.data.WorkoutService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An activity representing a list of Workouts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WorkoutDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WorkoutListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    WorkoutListOptions options;
    SearchView search_view;
    SimpleItemRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        View recyclerView = findViewById(R.id.workout_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        search_view = (SearchView) findViewById(R.id.search_view);
        search_view.setOnQueryTextListener(this);

        if (findViewById(R.id.workout_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (adapter != null)
            adapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_workout_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            Intent myIntent = new Intent(this, WorkoutFilterActivity.class);
            myIntent.putExtra("options", (Parcelable) options);
            this.startActivityForResult(myIntent, 1001);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void applyOptions(final WorkoutListOptions options) {
        if (adapter != null)
            adapter.setFilterOptions(options);
            adapter.getFilter().filter(search_view.getQuery(), new Filter.FilterListener() {
                @Override
                public void onFilterComplete(int count) {
                    Toast.makeText(WorkoutListActivity.this, "Workouts: " + count,
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (1001): {
                if (resultCode == Activity.RESULT_OK) {
                    options = data.getParcelableExtra("options");
                    applyOptions(options);
                }
                break;
            }
        }
    }

    private void setupRecyclerView(final @NonNull RecyclerView recyclerView) {
        WorkoutService.getWorkouts(new IGetAsyncListener<List<Workout>>() {
            @Override
            public void onDataGet(boolean success, List<Workout> data) {
                adapter = new SimpleItemRecyclerViewAdapter(data);
                recyclerView.setAdapter(adapter);
            }
        });

    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> implements Filterable {

        private List<Workout> originalValues;
        private List<Workout> list;
        private final Object lock = new Object();
        private WorkoutListOptions filterOptions;
        private Filter filter;

        public SimpleItemRecyclerViewAdapter(List<Workout> items) {
            list = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.workout_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = list.get(position);
            holder.txtTitle.setText(list.get(position).name);
            holder.txtDuration.setText(String.valueOf(TrainApplication.formatTime(list.get(position).totalTicks)));
            holder.txtIf.setText(String.valueOf(list.get(position).intensityFactor));
            holder.txtTss.setText(String.valueOf(list.get(position).tss));
            holder.workoutView.setWorkout(list.get(position));
            holder.tbWorkoutFavorite.setChecked(TrainApplication.isFavorite(holder.mItem.key));
            holder.tbWorkoutFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((ToggleButton) v).isChecked()) {
                        TrainApplication.addFavorite(String.valueOf(holder.mItem.key));
                    } else {
                        TrainApplication.removeFavorite(String.valueOf(holder.mItem.key));
                    }
                }
            });
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(WorkoutDetailFragment.ARG_ITEM_ID, holder.mItem.key);
                        WorkoutDetailFragment fragment = new WorkoutDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.workout_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, WorkoutDetailActivity.class);
                        intent.putExtra(WorkoutDetailFragment.ARG_ITEM_ID, holder.mItem.key);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            TextView txtTitle, txtDuration, txtTss, txtIf;
            ToggleButton tbWorkoutFavorite;
            public final WorkoutView workoutView;
            public Workout mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                txtTitle = (TextView) view.findViewById(R.id.tvWorkoutItemName);
                txtDuration = (TextView) view.findViewById(R.id.tvWorkoutItemTime);
                txtTss = (TextView) view.findViewById(R.id.tvWorkoutItemTss);
                txtIf = (TextView) view.findViewById(R.id.tvWorkoutItemIf);
                workoutView = (WorkoutView) view.findViewById(R.id.workoutView);
                tbWorkoutFavorite = (ToggleButton) view.findViewById(R.id.tbWorkoutFavorite);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + txtTitle.getText() + "'";
            }
        }

        public void setFilterOptions(WorkoutListOptions filterOptions) {
            this.filterOptions = filterOptions;
        }

        private class ArrayFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Workout> FilteredArrList = new ArrayList<Workout>();

                if (originalValues == null) {
                    synchronized (lock) {
                        originalValues = new ArrayList<Workout>(list); // saves the original data in mOriginalValues
                    }
                }
                if ((constraint == null || constraint.length() == 0) && filterOptions == null) {
                    // set the Original result to return
                    results.count = originalValues.size();
                    results.values = originalValues;
                } else {
                    if ((constraint != null && constraint.length() > 0)) {
                        constraint = constraint.toString().toLowerCase();
                        for (int i = 0; i < originalValues.size(); i++) {
                            Workout data = originalValues.get(i);
                            if (data.toString().toLowerCase().contains(constraint.toString())) {
                                FilteredArrList.add(data);
                            }
                        }
                    } else {
                        for (int i = 0; i < originalValues.size(); i++) {
                            Workout data = originalValues.get(i);
                            FilteredArrList.add(data);
                        }
                    }
                    if (filterOptions != null) {
                        for (WorkoutListParent parent : filterOptions.getList()) {
                            if (parent.sort) continue;
                            for (WorkoutListItem item : parent.filters) {
                                if (item.active) {
                                    for (int i = FilteredArrList.size(); --i >= 0; ) {
                                        Workout workout = FilteredArrList.get(i);
                                        switch (item.type) {
                                            case filterFavorite:
                                                if (!TrainApplication.isFavorite(String.valueOf(workout.key))) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                        }
                                    }
                                } else if (!item.active) {
                                    for (int i = FilteredArrList.size(); --i >= 0; ) {
                                        Workout workout = FilteredArrList.get(i);
                                        switch (item.type) {
                                            case filterZoneAnaerobic:
                                                if (workout.tags.containsKey("Anaerobic")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterZoneEndurance:
                                                if (workout.tags.containsKey("eEndurance")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterZoneSprint:
                                                if (workout.tags.containsKey("Sprint")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterZoneSweetSpot:
                                                if (workout.tags.containsKey("SweetSpot")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterZoneTempo:
                                                if (workout.tags.containsKey("Tempo")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterZoneVo2:
                                                if (workout.tags.containsKey("V02Max")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterZoneThreshold:
                                                if (workout.tags.containsKey("Threshold")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterZoneStrength:
                                                if (workout.tags.containsKey("Strength")) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;

                                            case filterDurationLess1h:
                                                if (workout.totalTicks <= 60 * 60 * 1000) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterDuration60_90:
                                                if (workout.totalTicks >= 60 * 60 * 1000 && workout.totalTicks <= 90 * 60 * 1000) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterDuration90_2h:
                                                if (workout.totalTicks >= 90 * 60 * 1000 && workout.totalTicks <= 120 * 60 * 1000) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterDuration2_3h:
                                                if (workout.totalTicks >= 120 * 60 * 1000 && workout.totalTicks <= 180 * 60 * 1000) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                            case filterDuration3hmore:
                                                if (workout.totalTicks >= 180 * 60 * 1000) {
                                                    FilteredArrList.remove(i);
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;

                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (ArrayList<Workout>) results.values;
                if (results.count > 0) {
                    if (filterOptions != null)
                        Collections.sort(list, new WorkoutComparator(filterOptions));
                    notifyDataSetChanged();
                } else {
                    SimpleItemRecyclerViewAdapter.this.notifyDataSetChanged();
                }
            }
        }

        @Override
        public Filter getFilter() {
            if (filter == null)
                filter = new ArrayFilter();
            return filter;
        }

    }
}

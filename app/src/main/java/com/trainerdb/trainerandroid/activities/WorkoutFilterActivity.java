package com.trainerdb.trainerandroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.trainerdb.trainerandroid.R;

import java.util.List;

public class WorkoutFilterActivity extends AppCompatActivity {

    WorkoutListOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (savedInstanceState == null) {
            Intent intent = this.getIntent();
            this.options = intent.getParcelableExtra("options");
        } else {
            this.options = (WorkoutListOptions)savedInstanceState.getSerializable("options");
        }
        if (options == null) {
            options = new WorkoutListOptions();
            options.prepare();
        }

        final ExpandableListView expListView = (ExpandableListView) findViewById(R.id.listFilter);
        FilterListAdapter expListAdapter = new FilterListAdapter(this, options.getList());
        expListView.setAdapter(expListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter_apply) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("options", (Parcelable) options);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        } else if (id == R.id.action_filter_cancel) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class FilterListAdapter extends BaseExpandableListAdapter {

        private Activity context;
        private List<WorkoutListParent> list;

        public FilterListAdapter(Activity context, List<WorkoutListParent> listPlanCategories) {
            this.context = context;
            this.list = listPlanCategories;
        }

        public Object getChild(int groupPosition, int childPosition) {
            return list.get(groupPosition).filters.get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public int getChildType(int groupPosition, int childPosition) {
            return ((WorkoutListParent)getGroup(groupPosition)).sort ? 1 : 0;
        }

        @Override
        public int getChildTypeCount(){
            return 2;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final WorkoutListItem item = (WorkoutListItem) getChild(groupPosition, childPosition);
            final WorkoutListParent parentItem = (WorkoutListParent) getGroup(groupPosition);

            int type = getChildType(groupPosition, childPosition);
            if (type == 1) {

                if (convertView == null) {
                    LayoutInflater inflater = context.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.workout_filter_sort_content, null);
                }

                TextView tvFilterItemName = (TextView) convertView.findViewById(R.id.tvSortName);
                final ToggleButton toggleSortAsc = (ToggleButton) convertView.findViewById(R.id.toggleSortAsc);
                final ToggleButton toggleSortDesc = (ToggleButton) convertView.findViewById(R.id.toggleSortDesc);

                toggleSortAsc.setTextOn(item.ascName);
                toggleSortAsc.setTextOff(item.ascName);
                toggleSortDesc.setTextOn(item.descName);
                toggleSortDesc.setTextOff(item.descName);

                toggleSortAsc.setChecked(item.active && item.asc);
                toggleSortDesc.setChecked(item.active && !item.asc);

                toggleSortAsc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (toggleSortAsc.isChecked()) {
                            toggleSortDesc.setChecked(false);
                            item.active = true;
                            item.asc = true;
                        } else {
                            item.active = false;
                        }
                    }
                });

                toggleSortDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (toggleSortDesc.isChecked()) {
                            toggleSortAsc.setChecked(false);
                            item.active = true;
                            item.asc = false;
                        } else {
                            item.active = false;
                        }
                    }
                });

                tvFilterItemName.setText(item.name);
            }
            else {
                if (convertView == null) {
                    LayoutInflater inflater = context.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.workout_filter_list_content, null);
                }
                TextView tvFilterItemName = (TextView) convertView.findViewById(R.id.tvFilterItemName);
                final CheckBox checkFilterItem = (CheckBox) convertView.findViewById(R.id.checkFilterItem);
                checkFilterItem.setChecked(item.active);
                checkFilterItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.active = checkFilterItem.isChecked();
                    }
                });

                tvFilterItemName.setText(item.name);
            }
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return list.get(groupPosition).filters.size(); // laptopCollections.get(laptops.get(groupPosition)).size();
        }

        public Object getGroup(int groupPosition) {
            return list.get(groupPosition);
        }

        public int getGroupCount() {
            return list.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            WorkoutListParent parentItem = (WorkoutListParent) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.workout_filter_group, null);
            }
            TextView item = (TextView) convertView.findViewById(R.id.tvParentItemName);
            //item.setTypeface(null, Typeface.BOLD);
            item.setText(parentItem.name);
            return convertView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

}

package com.trainerdb.trainerandroid.activities;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dcotrim on 22/09/2016.
 */
public class WorkoutListOptions implements Serializable, Parcelable {
    public enum ItemType {
        sortName, sortDuration, sortIntensity, sortTss,
        filterZoneEndurance, filterZoneTempo, filterZoneSweetSpot, filterZoneThreshold, filterZoneVo2, filterZoneAnaerobic, filterZoneSprint, filterZoneStrength,
        filterDurationLess1h, filterDuration60_90, filterDuration90_2h, filterDuration2_3h, filterDuration3hmore,
        filterFavorite
    }

    private List<WorkoutListParent> list;

    public WorkoutListOptions() {

    }

    public void prepare() {
        list = new ArrayList<>();

        WorkoutListParent sort = new WorkoutListParent("Sort by", true);
        sort.filters.add(sort.newSort(ItemType.sortName, "Name", "A - Z", "Z - A"));
        sort.filters.add(sort.newSort(ItemType.sortDuration, "Duration", "Shortest", "Longest"));
        sort.filters.add(sort.newSort(ItemType.sortIntensity, "Intensity", "Lowest", "Highest"));
        sort.filters.add(sort.newSort(ItemType.sortTss, "TSS", "Lowest", "Highest"));
        list.add(sort);

        WorkoutListParent filterZone = new WorkoutListParent("Filter by tags", false);
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneEndurance, "Endurance", true));
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneTempo, "Tempo", true));
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneSweetSpot, "Sweet Spot", true));
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneThreshold, "Threshold", true));
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneVo2, "VO2 Max", true));
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneAnaerobic, "Anaerobic", true));
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneSprint, "Sprint", true));
        filterZone.filters.add(filterZone.newFilter(ItemType.filterZoneStrength, "Strength", true));

        list.add(filterZone);

        WorkoutListParent filterDuration = new WorkoutListParent("Filter by duration", false);
        filterDuration.filters.add(filterDuration.newFilter(ItemType.filterDurationLess1h, "Less than 1h", true));
        filterDuration.filters.add(filterDuration.newFilter(ItemType.filterDuration60_90, "60 - 90min", true));
        filterDuration.filters.add(filterDuration.newFilter(ItemType.filterDuration90_2h, "90min - 2h", true));
        filterDuration.filters.add(filterDuration.newFilter(ItemType.filterDuration2_3h, "2 - 3h", true));
        filterDuration.filters.add(filterDuration.newFilter(ItemType.filterDuration3hmore, "More than 3h", true));
        list.add(filterDuration);

        WorkoutListParent filterOptions = new WorkoutListParent("Filter Options", false);
        filterOptions.filters.add(filterDuration.newFilter(ItemType.filterFavorite, "Show only Favorites", false));
        list.add(filterOptions);
    }

    public List<WorkoutListParent> getList() {
        return list;
    }

    protected WorkoutListOptions(Parcel in) {
        if (in.readByte() == 0x01) {
            list = new ArrayList<WorkoutListParent>();
            in.readList(list, WorkoutListParent.class.getClassLoader());
        } else {
            list = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (list == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(list);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WorkoutListOptions> CREATOR = new Parcelable.Creator<WorkoutListOptions>() {
        @Override
        public WorkoutListOptions createFromParcel(Parcel in) {
            return new WorkoutListOptions(in);
        }

        @Override
        public WorkoutListOptions[] newArray(int size) {
            return new WorkoutListOptions[size];
        }
    };
}

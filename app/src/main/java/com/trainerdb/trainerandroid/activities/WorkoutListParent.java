package com.trainerdb.trainerandroid.activities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dcotrim on 22/09/2016.
 */
public class WorkoutListParent implements Parcelable {
    public String name;
    public List<WorkoutListItem> filters;
    public boolean sort = false;

    public WorkoutListParent(String name, boolean sort) {
        this.name = name;
        this.sort = sort;
        filters = new ArrayList<>();
    }

    public WorkoutListItem newFilter(WorkoutListOptions.ItemType type, String name, boolean active) {
        WorkoutListItem i = new WorkoutListItem();
        i.type = type;
        i.name = name;
        i.active = active;
        return i;
    }

    public WorkoutListItem newSort(WorkoutListOptions.ItemType type, String name, String ascName, String descName) {
        WorkoutListItem i = new WorkoutListItem();
        i.type = type;
        i.name = name;
        i.ascName = ascName;
        i.descName = descName;
        i.active = false;
        return i;
    }

    protected WorkoutListParent(Parcel in) {
        name = in.readString();
        sort = in.readByte() != 0x00;
        if (in.readByte() == 0x01) {
            filters = new ArrayList<WorkoutListItem>();
            in.readList(filters, WorkoutListItem.class.getClassLoader());
        } else {
            filters = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (sort ? 0x01 : 0x00));
        if (filters == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(filters);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WorkoutListParent> CREATOR = new Parcelable.Creator<WorkoutListParent>() {
        @Override
        public WorkoutListParent createFromParcel(Parcel in) {
            return new WorkoutListParent(in);
        }

        @Override
        public WorkoutListParent[] newArray(int size) {
            return new WorkoutListParent[size];
        }
    };
}
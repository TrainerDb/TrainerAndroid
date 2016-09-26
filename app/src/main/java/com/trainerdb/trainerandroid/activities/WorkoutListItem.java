package com.trainerdb.trainerandroid.activities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Daniel on 09/07/2016.
 */
public class WorkoutListItem implements Parcelable {
    public String name;
    public String ascName;
    public String descName;
    public WorkoutListOptions.ItemType type;
    public boolean active = false;
    public boolean asc = true;


    public WorkoutListItem() {

    }
    protected WorkoutListItem(Parcel in) {
        name = in.readString();
        ascName = in.readString();
        descName = in.readString();
        type = (WorkoutListOptions.ItemType) in.readValue(WorkoutListOptions.ItemType.class.getClassLoader());
        active = in.readByte() != 0x00;
        asc = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(ascName);
        dest.writeString(descName);
        dest.writeValue(type);
        dest.writeByte((byte) (active ? 0x01 : 0x00));
        dest.writeByte((byte) (asc ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Creator<WorkoutListItem> CREATOR = new Creator<WorkoutListItem>() {
        @Override
        public WorkoutListItem createFromParcel(Parcel in) {
            return new WorkoutListItem(in);
        }

        @Override
        public WorkoutListItem[] newArray(int size) {
            return new WorkoutListItem[size];
        }
    };
}
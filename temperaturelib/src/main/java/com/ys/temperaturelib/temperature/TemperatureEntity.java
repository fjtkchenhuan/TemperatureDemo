package com.ys.temperaturelib.temperature;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class TemperatureEntity implements Parcelable {
    public List<Float> tempList;
    public float max;
    public float min;
    public float ta;
    public float temperatue;

    public TemperatureEntity(){}
    protected TemperatureEntity(Parcel in) {
        max = in.readFloat();
        min = in.readFloat();
        ta = in.readFloat();
        temperatue = in.readFloat();
    }

    public static final Creator<TemperatureEntity> CREATOR = new Creator<TemperatureEntity>() {
        @Override
        public TemperatureEntity createFromParcel(Parcel in) {
            return new TemperatureEntity(in);
        }

        @Override
        public TemperatureEntity[] newArray(int size) {
            return new TemperatureEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(max);
        parcel.writeFloat(min);
        parcel.writeFloat(ta);
        parcel.writeFloat(temperatue);
    }
}

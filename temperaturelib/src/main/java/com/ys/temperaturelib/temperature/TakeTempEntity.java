package com.ys.temperaturelib.temperature;

import android.os.Parcel;
import android.os.Parcelable;

public class TakeTempEntity implements Parcelable {
    int distances;
    float takeTemperature;
    boolean needCheck = true;

    public boolean isNeedCheck() {
        return needCheck;
    }

    public void setNeedCheck(boolean needCheck) {
        this.needCheck = needCheck;
    }

    public int getDistances() {
        return distances;
    }

    public void setDistances(int distances) {
        this.distances = distances;
    }

    public float getTakeTemperature() {
        return takeTemperature;
    }

    public void setTakeTemperature(float takeTemperature) {
        this.takeTemperature = takeTemperature;
    }

    public TakeTempEntity() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.distances);
        dest.writeFloat(this.takeTemperature);
        dest.writeByte(this.needCheck ? (byte) 1 : (byte) 0);
    }

    protected TakeTempEntity(Parcel in) {
        this.distances = in.readInt();
        this.takeTemperature = in.readFloat();
        this.needCheck = in.readByte() != 0;
    }

    public static final Creator<TakeTempEntity> CREATOR = new Creator<TakeTempEntity>() {
        @Override
        public TakeTempEntity createFromParcel(Parcel source) {
            return new TakeTempEntity(source);
        }

        @Override
        public TakeTempEntity[] newArray(int size) {
            return new TakeTempEntity[size];
        }
    };
}

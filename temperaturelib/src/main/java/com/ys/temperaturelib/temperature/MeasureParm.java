package com.ys.temperaturelib.temperature;

import android.os.Parcel;
import android.os.Parcelable;

public class MeasureParm implements Parcelable {

    public String mode;
    public boolean isLight;//是否逆光
    public int radio; //热力图半径
    public int perid; //读取数据频率
    public int xCount;//一行个数
    public int yCount;//一列个数

    public MeasureParm(String mode, int radio, int perid, int xCount, int yCount) {
        this.mode = mode;
        this.radio = radio;
        this.perid = perid;
        this.xCount = xCount;
        this.yCount = yCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mode);
        dest.writeByte(this.isLight ? (byte) 1 : (byte) 0);
        dest.writeInt(this.radio);
        dest.writeInt(this.perid);
        dest.writeInt(this.xCount);
        dest.writeInt(this.yCount);
    }

    protected MeasureParm(Parcel in) {
        this.mode = in.readString();
        this.isLight = in.readByte() != 0;
        this.radio = in.readInt();
        this.perid = in.readInt();
        this.xCount = in.readInt();
        this.yCount = in.readInt();
    }

    public static final Creator<MeasureParm> CREATOR = new Creator<MeasureParm>() {
        @Override
        public MeasureParm createFromParcel(Parcel source) {
            return new MeasureParm(source);
        }

        @Override
        public MeasureParm[] newArray(int size) {
            return new MeasureParm[size];
        }
    };
}

package com.ys.bodymeasure;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.device.MeasureResult;
import com.ys.temperaturelib.heatmap.DefaultHeatMap;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MatrixI2CFragment extends BaseFragment {
    private TextView mTaText;
    private TextView mToText;
    private ImageView mDataImageView;
    private DefaultHeatMap mHeatMap;
    private IMatrixThermometer mThermometer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_matrix_i2c_measure, container, false);
        mDataImageView = inflate.findViewById(R.id.measure_data_img);
        mTaText = inflate.findViewById(R.id.measure_data_ta);
        mToText = inflate.findViewById(R.id.measure_data_to);
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mThermometer = (IMatrixThermometer) ((I2CActivity) getActivity()).getCurProduct();
        mDataImageView.post(new Runnable() {
            @Override
            public void run() {
                MeasureParm parm = mThermometer.getParm();
                int w = mDataImageView.getWidth() / (parm.xCount > 32 ? 1 : 32 / parm.xCount);
                int h = mDataImageView.getHeight() / (parm.xCount > 24 ? 1 : 24 / parm.yCount);
                mHeatMap = new DefaultHeatMap(w, h, mThermometer.getParm().radio);
            }
        });

        mThermometer.startUp(new MeasureResult<float[]>() {
            @Override
            public void onResult(TemperatureEntity entity, float[] oneFrame) {
                Message message = mHandler.obtainMessage();
                message.obj = entity;
                mHandler.sendMessage(message);
            }
        });

    }

    DecimalFormat fnum = new DecimalFormat("##0.0");
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            TemperatureEntity temperature = (TemperatureEntity) message.obj;
            if (mHeatMap != null && temperature != null && mThermometer != null) {

                mToText.setText("TO:" + fnum.format(temperature.temperatue) + "°");
                mTaText.setText("TA:" + fnum.format(temperature.ta) + "°");
                Bitmap bitmap = mHeatMap.drawHeatMap(temperature
                        , mThermometer.getParm().xCount
                        , mThermometer.getParm().yCount);
                if (bitmap != null)
                    mDataImageView.setImageBitmap(bitmap);
            }
            return true;
        }
    });
}

package com.ys.bodymeasure;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ys.temperaturelib.device.MeasureResult;
import com.ys.temperaturelib.device.serialport.ProductImp;
import com.ys.temperaturelib.temperature.TemperatureEntity;

import java.text.DecimalFormat;

public class PointSerialFragment extends BaseFragment {
    TextView measureText;
    ProductImp mSerialProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_point, container, false);
        measureText = inflate.findViewById(R.id.measure_value);
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSerialProduct = ((SerialActivity) getActivity()).getCurProduct();
        measure(mSerialProduct.getDevice(), mSerialProduct.getBaudrate());
    }

    @Override
    public void measure(String devicePath, int deviceRate) {
        super.measure(devicePath, deviceRate);
        measureText.setText("");
        if (mSerialProduct != null) {
            mSerialProduct.startUp(new MeasureResult<byte[]>() {
                @Override
                public void onResult(TemperatureEntity entity, byte[] oneFrame) {
                    Message message = mHandler.obtainMessage();
                    message.obj = entity;
                    mHandler.sendMessage(message);
                }
            }, devicePath, deviceRate);
        }
    }
    DecimalFormat fnum = new DecimalFormat("##0.00");
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            TemperatureEntity entity = (TemperatureEntity) message.obj;
            measureText.setText("TO : " + fnum.format(entity.temperatue) + "°"
                    + "\nTA : " +  fnum.format(entity.ta) + "°");
            return false;
        }
    });
}

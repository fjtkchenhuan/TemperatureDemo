package com.ys.bodymeasure;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ys.temperaturelib.device.MeasureResult;
import com.ys.temperaturelib.device.serialport.ProductImp;
import com.ys.temperaturelib.device.serialport.SLSC_HM_32x32;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.utils.DataFormatUtil;

import java.text.DecimalFormat;

public class PointSerialFragment extends BaseFragment {
    TextView measureText;
    ProductImp mSerialProduct;
    private Handler handler;

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
        if (mSerialProduct instanceof SLSC_HM_32x32) {
            handler = new Handler();
            handler.post(sendData);
        }
    }

    private Runnable sendData = new Runnable() {
        @Override
        public void run() {
            if (mSerialProduct != null) {
                byte[] order = mSerialProduct.getOrderDataOutputQuery();
                mSerialProduct.order(order);
                handler.postDelayed(sendData,500);
            }
        }
    };

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

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendData);
        super.onDestroy();
    }
}

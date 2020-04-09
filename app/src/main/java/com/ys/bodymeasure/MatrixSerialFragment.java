package com.ys.bodymeasure;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ys.temperaturelib.device.serialport.SHAIMAN_32x24;
import com.ys.temperaturelib.device.serialport.SM23_32x32_XM;
import com.ys.temperaturelib.device.serialport.ProductImp;
import com.ys.temperaturelib.device.serialport.SMLX90621_RR;
import com.ys.temperaturelib.device.serialport.SYM32A_32x32_XM;
import com.ys.temperaturelib.device.MeasureResult;
import com.ys.temperaturelib.heatmap.DefaultHeatMap;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.utils.DataFormatUtil;

import java.text.DecimalFormat;


public class MatrixSerialFragment extends BaseFragment implements View.OnClickListener {
    private TextView mDataText;
    private TextView mTaText;
    private TextView mToText;
    private Button mSendButton;
    private ImageView mDataImageView;
    private CheckBox mCheckBox;
    DefaultHeatMap mHeatMap;
    private boolean isAuto;
    ProductImp mSerialProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_matrix_measure, container, false);
        mCheckBox = inflate.findViewById(R.id.measure_data_mode);
        mDataImageView = inflate.findViewById(R.id.measure_data_img);
        mTaText = inflate.findViewById(R.id.measure_data_ta);
        mToText = inflate.findViewById(R.id.measure_data_to);
        mDataText = inflate.findViewById(R.id.measure_data_text);
        mDataText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mSendButton = inflate.findViewById(R.id.measure_data_send);
        mSendButton.setOnClickListener(this);
        isAuto = mCheckBox.isChecked();
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isAuto = isChecked;
                checkOrder();
                SM32ASendOrder();
            }
        });
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSerialProduct = ((SerialActivity) getActivity()).getCurProduct();
        measure(mSerialProduct.getDevice(), mSerialProduct.getBaudrate());
        mDataImageView.post(new Runnable() {
            @Override
            public void run() {
                mHeatMap = new DefaultHeatMap(mDataImageView.getWidth(),
                        mDataImageView.getHeight(), mSerialProduct.getParm().radio);
            }
        });
    }

    private void checkOrder() {
        byte[] order = mSerialProduct.getOrderDataOutputType(isAuto);
        if (isQueryAotu()) {
            mSerialProduct.setWriteInThread(isAuto);
        }
        onOrder(order);
    }

    @Override
    public void measure(String devicePath, int deviceRate) {
        mSerialProduct.startUp(new MeasureResult<byte[]>() {

            @Override
            public void onResult(TemperatureEntity entity, byte[] oneFrame) {
                Message message = mHandler.obtainMessage();
                Bundle data = message.getData();
                data.putByteArray("_byte", oneFrame);
                data.putParcelable("_temp", entity);
                mHandler.sendMessage(message);
            }
        }, devicePath, deviceRate);
        if (mSerialProduct instanceof SYM32A_32x32_XM) {
            byte[][] initOrder = ((SYM32A_32x32_XM) mSerialProduct).getInitOrder();
            for (int i = 0; i < initOrder.length; i++) {
                onOrder(initOrder[i]);
            }
        } else {
            onClick(null);
        }
    }
    DecimalFormat fnum = new DecimalFormat("##0.00");
    Bitmap bitmap;
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Bundle bundle = message.getData();
            byte[] bytes = bundle.getByteArray("_byte");
            String dataText = DataFormatUtil.bytesToHex(bytes);
            mDataText.setText(dataText);

            TemperatureEntity temperature = bundle.getParcelable("_temp");
            if (temperature != null) {
                mTaText.setText("TA:" + fnum.format(temperature.ta) + "°");
                mToText.setText("TO:" + fnum.format(temperature.temperatue) + "°");
                    recycleBitmap();
                    bitmap = mHeatMap.drawHeatMap(temperature, mSerialProduct.getParm().xCount,
                            mSerialProduct.getParm().yCount);
                    mDataImageView.setImageBitmap(bitmap);
            }
            return true;
        }
    });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recycleBitmap();
        handler.removeCallbacks(sendDate);
    }

    private void recycleBitmap() {
        if (bitmap != null) bitmap.recycle();
        bitmap = null;
    }

    private void onOrder(byte[] order) {
        if (mSerialProduct != null) {
            mSerialProduct.order(order);
        }
    }

    @Override
    public void onClick(final View view) {
        if (isAuto || mSerialProduct == null) return;
        byte[] order = mSerialProduct.getOrderDataOutputQuery();
        if (isQueryAotu()) {
            mSerialProduct.setWriteInThread(isAuto);
        }
        onOrder(order);
    }

    private boolean isQueryAotu() {
        return mSerialProduct instanceof SM23_32x32_XM || mSerialProduct instanceof SHAIMAN_32x24
                || mSerialProduct instanceof SMLX90621_RR;
    }

    private Handler handler;
    private void SM32ASendOrder() {
        handler = new Handler();
        if (mSerialProduct instanceof SYM32A_32x32_XM) {
               handler.postDelayed(sendDate,500);
        }
    }

    private Runnable sendDate = new Runnable() {
        @Override
        public void run() {
            if (mSerialProduct == null) return;
            byte[] order = mSerialProduct.getOrderDataOutputQuery();
            onOrder(order);
            if (mCheckBox.isChecked())
               handler.postDelayed(sendDate,500);
        }
    };


}

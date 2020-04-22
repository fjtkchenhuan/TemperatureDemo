package com.ys.bodymeasure;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ys.temperaturelib.depth.DepthImpl;
import com.ys.temperaturelib.device.serialport.RQVK002_TW;
import com.ys.temperaturelib.device.serialport.SGY_MCU90614_TW;
import com.ys.temperaturelib.device.serialport.SGY_MCU90640_32x24;
import com.ys.temperaturelib.device.serialport.SHAIMAN_32x24;
import com.ys.temperaturelib.device.serialport.SM23_32x32_XM;
import com.ys.temperaturelib.device.serialport.SMLX90614;
import com.ys.temperaturelib.device.serialport.ProductImp;
import com.ys.temperaturelib.device.SThermometer;
import com.ys.temperaturelib.device.serialport.SMLX90621_RR;
import com.ys.temperaturelib.device.serialport.SMLX90621_YS;
import com.ys.temperaturelib.device.serialport.SYM32A_32x32_XM;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;

/**
 * Created by Administrator on 2019/3/12.
 */

public class SerialActivity extends AppCompatActivity implements View.OnClickListener {
    //所有串口模块型号
    public static final String[] MODE_SERIALPORT = new String[]{RQVK002_TW.DEFAULT_MODE_NAME,SMLX90614.DEFAULT_MODE_NAME,
            SGY_MCU90614_TW.DEFAULT_MODE_NAME, SGY_MCU90640_32x24.DEFAULT_MODE_NAME, SM23_32x32_XM.DEFAULT_MODE_NAME,
            SYM32A_32x32_XM.DEFAULT_MODE_NAME, SHAIMAN_32x24.DEFAULT_MODE_NAME, SMLX90621_RR.DEFAULT_MODE_NAME,
            SMLX90621_YS.DEFAULT_MODE_NAME};

    ProductImp[] mSerialPorts = new ProductImp[]{
            new RQVK002_TW(),new SMLX90614(), new SGY_MCU90614_TW(), new SGY_MCU90640_32x24(), new SM23_32x32_XM(),
            new SYM32A_32x32_XM(), new SHAIMAN_32x24(), new SMLX90621_RR(),new SMLX90621_YS()
    };

    AlertDialog mDevicesDialog;
    AlertDialog mRateDialog;
    Button deviceButton;
    Button rateButton;
    private TextView distanceText;
    String curDevice, lastDevice;
    private Button mTakeButton;
    int curRate, lastRate;
    BaseFragment mSerialFragment;
    ProductImp mCurProduct;
    DepthImpl mDepth;
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            distanceText.setText(message.arg1 + "mm");
            return true;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int index = intent.getIntExtra("device_index", -1);
        if (index < 0) return;
        setContentView(R.layout.activity_serialport);
        deviceButton = findViewById(R.id.serialport_device);
        deviceButton.setOnClickListener(this);
        rateButton = findViewById(R.id.serialport_rate);
        rateButton.setOnClickListener(this);
        mTakeButton = findViewById(R.id.measure_take);
        mTakeButton.setOnClickListener(this);
        distanceText = findViewById(R.id.measure_distance);
//        mDepth = new DepthImpl(this, new DepthImpl.DepthResult() {
//            @Override
//            public void onResult(int distance) {
//                Message message = mHandler.obtainMessage(0);
//                message.arg1 = distance;
//                message.sendToTarget();
//            }
//        });
        mCurProduct = mSerialPorts[index];
        setTitle(mCurProduct.getParm().mode);
        init(mCurProduct.getDevice(), mCurProduct.getBaudrate());
        setFragmet(mCurProduct.isPoint() ? new PointSerialFragment() : new MatrixSerialFragment());

        setButtonValue(mTakeButton);
    }

    public ProductImp getCurProduct() {
        return mCurProduct;
    }

    protected void setFragmet(Fragment fragmet) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.serialport_view, fragmet).commit();
        mSerialFragment = (BaseFragment) fragmet;
    }

    protected void init(String devive, int rate) {
        lastDevice = curDevice = devive;
        lastRate = curRate = rate;
        rateButton.setText(curRate + "");
        deviceButton.setText(curDevice);
        ratesDialog();
        devicesDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCurProduct != null) mCurProduct.destroy();
        mCurProduct = null;
        if (mDevicesDialog != null) mDevicesDialog.dismiss();
        mDevicesDialog = null;
        if (mRateDialog != null) mRateDialog.dismiss();
        mRateDialog = null;
        if (mTemptakeDialog != null) mTemptakeDialog.distroy();
        mTemptakeDialog = null;
//        mDepth.release();
    }


    private void ratesDialog() {
        if (mRateDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(SThermometer.RATES, checkRateItem(SThermometer.RATES, curRate),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curRate = Integer.parseInt(SThermometer.RATES[which]);
                            rateButton.setText(SThermometer.RATES[which]);
                            mRateDialog.dismiss();
                        }
                    });
            mRateDialog = builder.create();
        }
    }

    private int checkRateItem(String[] RATES, int curRate) {
        for (int i = 0; i < RATES.length; i++) {
            if (RATES[i].equals(String.valueOf(curRate))) return i;
        }
        return 0;
    }

    private int checkDeviceItem(String[] devices, String curDevice) {
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].equals(curDevice)) return i;
        }
        return 0;
    }

    private void devicesDialog() {
        if (mDevicesDialog == null) {
            final String[] devices = SThermometer.getDevices();
            if (devices == null || devices.length == 0) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(devices, checkDeviceItem(devices, curDevice),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curDevice = devices[which];
                            deviceButton.setText(curDevice);
                            mDevicesDialog.dismiss();
                        }
                    });
            mDevicesDialog = builder.create();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.serialport_device) {
            if (mDevicesDialog != null) mDevicesDialog.show();
        } else if (id == R.id.serialport_rate) {
            if (mRateDialog != null) mRateDialog.show();
        } else if (v == mTakeButton) {
            if (mTemptakeDialog != null) mTemptakeDialog.distroy();
            mTemptakeDialog = null;
            mTemptakeDialog = new TemptakeDialog(this, mCurProduct, new TemptakeDialog.TakeTempCallback() {

                @Override
                public void callback(TakeTempEntity tempEntity) {
                    setButtonValue(mTakeButton);
                }
            });
            mTemptakeDialog.show();
        }
    }

    public void onSerialStart(View view) {
        if (lastDevice.equals(curDevice) && lastRate == curRate) return;
        if (mSerialFragment != null) {
            mSerialFragment.measure(curDevice, curRate);
        }
        lastDevice = curDevice;
        lastRate = curRate;
    }

    TakeTempEntity curTempEntity;

    private void setButtonValue(Button view) {
        curTempEntity = mCurProduct.getTakeTempEntity();
        if (curTempEntity == null) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setText("温度补偿(" + curTempEntity.getDistances() + "cm)");
            view.setVisibility(View.VISIBLE);
        }
    }

    TemptakeDialog mTemptakeDialog;

}

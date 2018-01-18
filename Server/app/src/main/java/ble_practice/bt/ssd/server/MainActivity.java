package ble_practice.bt.ssd.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = "ted_log";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    Button btn_adv;
    boolean mAdvertising = false;

    private AdvertiseData creatAdvertiseData(byte[] data) {
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.addManufacturerData(0x01AC, data);
        return mDataBuilder.build();
    }

    private AdvertiseSettings createAdvertiseSettings(boolean connectable, int timeout) {
        AdvertiseSettings.Builder mSettingBuilder = new AdvertiseSettings.Builder();
        mSettingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mSettingBuilder.setConnectable(connectable);
        mSettingBuilder.setTimeout(timeout);
        return mSettingBuilder.build();
    }


    public void startAdvertising() {
        byte[] broadcatData = {0x34, 0x56};
        mBluetoothLeAdvertiser.startAdvertising(
                createAdvertiseSettings(true, 0),
                creatAdvertiseData(broadcatData),
                mAdvertiseStartCallback);
    }

    public void stopAdvertising() {
        Log.d(LOG_TAG, "stop---------");
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseStopCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setListener();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

    }

    void findView() {
        btn_adv = (Button) findViewById(R.id.btn_adv);
    }

    void setListener() {
        btn_adv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "OWO");
                if (!mAdvertising)
                    startAdvertising();
                else
                    stopAdvertising();
            }
        });


    }

    private AdvertiseCallback mAdvertiseStartCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            mAdvertising = true;
            btn_adv.setText("stop");
            Log.d(LOG_TAG, "start advertising successfully");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(LOG_TAG, "start advertising unsuccessfully");
        }
    };

    private AdvertiseCallback mAdvertiseStopCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            mAdvertising = false;
            btn_adv.setText("start");
            Log.d(LOG_TAG, "stop advertising successfully");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(LOG_TAG, "stop advertising unsuccessfully");
        }
    };

}

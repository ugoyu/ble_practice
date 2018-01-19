package ble_practice.bt.ssd.server;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.PeriodicAdvertisingParameters;

import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;

import android.util.Log;

/**
 * Created by root on 2018/1/19.
 */

public class AdvertiserWrapperService extends Service {
    private final String LOG_TAG = "AdvertiserWrapperService";
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    AdvertiserEventListener mEventListener;
    boolean mAdvertising = false;


    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

    }
    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");

        mBluetoothLeAdvertiser = null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void setEventListener(AdvertiserEventListener eventListener) {
        mEventListener = eventListener;
    }
    public boolean isAdvertising() { return mAdvertising; }
    public void startAdvertising() {}
    public void stopAdvertising() {}

    private final IBinder mBiner = new LocalBinder();
    public class LocalBinder extends Binder {
        AdvertiserWrapperService getService() {
            return AdvertiserWrapperService.this;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        return mBiner;
    }

    private AdvertiseData creatAdvertiseData() {
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.setIncludeDeviceName(true);
        return mDataBuilder.build();
    }

    private AdvertisingSetParameters createAdvertisingSetPararmeters(boolean connectable) {
        AdvertisingSetParameters.Builder mParametersBuilder = new AdvertisingSetParameters.Builder();
        mParametersBuilder.setConnectable(connectable);
        mParametersBuilder.setLegacyMode(false);
        mParametersBuilder.setScannable(false);
        return mParametersBuilder.build();
    }

    private PeriodicAdvertisingParameters createPeriodicAdvertisingParameters() {
        PeriodicAdvertisingParameters.Builder mParametersBuilder = new PeriodicAdvertisingParameters.Builder();
        return mParametersBuilder.build();
    }

    public void startAdvertisingSet() {

        mBluetoothLeAdvertiser.startAdvertisingSet(
                createAdvertisingSetPararmeters(true),
                creatAdvertiseData(),
                null,
                null,
                null,
                mAdvertiseSetCallback);
    }

    public void stopAdvertisingSet() {

        mBluetoothLeAdvertiser.stopAdvertisingSet(mAdvertiseSetCallback);
    }


    private AdvertisingSetCallback mAdvertiseSetCallback = new AdvertisingSetCallback() {
        @Override
        public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
            super.onAdvertisingSetStarted(advertisingSet, txPower, status);
            Log.d(LOG_TAG, "start advertising set successfully");
            mEventListener.onAdvertisingStateChanged(true);
        }

        @Override
        public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
            Log.d(LOG_TAG, "stop advertising set successfully");
            super.onAdvertisingSetStopped(advertisingSet);
            mEventListener.onAdvertisingStateChanged(false);
        }
    };

    public interface AdvertiserEventListener {
        public void onAdvertisingStateChanged(boolean state);
    }

}

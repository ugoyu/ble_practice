package ble_practice.bt.ssd.server;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.PeriodicAdvertisingParameters;

import android.content.Context;
import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;

import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 2018/1/19.
 */

public class AdvertiserWrapperService extends Service {

    public final ParcelUuid TEXT_UUID = ParcelUuid.fromString("0000ae8f-0000-1000-8000-123456789000");

    private final String LOG_TAG = "AdvertiserWrapperService";
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    AdvertiserEventListener mEventListener;
    BluetoothGattServer mBluetootGattServer;
    private int mConnectionState;
    private BluetoothDevice mConnectedDevice;

    boolean mAdvertising = false;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetootGattServer =  manager.openGattServer(this, mGattServerCallback);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");

        mBluetoothLeAdvertiser = null;
    }

    public void setEventListener(AdvertiserEventListener eventListener) {
        mEventListener = eventListener;
    }

    void testC() {
        Log.d(LOG_TAG, "OWO test");

        BluetoothGattCharacteristic characteristic1 = new BluetoothGattCharacteristic(
                java.util.UUID.fromString("0000ccc1-0000-1000-8000-00805f9b34fb"),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic characteristic2 = new BluetoothGattCharacteristic(
                java.util.UUID.fromString("0000ccc2-0000-1000-8000-00805f9b34fb"),
                BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE|BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattService service1 = new BluetoothGattService(java.util.UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb"),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattService service2 = new BluetoothGattService(java.util.UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fc"),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        service1.addCharacteristic(characteristic1);
        service2.addCharacteristic(characteristic2);


        mBluetootGattServer.addService(service1);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mBluetootGattServer.addService(service2);


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<BluetoothGattService> services = mBluetootGattServer.getServices();

        if (services.size() > 0) {
            Log.d(LOG_TAG, services.get(0).getUuid() + "");
            //Log.d(LOG_TAG, services.get(1).getUuid() + "");
        }
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
        byte data[] = "TED".getBytes();
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.setIncludeDeviceName(true);
        mDataBuilder.addServiceData(TEXT_UUID, data);

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

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(LOG_TAG, "onConnectionStateChange(): " + device + " " + newState);
            super.onConnectionStateChange(device, status, newState);
            mConnectionState = newState;
            mConnectedDevice = device;
            if (mEventListener != null) {
                mEventListener.onConnectStateChanged(mConnectedDevice, mConnectionState);
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d(LOG_TAG, "status:" + status + "onServiceAdded" + service.getUuid());
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(LOG_TAG, " onCharacteristicReadRequest requestId:" + requestId + " offset:" + offset + " characteristic:" + characteristic.getUuid().toString());

            mBluetootGattServer.sendResponse(device, requestId, 0, offset, "Hello".getBytes());

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.e(LOG_TAG, " onCharacteristicWriteRequest requestId:" + requestId + " preparedWrite:" + preparedWrite + " responseNeeded:" + responseNeeded + " offset:" + offset + " value:" + new String(value) + " characteristic:" + characteristic.getUuid().toString());
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);


        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
        }

        @Override
        public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(device, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyRead(device, txPhy, rxPhy, status);
        }
    };

    private AdvertisingSetCallback mAdvertiseSetCallback = new AdvertisingSetCallback() {
        @Override
        public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
            super.onAdvertisingSetStarted(advertisingSet, txPower, status);
            Log.d(LOG_TAG, "start advertising set successfully");
            mAdvertising = true;
            mEventListener.onAdvertisingStateChanged(mAdvertising);
        }

        @Override
        public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
            Log.d(LOG_TAG, "stop advertising set successfully");
            super.onAdvertisingSetStopped(advertisingSet);
            mAdvertising = false;
            mEventListener.onAdvertisingStateChanged(mAdvertising);
        }
    };

    public interface AdvertiserEventListener {
        public void onAdvertisingStateChanged(boolean state);
        public void onConnectStateChanged(BluetoothDevice device, int connectState);
    }

}

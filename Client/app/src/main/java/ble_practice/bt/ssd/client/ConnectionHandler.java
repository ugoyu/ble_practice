package ble_practice.bt.ssd.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Created by root on 1/23/18.
 */

public class ConnectionHandler {
    private String TAG = MainActivity.TAG;

    private final int MSG_CONNECTION_STATE_CALLBACK     = 1;
    private final int MSG_SERVICE_DISCOVERED_CALLBACK   = 2;

    private BluetoothGatt           mGatt;

    private Context                 mContext;
    private BluetoothManager        mManager;
    private BluetoothAdapter        mAdapter;

    private HandlerThread           mHandlerThread;
    private MsgHandler              mHandler;

    public ConnectionHandler(Context context) {
        mContext = context;
    }

    public void initConnectionHandler() {
        mManager = (BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE);
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("handlerThread");
            mHandlerThread.start();
            mHandler = new MsgHandler(mHandlerThread.getLooper());
        }
    }

    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }

    public BluetoothManager getManager() {
        return mManager;
    }

    public BluetoothGatt getGatt () {
        return mGatt;
    }

    public void connectGatt(BluetoothDevice device, boolean auto) {
        device.connectGatt(mContext, auto, mConnectionCallback);
    }

    private void handleConnectionCallback(BluetoothGatt gatt) {
        Log.d(TAG, "handleConnectionCallback");

        BluetoothDevice device = gatt.getDevice();

        int state = mManager.getConnectionState(device, BluetoothProfile.GATT);

        String text = device.getAddress() + " connection state: " + state;
        Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        toast.show();

        if (state == BluetoothGatt.STATE_CONNECTED)
            gatt.discoverServices();
    }

    private void handleServiceDiscovered(BluetoothGatt gatt) {
        mGatt = gatt;

        String text = gatt.getDevice().getAddress() + " service discovered," +
                "service:\r\n" + gatt.getServices();
        /*Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        toast.show();*/

        Log.d(TAG, text);

        Intent intent = new Intent(mContext, ConnectedDeviceActivity.class);
        mContext.startActivity(intent);

    }

    public class MsgHandler extends Handler {
        public MsgHandler (Looper loop) {
            super(loop);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.what);

            switch (msg.what) {
                case MSG_CONNECTION_STATE_CALLBACK:
                    handleConnectionCallback((BluetoothGatt)msg.obj);
                    break;
                case MSG_SERVICE_DISCOVERED_CALLBACK:
                    handleServiceDiscovered((BluetoothGatt)msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
    BluetoothGattCallback mConnectionCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange, state=" + newState);
            Message msg = mHandler.obtainMessage(
                    MSG_CONNECTION_STATE_CALLBACK, gatt);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered, status:" + status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                Message msg = mHandler.obtainMessage(
                        MSG_SERVICE_DISCOVERED_CALLBACK, gatt);
                mHandler.sendMessage(msg);
            } else {
                mGatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };


}

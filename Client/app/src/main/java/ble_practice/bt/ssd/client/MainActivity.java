package ble_practice.bt.ssd.client;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Test Client";

    private final ParcelUuid UUID = ParcelUuid.fromString("0000ae8f-0000-1000-8000-123456789000");
    private final int MSG_CONNECTION_STATE_CALLBACK     = 1;
    private final int MSG_SERVICE_DISCOVERED_CALLABCK   = 2;


    private Context             mContext;
    private BluetoothAdapter    mAdapter;
    private BluetoothLeScanner  mScanner;

    private boolean             mScanning;

    private static BluetoothGatt    mGatt;

    private Map<Button, BluetoothDevice>        mScanResults;
    private Map<BluetoothDevice, ScanRecord>    mScanRecords;

    private HandlerThread mHandlerThread;
    private MsgHandler mHandler;


    /* UI items */
    Button btnStartScan;
    LinearLayout llScanResult;

    void init() {
        mContext = this;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner = mAdapter.getBluetoothLeScanner();
        mScanResults = new HashMap<>();
        mScanRecords = new HashMap<>();

        mScanning = false;
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("handlerThread");
            mHandlerThread.start();
            mHandler = new MsgHandler(mHandlerThread.getLooper());
        }
    }

    void initView() {
        btnStartScan = (Button)findViewById(R.id.btn_startscan);
        btnStartScan.setOnClickListener(clStartScan);

        llScanResult = (LinearLayout)findViewById(R.id.ll_scanResult);
    }

    void startScan(boolean enable) {
        if(enable) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setLegacy(false)
                    .build();

            llScanResult.removeAllViews();
            mScanResults.clear();
            mScanner.startScan(null, settings, mScannerCallback);
            mScanning = true;
            btnStartScan.setText("stop Scan");
        } else {
            mScanner.stopScan(mScannerCallback);
            btnStartScan.setText("start Scan");
            mScanning = false;
        }
    }

    View.OnClickListener clScanResult = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btn = (Button)view;
            BluetoothDevice device = mScanResults.get(btn);
            ScanRecord record = mScanRecords.get(device);

            /* Demo to show how to get info from device record */
            byte data[] = record.getServiceData(UUID);
            String text = "Name:" + record.getDeviceName() + "\r\n"
                        + "Address:" + device.getAddress() + "\r\n"
                        + "Service Data:\r\n"
                        + (data == null ? "null" : new String(data));
            Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
            toast.show();

            if(mScanning)
                startScan(false);


            device.connectGatt(mContext, false, mConnectionCallback);
        }
    };

    View.OnClickListener clStartScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            turnOnBluetooth();
            startScan(!mScanning);
        }
    };

    void turnOnBluetooth() {
        if (mAdapter.getState() != BluetoothAdapter.STATE_ON)
            mAdapter.enable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize functionality
        init();
        //Initialize UI
        initView();

        //Request location permission for BLE events
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

        //Auto turn ON Bluetooth
        turnOnBluetooth();
    }

    String parseScanResult(ScanResult result) {
        String isLegacy = result.isLegacy() ? "Legacy | " : "Extend | ";
        String deviceName = "";
        if (result.getDevice().getName() != null)
            deviceName = " (" + result.getDevice().getName() + ")";

        return (isLegacy + result.getDevice().getAddress() + deviceName + "\r\n"
                + "RSSI:" + result.getRssi());
    }

    ScanCallback mScannerCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

            synchronized (mScanResults) {
                if (!mScanResults.containsValue(device)) {
                    /* Create button and add device to HashMap */
                    Button btn = new Button(mContext);
                    btn.setGravity(Gravity.LEFT);
                    btn.setText(parseScanResult(result));
                    btn.setBackgroundResource(R.drawable.button_style);
                    btn.setOnClickListener(clScanResult);
                    llScanResult.addView(btn);

                    /* Highlight server app's advertising */
                    if(result.getScanRecord().getServiceData(UUID) != null)
                        btn.setTextColor(Color.RED);

                    /* Save scanRecord and device info */
                    mScanResults.put(btn, device);
                    mScanRecords.put(device, result.getScanRecord());
                } else {
                    Button btn;
                    for (Object o : mScanResults.keySet()) {
                        btn = (Button) o;
                        BluetoothDevice preDev = mScanResults.get(btn);

                        if(preDev.getAddress().equals(device.getAddress())) {
                            /* Update new scanRecord and device info to HashMap */
                            ScanRecord record = mScanRecords.get(preDev);
                            mScanRecords.remove(preDev);
                            mScanRecords.put(device, record);
                            mScanResults.replace(btn, device);

                            /* Update button text */
                            btn.setText(parseScanResult(result));
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void handleConnectionCallback(BluetoothGatt gatt) {
        Log.d(TAG, "handleConnectionCallback");

        BluetoothDevice device = gatt.getDevice();
        BluetoothManager manager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        int state = manager.getConnectionState(device, BluetoothProfile.GATT);

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

        Intent intent = new Intent(this, ConnectedDeviceActivity.class);
        startActivity(intent);

    }

    private class MsgHandler extends Handler {
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
                case MSG_SERVICE_DISCOVERED_CALLABCK:
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
                        MSG_SERVICE_DISCOVERED_CALLABCK, gatt);
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

    public static BluetoothGatt getGatt () {
        return mGatt;
    }
}

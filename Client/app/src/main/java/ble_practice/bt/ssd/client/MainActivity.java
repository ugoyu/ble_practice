package ble_practice.bt.ssd.client;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
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

    private Context             mContext;
    private BluetoothAdapter    mAdapter;
    private BluetoothLeScanner  mScanner;

    private boolean             mScanning;

    private Map<Button, BluetoothDevice>        mScanResults;
    private Map<BluetoothDevice, ScanRecord>    mScanRecords;

    /* UI items */
    Button btnStartScan;
    LinearLayout llScanResult;

    void initView() {
        btnStartScan = (Button)findViewById(R.id.btn_startscan);
        btnStartScan.setOnClickListener(clStartScan);

        llScanResult = (LinearLayout)findViewById(R.id.ll_scanResult);
    }

    View.OnClickListener clScanResult = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btn = (Button)view;
            BluetoothDevice device = mScanResults.get(btn);
            ScanRecord record = mScanRecords.get(device);

            /* Demo to show how to get info from device record */
            String text = "Name:" + record.getDeviceName() + "\r\n"
                        + "Address:" + device.getAddress();
            Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    View.OnClickListener clStartScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            turnOnBluetooth();
            Button btn = (Button)view;

            if(mScanning) {
                mScanner.stopScan(mScannerCallback);
                btn.setText("start Scan");
                mScanning = false;
            } else {
                ScanSettings settings = new ScanSettings.Builder()
                        .setLegacy(false)
                        .build();

                llScanResult.removeAllViews();
                mScanResults.clear();
                mScanner.startScan(null, settings, mScannerCallback);
                mScanning = true;
                btn.setText("stop Scan");
            }
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

        mContext = this;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner = mAdapter.getBluetoothLeScanner();
        mScanResults = new HashMap<>();
        mScanRecords = new HashMap<>();

        mScanning = false;

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
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
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

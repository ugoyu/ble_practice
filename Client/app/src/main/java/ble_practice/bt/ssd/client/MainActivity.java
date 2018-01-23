package ble_practice.bt.ssd.client;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Test Client";

    private final ParcelUuid UUID = ParcelUuid.fromString("0000ae8f-0000-1000-8000-123456789000");

    private Context             mContext;
    private BluetoothManager    mManager;
    private BluetoothAdapter    mAdapter;
    private BluetoothLeScanner  mScanner;

    private boolean             mScanning;

    private Map<Button, BluetoothDevice>        mScanResults;
    private Map<BluetoothDevice, ScanRecord>    mScanRecords;

    private static ConnectionHandler    mConnectionHandler;

    /* UI items */
    Button btnStartScan;
    LinearLayout llScanResult;

    void init() {

        mContext = this;
        mConnectionHandler = new ConnectionHandler(this);
        mConnectionHandler.initConnectionHandler();
        mManager = mConnectionHandler.getManager();
        mAdapter = mConnectionHandler.getAdapter();
        mScanner = mAdapter.getBluetoothLeScanner();
        mScanResults = new HashMap<>();
        mScanRecords = new HashMap<>();

        mScanning = false;
    }

    void initView() {
        btnStartScan = (Button)findViewById(R.id.btn_startscan);
        btnStartScan.setOnClickListener(clStartScan);

        llScanResult = (LinearLayout)findViewById(R.id.ll_scanResult);
    }

    public static ConnectionHandler getConnectionHandler () {
        return mConnectionHandler;
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

            mConnectionHandler.connectGatt(device, false);
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
}

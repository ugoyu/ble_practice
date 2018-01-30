package ble_practice.bt.ssd.client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

public class ConnectedDeviceActivity extends AppCompatActivity {
    public static final UUID ALERT_LEVEL = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    private BluetoothGatt       mGatt;
    private BluetoothDevice     mDevice;
    private List<BluetoothGattService> mGattServices;

    LinearLayout mLlOverView;
    ConnectionHandler mConnectionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);
        mConnectionHandler = MainActivity.getConnectionHandler();
        mGatt = mConnectionHandler.getGatt();
        mDevice = mGatt.getDevice();
        mGattServices = mGatt.getServices();

        mLlOverView = (LinearLayout)findViewById(R.id.ll_con_overview);
        showInfoToLL();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGatt.disconnect();
    }

    void parseStringToLL(String str) {
        TextView view = new TextView(this);
        view.setText(str);

        //view.setTextSize(20);
        mLlOverView.addView(view);
    }

    void parseStringToLL(String str, int size) {
        TextView view = new TextView(this);
        view.setText(str);

        view.setTextSize(size);
        mLlOverView.addView(view);
    }

    void showInfoToLL() {
        String str;

        /* Add device name/address */
        str = "Device: ";
        if(mDevice.getAddress() != null) {
            if(mDevice.getName() != null) {
                str = mDevice.getName() + " / ";
            }
            str = str + mDevice.getAddress();
        }
        parseStringToLL(str, 20);

        /* Show device services */
        str = "\r\n\r\nServices:";
        parseStringToLL(str, 20);

        parseStringToLL("--------------------------------------------------------------------");
        for(int i=0 ; i<mGattServices.size() ; i++) {
            BluetoothGattService service = mGattServices.get(i);
            parseStringToLL("   UUID:");
            str = "   " + service.getUuid();
            parseStringToLL(str);

            List<BluetoothGattCharacteristic> charList = service.getCharacteristics();
            for(int j=0 ; j<charList.size() ; j++) {
                if(j==0) {
                    parseStringToLL("       Characteristics:");
                }
                parseStringToLL("       " + charList.get(j).getUuid());
                if(charList.get(j).getUuid().equals(ALERT_LEVEL)) {
                    BluetoothGattCharacteristic alertLevel = charList.get(j);
                    byte[] value = {0x02};
                    alertLevel.setValue(value);
                    mGatt.writeCharacteristic(alertLevel);
                }

            }
            parseStringToLL("--------------------------------------------------------------------");
        }

    }
}

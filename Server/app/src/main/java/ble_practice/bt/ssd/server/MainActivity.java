package ble_practice.bt.ssd.server;

import android.Manifest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity
        implements AdvertiserWrapperService.AdvertiserEventListener {


    private static final String LOG_TAG = "ted_log";


    Button btn_adv;


    AdvertiserWrapperService mAdvertiserWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setListener();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);


        Intent intent = new Intent(this, AdvertiserWrapperService.class);
        startService(intent);
        Log.d(LOG_TAG, "----");
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "AdvertiserWrapperService connected");
            mAdvertiserWrapper = ((AdvertiserWrapperService.LocalBinder) iBinder).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mAdvertiserWrapper = null;
        }
    };





    void findView() {
        btn_adv = (Button) findViewById(R.id.btn_adv);
    }

    void setListener() {
        btn_adv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "OWO");
                boolean isAdvertising = mAdvertiserWrapper.isAdvertising();

                if (!isAdvertising) {
                    mAdvertiserWrapper.startAdvertisingSet();
                }
                else {
                    mAdvertiserWrapper.stopAdvertisingSet();
                }
            }
        });


    }


    @Override
    public void onAdvertisingStateChanged(boolean state) {
        btn_adv.setText((!state) ? "stop" : "start");
    }
}

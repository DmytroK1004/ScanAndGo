package com.example.uhf_bt;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.rscja.deviceapi.BluetoothReader;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;
import com.rscja.team.qcom.deviceapi.S;

import java.util.ArrayList;
import java.util.List;



public class BlueActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner scanner;
    private boolean isScanning;
    private long time = 5000;
    private RFIDWithUHFBLE uhf = RFIDWithUHFBLE.getInstance();
    private boolean isInventory = false;
    private int currentStatus;
    Button btns,btne,btncon,btndiscon;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue);
        btns=(Button)findViewById(R.id.csstart);
        btne=(Button)findViewById(R.id.csend);
        btncon=(Button)findViewById(R.id.conn);
        btndiscon=(Button)findViewById(R.id.disconn);
        init();
      boolean re2=  uhf.init(this);


        btncon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uhf.connect("C6:2C:61:B0:BA:08");
                uhf.setSupportRssi(true);
                //设置频率
                boolean re=    uhf.setFrequencyMode(0x08);
                //设置功率
                boolean re1=     uhf.setPower(30);
            }
        });
        btndiscon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                scanner.stopScan(scanCallback);
                uhf.disconnect();

            }
        });

        btns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                scanner.stopScan(scanCallback);

                uhf.setInventoryCallback(new IUHFInventoryCallback() {
                    @Override
                    public void callback(UHFTAGInfo uhftagInfo) {
                        Log.e("xsy", "=========callback========");
                    }
                });

                uhf.startInventoryTag();

            }
        });

        btne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                scanner.stopScan(scanCallback);

                uhf.stopInventory();
            }
        });


//        uhf.setOnDataChangeListener(new BluetoothReader.OnDataChangeListener() {
//            @Override
//            public void receive(byte[] bytes) {
////                Log.e("xsy", "=========receive========"+ByteUtil.BinaryToHexString(bytes));
//            }
//        });

        uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int i) {
                Log.e("xsy", "=========onKeyDown========");
            }

            @Override
            public void onKeyUp(int i) {
                Log.e("xsy", "=========onKeyUp========");
            }
        });

        uhf.setConnectionStatusCallback(new ConnectionStatusCallback<Object>() {
            @Override
            public void getStatus(ConnectionStatus connectionStatus, Object o) {
                Log.e("xsy", "=========getStatus========"+connectionStatus);
            }
        });

        setListener();
    }

    private void init() {




    }

    @SuppressLint("MissingPermission")
    private void setListener() {



    }





}
package com.example.uhf_bt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.fragment.BTRenameFragment;
import com.example.uhf_bt.fragment.BarcodeFragment;
import com.example.uhf_bt.fragment.UHFEraseFragment;
import com.example.uhf_bt.fragment.UHFKillFragment;
import com.example.uhf_bt.fragment.UHFLocationFragment;
import com.example.uhf_bt.fragment.UHFLockFragment;
import com.example.uhf_bt.fragment.UHFRadarLocationFragment;
import com.example.uhf_bt.fragment.UHFReadWriteFragment;
import com.example.uhf_bt.fragment.UHFReadTagFragment;
import com.example.uhf_bt.fragment.UHFSetFragment;
import com.example.uhf_bt.fragment.UHFUpdataFragment;
import com.example.uhf_bt.tool.ExcelUtils;
import com.example.uhf_bt.tool.FileUtils;
import com.example.uhf_bt.tool.SPUtils;
import com.example.uhf_bt.tool.Utils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback;
import com.rscja.deviceapi.interfaces.ScanBTCallback;
import com.rscja.team.qcom.utility.LogUtility_qcom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTabHost;

import no.nordicsemi.android.dfu.BuildConfig;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";

    public boolean isScanning = false;
    public String remoteBTName = "";
    public String remoteBTAdd = "";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public List<UHFTAGInfo> tagList;
    public String selectEPC = null;

    public boolean isShowDuplicateTags = true;

    private NfcAdapter nfcAdapter;
    public BluetoothDevice mDevice = null;
    private FragmentTabHost mTabHost;
    private FragmentManager fm;
    private Button btn_connect, btn_search;
    private TextView tvAddress;
    public BluetoothAdapter mBtAdapter = null;
    public RFIDWithUHFBLE uhf = RFIDWithUHFBLE.getInstance();
    BTStatus btStatus = new BTStatus();

    public static final String SHOW_HISTORY_CONNECTED_LIST = "showHistoryConnectedList";

    private boolean mIsActiveDisconnect = true; // 是否主动断开连接
    private static final int RECONNECT_NUM = Integer.MAX_VALUE; // 重连次数
    private int mReConnectCount = RECONNECT_NUM; // 重新连接次数

    private Timer mDisconnectTimer = new Timer();
    private DisconnectTimerTask timerTask;
    private long timeCountCur; // 断开时间选择
    private long period = 1000 * 30; // 隔多少时间更新一次
    private long lastTouchTime = System.currentTimeMillis(); // 上次接触屏幕操作的时间戳
    public static boolean isKeyDownUP = false;


    private static final int RUNNING_DISCONNECT_TIMER = 10;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RUNNING_DISCONNECT_TIMER:
                    long time = (long) msg.obj;
                    formatConnectButton(time);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG) {
            setTitle(String.format("%s(v%s-debug)", getString(R.string.app_name), getVerName()));
        } else {
            setTitle(String.format("%s(v%s)", getString(R.string.app_name), getVerName()));
        }
        initUI();
        checkPermission();
        uhf.init(getApplicationContext());
        Utils.initSound(getApplicationContext());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Log.e(TAG, "onCreate: 设备不支持NFC功能");
        } else {
            handleIntent(getIntent());
        }

        // DeviceAPI日志开关，非调试不建议开启
        LogUtility_qcom.setDebug(true);
//        LogUtility_qcom.setWriteLog(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        );
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        uhf.free();
        Utils.freeSound();
        connectStatusList.clear();
        cancelDisconnectTimer();
        super.onDestroy();
        android.os.Process.killProcess(Process.myPid());
    }

    protected void initUI() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(this);

        fm = getSupportFragmentManager();
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, fm, R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_inventory)).setIndicator(getString(R.string.title_inventory)), UHFReadTagFragment.class, null);
        // mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_inventory2)).setIndicator(getString(R.string.title_inventory2)), UHFNewReadTagFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.uhf_msg_tab_set)).setIndicator(getString(R.string.uhf_msg_tab_set)), UHFSetFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.uhf_radar_loaction)).setIndicator(getResources().getString(R.string.uhf_radar_loaction)), UHFRadarLocationFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.location)).setIndicator(getString(R.string.location)), UHFLocationFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_2d_Scan)).setIndicator(getString(R.string.title_2d_Scan)), BarcodeFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.uhf_msg_tab_read_write)).setIndicator(getString(R.string.uhf_msg_tab_read_write)), UHFReadWriteFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.uhf_msg_tab_lock)).setIndicator(getString(R.string.uhf_msg_tab_lock)), UHFLockFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.uhf_msg_tab_kill)).setIndicator(getString(R.string.uhf_msg_tab_kill)), UHFKillFragment.class, null);

//        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.uhf_msg_tab_erase)).setIndicator(getString(R.string.uhf_msg_tab_erase)), UHFEraseFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_update)).setIndicator(getString(R.string.title_update)), UHFUpdataFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_bt_rename)).setIndicator(getString(R.string.title_bt_rename)), BTRenameFragment.class, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
//                if (isScanning) {
//                    showToast(R.string.title_stop_read_card);
//                } else
                if (uhf.getConnectStatus() == ConnectionStatus.CONNECTING) {
                    showToast(R.string.connecting);
                } else if (uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                    disconnect(true);
                } else {
                    showBluetoothDevice(true);
                }
                break;
            case R.id.btn_search:
                if (isScanning) {
                    showToast(R.string.title_stop_read_card);
                } else if (uhf.getConnectStatus() == ConnectionStatus.CONNECTING) {
                    showToast(R.string.connecting);
                } else {
                    showBluetoothDevice(false);
                }
                break;
        }
    }

    private void formatConnectButton(long disconnectTime) {
        if (uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
            if (!isScanning && System.currentTimeMillis() - lastTouchTime > 1000 * 30 && timerTask != null) {
                long minute = disconnectTime / 1000 / 60;
                if (minute > 0) {
                    btn_connect.setText(getString(R.string.disConnectForMinute, minute)); //倒计时分
                } else {
                    btn_connect.setText(getString(R.string.disConnectForSecond, disconnectTime / 1000)); // 倒计时秒
                }
            } else {
                btn_connect.setText(R.string.disConnect);
            }
        } else {
            btn_connect.setText(R.string.Connect);
        }
    }

    /**
     * 重置断开时间
     */
    public void resetDisconnectTime() {
        timeCountCur = SPUtils.getInstance(getApplicationContext()).getSPLong(SPUtils.DISCONNECT_TIME, 0);
        if (timeCountCur > 0) {
            formatConnectButton(timeCountCur);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        lastTouchTime = System.currentTimeMillis();
        resetDisconnectTime();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (!isScanning) {
            if (item.getItemId() == R.id.UHF_Battery) {
                String ver = getString(R.string.action_uhf_bat) + ":" + uhf.getBattery() + "%";
                Utils.alert(MainActivity.this, R.string.action_uhf_bat, ver, R.drawable.webtext);
            } else if (item.getItemId() == R.id.UHF_T) {
                String temp = getString(R.string.title_about_Temperature) + ":" + uhf.getTemperature() + "℃";
                Utils.alert(MainActivity.this, R.string.title_about_Temperature, temp, R.drawable.webtext);
            } else if (item.getItemId() == R.id.UHF_ver) {
                String ver = uhf.getVersion();
                Utils.alert(MainActivity.this, R.string.action_uhf_ver, ver, R.drawable.webtext);
            } else if (item.getItemId() == R.id.Ex10SDKFirmware_ver) {
                String ver = uhf.getEx10SDKFirmware();
                Utils.alert(MainActivity.this, R.string.action_ver, ver, R.drawable.webtext);
            } else if (item.getItemId() == R.id.Mainboard_ver) {
                String ver = uhf.getSTM32Version();
                Utils.alert(MainActivity.this, R.string.action_ver, ver, R.drawable.webtext);
            } else if (item.getItemId() == R.id.ble_ver) {
                HashMap<String, String> versionMap = uhf.getBluetoothVersion();
                if (versionMap != null) {
                    String verMsg = "固件版本：" + versionMap.get(RFIDWithUHFBLE.VERSION_BT_FIRMWARE)
                            + "\n硬件版本：" + versionMap.get(RFIDWithUHFBLE.VERSION_BT_HARDWARE)
                            + "\n软件版本：" + versionMap.get(RFIDWithUHFBLE.VERSION_BT_SOFTWARE);
                    Utils.alert(MainActivity.this, R.string.action_ble_ver, verMsg, R.drawable.webtext);
                }
            } else if (item.getItemId() == R.id.ble_disconnectTime) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_disconnect_time, null);
                final Spinner spDisconnectTime = view.findViewById(R.id.spDisconnectTime);
                int index = SPUtils.getInstance(getApplicationContext()).getSPInt(SPUtils.DISCONNECT_TIME_INDEX, 0);
                spDisconnectTime.setSelection(index);
                Utils.alert(this, R.string.disconnectTime, view, R.drawable.webtext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int index = spDisconnectTime.getSelectedItemPosition();
                        long time = 1000 * 60 * 60 * index;
                        SPUtils.getInstance(getApplicationContext()).setSPInt(SPUtils.DISCONNECT_TIME_INDEX, index);
                        SPUtils.getInstance(getApplicationContext()).setSPLong(SPUtils.DISCONNECT_TIME, time);
                        switch (index) {
                            case 0:
                                cancelDisconnectTimer();
                                break;
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                                if (uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                                    cancelDisconnectTimer();
                                    startDisconnectTimer(time);
                                }
                                break;
                        }
                    }
                });
            } else if (item.getItemId() == R.id.exportData) {
                //导出数据
                if (tagList != null && tagList.size() > 0) {
                    new ExcelTask(this).execute();
                } else {
                    Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
                }
            } else if (item.getItemId() == R.id.r2_extract_data) {
                //R2提取数据
                Intent intent = new Intent(MainActivity.this, ExtractDataActivity.class);
                startActivity(intent);
            }
        } else {
            showToast(R.string.title_stop_read_card);
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                        disconnect(true);
                    }
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    if (data.getBooleanExtra("isSearch", true)) {
                        uhf.startScanBTDevices(new ScanBTCallback() {
                            @Override
                            public void getDevices(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

                            }
                        });
                        SystemClock.sleep(2000);
                        uhf.stopScanBTDevices();
                    }
                    tvAddress.setText(String.format("%s(%s)\nconnecting", mDevice.getName(), deviceAddress));
                    connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    showToast("Bluetooth has turned on ");
                } else {
                    showToast("Problem in BT Turning ON ");
                }
                break;
            case PERMISSION_REQUEST_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        showPermissionAlertDialog(getString(R.string.permission_external_storage), (dialog, which) -> checkReadWritePermission());
                    }
                } else {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        showPermissionAlertDialog(getString(R.string.permission_external_storage), (dialog, which) -> checkReadWritePermission());
                    }
                }
                break;
            case REQUEST_ACTION_LOCATION_SETTINGS:
                if (isLocationEnabled()) {
                    checkPermission();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission))
                            .setMessage(getString(R.string.open_location_msg))
                            .setPositiveButton(getString(R.string.open_location), (dialogInterface, i) -> {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, REQUEST_ACTION_LOCATION_SETTINGS);
                            })
                            .setNegativeButton(getString(R.string.permission_cancel), (dialog1, which) -> finish())
                            .setCancelable(false)
                            .show();
                }
            default:
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void showBluetoothDevice(boolean isHistory) {
        if (mBtAdapter == null) {
            showToast("Bluetooth is not available");
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            newIntent.putExtra(SHOW_HISTORY_CONNECTED_LIST, isHistory);
            startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            cancelDisconnectTimer();
        }
    }

    public void connect(String deviceAddress) {
        if (uhf.getConnectStatus() == ConnectionStatus.CONNECTING) {
            showToast(R.string.connecting);
        } else {
            showToast(getString(R.string.Connect) + " " + deviceAddress);
            uhf.connect(deviceAddress, btStatus);
        }
    }

    public void disconnect(boolean isActiveDisconnect) {
        cancelDisconnectTimer();
        mIsActiveDisconnect = isActiveDisconnect; // 主动断开为true
        uhf.disconnect();
    }

    /**
     * 重新连接
     *
     * @param deviceAddress
     */
    private void reConnect(String deviceAddress) {
        if (!mIsActiveDisconnect && mReConnectCount > 0) {
            connect(deviceAddress);
            mReConnectCount--;
        }
    }

    /**
     * 应该提示未连接状态
     *
     * @return
     */
    private boolean shouldShowDisconnected() {
        return mIsActiveDisconnect || mReConnectCount == 0;
    }

    class BTStatus implements ConnectionStatusCallback<Object> {
        @Override
        public void getStatus(final ConnectionStatus connectionStatus, final Object device1) {
            runOnUiThread(new Runnable() {
                @SuppressLint("MissingPermission")
                public void run() {
                    BluetoothDevice device = (BluetoothDevice) device1;
                    remoteBTName = "";
                    remoteBTAdd = "";
                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        remoteBTName = device.getName();
                        remoteBTAdd = device.getAddress();
                        Log.i(TAG, "remoteBTName=" + remoteBTName + " remoteBTAdd=" + remoteBTAdd);

                        tvAddress.setText(String.format("%s(%s)\nconnected", remoteBTName, remoteBTAdd));
                        if (shouldShowDisconnected()) {
                            showToast(R.string.connect_success);
                        }

                        timeCountCur = SPUtils.getInstance(getApplicationContext()).getSPLong(SPUtils.DISCONNECT_TIME, 0);
                        if (timeCountCur > 0) {
                            startDisconnectTimer(timeCountCur);
                        } else {
                            formatConnectButton(timeCountCur);
                        }

                        // 保存已链接记录
                        if (!TextUtils.isEmpty(remoteBTAdd)) {
                            saveConnectedDevice(remoteBTAdd, remoteBTName);
                        }


                        mIsActiveDisconnect = false;
                        mReConnectCount = RECONNECT_NUM;
                    } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                        isKeyDownUP = false;
                        cancelDisconnectTimer();
                        formatConnectButton(timeCountCur);
                        if (device != null) {
                            remoteBTName = device.getName();
                            remoteBTAdd = device.getAddress();
//                            if (shouldShowDisconnected())
                            tvAddress.setText(String.format("%s(%s)\ndisconnected", remoteBTName, remoteBTAdd));
                        } else {
//                            if (shouldShowDisconnected())
                            tvAddress.setText("disconnected");
                        }
                        if (shouldShowDisconnected())
                            showToast(R.string.disconnect);

                        boolean reconnect = SPUtils.getInstance(getApplicationContext()).getSPBoolean(SPUtils.AUTO_RECONNECT, false);
                        if (mDevice != null && reconnect) {
                            reConnect(mDevice.getAddress()); // 重连
                        }
                    }

                    for (IConnectStatus iConnectStatus : connectStatusList) {
                        if (iConnectStatus != null) {
                            iConnectStatus.getStatus(connectionStatus);
                        }
                    }
                }
            });
        }
    }

    public void saveConnectedDevice(String address, String name) {
        List<String[]> list = FileUtils.readXmlList();
        for (int k = 0; k < list.size(); k++) {
            if (address.equals(list.get(k)[0])) {
                list.remove(list.get(k));
                break;
            }
        }
        String[] strArr = new String[]{address, name};
        list.add(0, strArr);
        FileUtils.saveXmlList(list);
    }

    public void updateConnectMessage(String oldName, String newName) {
        if (!TextUtils.isEmpty(oldName) && !TextUtils.isEmpty(newName)) {
            tvAddress.setText(tvAddress.getText().toString().replace(oldName, newName));
            remoteBTName = newName;
        }
    }

    //------------连接状态监听-----------------------
    private List<IConnectStatus> connectStatusList = new ArrayList<>();

    public void addConnectStatusNotice(IConnectStatus iConnectStatus) {
        connectStatusList.add(iConnectStatus);
    }

    public void removeConnectStatusNotice(IConnectStatus iConnectStatus) {
        connectStatusList.remove(iConnectStatus);
    }

    public interface IConnectStatus {
        void getStatus(ConnectionStatus connectionStatus);
    }

    private void showPermissionAlertDialog(String msg, DialogInterface.OnClickListener listener) {
        if (msg == null) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.permission_enable), listener)
                .setNegativeButton(getString(R.string.permission_cancel), (dialog1, which) -> finish())
                .setCancelable(false)
                .show();
//            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//            positiveButton.setTextColor(getColor(R.color.font_default));
    }

    //------------------获取权限--------------------------------
    private static final int REQUEST_ACTION_LOCATION_SETTINGS = 99;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100;
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 101;
    private static final int PERMISSION_REQUEST_ACTION_LOCATION_SETTINGS = 103;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 104;
    private static final int PERMISSION_REQUEST_BLUETOOTH_CONNECT = 105;

    // 打开定位，然后依次检查权限：定位权限、蓝牙权限（默认请求就会成功）、存储权限
    private void checkPermission() {
        if (!isLocationEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, REQUEST_ACTION_LOCATION_SETTINGS);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) ||
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            checkReadWritePermission();
        } else if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            checkBluetoothPermission();
        }
    }

    private boolean checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return false;
        }
        return true;
    }

    private void checkReadWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_EXTERNAL_STORAGE);
//                finish();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }

    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    PERMISSION_REQUEST_BLUETOOTH
            );
        } else {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    private boolean isLocationEnabled() {
        try {
            int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "onRequestPermissionsResult requestCode=" + requestCode + " permissions=" + Arrays.toString(permissions) + " grantResults=" + Arrays.toString(grantResults));
        switch (requestCode) {
            case PERMISSION_REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    checkLocationPermission();
                    //BleApplication.getApplication().createDir();
                } else {
                    showPermissionAlertDialog(getString(R.string.permission_external_storage), (dialog, which) -> checkReadWritePermission());
                }
                break;
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBluetoothPermission();
                } else {
                    showPermissionAlertDialog(getString(R.string.permission_location), (dialog, which) -> checkLocationPermission());
                }
                break;
            case PERMISSION_REQUEST_BLUETOOTH:
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showPermissionAlertDialog(getString(R.string.permission_bluetooth), (dialog, which) -> checkBluetoothPermission());
                } else {
                    checkReadWritePermission();
                }
                break;
            case PERMISSION_REQUEST_BLUETOOTH_CONNECT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    showPermissionAlertDialog(getString(R.string.permission_bluetooth), (dialog, which) -> {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_BLUETOOTH_CONNECT);
                    });
                }
        }
    }


    private void startDisconnectTimer(long time) {
        timeCountCur = time;
        timerTask = new DisconnectTimerTask();
        mDisconnectTimer.schedule(timerTask, 0, period);
    }

    public void cancelDisconnectTimer() {
        timeCountCur = 0;
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private class DisconnectTimerTask extends TimerTask {

        @Override
        public void run() {
            Log.e(TAG, "timeCountCur = " + timeCountCur);
            Message msg = mHandler.obtainMessage(RUNNING_DISCONNECT_TIMER, timeCountCur);
            mHandler.sendMessage(msg);
            if (isScanning) {
                resetDisconnectTime();
            } else if (timeCountCur <= 0) {
                disconnect(true);
            }
            timeCountCur -= period;
        }
    }

    public class ExcelTask extends AsyncTask<String, Integer, Boolean> {
        protected ProgressDialog mypDialog;
        protected Activity mContxt;
        boolean isSotp = false;
        String path = "sdcard/uhf" + File.separator + GetTimesyyyymmddhhmmss() + ".xls";
        String txtPath = path.replace("xls", "txt");

        public ExcelTask(Activity act) {
            mContxt = act;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean result = false;
            File f = new File("sdcard/uhf");
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    return false;
                }
            }


            File file = new File(path);
            String[] h = new String[]{"EPC", "TID", "USER", "COUNT", "RSSI",};//{"EPC", "TID", "COUNT", "RSSI"};
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.createExcel(file, h);
            int size = tagList.size();
            List<String[]> list = new ArrayList<>();
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(txtPath, true));

                for (int k = 0; !isSotp && k < size; k++) {
                    String epc = tagList.get(k).getEPC();
                    String tid = tagList.get(k).getTid();
                    String user = tagList.get(k).getUser();
                    String count = String.valueOf(tagList.get(k).getCount());
                    String rssi = tagList.get(k).getRssi();
                    int pro = (int) (div(k + 1, size, 2) * 100);
                    publishProgress(pro);
                    Log.d(TAG, "size:" + tagList.size() + " k=" + k);
                    String[] data = new String[]{
                            epc,
                            tid,
                            user,
                            count,
                            rssi,
                    };
                    list.add(data);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(epc);
                    if (!TextUtils.isEmpty(tid)) {
                        stringBuilder.append(",");
                        stringBuilder.append(tid);
                    }
                    if (!TextUtils.isEmpty(user)) {
                        stringBuilder.append(",");
                        stringBuilder.append(user);
                    }
                    bufferedWriter.write(stringBuilder.toString());
                    bufferedWriter.newLine();
                }


                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (Exception ex) {
                Log.e(TAG, "ex=" + ex.toString());
            }

            publishProgress(101);
            excelUtils.writeToExcel(list);
            notifySystemToScan(file);
            notifySystemToScan(new File(txtPath));
            sleepTime(2000);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 101) {
                mypDialog.setMessage("path:" + path);
            } else {
                mypDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(mContxt);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mypDialog.setMessage("...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.setMax(100);
            mypDialog.setProgress(0);

            mypDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isSotp = true;
                }
            });

            if (mContxt != null) {
                mypDialog.show();
            }
        }

        public String GetTimesyyyymmddhhmmss() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String dt = formatter.format(curDate);
            return dt;
        }

        private float div(float v1, float v2, int scale) {
            BigDecimal b1 = new BigDecimal(Float.toString(v1));
            BigDecimal b2 = new BigDecimal(Float.toString(v2));
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
        }

        public void notifySystemToScan(File file) {
            // mLog.info
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                sendBroadcast(intent);
            }
        }

        private void sleepTime(long time) {
            try {
                Thread.sleep(time);
            } catch (Exception ex) {
            }
        }

    }

    private static final long DOUBLE_PRESS_INTERVAL = 2000; // 双击间隔时间，单位为毫秒
    private long lastBackPressedTime;

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackPressedTime < DOUBLE_PRESS_INTERVAL) {
            super.onBackPressed();
        } else {
            showToast(R.string.msg_exit);
            lastBackPressedTime = currentTime;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.i(TAG, "handleIntent: " + intent.getAction());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                try {
                    ndef.connect();
                    NdefMessage ndefMessage = ndef.getNdefMessage();
                    if (ndefMessage != null) {
                        for (NdefRecord record : ndefMessage.getRecords()) {
                            Log.i(TAG, "record " + record.toString());
                            if (record.toMimeType().equals("application/vnd.bluetooth.ep.oob")) {
                                Log.i(TAG, "record.getPayload()=" + new String(record.getPayload()));
                                String mac = parseOobData(record.getPayload());
                                connect(mac);
                                Log.i(TAG, "Bluetooth MAC: " + mac);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading NFC tag", e);
                } finally {
                    try {
                        ndef.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing NFC connection", e);
                    }
                }
            }
        }
    }

    private String parseOobData(byte[] payload) {
        // 获取MAC地址的部分（假设是从索引1到6，但要检查具体OOB数据结构）
        byte[] macBytes = Arrays.copyOfRange(payload, 2, 8);
        // 倒序字节数组
        for (int i = 0; i < macBytes.length / 2; i++) {
            byte temp = macBytes[i];
            macBytes[i] = macBytes[macBytes.length - 1 - i];
            macBytes[macBytes.length - 1 - i] = temp;
        }
        // 将字节数组转换为十六进制字符串
        String bluetoothAddress = bytesToHex(macBytes);
        // 格式化MAC地址（加冒号）
        return formatMacAddress(bluetoothAddress);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String formatMacAddress(String hexString) {
        StringBuilder formattedAddress = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            if (i > 0) {
                formattedAddress.append(':');
            }
            formattedAddress.append(hexString.substring(i, i + 2));
        }
        return formattedAddress.toString().toUpperCase();
    }

}
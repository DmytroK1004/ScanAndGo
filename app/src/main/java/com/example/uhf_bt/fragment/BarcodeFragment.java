package com.example.uhf_bt.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.tool.BarcodeUtil;
import com.example.uhf_bt.tool.Utils;
import com.rscja.deviceapi.entity.BarcodeResult;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.KeyEventCallback;
import com.rscja.utility.StringUtility;

public class BarcodeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = BarcodeFragment.class.getSimpleName();
    MainActivity mContext;

    static boolean isExit_ = false;
    ScrollView scrBarcode;
    TextView tvData;
    Button btnScan, btClear;
    Spinner spingCodingFormat;
    CheckBox cbContinuous, cbBarcodeType;
    EditText etTime;
    EditText etId;
    EditText etValue;
    Button btnSet;
    Button btGet;


    Object lock = new Object();

    ConnectStatus connectStatus = new ConnectStatus();
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                if (tvData.getText().length() > 1000) {
                    tvData.setText(msg.obj.toString() + "\r\n");
                } else {
                    tvData.setText(tvData.getText() + msg.obj.toString() + "\r\n");
                }
                scroll2Bottom(scrBarcode, tvData);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated----------------");
        mContext = (MainActivity) getActivity();
        isExit_ = false;
        cbContinuous = (CheckBox) getView().findViewById(R.id.cbContinuous);
        etTime = (EditText) getView().findViewById(R.id.etTime);
        scrBarcode = (ScrollView) getView().findViewById(R.id.scrBarcode);
        tvData = (TextView) getView().findViewById(R.id.tvData);
        btnScan = (Button) getView().findViewById(R.id.btnScan);
        btClear = (Button) getView().findViewById(R.id.btClear);

        etId = (EditText) getView().findViewById(R.id.etId);
        etValue = (EditText) getView().findViewById(R.id.etValue);
        btnSet = (Button) getView().findViewById(R.id.btnSet);
        btGet = (Button) getView().findViewById(R.id.btGet);
        btnSet.setOnClickListener(this);
        btGet.setOnClickListener(this);


        cbBarcodeType = (CheckBox) getView().findViewById(R.id.cbBarcodeType);
        btnScan.setOnClickListener(this);
        btClear.setOnClickListener(this);
        spingCodingFormat = (Spinner) getView().findViewById(R.id.spingCodingFormat);
        handler.postDelayed(() -> {
            mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
                @Override
                public void onKeyDown(int keycode) {
                    Log.d(TAG, "  keycode =" + keycode + "   ,isExit_=" + isExit_);
                    if (!isExit_ && mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                        scan();
                    }
                }

                @Override
                public void onKeyUp(int i) {
                }
            });
        }, 200);
        cbContinuous.setOnClickListener(this);
        cbBarcodeType.setOnClickListener(this);
        mContext.addConnectStatusNotice(connectStatus);


        handler.post(() -> {
            if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                int isFlag = mContext.uhf.getBarcodeTypeInSSIID();
                if (isFlag == 1) {
                    cbBarcodeType.setChecked(true);
                } else if (isFlag == 0) {
                    cbBarcodeType.setChecked(false);
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isExit_ = true;
        isRuning = false;
        mContext.removeConnectStatusNotice(connectStatus);
        mContext.uhf.setKeyEventCallback(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScan:
                scan();
                break;
            case R.id.btClear:
                tvData.setText("");
                break;
            case R.id.cbContinuous:
                if (!cbContinuous.isChecked()) {
                    isRuning = false;
                }
                break;
            case R.id.cbBarcodeType:
                if (cbBarcodeType.isChecked()) {
                    boolean result = mContext.uhf.setBarcodeTypeInSSIID(true);
                    if (!result) {
                        cbBarcodeType.setChecked(false);
                    }
                } else {
                    boolean result = mContext.uhf.setBarcodeTypeInSSIID(false);
                    if (!result) {
                        cbBarcodeType.setChecked(true);
                    }
                }
                break;
            case R.id.btnSet:
                String hexIdData = etId.getText().toString();
                if (hexIdData != null) {
                    hexIdData = hexIdData.replace("0x", "").replace(" ", "");
                }
                if (!Utils.vailHexInput(hexIdData)) {
                    Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
                    return;
                }
                String hexValueData = etValue.getText().toString();
                if (hexValueData != null) {
                    hexValueData = hexValueData.replace("0x", "").replace(" ", "");
                }
                if (!Utils.vailHexInput(hexValueData)) {
                    Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean result = mContext.uhf.setParameter(StringUtility.hexStringToBytes(hexIdData), StringUtility.hexString2Bytes(hexValueData));
                if (result) {
                    Toast.makeText(mContext, R.string.setting_succ, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, R.string.setting_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btGet:
                String hexData = etId.getText().toString();
                if (hexData != null) {
                    hexData = hexData.replace("0x", "").replace(" ", "");
                }
                if (!Utils.vailHexInput(hexData)) {
                    Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] data = mContext.uhf.getParameter(StringUtility.hexString2Bytes(hexData));
                if (data == null) {
                    Toast.makeText(mContext, R.string.get_fail, Toast.LENGTH_SHORT).show();
                    return;
                }
                etValue.setText("0x" + StringUtility.bytes2HexString(data));
                Toast.makeText(mContext, R.string.get_succ, Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private synchronized void scan() {
        if (!isRuning) {
            isRuning = true;
            String str = etTime.getText().toString();
            if (str == null || str.isEmpty()) {
                new ScanThread(cbContinuous.isChecked(), Integer.parseInt(etTime.getHint().toString()), cbBarcodeType.isChecked()).start();
            } else {
                new ScanThread(cbContinuous.isChecked(), Integer.parseInt(str), cbBarcodeType.isChecked()).start();
            }
        }
    }

    boolean isRuning = false;

    class ScanThread extends Thread {
        boolean isContinuous = false;
        int time;
        boolean isShowBarcodeType = false;

        public ScanThread(boolean isContinuous, int time, boolean isShowBarcodeType) {
            this.isContinuous = isContinuous;
            this.time = time;
            this.isShowBarcodeType = isShowBarcodeType;
        }

        public void run() {
            while (isRuning) {
                String data = null;
                byte[] temp = null;
                BarcodeResult result = mContext.uhf.startScanBarcode();
                if (result != null) {
                    temp = result.getBarcodeBytesData();
                    Utils.playSound(1);
                }
                if (temp != null && temp.length > 0) {
                    if (spingCodingFormat.getSelectedItemPosition() == 1) {
                        try {
                            data = new String(temp, "utf8");
                        } catch (Exception ex) {
                        }
                    } else if (spingCodingFormat.getSelectedItemPosition() == 2) {
                        try {
                            data = new String(temp, "gb2312");
                        } catch (Exception ex) {
                        }
                    } else {
                        data = new String(temp);
                    }
                    if (result.getBarcodeSSIID() > 0) {
                        data = data + "  type=" + BarcodeUtil.getBarcodeType(result.getBarcodeSSIID());
                    } else if (result.getBarcodeCodeID() != null) {
                        data = data + "  type=" + BarcodeUtil.getBarcodeType(result.getBarcodeCodeID());
                    }

                    Message msg = Message.obtain();
                    msg.obj = data;
                    handler.sendMessage(msg);
                } else {
                    Message msg = Message.obtain();
                    msg.obj = "扫描失败";
                    handler.sendMessage(msg);
                }

                if (!isContinuous) {
                    isRuning = false;
                    break;
                } else {
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public static void scroll2Bottom(final ScrollView scroll, final View inner) {
        Handler handler = new Handler();
        handler.post(() -> {
            if (scroll == null || inner == null) {
                return;
            }
            // 内层高度超过外层
            int offset = inner.getMeasuredHeight() - scroll.getMeasuredHeight();
            if (offset < 0) {
                offset = 0;
            }
            scroll.scrollTo(0, offset);
        });

    }

    class ConnectStatus implements MainActivity.IConnectStatus {
        @Override
        public void getStatus(ConnectionStatus connectionStatus) {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                handler.post(() -> {
                    int isFlag = mContext.uhf.getBarcodeTypeInSSIID();
                    Log.d(TAG, "isFlag=" + isFlag);
                    if (isFlag == 1) {
                        cbBarcodeType.setChecked(true);
                    } else if (isFlag == 0) {
                        cbBarcodeType.setChecked(false);
                    }
                });
            }
        }
    }
}

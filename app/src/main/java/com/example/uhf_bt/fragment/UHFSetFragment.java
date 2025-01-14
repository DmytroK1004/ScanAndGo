package com.example.uhf_bt.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.tool.SPUtils;
import com.example.uhf_bt.tool.StringUtils;
import com.rscja.deviceapi.entity.Gen2Entity;
import com.rscja.deviceapi.entity.InventoryModeEntity;
import com.rscja.team.qcom.deviceapi.V;
import com.rscja.utility.BatteryUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;

import static com.example.uhf_bt.tool.FileUtils.readFile;


public class UHFSetFragment extends Fragment implements View.OnClickListener {
    MainActivity context;

    Button btnGetPower, btnSetPower, BtSetFre, BtGetFre, btnSetProtocol, btnGetProtocol, btnSetFreHop, btnbeepOpen, btnbeepClose;
    Button btnSetRFlink, btnGetRFlink, btnSetInventoryBank, btnGetInventoryBank, btnSetGen2, btnGetGen2;
    Button btnSetFastInventory, btnGetFastInventory, btnFactoryReset;

    Spinner spPower, SpinnerMode, spFreHop, splinkParams, spProtocol, spMemoryBank, spSessionID, spInventoried;
    CheckBox cbTagFocus, cbRssi, cbShowDuplicateTags, cbContinuousWave, cbAutoReconnect;

    RadioButton rbFastInventoryOpen, rbFastInventoryClose;


    LinearLayout llFreHop, llMemoryBankParam;
    EditText etOffset, etLength;


    private String[] arrayPower;
    private int[] arrayLinkValue;
    private int[] arrayMemoryBankValue;

    private final static int GET_FRE = 1;
    private final static int GET_POWER = 2;
    private final static int GET_PROTOCOL = 3;
    private final static int GET_CW = 4;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_FRE:
                    String mode = (String) msg.obj;
                    if (!mode.equals("")) {
                        String[] arrayMode = getResources().getStringArray(R.array.arrayMode);
                        for (int k = 0; k < arrayMode.length; k++) {
                            if (arrayMode[k].equals(mode)) {
                                SpinnerMode.setSelection(k);
                                if (msg.arg1 == 1) context.showToast(R.string.get_succ);
                                break;
                            }
                        }
                    } else {
                        if (msg.arg1 == 1) {
                            context.showToast(R.string.get_fail);
                        }
                    }
                    break;
                case GET_POWER:
                    int iPower = (int) msg.obj;
                    if (arrayPower != null && iPower > -1) {
                        for (int i = 0; i < arrayPower.length; i++) {
                            if (iPower == Integer.valueOf(arrayPower[i])) {
                                spPower.setSelection(i);
                                if (msg.arg1 == 1) context.showToast(R.string.get_succ);
                                break;
                            }
                        }
                    } else if (msg.arg1 == 1) {
                        context.showToast(R.string.get_fail);
                    }
                    break;
                case GET_PROTOCOL:
                    int pro = (int) msg.obj;
                    if (pro >= 0 && pro < spProtocol.getCount()) {
                        spProtocol.setSelection(pro);
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_succ);
                    } else {
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_fail);
                    }
                    break;
                case GET_CW:
                    int flag = (int) msg.obj;
                    if (flag == 1) {
                        cbContinuousWave.setChecked(true);
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_succ);
                    } else if (flag == 0) {
                        cbContinuousWave.setChecked(false);
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_succ);
                    } else {
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_fail);
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_uhfset, container, false);
        init(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (MainActivity) getActivity();
        loadData();
    }


    private void init(View view) {
        spSessionID = view.findViewById(R.id.spSessionID);
        spInventoried = view.findViewById(R.id.spInventoried);
        btnGetGen2 = view.findViewById(R.id.btnGetGen2);
        btnSetGen2 = view.findViewById(R.id.btnSetGen2);
        btnGetGen2.setOnClickListener(this);
        btnSetGen2.setOnClickListener(this);
        llFreHop = view.findViewById(R.id.llFreHop);

        btnGetPower = (Button) view.findViewById(R.id.btnGetPower);
        btnSetPower = (Button) view.findViewById(R.id.btnSetPower);

        spPower = (Spinner) view.findViewById(R.id.spPower);
        arrayPower = getResources().getStringArray(R.array.arrayPower);
//        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, arrayPower);
//        spPower.setAdapter(adapter);

        SpinnerMode = (Spinner) view.findViewById(R.id.SpinnerMode);
        BtSetFre = (Button) view.findViewById(R.id.BtSetFre);
        BtGetFre = (Button) view.findViewById(R.id.BtGetFre);

        spFreHop = (Spinner) view.findViewById(R.id.spFreHop);
        btnSetFreHop = (Button) view.findViewById(R.id.btnSetFreHop);

        btnbeepOpen = (Button) view.findViewById(R.id.btnbeepOpen);
        btnbeepClose = (Button) view.findViewById(R.id.btnbeepClose);
        cbTagFocus = (CheckBox) view.findViewById(R.id.cbTagFocus);
        cbRssi = (CheckBox) view.findViewById(R.id.cbRssi);
        cbShowDuplicateTags = (CheckBox) view.findViewById(R.id.cbShowDuplicateTags);
        splinkParams = view.findViewById(R.id.splinkParams);
        arrayLinkValue = getResources().getIntArray(R.array.arrayLinkValue);
        btnSetRFlink = (Button) view.findViewById(R.id.btnSetRFlink);
        btnGetRFlink = (Button) view.findViewById(R.id.btnGetRFlink);


        spMemoryBank = (Spinner) view.findViewById(R.id.spMemoryBank);
        arrayMemoryBankValue = getResources().getIntArray(R.array.arrayMemoryBankValue);
        llMemoryBankParam = view.findViewById(R.id.llMemoryBankParam);
        etOffset = view.findViewById(R.id.etOffset);
        etLength = view.findViewById(R.id.etLength);
        spMemoryBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                llMemoryBankParam.setVisibility(i == 2 || i == 3 ? View.VISIBLE : View.GONE);
                if (i == 2) {
                    etLength.setText("6");
                } else if (i == 3) {
                    etLength.setText("4");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        llMemoryBankParam.setVisibility(spMemoryBank.getSelectedItemPosition() == 2 || spMemoryBank.getSelectedItemPosition() == 3 ? View.VISIBLE : View.GONE);


        rbFastInventoryOpen = view.findViewById(R.id.rbFastInventoryOpen);
        rbFastInventoryClose = view.findViewById(R.id.rbFastInventoryClose);
        btnSetFastInventory = view.findViewById(R.id.btnSetFastInventory);
        btnGetFastInventory = view.findViewById(R.id.btnGetFastInventory);

        btnSetInventoryBank = view.findViewById(R.id.btnSetInventoryBank);
        btnGetInventoryBank = view.findViewById(R.id.btnGetInventoryBank);
        btnSetInventoryBank.setOnClickListener(this);
        btnGetInventoryBank.setOnClickListener(this);

        btnGetFastInventory.setOnClickListener(this);
        btnSetFastInventory.setOnClickListener(this);
        btnSetRFlink.setOnClickListener(this);
        btnGetRFlink.setOnClickListener(this);
        cbTagFocus.setOnClickListener(this);

        btnSetFreHop.setOnClickListener(this);
        btnGetPower.setOnClickListener(this);
        btnSetPower.setOnClickListener(this);
        BtSetFre.setOnClickListener(this);
        BtGetFre.setOnClickListener(this);
        cbRssi.setOnClickListener(this);
        btnbeepOpen.setOnClickListener(this);
        btnbeepClose.setOnClickListener(this);
        cbShowDuplicateTags.setOnClickListener(this);
        spProtocol = (Spinner) view.findViewById(R.id.spProtocol);
        btnSetProtocol = (Button) view.findViewById(R.id.btnSetProtocol);
        btnSetProtocol.setOnClickListener(this);
        btnGetProtocol = (Button) view.findViewById(R.id.btnGetProtocol);
        btnGetProtocol.setOnClickListener(this);

        btnFactoryReset = (Button) view.findViewById(R.id.btnFactoryReset);
        btnFactoryReset.setOnClickListener(v -> factoryReset());

        cbContinuousWave = (CheckBox) view.findViewById(R.id.cbContinuousWave);
        cbContinuousWave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = cbContinuousWave.isChecked() ? 1 : 0;
                setCW(flag, true);
            }
        });

        boolean reconnect = SPUtils.getInstance(getContext().getApplicationContext()).getSPBoolean(SPUtils.AUTO_RECONNECT, false);
        cbAutoReconnect = (CheckBox) view.findViewById(R.id.cbAutoReconnect);
        cbAutoReconnect.setChecked(reconnect);
        cbAutoReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.getInstance(getContext().getApplicationContext()).setSPBoolean(SPUtils.AUTO_RECONNECT, cbAutoReconnect.isChecked());
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> {
            getFre(false);
            getPower(false);
            getLinkParams(false);
            //getProtocol(false);
            getGen2(false);
            getMemoryBankMode(false);
            getFastInventory();
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cbRssi.setChecked(context.uhf.isSupportRssi());
                    cbShowDuplicateTags.setChecked(context.isShowDuplicateTags);
                }
            });
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 连续波一般只在认证使用，退出设置页面建议关掉
        if (cbContinuousWave.isChecked()) {
            setCW(0, false);
            cbContinuousWave.setChecked(false);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGetPower:
                getPower(true);
                break;
            case R.id.btnSetPower:
                setPower();
                break;
            case R.id.BtGetFre:
                getFre(true);
                break;
            case R.id.BtSetFre:
                setFre();
                break;
            case R.id.btnSetFreHop:
                setFre2();
                break;

            case R.id.btnSetProtocol:
                setProtocol();
                break;
            case R.id.btnGetProtocol:
                getProtocol(true);
                break;
            case R.id.btnbeepClose:
                if (context.uhf.setBeep(false)) {
                    context.showToast(R.string.setting_succ);
                } else {
                    context.showToast(R.string.setting_fail);
                }
                break;
            case R.id.btnbeepOpen:
                if (context.uhf.setBeep(true)) {
                    context.showToast(R.string.setting_succ);
                } else {
                    context.showToast(R.string.setting_fail);
                }
                break;
            case R.id.cbTagFocus:
                if (context.uhf.setTagFocus(cbTagFocus.isChecked())) {
                    context.showToast(R.string.setting_succ);
                } else {
                    context.showToast(R.string.setting_fail);
                }
                break;
            case R.id.btnSetRFlink:
                int index = splinkParams.getSelectedItemPosition();
                int link = arrayLinkValue[index];
                if (context.uhf.setRFLink(link)) {
                    context.showToast(R.string.setting_succ);
                } else {
                    context.showToast(R.string.setting_fail);
                }
                break;
            case R.id.btnGetRFlink:
                getLinkParams(true);
                break;
            case R.id.btnSetInventoryBank:
                setMemoryBankMode();
                break;
            case R.id.btnGetInventoryBank:
                getMemoryBankMode(true);
                break;
            case R.id.cbRssi:
                context.uhf.setSupportRssi(cbRssi.isChecked());
                break;
            case R.id.cbShowDuplicateTags:
                context.isShowDuplicateTags = cbShowDuplicateTags.isChecked();
                break;
            case R.id.btnGetGen2:
                getGen2(true);
                break;
            case R.id.btnSetGen2:
                setSession();
                break;
            case R.id.btnGetFastInventory:
                if (getFastInventory()) {
                    context.showToast(R.string.get_succ);
                } else {
                    context.showToast(R.string.get_fail);
                }
                break;
            case R.id.btnSetFastInventory:
                setFastInventory();
                break;
        }
    }

    private void sendMessage(int what, Object obj, int arg1) {
        Message msg = mHandler.obtainMessage(what, obj);
        msg.arg1 = arg1;
        mHandler.sendMessage(msg);
    }

    private void getPower(boolean showToast) {
        int iPower = context.uhf.getPower();
        sendMessage(GET_POWER, iPower, showToast ? 1 : 0);
    }

    private void setPower() {
        int iPower = Integer.parseInt(spPower.getSelectedItem().toString());
        if (context.uhf.setPower(iPower)) {
            context.showToast(R.string.setting_succ);
        } else {
            context.showToast(R.string.setting_fail);
        }
    }

    public void getLinkParams(boolean isToast) {
        int link = context.uhf.getRFLink();
//        sendMessage(GET_LINK_PARAMS, link, isToast ? 1 : 0);
        mHandler.post(() -> {
            if (link == -1) {
                if (isToast) context.showToast(R.string.get_fail);
                return;
            }
            for (int i = 0; i < arrayLinkValue.length; i++) {
                if (link == arrayLinkValue[i]) {
                    splinkParams.setSelection(i);
                    if (isToast) context.showToast(R.string.get_succ);
                    return;
                }
            }
            if (isToast) context.showToast("RFLink = " + link);
        });
    }

    public void getFre(boolean showToast) {
        int idx = context.uhf.getFrequencyMode();
        String mode = "";
        switch (idx) {
            case 0x01:
                mode = getString(R.string.china_standard1);
                break;
            case 0x02:
                mode = getString(R.string.china_standard2);
                break;
            case 0x04:
                mode = getString(R.string.europe_standard);
                break;
            case 0x08:
                mode = getString(R.string.united_states_standard);
                break;
            case 0x016:
                mode = getString(R.string.korea);
                break;
            case 0x032:
                mode = getString(R.string.japan);
                break;
            case 0x033:
                mode = getString(R.string.South_Africa_915_919MHz);
                break;
            case 0x034:
                mode = getString(R.string.TAIWAN);
                break;
            case 0x035:
                mode = getString(R.string.vietnam_918_923MHz);
                break;
            case 0x036:
                mode = getString(R.string.Peru_915_928MHz);
                break;
            case 0x037:
                mode = getString(R.string.Russia_860_867MHZ);
                break;
            case 0x080:
                mode = getString(R.string.Morocco);
                break;
            case 0x3B:
                mode = getString(R.string.Malaysia);
                break;
            case 0x3C:
                mode = getString(R.string.Brazil);
                break;
        }
        sendMessage(GET_FRE, mode, showToast ? 1 : 0);
    }

    private void setFre() {
        int f = 0;
        String mode = SpinnerMode.getSelectedItem().toString();
        if (getString(R.string.china_standard1).equals(mode)) {
            f = 0x01;
        } else if (getString(R.string.china_standard2).equals(mode)) {
            f = 0x02;
        } else if (getString(R.string.europe_standard).equals(mode)) {
            f = 0x04;
        } else if (getString(R.string.united_states_standard).equals(mode)) {
            f = 0x08;
        } else if (getString(R.string.korea).equals(mode)) {
            f = 0x16;
        } else if (getString(R.string.japan).equals(mode)) {
            f = 0x32;
        } else if (getString(R.string.South_Africa_915_919MHz).equals(mode)) {
            f = 0x33;
        } else if (getString(R.string.TAIWAN).equals(mode)) {
            f = 0x34;
        } else if (getString(R.string.vietnam_918_923MHz).equals(mode)) {
            f = 0x35;
        } else if (getString(R.string.Peru_915_928MHz).equals(mode)) {
            f = 0x36;
        } else if (getString(R.string.Russia_860_867MHZ).equals(mode)) {
            f = 0x37;
        } else if (getString(R.string.Morocco).equals(mode)) {
            f = 0x80;
        } else if (getString(R.string.Malaysia).equals(mode)) {
            f = 0x3B;
        } else if (getString(R.string.Brazil).equals(mode)) {
            f = 0x3C;
        }

        if (context.uhf.setFrequencyMode(f)) {
            context.showToast(R.string.uhf_msg_set_frequency_succ);
        } else {
            context.showToast(R.string.uhf_msg_set_frequency_fail);
        }
    }

    private void setFre2() {
        if (context.uhf.setFreHop(new Float(spFreHop.getSelectedItem().toString().trim()).floatValue())) {
            context.showToast(R.string.uhf_msg_set_frequency_succ);
        } else {
            context.showToast(R.string.uhf_msg_set_frequency_fail);
        }
    }

    /**
     * 设置协议
     *
     * @return
     */
    private boolean setProtocol() {
        if (context.uhf.setProtocol(spProtocol.getSelectedItemPosition())) {
            context.showToast(R.string.uhf_msg_set_protocol_succ);
            return true;
        } else {
            context.showToast(R.string.uhf_msg_set_protocol_fail);
        }
        return false;
    }

    /**
     * 获取协议
     *
     * @param showToast
     * @return
     */
    private void getProtocol(boolean showToast) {
        int pro = context.uhf.getProtocol();
        sendMessage(GET_PROTOCOL, pro, showToast ? 1 : 0);
    }

    /**
     * 设置连续波
     *
     * @param flag
     * @param showToast
     */
    private void setCW(int flag, boolean showToast) {
        boolean res = context.uhf.setCW(flag);
        if (showToast) {
            context.showToast(res ? getString(R.string.setting_succ) : getString(R.string.setting_fail));
        }
    }


    public void loadData() {
        String path = "sdcard/uhfData.txt";
        if (new File(path).exists()) {
            String data = readFile("sdcard/uhfData.txt");
            if (data != null && data.length() > 0) {
                llFreHop.setVisibility(View.VISIBLE);
                String[] strArr = data.split("\r\n");
                //  ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.arrayFreHop, android.R.layout.simple_spinner_item);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, strArr);
                spFreHop.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void getMemoryBankMode(boolean isClick) {
        InventoryModeEntity mode = context.uhf.getEPCAndTIDUserMode();
        if (mode == null) {
            if (isClick) context.showToast(R.string.get_fail);
            return;
        }
        mHandler.post(() -> {
            int bank = mode.getMode();
            boolean result = false;
            for (int i = 0; i < arrayMemoryBankValue.length; i++) {
                if (bank == arrayMemoryBankValue[i]) {
                    spMemoryBank.setSelection(i);
                    mHandler.postDelayed(() -> {
                        if (bank == InventoryModeEntity.MODE_EPC_TID_USER) {
                            etOffset.setText(String.valueOf(mode.getUserOffset()));
                            etLength.setText(String.valueOf(mode.getUserLength()));
                        } else if (bank == InventoryModeEntity.MODE_EPC_RESERVED) {
                            etOffset.setText(String.valueOf(mode.getReservedOffset()));
                            etLength.setText(String.valueOf(mode.getReservedLength()));
                        }
                    }, 50);
                    result = true;
                    break;
                }
            }
            if (isClick) {
                context.showToast(result ? getString(R.string.get_succ) : getString(R.string.get_fail) + " mode=" + mode.getMode());
            }
        });
    }

    private void setMemoryBankMode() {
        if (StringUtils.toInt(etOffset.getText().toString().trim(), Integer.MIN_VALUE) == Integer.MIN_VALUE) {
            context.showToast(R.string.uhf_msg_offset_error);
            return;
        }
        if (StringUtils.toInt(etLength.getText().toString().trim(), Integer.MIN_VALUE) == Integer.MIN_VALUE) {
            context.showToast(R.string.uhf_msg_length_error);
            return;
        }
        int position = spMemoryBank.getSelectedItemPosition();
        boolean result = false;
        int offset = 0, length = 6;
        if (position == 0) {
            result = context.uhf.setEPCMode();
        } else if (position == 1) {
            result = context.uhf.setEPCAndTIDMode();
        } else if (position == 2) {
            offset = StringUtils.toInt(etOffset.getText().toString().trim(), 0);
            length = StringUtils.toInt(etLength.getText().toString().trim(), 6);
            result = context.uhf.setEPCAndTIDUserMode(offset, length);
        } else if (position == 3) {
            offset = StringUtils.toInt(etOffset.getText().toString().trim(), 0);
            length = StringUtils.toInt(etLength.getText().toString().trim(), 4);
            InventoryModeEntity entity = new InventoryModeEntity
                    .Builder()
                    .setMode(InventoryModeEntity.MODE_EPC_RESERVED)
                    .setReservedOffset(offset)
                    .setReservedLength(length)
                    .build();
            result = context.uhf.setEPCAndTIDUserMode(entity);
        } else if(position == 4) {  // TAG LED
            InventoryModeEntity entity = new InventoryModeEntity
                    .Builder()
                    .setMode(InventoryModeEntity.MODE_LED_TAG)
                    .setReservedOffset(offset)
                    .setReservedLength(length)
                    .build();
            result = context.uhf.setEPCAndTIDUserMode(entity);
        }

        context.showToast(result ? R.string.setting_succ : R.string.setting_fail);
    }


    private boolean getGen2(final boolean isClick) {
        final Gen2Entity entity = context.uhf.getGen2();
        Log.e("TAG", "Get Gen2Entity: " + entity);
        if (entity != null) {
            context.runOnUiThread(() -> {
                int sess = entity.getQuerySession();
                int target = entity.getQueryTarget();
                spSessionID.setSelection(sess);
                spInventoried.setSelection(target);
                if (isClick) context.showToast(R.string.get_succ);
            });
            return true;
        }
        if (isClick) context.showToast(R.string.get_fail);
        return false;
    }

    private void setSession() {
        int seesionid = spSessionID.getSelectedItemPosition();
        int inventoried = spInventoried.getSelectedItemPosition();
        if (seesionid < 0 || inventoried < 0) {
            return;
        }
        Gen2Entity entity = context.uhf.getGen2();
        Log.e("TAG", "Set Gen2Entity: " + entity);
        if (entity != null) {
            entity.setQueryTarget(inventoried);
            entity.setQuerySession(seesionid);
            if (context.uhf.setGen2(entity)) {
                context.showToast(R.string.setting_succ);
            } else {
                context.showToast(R.string.setting_fail);
            }
        } else {
            context.showToast(R.string.setting_fail);
        }
    }

    private boolean getFastInventory() {
        final int result = context.uhf.getFastInventoryMode();
        Log.e("TAG", "getFastInventory: " + result);
        if (result >= 0) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result == 1) {
                        rbFastInventoryOpen.setChecked(true);
                        rbFastInventoryClose.setChecked(false);
                    } else {
                        rbFastInventoryOpen.setChecked(false);
                        rbFastInventoryClose.setChecked(true);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void setFastInventory() {
        if (!rbFastInventoryOpen.isChecked() && !rbFastInventoryClose.isChecked()) {
            return;
        }
        boolean result = context.uhf.setFastInventoryMode(rbFastInventoryOpen.isChecked());
        context.showToast(result ? R.string.setting_succ : R.string.setting_fail);
    }


    private void factoryReset() {
        if (context.uhf.factoryReset()) {
            context.showToast(R.string.reset_succ);
            new Thread(() -> {
                getFre(false);
                getPower(false);
                //getProtocol(false);
                getLinkParams(false);
                getGen2(false);
                getMemoryBankMode(false);
                getFastInventory();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cbRssi.setChecked(context.uhf.isSupportRssi());
                        cbShowDuplicateTags.setChecked(context.isShowDuplicateTags);
                    }
                });
            }).start();
        } else {
            context.showToast(R.string.reset_fail);
        }
    }

}

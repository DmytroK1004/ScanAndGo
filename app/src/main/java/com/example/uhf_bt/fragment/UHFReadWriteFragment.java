package com.example.uhf_bt.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.tool.StringUtils;
import com.example.uhf_bt.tool.Utils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.interfaces.KeyEventCallback;
import com.rscja.utility.StringUtility;

import androidx.fragment.app.Fragment;


public class UHFReadWriteFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "UHFReadFragment";
    private MainActivity mContext;

    private boolean isExit = false;
    Spinner spinnerBank;
    EditText EtPtr, EtLen, EtAccessPwd, EtData;
    Button BtRead, BTWrite;
    private ViewGroup layout_read_filter;

    CheckBox cb_filter;
    EditText etPtr_filter, etData_filter, etLen_filter;
    RadioButton rbEPC_filter, rbTID_filter, rbUser_filter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uhfread, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        spinnerBank = (Spinner) getView().findViewById(R.id.spinnerBank);
        EtPtr = (EditText) getView().findViewById(R.id.EtPtr);
        EtLen = (EditText) getView().findViewById(R.id.EtLen);
        EtAccessPwd = (EditText) getView().findViewById(R.id.EtAccessPwd);
        EtData = (EditText) getView().findViewById(R.id.EtData);
        BtRead = (Button) getView().findViewById(R.id.BtRead);
        BTWrite = getView().findViewById(R.id.BtWrite);

        cb_filter = (CheckBox) getView().findViewById(R.id.cb_filter);
        etPtr_filter = (EditText) getView().findViewById(R.id.etPtr_filter);
        etLen_filter = (EditText) getView().findViewById(R.id.etLen_filter);
        etData_filter = (EditText) getView().findViewById(R.id.etData_filter);
        rbEPC_filter = (RadioButton) getView().findViewById(R.id.rbEPC_filter);
        rbTID_filter = (RadioButton) getView().findViewById(R.id.rbTID_filter);
        rbUser_filter = (RadioButton) getView().findViewById(R.id.rbUser_filter);
        layout_read_filter = getView().findViewById(R.id.layout_read_filter);
        layout_read_filter.setVisibility(cb_filter.isChecked() ? View.VISIBLE : View.GONE);

        rbEPC_filter.setOnClickListener(this);
        rbTID_filter.setOnClickListener(this);
        rbUser_filter.setOnClickListener(this);
        BtRead.setOnClickListener(this);
        BTWrite.setOnClickListener(this);

        etData_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                etLen_filter.setText(String.valueOf(etData_filter.getText().toString().trim().length() * 4));
            }
        });

        EtData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                EtLen.setText(String.valueOf(EtData.getText().toString().trim().length() / 4));
            }
        });

        cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout_read_filter.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        spinnerBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {    // RESERVED
                    EtPtr.setText("0");
                    EtLen.setText("4");
                } else if (position == 1) { // EPC
                    EtPtr.setText("2");
                    EtLen.setText("6");
                } else { // TID USER
                    EtPtr.setText("0");
                    EtLen.setText("6");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                read();
            }

            @Override
            public void onKeyUp(int i) {
            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rbEPC_filter:
                if (rbEPC_filter.isChecked()) {
                    etPtr_filter.setText("32");
                }
                break;
            case R.id.rbTID_filter:
                if (rbTID_filter.isChecked()) {
                    etPtr_filter.setText("0");
                }
                break;
            case R.id.rbUser_filter:
                if (rbUser_filter.isChecked()) {
                    etPtr_filter.setText("0");
                }
                break;
            case R.id.BtRead:
                read();
                break;
            case R.id.BtWrite:
                write();
                break;
        }
    }

    private void read() {
        String ptrStr = EtPtr.getText().toString().trim();
        if (ptrStr.equals("")) {
            mContext.showToast(R.string.uhf_msg_addr_not_null);
            return;
        } else if (!TextUtils.isDigitsOnly(ptrStr)) {
            mContext.showToast(R.string.uhf_msg_addr_must_decimal);
            return;
        }

        String cntStr = EtLen.getText().toString().trim();
        if (cntStr.equals("")) {
            mContext.showToast(R.string.uhf_msg_len_not_null);
            return;
        } else if (!TextUtils.isDigitsOnly(cntStr)) {
            mContext.showToast(R.string.uhf_msg_len_must_decimal);
            return;
        }

        String pwdStr = EtAccessPwd.getText().toString().trim();
        if (!TextUtils.isEmpty(pwdStr)) {
            if (pwdStr.length() != 8) {
                mContext.showToast(R.string.uhf_msg_addr_must_len8);
                return;
            } else if (!Utils.vailHexInput(pwdStr)) {
                mContext.showToast(R.string.rfid_mgs_error_nohex);
                return;
            }
        } else {
            pwdStr = "00000000";
        }

        String data = "";
        int Bank = spinnerBank.getSelectedItemPosition();
        if (cb_filter.isChecked()) { //  过滤
            String filterData = etData_filter.getText().toString();
            String filterPtrStr = etPtr_filter.getText().toString();
            String filterLenStr = etLen_filter.getText().toString();

            if (TextUtils.isEmpty(filterPtrStr)) {
                mContext.showToast(R.string.uhf_msg_filter_addr_empty);
                return;
            }
            if (StringUtils.toInt(filterPtrStr, -1) < 0) {
                mContext.showToast(R.string.uhf_msg_filter_addr_error);
                return;
            }
            if (TextUtils.isEmpty(filterLenStr)) {
                mContext.showToast(R.string.uhf_msg_filter_len_empty);
                return;
            }
            if (StringUtils.toInt(filterLenStr, -1) < 0) {
                mContext.showToast(R.string.uhf_msg_filter_len_error);
                return;
            }
            if (TextUtils.isEmpty(filterData)) {
                mContext.showToast(R.string.uhf_msg_filter_data_empty);
                return;
            }
            if (filterData.isEmpty() || !StringUtils.isHexNumber(filterData)) {
                mContext.showToast(R.string.uhf_msg_filter_data_nohex);
                return;
            }
            if (StringUtils.toInt(filterLenStr) / 4 > filterData.length()) {
                mContext.showToast(R.string.uhf_msg_filter_data_not_match);
                return;
            }
            if (filterData.length() % 2 != 0) {
                filterData = filterData + "0";
            }

            int filterPtr = Integer.parseInt(filterPtrStr);
            int filterCnt = Integer.parseInt(filterLenStr);
            int filterBank = RFIDWithUHFBLE.Bank_EPC;
            if (rbEPC_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_EPC;
            } else if (rbTID_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_TID;
            } else if (rbUser_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_USER;
            }
            data = mContext.uhf.readData(pwdStr,
                    filterBank,
                    filterPtr,
                    filterCnt,
                    filterData,
                    Bank,
                    Integer.parseInt(ptrStr),
                    Integer.parseInt(cntStr)
            );
        } else {
            data = mContext.uhf.readData(pwdStr,
                    Bank,
                    Integer.parseInt(ptrStr),
                    Integer.parseInt(cntStr));
        }
        if (data != null && data.length() > 0) {
            EtData.setText(data);
            mContext.showToast(R.string.rfid_msg_read_succ);
            Utils.playSound(1);
        } else {
            mContext.showToast(R.string.rfid_msg_read_fail);
            Utils.playSound(2);
        }
    }

    private void write() {
        String strPtr = EtPtr.getText().toString().trim();
        if (strPtr.isEmpty()) {
            mContext.showToast(R.string.uhf_msg_addr_not_null);
            return;
        } else if (!StringUtility.isDecimal(strPtr)) {
            mContext.showToast(R.string.uhf_msg_addr_must_decimal);
            return;
        }

        String strPWD = EtAccessPwd.getText().toString().trim();// 访问密码
        if (strPWD.isEmpty()) {
            strPWD = "00000000";
        }
        if (strPWD.length() != 8) {
            mContext.showToast(R.string.uhf_msg_addr_must_len8);
            return;
        } else if (!Utils.vailHexInput(strPWD)) {
            mContext.showToast(R.string.rfid_mgs_error_nohex);
            return;
        }

        String strData = EtData.getText().toString().trim();// 要写入的内容
        if (strData.isEmpty()) {
            mContext.showToast(R.string.uhf_msg_write_must_not_null);
            return;
        } else if (!Utils.vailHexInput(strData)) {
            mContext.showToast(R.string.rfid_mgs_error_nohex);
            return;
        }

        // 多字单次
        String cntStr = EtLen.getText().toString().trim();
        if (cntStr.isEmpty()) {
            mContext.showToast(R.string.uhf_msg_len_not_null);
            return;
        } else if (!StringUtility.isDecimal(cntStr)) {
            mContext.showToast(R.string.uhf_msg_len_must_decimal);
            return;
        }

        if ((strData.length()) % 4 != 0) {
            mContext.showToast(R.string.uhf_msg_write_must_len4x);
            return;
        } else if (!Utils.vailHexInput(strData)) {
            mContext.showToast(R.string.rfid_mgs_error_nohex);
            return;
        }

        if ((strData.length()) / 4 < Integer.parseInt(cntStr)) {
            mContext.showToast(R.string.uhf_msg_write_data_not_match);
            return;
        }

        boolean result = false;
        int Bank = spinnerBank.getSelectedItemPosition();
        if (cb_filter.isChecked()) { // 指定标签
            String filterData = etData_filter.getText().toString();
            String filterPtrStr = etPtr_filter.getText().toString();
            String filterLenStr = etLen_filter.getText().toString();

            if (TextUtils.isEmpty(filterPtrStr)) {
                mContext.showToast(R.string.uhf_msg_filter_addr_empty);
                return;
            }
            if (StringUtils.toInt(filterPtrStr, -1) < 0) {
                mContext.showToast(R.string.uhf_msg_filter_addr_error);
                return;
            }
            if (TextUtils.isEmpty(filterLenStr)) {
                mContext.showToast(R.string.uhf_msg_filter_len_empty);
                return;
            }
            if (StringUtils.toInt(filterLenStr, -1) < 0) {
                mContext.showToast(R.string.uhf_msg_filter_len_error);
                return;
            }
            if (TextUtils.isEmpty(filterData)) {
                mContext.showToast(R.string.uhf_msg_filter_data_empty);
                return;
            }
            if (filterData.isEmpty() || !StringUtils.isHexNumber(filterData)) {
                mContext.showToast(R.string.uhf_msg_filter_data_nohex);
                return;
            }
            if (StringUtils.toInt(filterLenStr) / 4 > filterData.length()) {
                mContext.showToast(R.string.uhf_msg_filter_data_not_match);
                return;
            }
            if (filterData.length() % 2 != 0) {
                filterData = filterData + "0";
            }

            int filterPtr = Integer.parseInt(filterPtrStr);
            int filterCnt = Integer.parseInt(filterLenStr);
            int filterBank = RFIDWithUHFBLE.Bank_EPC;
            if (rbEPC_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_EPC;
            } else if (rbTID_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_TID;
            } else if (rbUser_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_USER;
            }
            result = mContext.uhf.writeData(strPWD,
                    filterBank,
                    filterPtr,
                    filterCnt,
                    filterData,
                    Bank,
                    Integer.parseInt(strPtr),
                    Integer.parseInt(cntStr),
                    strData
            );
        } else {
            result = mContext.uhf.writeData(strPWD, Bank, Integer.parseInt(strPtr), Integer.parseInt(cntStr), strData);
        }
        if (result) {
            mContext.showToast(R.string.rfid_msg_write_succ);
            Utils.playSound(1);
        } else {
            mContext.showToast(R.string.rfid_msg_write_fail);
            Utils.playSound(2);
        }
    }
}

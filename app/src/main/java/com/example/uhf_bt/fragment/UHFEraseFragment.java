package com.example.uhf_bt.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.tool.StringUtils;
import com.example.uhf_bt.tool.Utils;
import com.rscja.deviceapi.RFIDWithUHFBLE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by Administrator on 2019/6/14.
 * Description:
 */

public class UHFEraseFragment extends Fragment implements View.OnClickListener {

    private MainActivity mContext;
    private CheckBox cb_filter;
    private LinearLayout layout_filter;
    private EditText etPtr_filter;
    private EditText etLen_filter;
    private EditText etData_filter;
    private RadioButton rbEPC_filter;
    private RadioButton rbTID_filter;
    private RadioButton rbUser_filter;

    private Spinner spEraseArea;
    private EditText EtStartAddress, EtStartLength;
    private EditText EtAccessPwd;
    private Button btnErase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uhf_erase, container, false);
        init(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
    }

    private void init(View view) {
        etPtr_filter = view.findViewById(R.id.etPtr_filter);
        etLen_filter = view.findViewById(R.id.etLen_filter);
        etData_filter = view.findViewById(R.id.etData_filter);

        rbEPC_filter = view.findViewById(R.id.rbEPC_filter);
        rbTID_filter = view.findViewById(R.id.rbTID_filter);
        rbUser_filter = view.findViewById(R.id.rbUser_filter);
        rbEPC_filter.setOnClickListener(this);
        rbTID_filter.setOnClickListener(this);
        rbUser_filter.setOnClickListener(this);

        EtStartAddress = view.findViewById(R.id.EtStartAddress);
        EtStartLength = view.findViewById(R.id.EtStartLength);
        EtAccessPwd = view.findViewById(R.id.EtAccessPwd);
        btnErase = view.findViewById(R.id.btnErase);
        btnErase.setOnClickListener(this);

        spEraseArea = view.findViewById(R.id.spEraseArea);
        spEraseArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spEraseArea.getSelectedItem().toString().equals("EPC")) {
                    EtStartAddress.setText("2");
                } else {
                    EtStartAddress.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cb_filter = view.findViewById(R.id.cb_filter);
        layout_filter = view.findViewById(R.id.layout_filter);
        layout_filter.setVisibility(cb_filter.isChecked() ? View.VISIBLE : View.GONE);
        cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout_filter.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        etData_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                etLen_filter.setText(String.valueOf(s.toString().trim().length() * 4));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnErase:
                erase();
                break;
            case R.id.rbEPC_filter:
                etPtr_filter.setText("32");
                break;
            case R.id.rbTID_filter:
            case R.id.rbUser_filter:
                etPtr_filter.setText("0");
                break;
        }
    }

    /**
     * 擦除数据
     */
    private void erase() {
        String strPWD = EtAccessPwd.getText().toString().trim();// 访问密码
        if (!TextUtils.isEmpty(strPWD)) {
            if (strPWD.length() != 8) {
                Toast.makeText(mContext, R.string.uhf_msg_addr_must_len8, Toast.LENGTH_SHORT).show();
                return;
            } else if (!Utils.vailHexInput(strPWD)) {
                Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
//            Toast.makeText(mContext, R.string.rfid_mgs_error_nopwd, Toast.LENGTH_SHORT).show();
//            return;
            strPWD = "00000000";
        }

        String startAddress = EtStartAddress.getText().toString().trim();
        if (startAddress.equals("")) {
            Toast.makeText(mContext, R.string.uhf_msg_addr_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!TextUtils.isDigitsOnly(startAddress)) {
            Toast.makeText(mContext, R.string.uhf_msg_addr_must_decimal, Toast.LENGTH_SHORT).show();
            return;
        }

        String startLen = EtStartLength.getText().toString().trim();
        if (startLen.equals("")) {
            Toast.makeText(mContext, R.string.uhf_msg_len_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!TextUtils.isDigitsOnly(startLen)) {
            Toast.makeText(mContext, R.string.uhf_msg_len_must_decimal, Toast.LENGTH_SHORT).show();
            return;
        }

        int eraseBank = spEraseArea.getSelectedItemPosition();
        boolean result = false;
        if (cb_filter.isChecked()) {
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
            if (rbTID_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_TID;
            } else if (rbUser_filter.isChecked()) {
                filterBank = RFIDWithUHFBLE.Bank_USER;
            }
            result = mContext.uhf.eraseData(strPWD,
                    filterBank,
                    filterPtr,
                    filterCnt,
                    filterData,
                    eraseBank,
                    Integer.valueOf(startAddress),
                    Integer.valueOf(startLen));
        } else {
            result = mContext.uhf.eraseData(strPWD,
                    eraseBank,
                    Integer.valueOf(startAddress),
                    Integer.valueOf(startLen));
        }
        if (result) {
            Toast.makeText(mContext, R.string.rfid_mgs_erase_succ, Toast.LENGTH_SHORT).show();
            Utils.playSound(1);
        } else {
            Toast.makeText(mContext, R.string.rfid_mgs_erase_fail, Toast.LENGTH_SHORT).show();
            Utils.playSound(2);
        }
    }
}

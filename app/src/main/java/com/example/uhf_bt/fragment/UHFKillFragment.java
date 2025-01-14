package com.example.uhf_bt.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.tool.StringUtils;
import com.example.uhf_bt.tool.Utils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import androidx.fragment.app.Fragment;


public class UHFKillFragment extends Fragment implements View.OnClickListener {
    private MainActivity mContext;

    CheckBox cb_filter;
    LinearLayout layout_filter;
    EditText etPtr_filter;
    EditText etLen_filter;
    EditText etData_filter;
    RadioButton rbEPC_filter;
    RadioButton rbTID_filter;
    RadioButton rbUser_filter;

    EditText EtAccessPwd_Kill;
    Button btnKill;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uhfkill, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();

        cb_filter = (CheckBox) getView().findViewById(R.id.cb_filter);
        layout_filter = (LinearLayout) getView().findViewById(R.id.layout_filter);
        layout_filter.setVisibility(cb_filter.isChecked() ? View.VISIBLE : View.GONE);
        etPtr_filter = (EditText) getView().findViewById(R.id.etPtr_filter);
        etLen_filter = (EditText) getView().findViewById(R.id.etLen_filter);
        etData_filter = (EditText) getView().findViewById(R.id.etData_filter);
        rbEPC_filter = (RadioButton) getView().findViewById(R.id.rbEPC_filter);
        rbTID_filter = (RadioButton) getView().findViewById(R.id.rbTID_filter);
        rbUser_filter = (RadioButton) getView().findViewById(R.id.rbUser_filter);
        rbEPC_filter.setOnClickListener(this);
        rbTID_filter.setOnClickListener(this);
        rbUser_filter.setOnClickListener(this);

        EtAccessPwd_Kill = (EditText) getView().findViewById(R.id.EtAccessPwd_Kill);
        btnKill = (Button) getView().findViewById(R.id.btnKill);
        btnKill.setOnClickListener(this);

        cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout_filter.setVisibility(cb_filter.isChecked() ? View.VISIBLE : View.GONE);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rbEPC_filter:
                etPtr_filter.setText("32");
                break;
            case R.id.rbTID_filter:
                etPtr_filter.setText("0");
                break;
            case R.id.rbUser_filter:
                etPtr_filter.setText("0");
                break;
            case R.id.btnKill:
                kill();
                break;
        }
    }

    public void kill() {
        String strPWD = EtAccessPwd_Kill.getText().toString().trim();// 访问密码

        if (!TextUtils.isEmpty(strPWD)) {
            if (strPWD.length() != 8) {
                Toast.makeText(mContext, R.string.uhf_msg_addr_must_len8, Toast.LENGTH_SHORT).show();
                return;
            } else if (!Utils.vailHexInput(strPWD)) {
                Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(mContext, R.string.rfid_mgs_error_nopwd, Toast.LENGTH_SHORT).show();
            return;
        }

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
            result = mContext.uhf.killTag(strPWD,
                    filterBank,
                    filterPtr,
                    filterCnt,
                    filterData);
        } else {
            result = mContext.uhf.killTag(strPWD);
        }
        if (!result) {
            Toast.makeText(mContext, R.string.rfid_mgs_kill_fail, Toast.LENGTH_SHORT).show();
            Utils.playSound(2);
        } else {
            Toast.makeText(mContext, R.string.rfid_mgs_kill_succ, Toast.LENGTH_SHORT).show();
            Utils.playSound(1);
        }

    }
}

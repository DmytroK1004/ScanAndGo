package com.example.uhf_bt.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.tool.Utils;
import com.example.uhf_bt.tool.UIHelper;
import com.example.uhf_bt.view.CircleSeekBar;
import com.example.uhf_bt.view.RadarView;
import com.rscja.deviceapi.entity.RadarLocationEntity;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.IUHF;
import com.rscja.deviceapi.interfaces.IUHFRadarLocationCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;

import java.util.List;
import java.util.Objects;

public class UHFRadarLocationFragment extends Fragment {

    public final String TAG = "UHFRadarLocationFrag";
    private MainActivity mContext;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private RadarView radarView;
    private EditText etEPC;
    private Button btStart;
    private Button btStop;
    private CircleSeekBar seekBarPower;

    private boolean inventoryFlag = false;
    private String targetEpc; // 定位标签号
    private boolean isPhoneAudioSource = false;
    int progress = 5;

    // 进入页面时蜂鸣器开关状态
    private int beepFlag = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uhf_radar_location, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext.uhf.setKeyEventCallback(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();

        radarView = getView().findViewById(R.id.radarView);
        etEPC = getView().findViewById(R.id.etRadarEPC);
        btStart = getView().findViewById(R.id.btRadarStart);
        btStop = getView().findViewById(R.id.btRadarStop);
        seekBarPower = getView().findViewById(R.id.seekBarPower);
        seekBarPower.setEnabled(false);
        seekBarPower.setProgress(5);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocated();
            }
        });
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocated();
            }
        });

        requireView().post(new Runnable() {
            @Override
            public void run() {
                if (mContext.selectEPC != null && !mContext.selectEPC.equals("")) {
                    etEPC.setText(mContext.selectEPC);
                    targetEpc = mContext.selectEPC;
                }
            }
        });
        handler.postDelayed(() -> {
            mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
                @SuppressLint("LongLogTag")
                @Override
                public void onKeyDown(int keycode) {
                    Log.i(TAG, "keycode=" + keycode);
                    if (!mContext.uhf.isSupportRssi()) {
                        UIHelper.ToastMessage(mContext, getResources().getString(R.string.uhf_not_support_rssi));
                        return;
                    }
                    if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                        if (keycode == 3) {
                            mContext.isKeyDownUP = true;
                            startLocated();
                        } else {
                            if (!mContext.isKeyDownUP) {
                                if (keycode == 1) {
                                    if (inventoryFlag) {
                                        stopLocated();
                                    } else {
                                        startLocated();
                                    }
                                }
                            }
                        }

                    }
                }

                @SuppressLint("LongLogTag")
                @Override
                public void onKeyUp(int keycode) {
                    Log.d(TAG, "  keycode =" + keycode);
                    if (keycode == 4) {
                        stopLocated();
                    }
                }
            });
        }, 200);
    }


    private void startLocated() {
        if (inventoryFlag) return;

        radarView.clearPanel();
        targetEpc = etEPC.getText().toString();
        if (!TextUtils.isEmpty(targetEpc)) {
            //如果定位一张标签，则使用手机蜂鸣器，越靠近标签声音越快
            mContext.uhf.setBeep(false);
            isPhoneAudioSource = true;
        } else {
            isPhoneAudioSource = false;
        }
        boolean result = mContext.uhf.startRadarLocation(mContext, targetEpc, IUHF.Bank_EPC, 32, new IUHFRadarLocationCallback() {
            @Override
            public void getLocationValue(final List<RadarLocationEntity> list) {
//                Log.i(TAG, " list.size=" + list.size());
                radarView.bindingData(list, targetEpc);
                if (isPhoneAudioSource) {
                    for (int k = 0; k < list.size(); k++) {
                        Log.i(TAG, " k=" + k + "  value=" + list.get(k).getValue());
                        Utils.playSoundDelayed(list.get(k).getValue());
                    }
                }
            }

            @Override
            public void getAngleValue(int angle) {
                //Log.i(TAG, "angle=" + angle);
                radarView.setRotation(-angle);
            }
        });
        if (!result) {
            UIHelper.ToastMessage(mContext, "启动失败");
            return;
        }

        seekBarPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress2, boolean fromUser) {
                Log.d(TAG, "  progress =" + progress2);
                progress = progress2;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "  onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int p = 35 - progress;
                mContext.uhf.setDynamicDistance(p);
                Log.d(TAG, "  onStopTrackingTouch  p=" + p + "  progress=" + progress);
                //  Toast.makeText(getContext(),"功率："+progress,Toast.LENGTH_SHORT).show();
            }
        });
        seekBarPower.setEnabled(true);
        inventoryFlag = true;
        btStart.setEnabled(false);
        etEPC.setEnabled(false);

        radarView.startRadar(); // 启动雷达扫描动画
        Log.i(TAG, "startLocated success");
    }

    @SuppressLint("LongLogTag")
    private void stopLocated() {
        radarView.stopRadar();  // 停止雷达扫描动画
        if (!inventoryFlag) return;
        boolean result = mContext.uhf.stopRadarLocation();
        if (!result) {
            //停止失败
            Log.e(TAG, "stopLocated failure");
            Utils.playSound(2);
            Toast.makeText(mContext, R.string.uhf_msg_inventory_stop_fail, Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "stopLocated success");
            inventoryFlag = false;
            btStart.setEnabled(true);
            etEPC.setEnabled(true);
        }
        seekBarPower.setOnSeekBarChangeListener(null);
        seekBarPower.setProgress(5);
        seekBarPower.setEnabled(false);
    }


    @Override
    public void onResume() {
        super.onResume();
        beepFlag = mContext.uhf.getBeep();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocated();
        if (beepFlag != -1) {
            mContext.uhf.setBeep(beepFlag == 1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}


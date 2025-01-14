package com.example.uhf_bt;

import android.app.Application;

import com.example.uhf_bt.tool.ToastUtil;

/**
 * 全局应用程序类：用于保存和调用全局应用配置
 */
public class AppContext extends Application {
    private static final String TAG = "AppContext";

    private static AppContext mApp;

    public static AppContext getInstance() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = this;
        ToastUtil.init(mApp);
    }
}

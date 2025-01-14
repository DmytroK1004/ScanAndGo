package com.example.uhf_bt.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Toast工具类，使用前必须先调用{@link #init(Context)}进行初始化
 */
public class ToastUtil {
    private static final int COLOR_SUCCESS = 0xFF008000; // 绿色
    private static final int COLOR_ERROR = 0xFFFF0000; // 红色
    private static final int COLOR_WARNING = 0xFFFFA500; // 橙黄色
    private static final int COLOR_INFO = 0xFFF5F5F5; // 淡灰近白色

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_BLACK = 0xFF000000;

    private static Toast toast;

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static TextView textView;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout layout;


    /**
     * 初始化Toast
     *
     * @param context 传入全局上下文Application，否则会导致内存泄漏
     */
    public static void init(Context context) {
        if (ToastUtil.context == null) {
            ToastUtil.context = context;

            textView = new TextView(context);
            textView.setText("");
            textView.setTextColor(0xFFE4A529);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setGravity(Gravity.CENTER);

            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setBackgroundColor(0xFFF7F58F);
            layout.setPadding(20, 30, 20, 30);
            layout.setGravity(Gravity.CENTER);
            layout.setMinimumWidth(100000);
            layout.addView(textView);
        }
    }

    /**
     * @param text     显示的内容
     * @param duration 显示时间  {@link Toast#LENGTH_SHORT} or {@link Toast#LENGTH_LONG}
     */
    public static synchronized void show(CharSequence text, int duration) {
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG) {
            Log.e("ToastUtil", "Illegal duration value");
            duration = Toast.LENGTH_SHORT;
        }
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * @param resId    资源id
     * @param duration 显示时间  {@link Toast#LENGTH_SHORT} or {@link Toast#LENGTH_LONG}
     */
    public static void show(int resId, int duration) {
        show(context.getString(resId), duration);
    }

    public static void show(CharSequence text) {
        show(text, Toast.LENGTH_SHORT);
    }

    public static void show(int resId) {
        show(resId, Toast.LENGTH_SHORT);
    }


    private static void showWithColor(CharSequence text, int color) {
        if (color == COLOR_INFO) textView.setTextColor(COLOR_BLACK);
        else textView.setTextColor(COLOR_WHITE);
        textView.setText(text);
        layout.setBackgroundColor(color);

        if (toast != null) toast.cancel();
        toast = new Toast(context);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showSuccess(int resId) {
        showSuccess(context.getString(resId));
    }

    public static void showSuccess(CharSequence text) {
        showWithColor(text, COLOR_SUCCESS);
    }

    public static void showError(int resId) {
        showError(context.getString(resId));
    }

    public static void showError(CharSequence text) {
        showWithColor(text, COLOR_ERROR);
    }

    public static void showWarning(int resId) {
        showWarning(context.getString(resId));
    }

    public static void showWarning(CharSequence text) {
        showWithColor(text, COLOR_WARNING);
    }

    public static void showInfo(int resId) {
        showInfo(context.getString(resId));
    }

    public static void showInfo(CharSequence text) {
        showWithColor(text, COLOR_INFO);
    }
}


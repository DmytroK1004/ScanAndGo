package com.example.uhf_bt.tool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


import com.example.uhf_bt.R;
import com.rscja.utility.StringUtility;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by Administrator on 2019-3-13.
 */

public class Utils {
    private static final String TAG = "Utils";

    private static HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private static SoundPool soundPool;
    private static float volumnRatio;
    private static AudioManager am;
    private static PlaySoundThread playSoundThread=null;
    public static void initSound(Context context) {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(context, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(context, R.raw.serror, 1));
        am = (AudioManager) context.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
        playSoundThread=new PlaySoundThread();
        playSoundThread.start();
    }

    public static void freeSound() {
        if (soundPool != null)
            soundPool.release();
        soundPool = null;
    }

    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public static void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0无不循环，-1无永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void playSoundDelayed(int speed) {
        playSoundThread.play(speed);
    }


    public static void alert(Activity act, int titleInt, String message, int iconInt) {
        alert(act, titleInt, message, iconInt, null);
    }

    public static void alert(Activity act, int titleInt, String message, int iconInt, DialogInterface.OnClickListener positiveListener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(titleInt);
            builder.setMessage(message);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            if (positiveListener != null) {
                builder.setPositiveButton(R.string.ok, positiveListener);
            }
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void alert(Activity act, String title, View view, int iconInt, DialogInterface.OnClickListener positiveListener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(title);
            builder.setView(view);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            if (positiveListener != null) {
                builder.setPositiveButton(R.string.ok, positiveListener);
            }
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void alert(Activity act, int titleInt, View view, int iconInt, DialogInterface.OnClickListener positiveListener) {
        alert(act, act.getString(titleInt), view, iconInt, positiveListener);
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    public static boolean vailHexInput(String str) {

        if (str == null || str.length() == 0) {
            return false;
        }
        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
    }


    private static Object objectLock = new Object();
    private static class PlaySoundThread extends Thread {
        private boolean isStop = false;
        int interval=500;
        long lastPlayTime=SystemClock.elapsedRealtime();

        @Override
        public void run() {
            while (!isStop) {
                long start=0;
                synchronized (objectLock) {
                    while (!isStop) {
                        if(start==0){
                            start= SystemClock.elapsedRealtime();
                        }else {
                            if(SystemClock.elapsedRealtime() - start >= interval){
                                break;
                            }else {
                                SystemClock.sleep(1);
                            }
                        }
                    }
                }
                if(SystemClock.elapsedRealtime()-lastPlayTime<500) {
                    playSound(1);
                }
            }
        }

        public void play(int speed) {
            //speed 1-100;
            //100-1
            //99-10
            //98-20
            //97-30

            int t = 3;
            if (speed > 85 ) {
                t = 5;
            } else if (speed > 66) {
                t = 100 - speed;
            } else if (speed > 33) {
                t = (100 - speed) * 2;
            } else {
                t = (100 - speed) * 3;
            }

            interval=t;
            lastPlayTime=SystemClock.elapsedRealtime();
           // Log.i("UHFRadarLocationFrag", " interval=" + interval );
        }

        public void stopPlay() {
            isStop = true;
            synchronized (objectLock) {
                objectLock.notifyAll();
            }
        }
    }

}

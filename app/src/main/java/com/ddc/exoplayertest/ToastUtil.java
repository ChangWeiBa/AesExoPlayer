package com.ddc.exoplayertest;

import android.widget.Toast;


public class ToastUtil {

    public static boolean isShow = true;

    private static String oldMsg;
    protected static Toast toast = null;
    private static long oneTime = 0;
    private static long twoTime = 0;

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort(CharSequence message) {
        if (toast == null) {
            toast = Toast.makeText(AppContext.getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message.toString();
                toast.setText(message.toString());
                toast.show();
            }
        }
        oneTime = twoTime;
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(CharSequence message) {
        if (toast == null) {
            toast = Toast.makeText(AppContext.getAppContext(), message, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message.toString();
                toast.setText(message.toString());
                toast.show();
            }
        }
        oneTime = twoTime;
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show(CharSequence message, int duration) {
        if (toast == null) {
            toast = Toast.makeText(AppContext.getAppContext(), message, duration);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                if (twoTime - oneTime > duration) {
                    toast.show();
                }
            } else {
                oldMsg = message.toString();
                toast.setText(message.toString());
                toast.show();
            }
        }
        oneTime = twoTime;
    }

}

package com.ddc.exoplayertest;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;

import com.google.android.exoplayer2.util.Util;

import java.io.File;

import timber.log.Timber;

/**
 * Created by wang on 2016/12/26
 */
public class AppContext extends Application {

    private static AppContext instance;
    private String userAgent;
    private static String FildDir =Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
            "aitriping";


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");


        //在这里先使用Timber.plant注册一个Tree，然后调用静态的.d .v 去使用
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }


        //Android N 文件权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }



    }

    public static Context getAppContext() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

    public static String getFildDir() {
        return FildDir;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}

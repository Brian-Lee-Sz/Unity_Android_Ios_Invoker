package com.crater.unitylib;

import android.app.Application;
import android.util.Log;

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("choosme", "执行自定义Application onCreate");
    }
}

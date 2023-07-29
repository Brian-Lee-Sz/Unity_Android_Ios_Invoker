package com.crater.unityinvoke;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TreeMap;

public class Unity2Android {
    private static final String Tag = "choosme_android_invo";
    private static Unity2Android mInstance;
    private Activity unityActivity;
    private Context context;

    public static Unity2Android getInstance() {
        if (mInstance == null) mInstance = new Unity2Android();
        return mInstance;
    }

    /**
     * 利用反射机制获取unity项目的上下文
     *
     * @return
     */
    public Activity getActivity() {
        if (null == unityActivity) {
            try {
                Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
                Activity activity = (Activity) classtype.getDeclaredField("currentActivity").get(classtype);
                unityActivity = activity;
                context = activity;
            } catch (ClassNotFoundException e) {
                Log.d(Tag, e.toString());
            } catch (IllegalAccessException e) {
                Log.d(Tag, e.toString());
            } catch (NoSuchFieldException e) {
                Log.d(Tag, e.toString());
            }
        }
        return unityActivity;
    }

    public void GetVersionCode() {
        PackageManager packageManager = getActivity().getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
            TreeMap<String, Object> values = new TreeMap<>();
            values.put("code", packageInfo.versionCode);
            values.put("name", packageInfo.versionName);
            CallUnity("PlatformManager", "GetAppVersionInfoAndroidCb", new JsonUtil(values).toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Android调用Unity的方法
     *
     * @param gameObjectName 调用的GameObject的名称
     * @param functionName   方法名
     * @param args           参数
     * @return 调用是否成功
     */
    public boolean CallUnity(String gameObjectName, String functionName, String args) {
        try {
            Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
            Method method = classtype.getMethod("UnitySendMessage", String.class, String.class, String.class);
            method.invoke(classtype, gameObjectName, functionName, args);
            return true;
        } catch (ClassNotFoundException e) {
            Log.d(Tag, e.toString());
        } catch (NoSuchMethodException e) {
            Log.d(Tag, e.toString());
        } catch (IllegalAccessException e) {
            Log.d(Tag, e.toString());
        } catch (InvocationTargetException e) {
            Log.d(Tag, e.toString());
        }
        return false;
    }


    /**
     * Unity调用安卓的方法
     * Toast显示unity发送过来的内容
     *
     * @param content 消息的内容
     * @return 调用是否成功
     */
    public boolean ShowToast(String content) {
        Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
        return true;
    }
}

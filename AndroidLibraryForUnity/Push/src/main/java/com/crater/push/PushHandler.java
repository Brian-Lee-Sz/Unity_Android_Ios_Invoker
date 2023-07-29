package com.crater.push;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.crater.unityinvoke.JsonUtil;
import com.crater.unityinvoke.Unity2Android;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.TreeMap;

public class PushHandler {
    private static final String Tag = "choosme_android_push";
    private static PushHandler mInstance;

    public static PushHandler getInstance() {
        if (mInstance == null) mInstance = new PushHandler();
        return mInstance;
    }

    public void initFirebase() {
        FirebaseApp.initializeApp(Unity2Android.getInstance().getActivity());
    }

    public void GetPushToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(Tag, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        Unity2Android.getInstance().CallUnity("PushManager", "androidCallToGetToken", task.getResult());
                    }
                });
    }

    public boolean isPushOpen(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public void openPush(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else {
            toPermissionSetting(context);
        }
    }

    public void checkInput(Intent intent) {
        if (intent != null) {
            TreeMap<String, Object> values = new TreeMap<>();
            values.put("paramKey", intent.getStringExtra("jumpParam"));
            values.put("paramValue", intent.getStringExtra("jumpValue"));
            Unity2Android.getInstance().CallUnity("PushManager", "androidCheckStartInfo", new JsonUtil(values).toString());
        }
    }

    public void toApplicationInfo(Context activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(localIntent);
    }

    public void toPermissionSetting(Context activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            toSystemConfig(activity);
        } else {
            try {
                toApplicationInfo(activity);
            } catch (Exception e) {
                e.printStackTrace();
                toSystemConfig(activity);
            }
        }
    }

    public void toSystemConfig(Context activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

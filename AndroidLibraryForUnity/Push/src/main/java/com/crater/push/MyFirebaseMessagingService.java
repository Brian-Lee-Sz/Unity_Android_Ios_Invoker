package com.crater.push;

import android.util.Log;

import androidx.annotation.NonNull;

import com.crater.unityinvoke.JsonUtil;
import com.crater.unityinvoke.Unity2Android;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull @org.jetbrains.annotations.NotNull String s) {
        super.onNewToken(s);
        Unity2Android.getInstance().CallUnity("PushManager", "androidCallToGetToken", s);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.w("choosme_android_push", "收到消息" + remoteMessage.getData().toString());
        TreeMap<String, Object> values = new TreeMap<>();
        values.put("paramKey", remoteMessage.getData().get("jumpParam"));
        values.put("paramValue", remoteMessage.getData().get("jumpValue"));
        Unity2Android.getInstance().CallUnity("PushManager", "androidCheckStartInfo", new JsonUtil(values).toString());
    }
}

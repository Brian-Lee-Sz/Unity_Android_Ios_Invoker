package com.crater.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class FcmErrorReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("action.mpaas.push.error.fcm.init".equalsIgnoreCase(action)) {
            Toast.makeText(context, "fcm error " + intent.getIntExtra("error", 0), Toast.LENGTH_SHORT).show();
        }
    }
}

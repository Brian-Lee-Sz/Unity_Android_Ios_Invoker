package com.crater.unitylib;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.crater.fblogin.FacebookLoginHandler;
import com.crater.googleSignIn.GoogleSignInHandler;
import com.crater.push.PushHandler;
import com.unity3d.player.UnityPlayerActivity;

import static com.crater.googleSignIn.GoogleSignInHandler.SIGN_LOGIN;

public class CustomActivity extends UnityPlayerActivity {
    private static final String Tag = "choosme_android";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i(Tag, "自定义Activity执行onCreate");
        PushHandler.getInstance().initFirebase();
        FacebookLoginHandler.getInstance().initFbLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SIGN_LOGIN:
                GoogleSignInHandler.getInstance().onActivityResult(requestCode, resultCode, data);
                break;
            default:
                FacebookLoginHandler.getInstance().onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        PushHandler.getInstance().checkInput((intent));
    }
}

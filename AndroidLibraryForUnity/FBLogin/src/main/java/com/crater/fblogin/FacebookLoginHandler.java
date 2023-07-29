package com.crater.fblogin;

import android.app.Activity;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.crater.unityinvoke.JsonUtil;
import com.crater.unityinvoke.Unity2Android;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;
import java.util.TreeMap;

public class FacebookLoginHandler {
    private static final String Tag = "choosme_android_fSignIn";
    private static FacebookLoginHandler mInstance;

    private CallbackManager callbackManager = null;

    public static FacebookLoginHandler getInstance() {
        if (mInstance == null) mInstance = new FacebookLoginHandler();
        return mInstance;
    }

    public void initFbLogin() {
        callbackManager = CallbackManager.Factory.create();
        registerFb();
    }

    public void registerFb() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbLoginResult(1, loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Activity mHandleActivity = Unity2Android.getInstance().getActivity();
                CookieSyncManager.createInstance(mHandleActivity);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                CookieSyncManager.getInstance().sync();
                fbLoginResult(-1, "");
            }

            @Override
            public void onError(FacebookException error) {
                if (error instanceof FacebookAuthorizationException) {
                    LoginManager.getInstance().logOut();
                }
                fbLoginResult(-2, "");
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void signIn() {
        Activity mHandleActivity = Unity2Android.getInstance().getActivity();
        mHandleActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken != null && !accessToken.isExpired()) {
                    fbLoginResult(1, accessToken.getToken());
                } else {
                    LoginManager.getInstance().logInWithReadPermissions(mHandleActivity, Arrays.asList("public_profile"));
                }
            }
        });
    }

    public void fbLoginResult(int resultCode, String accessToken) {
        TreeMap<String, Object> values = new TreeMap<>();
        values.put("token", accessToken);
        values.put("result", resultCode);
        Unity2Android.getInstance().CallUnity("PlatformManager", "FacebookLoginInfoFromAndroidCb", new JsonUtil(values).toString());
    }
}

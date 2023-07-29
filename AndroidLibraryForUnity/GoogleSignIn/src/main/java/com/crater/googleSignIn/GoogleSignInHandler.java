package com.crater.googleSignIn;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.crater.unityinvoke.JsonUtil;
import com.crater.unityinvoke.Unity2Android;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.TreeMap;

public class GoogleSignInHandler {
    private static final String Tag = "choosme_android_gSignIn";
    private static GoogleSignInHandler mInstance;
    public static final int SIGN_LOGIN = 10001;
    private GoogleSignInClient mGoogleSignInClient;


    public static GoogleSignInHandler getInstance() {
        if (mInstance == null) mInstance = new GoogleSignInHandler();
        return mInstance;
    }

    public void signIn() {
        Activity mHandleActivity = Unity2Android.getInstance().getActivity();
        mHandleActivity.startActivityForResult(getGoogleIntent(mHandleActivity), SIGN_LOGIN);
    }

    public void signInClient(Activity activity) {
        if (mGoogleSignInClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
                    .DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken("619146176628-5cee7ujji8vj9lf7tspk9mkj07pceped.apps.googleusercontent.com")
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        }
    }

    public Intent getGoogleIntent(Activity activity) {
        Intent signInInten;
        if (mGoogleSignInClient == null) {
            signInClient(activity);
        }
        signInInten = mGoogleSignInClient.getSignInIntent();
        return signInInten;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Tag, "setActivityResultGoogle");
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            TreeMap<String, Object> values = new TreeMap<>();
            values.put("token", account.getIdToken());
            values.put("result", 1);
            Unity2Android.getInstance().CallUnity("PlatformManager", "GoogleLoginInfoFromAndroidCb", new JsonUtil(values).toString());
        } catch (ApiException e) {
            Log.d(Tag, "ApiException:" + e.getMessage());
            TreeMap<String, Object> values = new TreeMap<>();
            values.put("token", "");
            values.put("result", 0);
            Unity2Android.getInstance().CallUnity("PlatformManager", "GoogleLoginInfoFromAndroidCb", new JsonUtil(values).toString());
        }
    }
}

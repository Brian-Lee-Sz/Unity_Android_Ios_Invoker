package com.crater.googleads;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crater.unityinvoke.JsonUtil;
import com.crater.unityinvoke.Unity2Android;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;

import java.util.TreeMap;

public class GoogleAdsHandler {

    private static final String Tag = "choosme_android_gad";
    private static GoogleAdsHandler mInstance;
    TreeMap<String, RewardedAd> mRewardedAds = new TreeMap<>();

    public static GoogleAdsHandler getInstance() {
        if (mInstance == null) mInstance = new GoogleAdsHandler();
        return mInstance;
    }

    public void initGoogleAds() {
        Activity mHandleActivity = Unity2Android.getInstance().getActivity();
        if (mHandleActivity != null) {
            MobileAds.initialize(mHandleActivity, initializationStatus -> {
                Log.d(Tag, "google Id init done");
                Unity2Android.getInstance().CallUnity("AdsManager", "androidResponseForAdInit", "success");
            });
        }
    }

    public void loadAd(String adId, String qid, String exInfo, String npa) {
        Activity mHandleActivity = Unity2Android.getInstance().getActivity();
        if (mHandleActivity != null) {
            mHandleActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(Tag, "加载广告 " + adId);
                    if (mRewardedAds.get(adId) != null) {
                        callBackUnity(adId, 1, 0, "hasLoad", 0);
                        return;
                    }
                    Bundle extras = new Bundle();
                    extras.putString("npa", npa);
                    AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
                    RewardedAd.load(mHandleActivity, adId, adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(Tag, loadAdError.toString());
                            mRewardedAds.remove(adId);
                            ResponseInfo adRes = loadAdError.getResponseInfo();
                            callBackUnity(adId, 1, -1, adRes != null ? adRes.getMediationAdapterClassName() : "", loadAdError.getCode());
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            Log.d(Tag, "Ad was loaded.");
                            if (mRewardedAds.get(adId) == null) {
                                ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                                        .setUserId(qid)
                                        .setCustomData(exInfo)
                                        .build();
                                rewardedAd.setServerSideVerificationOptions(options);
                                mRewardedAds.put(adId, rewardedAd);
                                ResponseInfo adRes = rewardedAd.getResponseInfo();
                                callBackUnity(adId, 1, 0, adRes != null ? adRes.getMediationAdapterClassName() : "", 0);
                            }
                        }
                    });
                }
            });
        }

    }

    public void showAd(String adId) {
        Activity mHandleActivity = Unity2Android.getInstance().getActivity();
        if (mHandleActivity != null) {
            mHandleActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RewardedAd cacheReward = mRewardedAds.get(adId);
                    if (cacheReward != null) {
                        cacheReward.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(Tag, "Ad was clicked.");
                                callBackUnity(adId, 2, 1, "onAdClicked", 0);
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(Tag, "Ad recorded an impression.");
                                callBackUnity(adId, 2, 2, "onAdImpression", 0);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(Tag, "Ad showed fullscreen content.");
                                callBackUnity(adId, 2, 3, "onAdShowedFullScreenContent", 0);
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(Tag, "Ad dismissed fullscreen content.");
                                mRewardedAds.remove(adId);
                                callBackUnity(adId, 3, -1, "onAdDismissedFullScreenContent", 0);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(Tag, "Ad failed to show fullscreen content.");
                                mRewardedAds.remove(adId);
                                callBackUnity(adId, 3, -2, "onAdFailedToShowFullScreenContent", 0);
                            }
                        });
                        cacheReward.show(mHandleActivity, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                // Handle the reward.
                                Log.d(Tag, "The user earned the reward.");
                                mRewardedAds.remove(adId);
                                TreeMap<String, Object> values = new TreeMap<>();
                                values.put("rewardAmount", rewardItem.getAmount());
                                values.put("rewardType", rewardItem.getType());
                                callBackUnity(adId, 3, 0, new JsonUtil(values).toString(), 0);
                            }
                        });
                    } else {
                        Log.d(Tag, "The rewarded ad wasn't ready yet.");
                        callBackUnity(adId, 3, -3, "The rewarded ad wasn't ready yet.", 0);
                    }
                }
            });
        }
    }

    public void callBackUnity(String adId, int status, int result, String resultString, int errorCode) {
        TreeMap<String, Object> values = new TreeMap<>();
        values.put("adId", adId);
        values.put("status", status);
        values.put("result", result);
        values.put("resultString", resultString);
        values.put("errorCode", errorCode);
        Unity2Android.getInstance().CallUnity("AdsManager", "androidResponseForAd", new JsonUtil(values).toString());
    }
}

package com.crater.googlepay;

import android.app.Activity;
import android.os.Debug;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.crater.unityinvoke.JsonUtil;
import com.crater.unityinvoke.Unity2Android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class GooglePayHandler {
    private static final String Tag = "choosme_android_gPay";
    private static GooglePayHandler mInstance;
    private BillingClient mBillingClient = null;
    private String googlePayDeveloperPayload = null;
    private Activity mHandleActivity;

    public static GooglePayHandler getInstance() {
        if (mInstance == null) mInstance = new GooglePayHandler();
        return mInstance;
    }

    public void initGooglePay() {
        mHandleActivity = Unity2Android.getInstance().getActivity();
        mBillingClient = BillingClient.newBuilder(mHandleActivity).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                Log.d(Tag, "onPurchasesUpdated code = " + billingResult.getResponseCode() + " ,  msg = " + billingResult.getDebugMessage());
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        Log.d(Tag, purchase.getOriginalJson());
                    }
                }
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    googlePayResult(-1, "cancel pay", "", "user cancel");
                } else {
                    googlePayResult(billingResult.getResponseCode(), "pay fail", "", billingResult.getDebugMessage() + billingResult.getResponseCode());
                }
            }
        }).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.e(Tag, "onBillingSetupFinished code = " + billingResult.getResponseCode() + " ,  msg = " + billingResult.getDebugMessage());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                googlePayResult(-5, "BillingServiceDisconnected", "", "try again");
            }
        });
    }

    public void connectGoogle() {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.e(Tag, "onBillingSetupFinished code = " + billingResult.getResponseCode() + " ,  msg = " + billingResult.getDebugMessage());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Unity2Android.getInstance().CallUnity("PayManager", "androidResponseGoogleConnect", "");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    public void googlePayRequest(String purchaseId, String businessIdentify, boolean isSub) {
        googlePayDeveloperPayload = businessIdentify;
        queryPurchases(purchaseId, isSub);
    }

    /**
     * 查询已经购买过但是没有被消耗的商品，可能网络不稳定或者中断导致的未被消耗
     * 如果购买成功没消耗，就去消耗，消耗完成视为完整的流程。
     */
    public void queryPurchases() {
        mBillingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchasesList) {
                if (purchasesList != null) {
                    for (int i = 0; i < purchasesList.size(); i++) {
                        Purchase purchase = purchasesList.get(i);
                        googleConsumeResult(3, purchase.getOriginalJson(), purchase.getSignature());
                    }
                }
            }
        });
    }

    public void reConsumeOrder(String purchaseToken, String originalJson, String signature) {
        mBillingClient.consumeAsync(ConsumeParams.newBuilder() // 只有消费成功之后，才能真正到账，否则3天之后，会执行退款处理 测试阶段只有5分钟
                        .setPurchaseToken(purchaseToken).build(),
                new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                        int rspC = billingResult.getResponseCode();
                        if (rspC == BillingClient.BillingResponseCode.OK) {
                            googlePayResult(0, originalJson, signature, "Success");
                        } else {
                            // 消费失败,后面查询消费记录后再次消费，否则，就只能等待退款
                            googleConsumeResult(rspC, originalJson, signature);
                        }
                    }
                });
    }

    public void getProductDetail(String goodsIdsJson) {
        Log.d(Tag, goodsIdsJson);
        List<String> goodsIds = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(goodsIdsJson);
            for (int i = 0, len = array.length(); i < len; i++) {
                goodsIds.add((String) array.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(goodsIds).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        Log.e(Tag, "onSkuDetailsResponse code = " + billingResult.getResponseCode() + " ,  msg = " + billingResult.getDebugMessage() + " , skuDetailsList = " + skuDetailsList);
                        if (skuDetailsList != null || !skuDetailsList.isEmpty()) {
                            List<JSONObject> details = new ArrayList<>();
                            try {
                                for (SkuDetails detail : skuDetailsList) {
                                    details.add(new JSONObject(detail.getOriginalJson()));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Unity2Android.getInstance().CallUnity("PayManager", "androidResponseProductDetail", new JSONArray(details).toString());
                        } else {
                            Unity2Android.getInstance().CallUnity("PayManager", "androidResponseProductDetail", "");
                        }
                    }
                });

    }

    public void getSubDetail(String goodsIdsJson) {
        List<String> goodsIds = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(goodsIdsJson);
            for (int i = 0, len = array.length(); i < len; i++) {
                goodsIds.add((String) array.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(goodsIds).setType(BillingClient.SkuType.SUBS);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        Log.e(Tag, "onSkuDetailsResponse code = " + billingResult.getResponseCode() + " ,  msg = " + billingResult.getDebugMessage() + " , skuDetailsList = " + skuDetailsList);
                        if (skuDetailsList != null || !skuDetailsList.isEmpty()) {
                            List<JSONObject> details = new ArrayList<>();
                            try {
                                for (SkuDetails detail : skuDetailsList) {
                                    details.add(new JSONObject(detail.getOriginalJson()));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Unity2Android.getInstance().CallUnity("PayManager", "androidResponseSubDetail", new JSONArray(details).toString());
                        } else {
                            Unity2Android.getInstance().CallUnity("PayManager", "androidResponseSubDetail", "");
                        }
                    }
                });

    }

    public void queryPurchases(@NonNull final String purchaseId, boolean isSub) {
        Log.e(Tag, "查询 = " + purchaseId);
        List<String> skuList = new ArrayList<>();
        skuList.add(purchaseId);
        skuList.add("hd_gp_go");  // 这个参数不能为空，值随便传
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(isSub ? BillingClient.SkuType.SUBS : BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        Log.e(Tag, "onSkuDetailsResponse code = " + billingResult.getResponseCode() + " ,  msg = " + billingResult.getDebugMessage() + " , skuDetailsList = " + skuDetailsList);
                        if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                            googlePayResult(billingResult.getResponseCode(), "check product detail fail", "", billingResult.getDebugMessage());
                            return;
                        }
                        SkuDetails skuDetails = null;
                        for (SkuDetails details : skuDetailsList) {
                            Log.e(Tag, "onSkuDetailsResponse skuDetails = " + details.toString());
                            if (purchaseId.equals(details.getSku())) {
                                skuDetails = details;
                            }
                        }
                        if (skuDetails != null) {
                            Unity2Android.getInstance().CallUnity("PayManager", "androidResponseSingleProductDetail", skuDetails.getOriginalJson());
                            googlePay(skuDetails);
                        } else {
                            googlePayResult(-3, "check product detail fail", "", billingResult.getResponseCode() + " ,  msg = " + billingResult.getDebugMessage() + purchaseId);
                        }
                    }
                });
    }

    private void googlePay(SkuDetails details) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(details)
                .setObfuscatedProfileId(details.getType())
                .setObfuscatedAccountId(googlePayDeveloperPayload)
                .build();
        int code = mBillingClient.launchBillingFlow(mHandleActivity, flowParams).getResponseCode();
    }

    private void handlePurchase(final Purchase purchase) {
        Log.d(Tag, "商品类型 " + purchase.getAccountIdentifiers().getObfuscatedProfileId());
        if (purchase.getAccountIdentifiers().getObfuscatedProfileId().equals("subs")) {
            Log.d(Tag, "订阅商品购买成功 " + purchase.getOriginalJson());
            googlePayResult(0, purchase.getOriginalJson(), purchase.getSignature(), "Success");
        } else {
            mBillingClient.consumeAsync(ConsumeParams.newBuilder() // 只有消费成功之后，才能真正到账，否则3天之后，会执行退款处理 测试阶段只有5分钟
                            .setPurchaseToken(purchase.getPurchaseToken()).build(),
                    new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                            int rspC = billingResult.getResponseCode();
                            if (rspC == BillingClient.BillingResponseCode.OK) {
                                googlePayResult(0, purchase.getOriginalJson(), purchase.getSignature(), "Success");
                            } else {
                                // 消费失败,后面查询消费记录后再次消费，否则，就只能等待退款
                                googleConsumeResult(rspC, purchase.getOriginalJson(), purchase.getSignature());
                            }
                        }
                    });
        }

    }

    private void googlePayResult(int resultCode, String resultJson, String resultSign, String resultMsg) {
        googlePayDeveloperPayload = null;
        TreeMap<String, Object> values = new TreeMap<>();
        values.put("payData", resultJson);
        values.put("paySign", resultSign);
        values.put("resultMsg", resultMsg);
        values.put("resultCode", resultCode);
        Unity2Android.getInstance().CallUnity("PayManager", "androidResponseForBuy", new JsonUtil(values).toString());
    }

    private void googleConsumeResult(int resultCode, String resultJson, String resultSign) {
        TreeMap<String, Object> values = new TreeMap<>();
        values.put("originalJson", resultJson);
        values.put("signature", resultSign);
        values.put("resultCode", resultCode);
        Unity2Android.getInstance().CallUnity("PayManager", "androidResponseForConsume", new JsonUtil(values).toString());
    }
}

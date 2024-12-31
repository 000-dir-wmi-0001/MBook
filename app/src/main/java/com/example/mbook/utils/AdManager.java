package com.example.mbook.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {

    private InterstitialAd mInterstitialAd;
    private static final String TAG = "AdManager";
    private final String adUnitId;

    public AdManager(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    // Load the interstitial ad
    public void loadAd(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.d(TAG, "Interstitial ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.d(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage());
                mInterstitialAd = null; // Clear ad reference if load fails
            }
        });
    }

    // Show the interstitial ad
    public void showAd(Context context, Runnable onAdDismissed) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.");
                    onAdDismissed.run(); // Execute callback
                    loadAd(context); // Load a new ad for next time
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.d(TAG, "Ad failed to show: " + adError.getMessage());
                    onAdDismissed.run(); // Proceed if ad fails to show
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd = null; // Clear the ad reference after it's shown
                }
            });
            mInterstitialAd.show((Activity) context);
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.");
            onAdDismissed.run(); // Proceed if ad isn't ready
        }
    }

    // Check if the ad is loaded
    public boolean isAdLoaded() {
        return mInterstitialAd != null;
    }
}


//package com.example.mbook.utils;
//
//
//import android.app.Activity;
//import android.content.Context;
//import android.util.Log;
//
//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
//
//public class AdManager {
//
//    private InterstitialAd mInterstitialAd;
//    private static final String TAG = "AdManager";
//    private final String adUnitId;
//
//    public AdManager(String adUnitId) {
//        this.adUnitId = adUnitId;
//    }
//
//    public void loadAd(Context context) {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        InterstitialAd.load(context, adUnitId, adRequest, new InterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(InterstitialAd interstitialAd) {
//                mInterstitialAd = interstitialAd;
//                Log.d(TAG, "Interstitial ad loaded.");
//            }
//
//            @Override
//            public void onAdFailedToLoad(LoadAdError loadAdError) {
//                Log.d(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage());
//                mInterstitialAd = null;
//            }
//        });
//    }
//
//    public void showAd(Context context, Runnable onAdDismissed) {
//        if (mInterstitialAd != null) {
//            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    onAdDismissed.run();
//                    loadAd(context); // Load a new ad for next time
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(AdError adError) {
//                    Log.d(TAG, "Ad failed to show: " + adError.getMessage());
//                    onAdDismissed.run(); // Proceed if ad fails to show
//                }
//            });
//            mInterstitialAd.show((Activity) context);
//        } else {
//            Log.d(TAG, "The interstitial ad wasn't ready yet.");
//            onAdDismissed.run(); // Proceed if ad isn't ready
//        }
//    }
//}

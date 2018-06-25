package com.shadyboshra2012.android.alarmthere;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;


public class AppPreferences {
    private Context mContext;
    private static AppPreferences sInstance;
    private SharedPreferences mPrefs;
    private static final String PREF_APP_RATE = "pref_app_rate";
    private static final String PREF_LAUNCH_COUNT = "pref_launch_count";
    private static final String PREF_AD_ON = "pref_ad_on";
    private static final String PREF_COUNT_TO_SHOW_AD = "pref_count_to_show_ad";

    private static InterstitialAd mInterstitialAd;

    public AppPreferences(Context paramContext) {
        this.mContext = paramContext;
        this.mPrefs = paramContext.getSharedPreferences("app_prefs", 0);
        if (mInterstitialAd == null) {
            MobileAds.initialize(paramContext, "ca-app-pub-3320602227796002~6302509110");
            mInterstitialAd = newInterstitialAd();
            loadInterstitial();
        }
    }

    public static AppPreferences getInstance(Context paramContext) {
        if (sInstance == null) {
            sInstance = new AppPreferences(paramContext);
        }
        return sInstance;
    }

    public boolean getAppRate() {
        return this.mPrefs.getBoolean(PREF_APP_RATE, true);
    }

    public void setAppRate(boolean paramBoolean) {
        SharedPreferences.Editor localEditor = this.mPrefs.edit();
        localEditor.putBoolean(PREF_APP_RATE, paramBoolean);
        localEditor.commit();
    }

    public int getLaunchCount() {
        return this.mPrefs.getInt(PREF_LAUNCH_COUNT, 0);
    }

    public void incrementLaunchCount() {
        int i = getLaunchCount();
        SharedPreferences.Editor localEditor = this.mPrefs.edit();
        localEditor.putInt(PREF_LAUNCH_COUNT, i + 1);
        localEditor.commit();
    }

    public void resetLaunchCount() {
        SharedPreferences.Editor localEditor = this.mPrefs.edit();
        localEditor.remove(PREF_LAUNCH_COUNT);
        localEditor.commit();
    }

    private boolean getAdOn() {
        return this.mPrefs.getBoolean(PREF_AD_ON, true);
    }

    private void adShowedCountIncreament() {
        int i = getAdShowedCount();
        SharedPreferences.Editor localEditor = this.mPrefs.edit();
        localEditor.putInt(PREF_COUNT_TO_SHOW_AD, i + 1);
        localEditor.commit();
    }

    private int getAdShowedCount() {
        return this.mPrefs.getInt(PREF_COUNT_TO_SHOW_AD, 0);
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(mContext);
        interstitialAd.setAdUnitId(mContext.getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                //mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                //goToNextLevel();
            }
        });
        return interstitialAd;
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }

    public void showAd() {
        adShowedCountIncreament();
        loadInterstitial();

        boolean isAdOn = getAdOn();
        int i = getAdShowedCount();
        if (isAdOn && i % 8 == 0 && i != 0) {
            showInterstitial();
        }
    }
}
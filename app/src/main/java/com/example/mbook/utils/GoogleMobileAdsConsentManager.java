package com.example.mbook.utils;

import android.app.Activity;
import android.content.Context;

import com.example.mbook.fragments.HomeFragment;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Manages user consent for Google Mobile Ads using Google's User Messaging Platform.
 */
public class GoogleMobileAdsConsentManager {
    private static GoogleMobileAdsConsentManager instance;
    private final ConsentInformation consentInformation;

    /** Private constructor. */
    private GoogleMobileAdsConsentManager(Context context) {
        this.consentInformation = UserMessagingPlatform.getConsentInformation(context);
    }

    /** Gets the singleton instance of the consent manager. */
    public static GoogleMobileAdsConsentManager getInstance(Context context) {
        if (instance == null) {
            instance = new GoogleMobileAdsConsentManager(context);
        }
        return instance;
    }

    /** Interface for consent gathering completion callback. */
    public interface OnConsentGatheringCompleteListener {
        void consentGatheringComplete(FormError error);
    }

    /** Checks if ads can be requested based on user consent. */
    public boolean canRequestAds() {
        return consentInformation.canRequestAds();
    }

    /** Checks if privacy options are required. */
    public boolean isPrivacyOptionsRequired() {
        return consentInformation.getPrivacyOptionsRequirementStatus() == PrivacyOptionsRequirementStatus.REQUIRED;
    }

    /**
     * Requests consent information and shows a consent form if necessary.
     */
    public void gatherConsent(Activity activity, OnConsentGatheringCompleteListener onConsentGatheringCompleteListener) {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
                .addTestDeviceHashedId(HomeFragment.TEST_DEVICE_HASHED_ID)
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings)
                .build();

        consentInformation.requestConsentInfoUpdate(activity, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,
                        formError -> onConsentGatheringCompleteListener.consentGatheringComplete(formError)),
                requestConsentError -> onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError));
    }

    /** Presents the privacy options form to the user. */
    public void showPrivacyOptionsForm(Activity activity, OnConsentFormDismissedListener onConsentFormDismissedListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener);
    }
}

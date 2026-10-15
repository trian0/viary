package com.trian0.viary.ads

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.trian0.viary.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

object AdsConsentManager {

    private val adsInitialized = AtomicBoolean(false)
    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    fun gather(activity: Activity) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        publish(activity, consentInformation)

        val params = ConsentRequestParameters.Builder().apply {
            if (BuildConfig.DEBUG) {
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity).build()
                )
            }
        }.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    publish(activity, consentInformation)
                }
            },
            { publish(activity, consentInformation) },
        )
    }

    private fun publish(activity: Activity, consentInformation: ConsentInformation) {
        if (!consentInformation.canRequestAds()) return
        if (adsInitialized.compareAndSet(false, true)) {
            MobileAds.initialize(activity.applicationContext)
        }
        _canRequestAds.value = true
    }
}

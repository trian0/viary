package com.trian0.viary.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.trian0.viary.BuildConfig
import com.trian0.viary.ads.AdsConsentManager

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = BuildConfig.BANNER_AD_UNIT_ID,
) {
    val canRequestAds by AdsConsentManager.canRequestAds.collectAsState()
    if (!canRequestAds) return

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val adSize = remember(configuration.screenWidthDp) {
        AdSize.getLargeAnchoredAdaptiveBannerAdSize(
            context,
            configuration.screenWidthDp
        )
    }

    // factory only runs once per AndroidView, so a new size or unit id needs a fresh view
    key(adUnitId, adSize) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    this.adUnitId = adUnitId
                    setAdSize(adSize)
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { it.destroy() }
        )
    }
}

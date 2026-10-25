package com.trian0.viary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.trian0.viary.BuildConfig
import com.trian0.viary.R
import com.trian0.viary.ads.AdsConsentManager
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary50
import com.trian0.viary.ui.theme.Secondary90

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = BuildConfig.BANNER_AD_UNIT_ID,
) {
    val canRequestAds by AdsConsentManager.canRequestAds.collectAsState()
    if (!canRequestAds) return

    // factory only runs once per AndroidView, so a new unit id needs a fresh view and a fresh loaded flag
    key(adUnitId) {
        var loaded by remember { mutableStateOf(false) }

        // The ad has to be composed to load, so until it does the card collapses to zero height
        Card(
            modifier = modifier
                .fillMaxWidth()
                .then(if (loaded) Modifier else Modifier.height(0.dp).clipToBounds()),
            colors = CardColors(
                containerColor = Secondary90,
                contentColor = Primary20,
                disabledContainerColor = Secondary90,
                disabledContentColor = Primary20,
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_screen_ad_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary50,
                    modifier = Modifier.fillMaxWidth()
                )
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            this.adUnitId = adUnitId
                            setAdSize(AdSize.MEDIUM_RECTANGLE)
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    loaded = true
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    loaded = false
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    onRelease = { it.destroy() }
                )
            }
        }
    }
}

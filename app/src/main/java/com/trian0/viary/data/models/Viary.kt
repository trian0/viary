package com.trian0.viary.data.models

import android.net.Uri
import androidx.annotation.StringRes
import com.trian0.viary.R
import java.util.Date
import java.util.UUID

data class Viary (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val origin: String,
    val departureTime: Date?,
    val kmStart: Double,
    val status: ViaryStatus,
    val kmEnd: Double,
    val selectedImage: String?,
) {
    enum class ViaryStatus {
        IN_PROGRESS,
        COMPLETED,
        DELETED
    }

    enum class ViaryClimate(@param:StringRes val labelRes: Int) {
        SUNNY(R.string.create_screen_climate_sunny),
        CLOUDY(R.string.create_screen_climate_cloudy),
        RAINY(R.string.create_screen_climate_rainy),
        CHILLY(R.string.create_screen_climate_chill)
    }
}
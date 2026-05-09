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
    val initialBudget: Double = 0.0,
    val status: ViaryStatus,
    val finalBudget: Double = 0.0,
    val kmEnd: Float,
    val selectedImage: String?,
    val climate: String?,
    val latitudeOrigin: Double = 0.0,
    val longitudeOrigin: Double = 0.0,
    val latitudeArrival: Double = 0.0,
    val longitudeArrival: Double = 0.0,
    val checkpoints: List<Checkpoint> = emptyList()
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
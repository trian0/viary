package com.trian0.viary.data.models

import android.net.Uri
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
}